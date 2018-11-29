/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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
public class SimilarityDAO extends WriterDAO<BuntataSimilarity>
{
	@Override
	protected DatabaseObjectWriter<BuntataSimilarity> getWriter()
	{
		return Writer.Inst.get();
	}

	public BuntataSimilarity getFor(Long nodeAId, Long nodeBId)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataSimilarity>("SELECT * FROM similarities WHERE node_a_id = ? AND node_b_id = ?")
				.setLong(nodeAId)
				.setLong(nodeBId)
				.run()
				.getObject(Parser.Inst.get());
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public boolean delete(Long id)
	{
		try
		{
			new ValueQuery("DELETE FROM `similarities` WHERE id = ?")
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

	public BuntataSimilarity get(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataSimilarity>("SELECT * FROM `similarities` WHERE id = ?")
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

	public static class Writer extends DatabaseObjectWriter<BuntataSimilarity>
	{
		public static final class Inst
		{
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
			return database.prepareStatement("INSERT INTO `similarities` (`id`, `node_a_id`, `node_b_id`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?)");
		}

		@Override
		public void write(BuntataSimilarity object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getNodeAId());
			stmt.setLong(i++, object.getNodeBId());
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
		public void writeBatched(BuntataSimilarity object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getNodeAId());
			stmt.setLong(i++, object.getNodeBId());
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

	public static class Parser extends DatabaseObjectParser<BuntataSimilarity>
	{
		public static final class Inst
		{
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
		public BuntataSimilarity parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			return new BuntataSimilarity(rs.getLong(DatabaseObject.ID), rs.getTimestamp(DatabaseObject.CREATED_ON), rs.getTimestamp(DatabaseObject.UPDATED_ON))
				.setNodeAId(rs.getLong(BuntataSimilarity.FIELD_NODE_A_ID))
				.setNodeBId(rs.getLong(BuntataSimilarity.FIELD_NODE_B_ID));
		}
	}
}
