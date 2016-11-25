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

import jhi.buntata.resource.*;

/**
 * @author Sebastian Raubach
 */
public class AttributeDAO
{
	public static class Writer extends DatabaseObjectWriter<BuntataAttribute>
	{
		public static final class Inst
		{
			/**
			 * {@link InstanceHolder} is loaded on the first execution of {@link #get()} or the first access to {@link InstanceHolder#INSTANCE},
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
		public void write(BuntataAttribute object, PreparedStatement stmt) throws SQLException
		{
			int i = 1;
			stmt.setInt(i++, object.getId());
			stmt.setString(i++, object.getName());
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

	public static class Parser extends DatabaseObjectParser<BuntataAttribute>
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
		public BuntataAttribute parse(ResultSet rs) throws SQLException
		{
			return new BuntataAttribute(rs.getInt(BuntataAttribute.FIELD_ID), rs.getTimestamp(BuntataAttribute.FIELD_CREATED_ON), rs.getTimestamp(BuntataAttribute.FIELD_UPDATED_ON))
					.setName(rs.getString(BuntataAttribute.FIELD_NAME));
		}
	}
}
