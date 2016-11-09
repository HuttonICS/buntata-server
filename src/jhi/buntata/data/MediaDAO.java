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
public class MediaDAO
{
	public Map<String, List<BuntataMedia>> getAllForNode(BuntataNode node)
	{
		Map<String, List<BuntataMedia>> result = new HashMap<>();

		result.put(BuntataMediaType.TYPE_IMAGE, new ArrayList<>());
		result.put(BuntataMediaType.TYPE_VIDEO, new ArrayList<>());

		// Get the images first
		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getByIdStringStatement(con, "SELECT media.* FROM media LEFT JOIN mediatypes ON mediatypes.id = media.mediatype_id WHERE mediatypes.name = ? AND EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id = ?)", node.getId(), BuntataMediaType.TYPE_IMAGE);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.get(BuntataMediaType.TYPE_IMAGE).add(Parser.Inst.get().parse(rs));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		// Then the videos
		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getByIdStringStatement(con, "SELECT media.* FROM media LEFT JOIN mediatypes ON mediatypes.id = media.mediatype_id WHERE mediatypes.name = ? AND EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id = ?)", node.getId(), BuntataMediaType.TYPE_VIDEO);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				result.get(BuntataMediaType.TYPE_VIDEO).add(Parser.Inst.get().parse(rs));
			}
		}
		catch (SQLException e)
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
		public void write(BuntataMedia object, PreparedStatement stmt) throws SQLException
		{
			int i = 1;
			stmt.setInt(i++, object.getId());
			stmt.setInt(i++, object.getMediaTypeId());
			stmt.setString(i++, object.getName());
			stmt.setString(i++, object.getDescription());
			stmt.setString(i++, object.getInternalLink());
			stmt.setString(i++, object.getExternalLink());
			stmt.setString(i++, object.getExternalLinkDescription());
			stmt.setString(i++, object.getCopyright());
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
		public BuntataMedia parse(ResultSet rs) throws SQLException
		{
			return new BuntataMedia(rs.getInt(BuntataMedia.FIELD_ID), rs.getTimestamp(BuntataMedia.FIELD_CREATED_ON), rs.getTimestamp(BuntataMedia.FIELD_UPDATED_ON))
					.setMediaTypeId(rs.getInt(BuntataMedia.FIELD_MEDIATYPE_ID))
					.setName(rs.getString(BuntataMedia.FIELD_NAME))
					.setDescription(rs.getString(BuntataMedia.FIELD_DESCRIPTION))
					.setInternalLink(rs.getString(BuntataMedia.FIELD_INTERNAL_LINK))
					.setExternalLink(rs.getString(BuntataMedia.FIELD_EXTERNAL_LINK))
					.setExternalLinkDescription(rs.getString(BuntataMedia.FIELD_EXTERNAL_LINK_DESCRIPTION))
					.setCopyright(rs.getString(BuntataMedia.FIELD_COPYRIGHT));
		}
	}
}
