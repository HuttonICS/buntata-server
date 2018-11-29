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
import jhi.database.shared.util.*;

/**
 * @author Sebastian Raubach
 */
public class DatasourceDAO extends WriterDAO<BuntataDatasource>
{
	@Override
	protected DatabaseObjectWriter<BuntataDatasource> getWriter()
	{
		return Writer.Inst.get();
	}

	public boolean delete(Long id)
	{
		try
		{
			new ValueQuery("DELETE FROM datasources WHERE id = ?")
				.setLong(id)
				.execute();
			return true;
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public List<BuntataDatasource> getAll(boolean includeInvisible)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataDatasource>(includeInvisible ? "SELECT * FROM datasources" : "SELECT * FROM datasources WHERE visibility = 1")
				.run()
				.getObjects(Parser.Inst.get());
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public BuntataDatasource get(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataDatasource>("SELECT * FROM datasources WHERE visibility = 1 AND id = ?")
				.setLong(id)
				.run()
				.getObject(Parser.Inst.get());
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public void updateIcon(BuntataDatasource datasource)
	{
		try
		{
			new ValueQuery("UPDATE datasources SET icon = ? WHERE id = ?")
				.setString(datasource.getIcon())
				.setLong(datasource.getId())
				.execute();
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}

	public void updateSize(BuntataDatasource datasource)
	{
		try
		{
			new ValueQuery("UPDATE datasources SET size_total = ?, size_no_video = ? WHERE id = ?")
				.setLong(datasource.getSizeTotal())
				.setLong(datasource.getSizeNoVideo())
				.setLong(datasource.getId())
				.execute();
		}
		catch (DatabaseException e)
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
		public BuntataDatasource parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			return new BuntataDatasource(rs.getLong(DatabaseObject.ID), rs.getTimestamp(DatabaseObject.CREATED_ON), rs.getTimestamp(DatabaseObject.UPDATED_ON))
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

	public static class Writer extends DatabaseObjectWriter<BuntataDatasource>
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
			return database.prepareStatement("INSERT INTO `datasources` (`id`, `name`, `description`, `version_number`, `data_provider`, `contact`, `show_key_name`, `show_single_child`, `icon`, `size_total`, `size_no_video`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}

		@Override
		public void write(BuntataDatasource object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
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
				setDate(i++, object.getCreatedOn(), stmt);
			else
				stmt.setNull(i++, Types.DATE);
			if (object.getUpdatedOn() != null)
				setDate(i++, object.getUpdatedOn(), stmt);
			else
				stmt.setNull(i++, Types.TIMESTAMP);

			List<Long> ids = stmt.execute();

			if (ids.size() > 0)
				object.setId(ids.get(0));
		}

		@Override
		public void writeBatched(BuntataDatasource object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
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
}
