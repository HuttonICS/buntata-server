/*
 * Copyright (c) 2016 Information & Computational Sciences, The James Hutton Institute
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
public class DatasourceDAO
{
	public List<BuntataDatasource> getAll()
	{
		List<BuntataDatasource> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = con.prepareStatement("SELECT * FROM datasources");
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public List<BuntataDatasource> get(int id)
	{
		List<BuntataDatasource> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getByIdStatement(con, "SELECT * FROM datasources WHERE id = ?", id);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.add(Parser.Inst.get().parse(rs));
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
			 PreparedStatement stmt = DatabaseUtils.updateByIdLongStatement(con, "UPDATE datasources SET size_total = ?, size_no_video = ? WHERE id = ?", datasource.getId(), datasource.getSizeTotal(), datasource.getSizeNoVideo()))
		{
			stmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}


	public static class Parser extends DatabaseObjectParser<BuntataDatasource>
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
		public BuntataDatasource parse(ResultSet rs) throws SQLException
		{
			return new BuntataDatasource(rs.getInt(BuntataDatasource.FIELD_ID), rs.getTimestamp(BuntataDatasource.FIELD_CREATED_ON), rs.getTimestamp(BuntataDatasource.FIELD_UPDATED_ON))
					.setName(rs.getString(BuntataDatasource.FIELD_NAME))
					.setDescription(rs.getString(BuntataDatasource.FIELD_DESCRIPTION))
					.setVersionNumber(rs.getInt(BuntataDatasource.FIELD_VERSION_NUMBER))
					.setDataProvider(rs.getString(BuntataDatasource.FIELD_DATA_PROVIDER))
					.setContact(rs.getString(BuntataDatasource.FIELD_CONTACT))
					.setIcon(rs.getString(BuntataDatasource.FIELD_ICON))
					.setSizeTotal(rs.getLong(BuntataDatasource.FIELD_SIZE_TOTAL))
					.setSizeNoVideo(rs.getLong(BuntataDatasource.FIELD_SIZE_NO_VIDEO));
		}
	}
}
