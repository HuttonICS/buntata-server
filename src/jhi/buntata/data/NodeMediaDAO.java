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
import jhi.database.shared.exception.*;
import jhi.database.shared.util.*;

/**
 * @author Sebastian Raubach
 */
public class NodeMediaDAO extends WriterDAO<BuntataNodeMedia>
{
	@Override
	protected DatabaseObjectWriter<BuntataNodeMedia> getWriter()
	{
		return Writer.Inst.get();
	}

	public static class Writer extends DatabaseObjectWriter<BuntataNodeMedia>
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
			return database.prepareStatement("INSERT INTO `nodemedia` (`id`, `node_id`, `media_id`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?)");
		}

		@Override
		public void write(BuntataNodeMedia object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getNodeId());
			stmt.setLong(i++, object.getMediaId());
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
		public void writeBatched(BuntataNodeMedia object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getNodeId());
			stmt.setLong(i++, object.getMediaId());
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

	public static class Parser extends DatabaseObjectParser<BuntataNodeMedia>
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
		public BuntataNodeMedia parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			return new BuntataNodeMedia(rs.getLong(DatabaseObject.ID), rs.getTimestamp(DatabaseObject.CREATED_ON), rs.getTimestamp(DatabaseObject.UPDATED_ON))
				.setNodeId(rs.getLong(BuntataNodeMedia.FIELD_NODE_ID))
				.setMediaId(rs.getLong(BuntataNodeMedia.FIELD_MEDIA_ID));
		}
	}
}
