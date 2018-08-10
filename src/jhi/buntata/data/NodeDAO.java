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

/**
 * @author Sebastian Raubach
 */
public class NodeDAO
{
	public List<BuntataNode> getAll()
	{
		List<BuntataNode> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = con.prepareStatement("SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1");
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs, true));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public BuntataNode get(int id)
	{
		BuntataNode result = null;

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND nodes.id = ?", id);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result = SimilarParser.Inst.get().parse(rs, true);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public List<BuntataNode> getAllForParent(int nodeParentId)
	{
		List<BuntataNode> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id AND relationships.parent = ?)", nodeParentId);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs, true));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public List<BuntataNode> getAllForDatasourceRoot(int id)
	{
		List<BuntataNode> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND datasource_id = ? AND NOT EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id)", id);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs, true));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public List<BuntataNode> getAllForDatasourceLeaf(int id)
	{
		List<BuntataNode> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND datasource_id = ? AND NOT EXISTS (SELECT 1 FROM relationships WHERE relationships.parent = nodes.id)", id);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs, true));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public List<BuntataNode> getSimilarTo(int id)
	{
		List<BuntataNode> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "SELECT * FROM nodes WHERE EXISTS (SELECT 1 FROM similarities WHERE similarities.node_b_id = nodes.id AND similarities.node_a_id = ?)", id);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs, true));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public List<BuntataNode> getAllForDatasource(int id)
	{
		List<BuntataNode> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "SELECT * FROM nodes LEFT JOIN datasources ON datasources.id = nodes.datasource_id WHERE datasources.visibility = 1 AND datasource_id = ?", id);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs, true));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public static class Writer implements DatabaseObjectWriter<BuntataNode>
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
		public void write(BuntataNode object, PreparedStatement stmt) throws SQLException
		{
			int i = 1;
			stmt.setInt(i++, object.getId());
			stmt.setInt(i++, object.getDatasourceId());
			stmt.setString(i++, object.getName());
			stmt.setString(i++, object.getDescription());
			if (object.getCreatedOn() != null)
				stmt.setLong(i++, object.getCreatedOn().getTime());
			else
				stmt.setNull(i++, Types.DATE);
			if (object.getUpdatedOn() != null)
				stmt.setLong(i++, object.getUpdatedOn().getTime());
			else
				stmt.setNull(i++, Types.TIMESTAMP);

			stmt.executeUpdate();
		}
	}

	public static class Parser implements DatabaseObjectParser<BuntataNode>
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
		public BuntataNode parse(ResultSet rs, boolean includeForeign) throws SQLException
		{
			BuntataNode node = new BuntataNode(rs.getInt(BuntataNode.FIELD_ID), rs.getTimestamp(BuntataNode.FIELD_CREATED_ON), rs.getTimestamp(BuntataNode.FIELD_UPDATED_ON))
					.setDatasourceId(rs.getInt(BuntataNode.FIELD_DATASOURCE_ID))
					.setName(rs.getString(BuntataNode.FIELD_NAME))
					.setDescription(rs.getString(BuntataNode.FIELD_DESCRIPTION));

			if (includeForeign)
				node.setMedia(mediaDao.getAllForNode(rs.getInt(BuntataNode.FIELD_ID), false))
					.setAttributeValues(attributeValueDao.getAllForNode(rs.getInt(BuntataNode.FIELD_ID)));

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
		public BuntataNode parse(ResultSet rs, boolean includeForeign) throws SQLException
		{
			BuntataNode result = super.parse(rs, includeForeign);

			if (result != null && includeForeign)
				result.setSimilarNodes(nodeDao.getSimilarTo(rs.getInt(BuntataNode.FIELD_ID)));

			return result;
		}
	}
}
