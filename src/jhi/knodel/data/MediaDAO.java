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

package jhi.knodel.data;

import java.sql.*;
import java.util.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */
public class MediaDAO
{
	public List<KnodelMedia> getAllForNode(KnodelNode node)
	{
		List<KnodelMedia> result = new ArrayList<>();

		try (Connection con = Database.INSTANCE.getMySQLDataSource().getConnection();
			 PreparedStatement stmt = DatabaseUtils.getByIdStatement(con, "SELECT * FROM media WHERE EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id = ?)", node.getId());
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

	public static class Parser extends DatabaseObjectParser<KnodelMedia>
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
		public KnodelMedia parse(ResultSet rs) throws SQLException
		{
			return new KnodelMedia(rs.getInt(KnodelMedia.FIELD_ID), rs.getTimestamp(KnodelMedia.FIELD_CREATED_ON), rs.getTimestamp(KnodelMedia.FIELD_UPDATED_ON))
					.setMediaTypeId(rs.getInt(KnodelMedia.FIELD_MEDIATYPE_ID))
					.setName(rs.getString(KnodelMedia.FIELD_NAME))
					.setDescription(rs.getString(KnodelMedia.FIELD_DESCRIPTION))
					.setInternalLink(rs.getString(KnodelMedia.FIELD_INTERNAL_LINK))
					.setExternalLink(rs.getString(KnodelMedia.FIELD_EXTERNAL_LINK))
					.setExternalLinkDescription(rs.getString(KnodelMedia.FIELD_EXTERNAL_LINK_DESCRIPTION))
					.setCopyright(rs.getString(KnodelMedia.FIELD_COPYRIGHT));
		}
	}
}
