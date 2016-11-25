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

	public static PreparedStatement getByIdStringStatement(Connection con, String query, int id, String... values) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement(query);
		int i=1;
		for(String s : values)
			stmt.setString(i++, s);
		stmt.setInt(i++, id);

		return stmt;
	}

	public static PreparedStatement updateByIdLongStatement(Connection con, String query, int id, long... values) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement(query);
		int i=1;
		for(long l : values)
			stmt.setLong(i++, l);
		stmt.setInt(i++, id);

		return stmt;
	}
}
