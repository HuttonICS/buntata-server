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
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;
import jhi.database.server.*;
import jhi.database.server.query.*;
import jhi.database.shared.exception.*;

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

	public static void main(String[] args)
		throws IOException
	{
		int i = 0;

		new MySqlToSqLiteConverter(Long.parseLong(args[i++]), Boolean.parseBoolean(args[i++]), new File(args[i++]), new File(args[i++]), args[i++], args[i++], args[i++]);
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
	private MySqlToSqLiteConverter(long id, boolean includeVideos, File source, File target, String database, String username, String password)
		throws IOException
	{
		this.target = target;
		this.folder = target.getParentFile();
		this.folder.mkdirs();

		Database.init(database, username, password);

		// Copy the template database to a new location. Then write to it later.
		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

		File copyrightSource = new File(source.getParentFile(), "copyright.txt");
		File copyrightTarget = new File(target.getParentFile(), "copyright.txt");
		if (copyrightSource.exists())
			Files.copy(copyrightSource.toPath(), copyrightTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// Copy the data source
		copyDataSources(id);
		// Copy all the nodes of this data source, then get their ids
		List<Long> nodeIds = copyNodes(id);
		// Copy all the attributes for the node ids, then get their ids
		List<Long> attributeIds = copyAttributes(nodeIds);
		// Copy all the attribute data for the node and attribute ids
		copyAttributeData(nodeIds, attributeIds);
		// Copy all the media types, then get their ids
		List<Long> mediaTypeIds = copyMediaTypes(nodeIds);
		// Copy all the media items, then get their ids
		List<Long> mediaIds = copyMedia(nodeIds, mediaTypeIds, includeVideos);
		// Copy all the node-media relationships
		copyNodeMedia(nodeIds, mediaIds);
		// Copy all the node-node relationships
		copyRelationships(nodeIds);
		// Copy all the node-node similarities
		copySimilarities(nodeIds);
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
	private void copyRelationships(List<Long> nodeIds)
	{
		if (nodeIds.size() < 1)
			return;

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataRelationship> streamer = new DatabaseObjectQuery<BuntataRelationship>("SELECT * FROM relationships WHERE parent IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND child IN (" + getFormattedPlaceholder(nodeIds.size()) + ")")
				.setLongs(nodeIds)
				.setLongs(nodeIds)
				.getStreamer(RelationshipDAO.Parser.Inst.get());

			BuntataRelationship relationship;

			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			DatabaseStatement stmt = RelationshipDAO.Writer.Inst.get().getStatement(database);
			while ((relationship = streamer.next()) != null)
			{
				RelationshipDAO.Writer.Inst.get().writeBatched(relationship, stmt);
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Copies the {@link BuntataRelationship} objects between the given {@link BuntataNode} ids.
	 *
	 * @param nodeIds The {@link BuntataNode} ids
	 */
	private void copySimilarities(List<Long> nodeIds)
	{
		if (nodeIds.size() < 1)
			return;

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataSimilarity> streamer = new DatabaseObjectQuery<BuntataSimilarity>("SELECT * FROM similarities WHERE node_a_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND node_b_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ")")
				.setLongs(nodeIds)
				.setLongs(nodeIds)
				.getStreamer(SimilarityDAO.Parser.Inst.get());

			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			BuntataSimilarity similarity;
			DatabaseStatement stmt = SimilarityDAO.Writer.Inst.get().getStatement(database);
			while ((similarity = streamer.next()) != null)
			{
				SimilarityDAO.Writer.Inst.get().writeBatched(similarity, stmt);
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException e)
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
	private void copyNodeMedia(List<Long> nodeIds, List<Long> mediaIds)
	{
		if (nodeIds.size() < 1 || mediaIds.size() < 1)
			return;

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataNodeMedia> streamer = new DatabaseObjectQuery<BuntataNodeMedia>("SELECT * FROM nodemedia WHERE node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND media_id IN (" + getFormattedPlaceholder(mediaIds.size()) + ")")
				.setLongs(nodeIds)
				.setLongs(mediaIds)
				.getStreamer(NodeMediaDAO.Parser.Inst.get());

			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			BuntataNodeMedia nodeMedia;
			DatabaseStatement stmt = NodeMediaDAO.Writer.Inst.get().getStatement(database);
			while ((nodeMedia = streamer.next()) != null)
			{
				NodeMediaDAO.Writer.Inst.get().writeBatched(nodeMedia, stmt);
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException e)
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
	private List<Long> copyMedia(List<Long> nodeIds, List<Long> mediaTypeIds, boolean includeVideos)
	{
		List<Long> ids = new ArrayList<>();

		if (nodeIds.size() < 1 || mediaTypeIds.size() < 1)
			return ids;

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataMedia> streamer = new DatabaseObjectQuery<BuntataMedia>("SELECT * FROM media WHERE EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ")) AND media.mediatype_id IN (" + getFormattedPlaceholder(mediaTypeIds.size()) + ")")
				.setLongs(nodeIds)
				.setLongs(mediaTypeIds)
				.getStreamer(MediaDAO.Parser.Inst.get());

			BuntataMedia media;
			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			DatabaseStatement stmt = MediaDAO.Writer.Inst.get().getStatement(database);
			while ((media = streamer.next()) != null)
			{
				File source = new File(media.getInternalLink());

				BuntataMediaType type = new DatabaseObjectQuery<BuntataMediaType>("SELECT * FROM mediatypes WHERE id = ?")
					.setLong(media.getMediaTypeId())
					.run()
					.getObject(MediaTypeDAO.Parser.Inst.get());

				boolean isVideo = BuntataMediaType.TYPE_VIDEO.equals(type.getName());

				// Set the internal link of videos to null if this has been requested by the client. The external link will still be available (YouTube, etc.)
				if (isVideo && !includeVideos)
				{
					System.out.println("SKIP");
					media.setInternalLink(null);
				}
				else
				{
					// Now copy the media file
					if (source.exists())
					{
						System.out.println(source.getAbsolutePath());
						System.out.println(new File(folder, source.getName()).getAbsolutePath());
						Files.copy(source.toPath(), new File(folder, source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
						media.setInternalLink(source.getName());
					}
					else
					{
						System.out.println("FILE NOT FOUND");
						media.setInternalLink(null);
					}
				}

				MediaDAO.Writer.Inst.get().writeBatched(media, stmt);

				ids.add(media.getId());
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException | IOException e)
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
	private List<Long> copyMediaTypes(List<Long> nodeIds)
	{
		List<Long> ids = new ArrayList<>();

		if (nodeIds.size() < 1)
			return ids;

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataMediaType> streamer = new DatabaseObjectQuery<BuntataMediaType>("SELECT * FROM mediatypes WHERE EXISTS (SELECT 1 FROM media LEFT JOIN nodemedia ON nodemedia.media_id = media.id WHERE media.mediatype_id = mediatypes.id AND nodemedia.node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + "))")
				.setLongs(nodeIds)
				.getStreamer(MediaTypeDAO.Parser.Inst.get());

			BuntataMediaType type;
			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			DatabaseStatement stmt = MediaTypeDAO.Writer.Inst.get().getStatement(database);
			while ((type = streamer.next()) != null)
			{
				MediaTypeDAO.Writer.Inst.get().writeBatched(type, stmt);

				ids.add(type.getId());
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException e)
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
	private void copyAttributeData(List<Long> nodeIds, List<Long> attributeIds)
	{
		if (nodeIds.size() < 1 || attributeIds.size() < 1)
			return;

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataAttributeValue> streamer = new DatabaseObjectQuery<BuntataAttributeValue>("SELECT * FROM attributevalues WHERE node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + ") AND attribute_id IN (" + getFormattedPlaceholder(attributeIds.size()) + ")")
				.setLongs(nodeIds)
				.setLongs(attributeIds)
				.getStreamer(AttributeValueDAO.Parser.Inst.get());

			BuntataAttributeValue value;
			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			DatabaseStatement stmt = AttributeValueDAO.Writer.Inst.get().getStatement(database);
			while ((value = streamer.next()) != null)
			{
				AttributeValueDAO.Writer.Inst.get().writeBatched(value, stmt);
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException e)
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
	private List<Long> copyAttributes(List<Long> nodeIds)
	{
		List<Long> ids = new ArrayList<>();

		if (nodeIds.size() < 1)
			return ids;

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataAttribute> streamer = new DatabaseObjectQuery<BuntataAttribute>("SELECT * FROM attributes WHERE EXISTS (SELECT 1 FROM attributevalues WHERE attributevalues.attribute_id = attributes.id AND attributevalues.node_id IN (" + getFormattedPlaceholder(nodeIds.size()) + "))")
				.setLongs(nodeIds)
				.getStreamer(AttributeDAO.Parser.Inst.get());

			BuntataAttribute attribute;
			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			DatabaseStatement stmt = AttributeDAO.Writer.Inst.get().getStatement(database);
			while ((attribute = streamer.next()) != null)
			{
				AttributeDAO.Writer.Inst.get().writeBatched(attribute, stmt);

				ids.add(attribute.getId());
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException e)
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
	private List<Long> copyNodes(long id)
	{
		List<Long> ids = new ArrayList<>();

		try
		{
			DatabaseObjectQuery.DatabaseObjectStreamer<BuntataNode> streamer = new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes WHERE datasource_id = ?")
				.setLong(id)
				.getStreamer(NodeDAO.Parser.Inst.get());

			BuntataNode node;
			Database database = connectToSqlite();
			database.getConnection().setAutoCommit(false);
			DatabaseStatement stmt = NodeDAO.Writer.Inst.get().getStatement(database);
			while ((node = streamer.next()) != null)
			{
				NodeDAO.Writer.Inst.get().writeBatched(node, stmt);

				ids.add(node.getId());
			}
			stmt.executeBatch();
			database.getConnection().setAutoCommit(true);
			database.close();
		}
		catch (DatabaseException | SQLException e)
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
	private void copyDataSources(long id)
	{
		try
		{
			BuntataDatasource ds = new DatabaseObjectQuery<BuntataDatasource>("SELECT * FROM datasources WHERE id = ?")
				.setLong(id)
				.run()
				.getObject(DatasourceDAO.Parser.Inst.get());

			File icon = null;

			if (ds.getIcon() != null)
				icon = new File(ds.getIcon());

			// Now copy the media file
			if (icon != null && icon.exists() && icon.isFile())
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

			Database database = connectToSqlite();
			DatabaseStatement stmt = DatasourceDAO.Writer.Inst.get().getStatement(database);
			DatasourceDAO.Writer.Inst.get().write(ds, stmt);
			database.close();
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}

	private Database connectToSqlite()
		throws DatabaseException
	{
		return Database.connect(Database.DatabaseType.SQLITE, target.getAbsolutePath(), null, null);
	}
}
