/*
 * Copyright 2016 Information & Computational Sciences, The James Hutton Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jhi.buntata.sqlite;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link MySqlToSqLiteConverter} converts the information for a single data source from MySQL to SQLite. <p> This is all run outside of Tomcat to
 * avoid loading the SQLite driver, which would lead to problems when trying to re-deploy applications as the driver uses native libraries.
 *
 * @author Sebastian Raubach
 */
public class MySqlToSqLiteConverter
{
	private final File target;
	private final File folder;

	private Connection sourceConnection;
	private Connection targetConnection;

	private final String database;
	private final String username;
	private final String password;

	public static void main(String[] args) throws IOException, URISyntaxException, SQLException
	{
		int i = 0;

		new MySqlToSqLiteConverter(Integer.parseInt(args[i++]), Boolean.parseBoolean(args[i++]), new File(args[i++]), new File(args[i++]), args[i++], args[i++], args[i++]);
	}

	/**
	 * Creates a new {@link MySqlToSqLiteConverter}
	 *
	 * @param id            The id of the {@link jhi.buntata.server.Datasource}
	 * @param includeVideos Should videos be exported as well?
	 * @param source        The source SQLite template database
	 * @param target        The target SQLite database (what gets downloaded)
	 * @param database      The MySQL database
	 * @param username      The MySQL username
	 * @param password      The MySQL password
	 * @throws IOException  Thrown if any file i/o operation fails
	 * @throws SQLException Thrown if any database interaction fails
	 */
	private MySqlToSqLiteConverter(int id, boolean includeVideos, File source, File target, String database, String username, String password) throws IOException, SQLException
	{
		this.target = target;
		this.folder = target.getParentFile();
		this.database = database;
		this.username = username;
		this.password = password;

		// Copy the template database to a new location. Then write to it later.
		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

		File copyrightSource = new File(source.getParentFile(), "copyright.txt");
		File copyrightTarget = new File(target.getParentFile(), "copyright.txt");
		if (copyrightSource.exists())
			Files.copy(copyrightSource.toPath(), copyrightTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// Establish the database connections
		sourceConnection = getSourceConnection();
		targetConnection = getTargetConnection();

		// Copy the data source
		copyDataSources(id);
		// Copy all the nodes of this data source, then get their ids
		List<Integer> nodeIds = copyNodes(id);
		// Copy all the attributes for the node ids, then get their ids
		List<Integer> attributeIds = copyAttributes(nodeIds);
		// Copy all the attribute data for the node and attribute ids
		copyAttributeData(nodeIds, attributeIds);
		// Copy all the media types, then get their ids
		List<Integer> mediaTypeIds = copyMediaTypes(nodeIds);
		// Copy all the media items, then get their ids
		List<Integer> mediaIds = copyMedia(nodeIds, mediaTypeIds, includeVideos);
		// Copy all the node-media relationships
		copyNodeMedia(nodeIds, mediaIds);
		// Copy all the node-node relationships
		copyRelationships(nodeIds);
		// Copy all the node-node similarities
		copySimilarities(nodeIds);

		/* Close the connections */
		sourceConnection.close();
		targetConnection.close();
	}

	/**
	 * Creates an SQL placeholder of the given size, e.g. passing 4 will return <code>"?, ?, ?, ?</code>
	 *
	 * @param size The number of placeholder items
	 * @return The generated String
	 */
	private String getFormattedPlaceholder(int size)
	{
		if (size < 1)
			return "";

		return IntStream.range(0, size)
						.mapToObj(i -> "?")
						.collect(Collectors.joining(", "));
	}

	/**
	 * Copies the {@link BuntataRelationship} objects between the given {@link BuntataNode} ids.
	 *
	 * @param nodeIds The {@link BuntataNode} ids
	 */
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
				BuntataRelationship relationship = RelationshipDAO.Parser.Inst.get().parse(rs);
				RelationshipDAO.Writer.Inst.get().write(relationship, targetStmt);
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Copies the {@link BuntataRelationship} objects between the given {@link BuntataNode} ids.
	 *
	 * @param nodeIds The {@link BuntataNode} ids
	 */
	private void copySimilarities(List<Integer> nodeIds)
	{
		if (nodeIds.size() < 1)
			return;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM similarities WHERE node_a_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND node_b_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ")");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `similarities` (`id`, `node_a_id`, `node_b_id`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?)"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				BuntataSimilarity similarity = SimilarityDAO.Parser.Inst.get().parse(rs);
				SimilarityDAO.Writer.Inst.get().write(similarity, targetStmt);
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Copies the {@link BuntataNodeMedia} objects between the given {@link BuntataNode} and {@link BuntataMedia} ids.
	 *
	 * @param nodeIds  The {@link BuntataNode} ids
	 * @param mediaIds The {@link BuntataMedia} ids
	 */
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
				BuntataNodeMedia media = NodeMediaDAO.Parser.Inst.get().parse(rs);
				NodeMediaDAO.Writer.Inst.get().write(media, targetStmt);
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Copies the {@link BuntataMedia} for the given {@link BuntataNode} and {@link BuntataMediaType} ids.
	 *
	 * @param nodeIds       The {@link BuntataNode} ids
	 * @param mediaTypeIds  The {@link BuntataMediaType} ids
	 * @param includeVideos Should videos be included?
	 * @return The ids of the {@link BuntataMedia} objects that have been copied
	 */
	private List<Integer> copyMedia(List<Integer> nodeIds, List<Integer> mediaTypeIds, boolean includeVideos)
	{
		List<Integer> ids = new ArrayList<>();

		if (nodeIds.size() < 1 || mediaTypeIds.size() < 1)
			return ids;

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM media WHERE EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ")) AND media.mediatype_id IN (" + getFormattedPlaceholder(mediaTypeIds.size()) + ")");
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `media` (`id`, `mediatype_id`, `name`, `description`, `internal_link`, `external_link`, `external_link_description`, `copyright`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			 PreparedStatement selectMediaType = sourceConnection.prepareStatement("SELECT * FROM mediatypes WHERE id = ?"))
		{
			int i = 1;
			for (Integer id : nodeIds)
				sourceStmt.setInt(i++, id);
			for (Integer id : mediaTypeIds)
				sourceStmt.setInt(i++, id);

			ResultSet rs = sourceStmt.executeQuery();

			while (rs.next())
			{
				BuntataMedia media = MediaDAO.Parser.Inst.get().parse(rs);

				File source = new File(media.getInternalLink());

				selectMediaType.setInt(1, media.getMediaTypeId());

				// Check if this is a video
				boolean isVideo = false;
				ResultSet rsTemp = selectMediaType.executeQuery();
				if (rsTemp.next())
				{
					BuntataMediaType type = MediaTypeDAO.Parser.Inst.get().parse(rsTemp);
					isVideo = BuntataMediaType.TYPE_VIDEO.equals(type.getName());
				}
				rsTemp.close();

				// Set the internal link of videos to null if this has been requested by the client. The external link will still be available (YouTube, etc.)
				if (isVideo && !includeVideos)
				{
					media.setInternalLink(null);
				}
				else
				{
					// Now copy the media file
					if (source.exists())
					{
						Files.copy(source.toPath(), new File(folder, source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
						media.setInternalLink(source.getName());
					}
					else
					{
						media.setInternalLink(null);
					}
				}

				MediaDAO.Writer.Inst.get().write(media, targetStmt);

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

	/**
	 * Copy the {@link BuntataMediaType}s for the given {@link BuntataNode} ids
	 *
	 * @param nodeIds The {@link BuntataNode} ids
	 * @return The ids of the copied {@link BuntataMediaType}s
	 */
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
				BuntataMediaType mediaType = MediaTypeDAO.Parser.Inst.get().parse(rs);
				MediaTypeDAO.Writer.Inst.get().write(mediaType, targetStmt);

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

	/**
	 * Copies the {@link BuntataAttributeValue}s for the given {@link BuntataNode} and {@link BuntataAttribute} ids
	 *
	 * @param nodeIds      The {@link BuntataNode} ids
	 * @param attributeIds The {@link BuntataAttribute} ids
	 */
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
				BuntataAttributeValue value = AttributeValueDAO.Parser.Inst.get().parse(rs);
				AttributeValueDAO.Writer.Inst.get().write(value, targetStmt);
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Copies the {@link BuntataAttribute}s for the given {@link BuntataNode} ids
	 *
	 * @param nodeIds The {@link BuntataNode} ids
	 * @return The ids of the copied {@link BuntataAttribute}s
	 */
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
				BuntataAttribute attribute = AttributeDAO.Parser.Inst.get().parse(rs);
				AttributeDAO.Writer.Inst.get().write(attribute, targetStmt);

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

	/**
	 * Copies the {@link BuntataNode}s for the given {@link BuntataDatasource} id
	 *
	 * @param id The {@link BuntataDatasource} id
	 * @return The ids of the copied {@link BuntataNode}s
	 */
	private List<Integer> copyNodes(int id)
	{
		List<Integer> ids = new ArrayList<>();

		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM nodes WHERE datasource_id = " + id);
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `nodes` (`id`, `datasource_id`, `name`, `description`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?)");
			 ResultSet rs = sourceStmt.executeQuery())
		{
			while (rs.next())
			{
				BuntataNode node = NodeDAO.Parser.Inst.get().parse(rs);
				NodeDAO.Writer.Inst.get().write(node, targetStmt);

				ids.add(node.getId());
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return ids;
	}

	/**
	 * Copies the {@link BuntataDatasource} with the given id
	 *
	 * @param id The {@link BuntataDatasource} id
	 */
	private void copyDataSources(int id)
	{
		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM datasources WHERE id = " + id);
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `datasources` (`id`, `name`, `description`, `version_number`, `data_provider`, `contact`, `show_key_name`, `show_single_child`, `icon`, `size_total`, `size_no_video`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			 ResultSet rs = sourceStmt.executeQuery())
		{
			if (rs.next())
			{
				BuntataDatasource ds = DatasourceDAO.Parser.Inst.get().parse(rs);

				File icon = new File(ds.getIcon());

				// Now copy the media file
				if (icon.exists() && icon.isFile())
				{
					try
					{
						Files.copy(icon.toPath(), new File(folder, icon.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
						ds.setIcon(icon.getName());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				DatasourceDAO.Writer.Inst.get().write(ds, targetStmt);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Establish a connection to the source MySQL database
	 *
	 * @return The {@link Connection} to the source MySQL database
	 */
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

	/**
	 * Establish a connection to the target SQLite database
	 *
	 * @return The {@link Connection} to the target SQLite database
	 */
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
