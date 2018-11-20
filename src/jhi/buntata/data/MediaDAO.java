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
public class MediaDAO extends WriterDAO<BuntataMedia>
{
	@Override
	protected DatabaseObjectWriter<BuntataMedia> getWriter()
	{
		return Writer.Inst.get();
	}

	public BuntataMedia get(Long id)
	{
		try
		{
			return new DatabaseObjectQuery<BuntataMedia>("SELECT * FROM media WHERE id = ?")
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

	public Map<String, List<BuntataMedia>> getAllForNode(Long id, boolean includePath)
	{
		Map<String, List<BuntataMedia>> result = new HashMap<>();

		result.put(BuntataMediaType.TYPE_IMAGE, new ArrayList<>());
		result.put(BuntataMediaType.TYPE_VIDEO, new ArrayList<>());

		try
		{
			result.get(BuntataMediaType.TYPE_IMAGE).addAll(new DatabaseObjectQuery<BuntataMedia>("SELECT media.* FROM media LEFT JOIN mediatypes ON mediatypes.id = media.mediatype_id WHERE mediatypes.name = ? AND EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id = ?)")
				.setString(BuntataMediaType.TYPE_IMAGE)
				.setLong(id)
				.run()
				.getObjects(Parser.Inst.get(), includePath));
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		try
		{
			result.get(BuntataMediaType.TYPE_VIDEO).addAll(new DatabaseObjectQuery<BuntataMedia>("SELECT media.* FROM media LEFT JOIN mediatypes ON mediatypes.id = media.mediatype_id WHERE mediatypes.name = ? AND EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id = ?)")
				.setString(BuntataMediaType.TYPE_VIDEO)
				.setLong(id)
				.run()
				.getObjects(Parser.Inst.get(), includePath));
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public static class Writer extends DatabaseObjectWriter<BuntataMedia>
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
			return database.prepareStatement("INSERT INTO `media` (`id`, `mediatype_id`, `name`, `description`, `internal_link`, `external_link`, `external_link_description`, `copyright`, `created_on`, `updated_on`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}

		@Override
		public void write(BuntataMedia object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getMediaTypeId());
			stmt.setString(i++, object.getName());
			stmt.setString(i++, object.getDescription());
			stmt.setString(i++, object.getInternalLink());
			stmt.setString(i++, object.getExternalLink());
			stmt.setString(i++, object.getExternalLinkDescription());
			stmt.setString(i++, object.getCopyright());
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
		public void writeBatched(BuntataMedia object, DatabaseStatement stmt)
			throws DatabaseException
		{
			int i = 1;
			stmt.setLong(i++, object.getId());
			stmt.setLong(i++, object.getMediaTypeId());
			stmt.setString(i++, object.getName());
			stmt.setString(i++, object.getDescription());
			stmt.setString(i++, object.getInternalLink());
			stmt.setString(i++, object.getExternalLink());
			stmt.setString(i++, object.getExternalLinkDescription());
			stmt.setString(i++, object.getCopyright());
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

	public static class Parser extends DatabaseObjectParser<BuntataMedia>
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
		public BuntataMedia parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			return new BuntataMedia(rs.getLong(BuntataMedia.ID), rs.getTimestamp(BuntataMedia.CREATED_ON), rs.getTimestamp(BuntataMedia.UPDATED_ON))
				.setMediaTypeId(rs.getLong(BuntataMedia.FIELD_MEDIATYPE_ID))
				.setName(rs.getString(BuntataMedia.FIELD_NAME))
				.setDescription(rs.getString(BuntataMedia.FIELD_DESCRIPTION))
				.setInternalLink(rs.getString(BuntataMedia.FIELD_INTERNAL_LINK))
				.setExternalLink(rs.getString(BuntataMedia.FIELD_EXTERNAL_LINK))
				.setExternalLinkDescription(rs.getString(BuntataMedia.FIELD_EXTERNAL_LINK_DESCRIPTION))
				.setCopyright(rs.getString(BuntataMedia.FIELD_COPYRIGHT));
		}
	}
}
