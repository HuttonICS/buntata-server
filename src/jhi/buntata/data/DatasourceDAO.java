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
import java.util.logging.*;

import jhi.buntata.resource.*;

/**
 * @author Sebastian Raubach
 */
public class DatasourceDAO
{
	public List<BuntataDatasource> getAll(boolean includeInvisible)
	{
		List<BuntataDatasource> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = con.prepareStatement(includeInvisible ? "SELECT * FROM datasources" : "SELECT * FROM datasources WHERE visibility = 1");
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

	public BuntataDatasource get(int id)
	{
		BuntataDatasource result = null;

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "SELECT * FROM datasources WHERE visibility = 1 AND id = ?", id);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result = Parser.Inst.get().parse(rs, true);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public void updateSize(BuntataDatasource datasource)
	{
		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getStatement(con, "UPDATE datasources SET size_total = ?, size_no_video = ? WHERE id = ?", datasource.getSizeTotal(), datasource.getSizeNoVideo(), datasource.getId()))
		{
			Logger.getLogger("").log(Level.INFO, stmt.toString());
			stmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static class Parser implements DatabaseObjectParser<BuntataDatasource>
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

		@Override
		public BuntataDatasource parse(ResultSet rs, boolean includeForeign) throws SQLException
		{
			return new BuntataDatasource(rs.getInt(BuntataDatasource.FIELD_ID), rs.getTimestamp(BuntataDatasource.FIELD_CREATED_ON), rs.getTimestamp(BuntataDatasource.FIELD_UPDATED_ON))
					.setName(rs.getString(BuntataDatasource.FIELD_NAME))
					.setDescription(rs.getString(BuntataDatasource.FIELD_DESCRIPTION))
					.setVisibility(rs.getBoolean(BuntataDatasource.FIELD_VISIBILITY))
					.setVersionNumber(rs.getInt(BuntataDatasource.FIELD_VERSION_NUMBER))
					.setDataProvider(rs.getString(BuntataDatasource.FIELD_DATA_PROVIDER))
					.setContact(rs.getString(BuntataDatasource.FIELD_CONTACT))
					.setShowKeyName(rs.getBoolean(BuntataDatasource.FIELD_SHOW_KEY_NAME))
					.setShowSingleChild(rs.getBoolean(BuntataDatasource.FIELD_SHOW_SINGLE_CHILD))
					.setIcon(rs.getString(BuntataDatasource.FIELD_ICON))
					.setSizeTotal(rs.getLong(BuntataDatasource.FIELD_SIZE_TOTAL))
					.setSizeNoVideo(rs.getLong(BuntataDatasource.FIELD_SIZE_NO_VIDEO));
		}
	}

	public static class Writer implements DatabaseObjectWriter<BuntataDatasource>
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
		public void write(BuntataDatasource object, PreparedStatement stmt) throws SQLException
		{
			int i = 1;
			stmt.setInt(i++, object.getId());
			stmt.setString(i++, object.getName());
			stmt.setString(i++, object.getDescription());
			stmt.setInt(i++, object.getVersionNumber());
			stmt.setString(i++, object.getDataProvider());
			stmt.setString(i++, object.getContact());
			stmt.setBoolean(i++, object.isShowKeyName());
			stmt.setBoolean(i++, object.isShowSingleChild());
			stmt.setString(i++, object.getIcon());
			stmt.setLong(i++, object.getSizeTotal());
			stmt.setLong(i++, object.getSizeNoVideo());
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
}
