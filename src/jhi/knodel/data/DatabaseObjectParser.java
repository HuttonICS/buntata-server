package jhi.knodel.data;

import java.sql.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */

public abstract class DatabaseObjectParser<T extends DatabaseObject>
{
	public abstract T parse(ResultSet rs) throws SQLException;
}
