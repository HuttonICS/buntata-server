package jhi.knodel.sqlite;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

import jhi.knodel.data.*;
import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */
public class MySqlToSqLiteConverter
{
	private File source;
	private File target;
	private File folder;

	private Connection sourceConnection;
	private Connection targetConnection;

	private String database;
	private String username;
	private String password;

	public static void main(String[] args) throws IOException, URISyntaxException, SQLException
	{
		int i = 0;
		new MySqlToSqLiteConverter(Integer.parseInt(args[i++]), new File(args[i++]), new File(args[i++]), args[i++], args[i++], args[i++]);
	}

	public MySqlToSqLiteConverter(int id, File source, File target, String database, String username, String password) throws URISyntaxException, IOException, SQLException
	{
		this.source = source;
		this.target = target;
		this.folder = target.getParentFile();
		this.database = database;
		this.username = username;
		this.password = password;

		System.out.println("Reading from: " + this.source.getAbsolutePath());
		System.out.println("Writing to: " + this.target.getAbsolutePath());

		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

		sourceConnection = getSourceConnection();
		targetConnection = getTargetConnection();

		copyDataSources(id);
		List<Integer> nodeIds = copyNodes(id);
		List<Integer> attributeIds = copyAttributes(nodeIds);
		copyAttributeData(nodeIds, attributeIds);
		List<Integer> mediaTypeIds = copyMediaTypes(nodeIds);
		List<Integer> mediaIds = copyMedia(nodeIds, mediaTypeIds);
		copyNodeMedia(nodeIds, mediaIds);
		copyRelationships(nodeIds);

		sourceConnection.close();
		targetConnection.close();
	}

	private String getFormattedPlaceholder(int size)
	{
		if (size < 1)
			return "";

		StringBuilder builder = new StringBuilder();
		builder.append("?");

		for (int i = 1; i < size; i++)
			builder.append(", ?");

		return builder.toString();
	}

