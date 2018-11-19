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

package jhi.buntata.data;

import java.sql.*;
import java.util.*;

import jhi.buntata.resource.*;
import jhi.database.server.*;
import jhi.database.server.parser.*;
import jhi.database.server.query.*;
import jhi.database.shared.exception.*;

/**
 * @author Sebastian Raubach
 */
public class NodeDAO
{
	public List<BuntataNode> getAll()
	{
		try
		{
			return new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1")
				.run()
				.getObjects(Parser.Inst.get(), true);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public boolean add(BuntataNode node)
	{
		try
		{
			Database database = Database.connect();
			Writer.Inst.get().write(node, Writer.Inst.get().getStatement(database));
			database.close();
			return true;
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public BuntataNode get(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND nodes.id = ?")
				.setLong(id)
				.run()
				.getObject(SimilarParser.Inst.get(), true);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public List<BuntataNode> getAllForParent(Long nodeParentId)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id AND relationships.parent = ?)")
				.setLong(nodeParentId)
				.run()
				.getObjects(Parser.Inst.get(), true);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public List<BuntataNode> getAllForDatasourceRoot(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND datasource_id = ? AND NOT EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id)")
				.setLong(id)
				.run()
				.getObjects(Parser.Inst.get(), true);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public List<BuntataNode> getAllForDatasourceLeaf(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND datasource_id = ? AND NOT EXISTS (SELECT 1 FROM relationships WHERE relationships.parent = nodes.id)")
				.setLong(id)
				.run()
				.getObjects(Parser.Inst.get(), true);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public List<BuntataNode> getSimilarTo(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes WHERE EXISTS (SELECT 1 FROM similarities WHERE similarities.node_b_id = nodes.id AND similarities.node_a_id = ?)")
				.setLong(id)
				.run()
				.getObjects(Parser.Inst.get(), true);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public List<BuntataNode> getAllForDatasource(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataNode>("SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND datasource_id = ?")
				.setLong(id)
				.run()
				.getObjects(Parser.Inst.get(), true);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public static class Writer extends DatabaseObjectWriter<BuntataNode>
	{
		public static final class Inst
		{
			/**
			 * {@link InstanceHolder} is loaded on the first execution of {@link Inst#get()} or the first access to {@link InstanceHolder#INSTANCE},
			 * not before.
			 * <p/>
			 * This solution (<a href= "http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom" >Initialization-on-demand holder
			 * idiom</a>) is thread-safe without requiring special language constructs (i.e. <code>volatile</code> or <code>synchronized</code>).
			 *
			 * @author Sebastian Raubach
			 */
			private static final class InstanceHolder
			{
				private static final Writer INSTANCE = new Writer();
			}

			public static Writer get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public DatabaseStatement getStatement(Database database)
			throws DatabaseException
		{
			return database.prepareStatement("INSERT INTO `nodes` (`id`, `datasource_id`, `name`, `description`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?)");
		}

		@Override
		public void write(BuntataNode object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getDatasourceId());
			stmt.setString(i++, object.getName());
			stmt.setString(i++, object.getDescription());
			if (object.getCreatedOn() != null)
				setDate(i++, object.getCreatedOn(), stmt);
			else
				stmt.setNull(i++, Types.DATE);
			if (object.getUpdatedOn() != null)
				setDate(i++, object.getUpdatedOn(), stmt);
			else
				stmt.setNull(i++, Types.TIMESTAMP);

			stmt.execute();
		}

		@Override
		public void writeBatched(BuntataNode object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getDatasourceId());
			stmt.setString(i++, object.getName());
			stmt.setString(i++, object.getDescription());
			if (object.getCreatedOn() != null)
				setDate(i++, object.getCreatedOn(), stmt);
			else
				stmt.setNull(i++, Types.DATE);
			if (object.getUpdatedOn() != null)
				setDate(i++, object.getUpdatedOn(), stmt);
			else
				stmt.setNull(i++, Types.TIMESTAMP);

			stmt.addBatch();
		}
	}

	public static class Parser extends DatabaseObjectParser<BuntataNode>
	{
		public static final class Inst
		{
			/**
			 * {@link InstanceHolder} is loaded on the first execution of {@link Inst#get()} or the first access to {@link InstanceHolder#INSTANCE},
			 * not before.
			 * <p/>
			 * This solution (<a href= "http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom" >Initialization-on-demand holder
			 * idiom</a>) is thread-safe without requiring special language constructs (i.e. <code>volatile</code> or <code>synchronized</code>).
			 *
			 * @author Sebastian Raubach
			 */
			private static final class InstanceHolder
			{
				private static final Parser INSTANCE = new Parser();
			}

			public static Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		private static MediaDAO          mediaDao          = new MediaDAO();
		private static AttributeValueDAO attributeValueDao = new AttributeValueDAO();

		@Override
		public BuntataNode parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			BuntataNode node = new BuntataNode(rs.getLong(BuntataMedia.ID), rs.getTimestamp(BuntataMedia.CREATED_ON), rs.getTimestamp(BuntataMedia.UPDATED_ON))
				.setDatasourceId(rs.getLong(BuntataNode.FIELD_DATASOURCE_ID))
				.setName(rs.getString(BuntataNode.FIELD_NAME))
				.setDescription(rs.getString(BuntataNode.FIELD_DESCRIPTION));

			if (includeForeign)
				node.setMedia(mediaDao.getAllForNode(rs.getLong(BuntataNode.ID), false))
					.setAttributeValues(attributeValueDao.getAllForNode(rs.getInt(BuntataNode.ID)));

			return node;
		}
	}

	public static class SimilarParser extends Parser
	{
		public static final class Inst
		{
			/**
			 * {@link InstanceHolder} is loaded on the first execution of {@link Inst#get()} or the first access to {@link InstanceHolder#INSTANCE},
			 * not before.
			 * <p/>
			 * This solution (<a href= "http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom" >Initialization-on-demand holder
			 * idiom</a>) is thread-safe without requiring special language constructs (i.e. <code>volatile</code> or <code>synchronized</code>).
			 *
			 * @author Sebastian Raubach
			 */
			private static final class InstanceHolder
			{
				private static final SimilarParser INSTANCE = new SimilarParser();
			}

			public static SimilarParser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		private static NodeDAO nodeDao = new NodeDAO();

		@Override
		public BuntataNode parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			BuntataNode result = super.parse(rs, includeForeign);

			if (result != null && includeForeign)
				result.setSimilarNodes(nodeDao.getSimilarTo(rs.getLong(BuntataNode.ID)));

			return result;
		}
	}
}
