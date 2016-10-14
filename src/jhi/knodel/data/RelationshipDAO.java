package jhi.knodel.data;

import java.sql.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */
public class RelationshipDAO
{
	public static class Parser extends DatabaseObjectParser<KnodelRelationship>
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
		public KnodelRelationship parse(ResultSet rs) throws SQLException
		{
			return new KnodelRelationship(rs.getInt(KnodelRelationship.FIELD_ID), rs.getTimestamp(KnodelRelationship.FIELD_CREATED_ON), rs.getTimestamp(KnodelRelationship.FIELD_UPDATED_ON))
					.setParent(rs.getInt(KnodelRelationship.FIELD_PARENT))
					.setChild(rs.getInt(KnodelRelationship.FIELD_CHILD));
		}
	}
}
