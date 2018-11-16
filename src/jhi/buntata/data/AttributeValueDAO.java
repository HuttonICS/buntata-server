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
public class AttributeValueDAO
{
	public List<BuntataAttributeValue> getAllForNode(int id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataAttributeValue>("SELECT * FROM attributevalues WHERE node_id = ?")
				.setLong(id)
				.run()
				.getObjects(Parser.Inst.get());
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	public static class Writer extends DatabaseObjectWriter<BuntataAttributeValue>
	{
		public static final class Inst
		{
			/**
			 * {@link InstanceHolder} is loaded on the first execution of {@link #get()} or the first access to {@link InstanceHolder#INSTANCE}, not
			 * before.
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
			return database.prepareStatement("INSERT INTO `attributevalues` (`id`, `node_id`, `attribute_id`, `value`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?)");
		}

		@Override
		public void write(BuntataAttributeValue object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getNodeId());
			stmt.setLong(i++, object.getAttributeId());
			stmt.setString(i++, object.getValue());
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
		public void writeBatched(BuntataAttributeValue object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getNodeId());
			stmt.setLong(i++, object.getAttributeId());
			stmt.setString(i++, object.getValue());
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

	public static class Parser extends DatabaseObjectParser<BuntataAttributeValue>
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

		private AttributeDAO dao = new AttributeDAO();

		@Override
		public BuntataAttributeValue parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			BuntataAttributeValue value = new BuntataAttributeValue(rs.getLong(BuntataDatasource.ID), rs.getTimestamp(BuntataDatasource.CREATED_ON), rs.getTimestamp(BuntataDatasource.UPDATED_ON))
				.setNodeId(rs.getLong(BuntataAttributeValue.FIELD_NODE_ID))
				.setAttributeId(rs.getLong(BuntataAttributeValue.FIELD_ATTRIBUTE_ID))
				.setValue(rs.getString(BuntataAttributeValue.FIELD_VALUE));

			if (includeForeign)
				value.setAttribute(dao.get(rs.getLong(BuntataAttributeValue.FIELD_ATTRIBUTE_ID)));

			return value;
		}
	}
}
