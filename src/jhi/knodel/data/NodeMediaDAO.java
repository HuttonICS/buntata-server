package jhi.knodel.data;

import java.sql.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */
public class NodeMediaDAO
{
	public static class Parser extends DatabaseObjectParser<KnodelNodeMedia>
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
		public KnodelNodeMedia parse(ResultSet rs) throws SQLException
		{
			return new KnodelNodeMedia(rs.getInt(KnodelNodeMedia.FIELD_ID), rs.getTimestamp(KnodelNodeMedia.FIELD_CREATED_ON), rs.getTimestamp(KnodelNodeMedia.FIELD_UPDATED_ON))
					.setNodeId(rs.getInt(KnodelNodeMedia.FIELD_NODE_ID))
					.setMediaId(rs.getInt(KnodelNodeMedia.FIELD_MEDIA_ID));
		}
	}
}
