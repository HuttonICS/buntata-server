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