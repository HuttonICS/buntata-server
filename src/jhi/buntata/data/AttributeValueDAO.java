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
public class AttributeValueDAO
{
	public static class Writer implements DatabaseObjectWriter<BuntataAttributeValue>
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
		public void write(BuntataAttributeValue object, PreparedStatement stmt) throws SQLException
		{
			int i = 1;
			stmt.setInt(i++, object.getId());
			stmt.setInt(i++, object.getNodeId());
			stmt.setInt(i++, object.getAttributeId());
			stmt.setString(i++, object.getValue());
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

	public static class Parser implements DatabaseObjectParser<BuntataAttributeValue>
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
		public BuntataAttributeValue parse(ResultSet rs) throws SQLException
		{
			return new BuntataAttributeValue(rs.getInt(BuntataAttributeValue.FIELD_ID), rs.getTimestamp(BuntataAttributeValue.FIELD_CREATED_ON), rs.getTimestamp(BuntataAttributeValue.FIELD_UPDATED_ON))
					.setNodeId(rs.getInt(BuntataAttributeValue.FIELD_NODE_ID))
					.setAttributeId(rs.getInt(BuntataAttributeValue.FIELD_ATTRIBUTE_ID))
					.setValue(rs.getString(BuntataAttributeValue.FIELD_VALUE));
		}
	}
}
