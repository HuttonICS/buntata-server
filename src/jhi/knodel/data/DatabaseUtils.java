package jhi.knodel.data;

import java.sql.*;

/**
 * @author Sebastian Raubach
 */
public class DatabaseUtils
{
	public static PreparedStatement getByIdStatement(Connection con, String query, int id) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setInt(1, id);

		return stmt;
	}

	public static PreparedStatement updateByIdLongStatement(Connection con, String query, int id, long value) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, value);
		stmt.setInt(2, id);

		return stmt;
	}
}