	private void copyRelationships(List<Integer> nodeIds)
	{
		if (nodeIds.size() < 1)
			return;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM relationships WHERE parent IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND child IN (" + getFormattedPlaceholder(nodeIds.size()) + ")");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `relationships` (`id`, `parent`, `child`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?)"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				KnodelRelationship relationship = RelationshipDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing relationship: " + relationship);

				i = 1;
				targetStmt.setInt(i++, relationship.getId());
				targetStmt.setInt(i++, relationship.getParent());
				targetStmt.setInt(i++, relationship.getChild());
				if (relationship.getCreatedOn() != null)
					targetStmt.setLong(i++, relationship.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (relationship.getUpdatedOn() != null)
					targetStmt.setLong(i++, relationship.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void copyNodeMedia(List<Integer> nodeIds, List<Integer> mediaIds)
	{
		if (nodeIds.size() < 1 || mediaIds.size() < 1)
			return;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM nodemedia WHERE node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND media_id IN (" + getFormattedPlaceholder(mediaIds.size()) + ")");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `nodemedia` (`id`, `node_id`, `media_id`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?)"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);
			for (Integer id : mediaIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				KnodelNodeMedia media = NodeMediaDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing nodemedia: " + media);

				i = 1;
				targetStmt.setInt(i++, media.getId());
				targetStmt.setInt(i++, media.getNodeId());
				targetStmt.setInt(i++, media.getMediaId());
				if (media.getCreatedOn() != null)
					targetStmt.setLong(i++, media.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (media.getUpdatedOn() != null)
					targetStmt.setLong(i++, media.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private List<Integer> copyMedia(List<Integer> nodeIds, List<Integer> mediaTypeIds)
	{
		List<Integer> ids = new ArrayList<>();

		if (nodeIds.size() < 1 || mediaTypeIds.size() < 1)
			return ids;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM media WHERE EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ")) AND media.mediatype_id IN (" + getFormattedPlaceholder(mediaTypeIds.size()) + ")");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `media` (`id`, `mediatype_id`, `name`, `description`, `internal_link`, `external_link`, `external_link_description`, `copyright`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);
			for (Integer id : mediaTypeIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				KnodelMedia media = MediaDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing media: " + media);

				File source = new File(media.getInternalLink());

				// Now copy the media file
				// TODO: Skip videos??? If so, update entry above and set internal to null
				if (source.exists())
					Files.copy(source.toPath(), new File(folder, source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
				else
					media.setInternalLink(null);

				i = 1;
				targetStmt.setInt(i++, media.getId());
				targetStmt.setInt(i++, media.getMediaTypeId());
				targetStmt.setString(i++, media.getName());
				targetStmt.setString(i++, media.getDescription());
				targetStmt.setString(i++, source.getName());
				targetStmt.setString(i++, media.getExternalLink());
				targetStmt.setString(i++, media.getExternalLinkDescription());
				targetStmt.setString(i++, media.getCopyright());
				if (media.getCreatedOn() != null)
					targetStmt.setLong(i++, media.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (media.getUpdatedOn() != null)
					targetStmt.setLong(i++, media.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();

				ids.add(media.getId());
			}

			rs.close();
		}
		catch (SQLException | IOException e)
		{
			e.printStackTrace();
		}

		return ids;
	}

	private List<Integer> copyMediaTypes(List<Integer> nodeIds)
	{
		List<Integer> ids = new ArrayList<>();

		if (nodeIds.size() < 1)
			return ids;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM mediatypes WHERE EXISTS (SELECT 1 FROM media LEFT JOIN nodemedia ON nodemedia.media_id = media.id WHERE media.mediatype_id = mediatypes.id AND nodemedia.node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + "))");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `mediatypes` (`id`, `name`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?)"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				KnodelMediaType mediaType = MediaTypeDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing mediaType: " + mediaType);

				i = 1;
				targetStmt.setInt(i++, mediaType.getId());
				targetStmt.setString(i++, mediaType.getName());
				if (mediaType.getCreatedOn() != null)
					targetStmt.setLong(i++, mediaType.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (mediaType.getUpdatedOn() != null)
					targetStmt.setLong(i++, mediaType.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();

				ids.add(mediaType.getId());
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return ids;
	}

	private void copyAttributeData(List<Integer> nodeIds, List<Integer> attributeIds)
	{
		if (nodeIds.size() < 1 || attributeIds.size() < 1)
			return;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM attributevalues WHERE node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND attribute_id IN (" + getFormattedPlaceholder(attributeIds.size()) + ")");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `attributevalues` (`id`, `node_id`, `attribute_id`, `value`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?)"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);
			for (Integer id : attributeIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				KnodelAttributeValue value = AttributeValueDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing value: " + value);

				i = 1;
				targetStmt.setInt(i++, value.getId());
				targetStmt.setInt(i++, value.getNodeId());
				targetStmt.setInt(i++, value.getAttributeId());
				targetStmt.setString(i++, value.getValue());
				if (value.getCreatedOn() != null)
					targetStmt.setLong(i++, value.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (value.getUpdatedOn() != null)
					targetStmt.setLong(i++, value.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private List<Integer> copyAttributes(List<Integer> nodeIds)
	{
		List<Integer> ids = new ArrayList<>();

		if (nodeIds.size() < 1)
			return ids;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM attributes WHERE EXISTS (SELECT 1 FROM attributevalues WHERE attributevalues.attribute_id = attributes.id AND attributevalues.node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + "))");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `attributes` (`id`, `name`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?)"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				KnodelAttribute attribute = AttributeDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing attribute: " + attribute);

				i = 1;
				targetStmt.setInt(i++, attribute.getId());
				targetStmt.setString(i++, attribute.getName());
				if (attribute.getCreatedOn() != null)
					targetStmt.setLong(i++, attribute.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (attribute.getUpdatedOn() != null)
					targetStmt.setLong(i++, attribute.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();

				ids.add(attribute.getId());
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return ids;
	}

	private List<Integer> copyNodes(int id)
	{
		List<Integer> ids = new ArrayList<>();

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM nodes WHERE datasource_id = " + id);
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `nodes` (`id`, `datasource_id`, `name`, `description`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?)");
			 ResultSet rs = sourceStmt.executeQuery())
		{
			while (rs.next())
			{
				KnodelNode node = NodeDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing node: " + node);

				int i = 1;
				targetStmt.setInt(i++, node.getId());
				targetStmt.setInt(i++, node.getDatasourceId());
				targetStmt.setString(i++, node.getName());
				targetStmt.setString(i++, node.getDescription());
				if (node.getCreatedOn() != null)
					targetStmt.setLong(i++, node.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (node.getUpdatedOn() != null)
					targetStmt.setLong(i++, node.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();

				ids.add(node.getId());
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return ids;
	}

	private void copyDataSources(int id)
	{
		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM datasources WHERE id = " + id);
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `datasources` (`id`, `name`, `description`, `version_number`, `data_provider`, `contact`, `icon`, `size`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			 ResultSet rs = sourceStmt.executeQuery())
		{
			while (rs.next())
			{
				KnodelDatasource ds = DatasourceDAO.Parser.Inst.get().parse(rs);
				System.out.println("Writing datasource: " + ds);

				System.out.println(ds.getUpdatedOn().getTime());

				int i = 1;
				targetStmt.setInt(i++, ds.getId());
				targetStmt.setString(i++, ds.getName());
				targetStmt.setString(i++, ds.getDescription());
				targetStmt.setInt(i++, ds.getVersionNumber());
				targetStmt.setString(i++, ds.getDataProvider());
				targetStmt.setString(i++, ds.getContact());
				targetStmt.setString(i++, ds.getIcon());
				targetStmt.setLong(i++, ds.getSize());
				if (ds.getCreatedOn() != null)
					targetStmt.setLong(i++, ds.getCreatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.DATE);
				if (ds.getUpdatedOn() != null)
					targetStmt.setLong(i++, ds.getUpdatedOn().getTime());
				else
					targetStmt.setNull(i++, Types.TIMESTAMP);

				targetStmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private Connection getSourceConnection()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection(database, username, password);
		}
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private Connection getTargetConnection()
	{
		try
		{
			Class.forName("org.sqlite.JDBC").newInstance();
			return DriverManager.getConnection("jdbc:sqlite://" + target.getAbsolutePath());
		}
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
