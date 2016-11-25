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

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

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

		new MySqlToSqLiteConverter(Integer.parseInt(args[i++]), Boolean.parseBoolean(args[i++]), new File(args[i++]), new File(args[i++]), args[i++], args[i++], args[i++]);
	}

	public MySqlToSqLiteConverter(int id, boolean includeVideos, File source, File target, String database, String username, String password) throws URISyntaxException, IOException, SQLException
	{
		this.source = source;
		this.target = target;
		this.folder = target.getParentFile();
		this.database = database;
		this.username = username;
		this.password = password;

//		System.out.println("Reading from: " + this.source.getAbsolutePath());
//		System.out.println("Writing to: " + this.target.getAbsolutePath());

		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

		sourceConnection = getSourceConnection();
		targetConnection = getTargetConnection();

		copyDataSources(id);
		List<Integer> nodeIds = copyNodes(id);
		List<Integer> attributeIds = copyAttributes(nodeIds);
		copyAttributeData(nodeIds, attributeIds);
		List<Integer> mediaTypeIds = copyMediaTypes(nodeIds);
		List<Integer> mediaIds = copyMedia(nodeIds, mediaTypeIds, includeVideos);
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

				// Skip videos if this has been requested by the client
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

	private void copyDataSources(int id)
	{
		try (PreparedStatement sourceStmt = sourceConnection.prepareStatement("SELECT * FROM datasources WHERE id = " + id);
			 PreparedStatement targetStmt = targetConnection.prepareStatement("INSERT INTO `datasources` (`id`, `name`, `description`, `version_number`, `data_provider`, `contact`, `show_key_name`, `icon`, `size_total`, `size_no_video`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			 ResultSet rs = sourceStmt.executeQuery())
		{
			if (rs.next())
			{
				BuntataDatasource ds = DatasourceDAO.Parser.Inst.get().parse(rs);
				DatasourceDAO.Writer.Inst.get().write(ds, targetStmt);
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
