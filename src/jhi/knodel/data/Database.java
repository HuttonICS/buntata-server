package jhi.knodel.data;

import javax.naming.*;
import javax.sql.*;

/**
 * Singleton object to wrap the connection to the Database. We connect to the database using JNDI which looks up a
 * pre-defined database setup held on the server. Removes need of application / build to know about the database
 * beyond which name we access it using.
 */
public enum Database
{
	INSTANCE;

	private DataSource mySqlDataSource;

	Database()
	{
		try
		{
			Context ctx = new InitialContext();
			mySqlDataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/database");
		}
		catch (NamingException e)
		{
			e.printStackTrace();
		}
	}

	public DataSource getMySQLDataSource()
	{
		return mySqlDataSource;
	}
}