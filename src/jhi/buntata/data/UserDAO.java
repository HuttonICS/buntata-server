/*
 * Copyright 2018 Information & Computational Sciences, The James Hutton Institute
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

import jhi.buntata.resource.*;
import jhi.database.server.*;
import jhi.database.server.parser.*;
import jhi.database.server.query.*;
import jhi.database.shared.exception.*;
import jhi.database.shared.util.*;

/**
 * @author Sebastian Raubach
 */
public class UserDAO
{
	private static final String SELECT_FOR_NAME = "SELECT * FROM users WHERE username = ?";

	public BuntataUser getForName(String username)
		throws DatabaseException
	{
		return new DatabaseObjectQuery<BuntataUser>(SELECT_FOR_NAME)
			.setString(username)
			.run()
			.getObject(Parser.Inst.get());
	}

	public static class Parser extends DatabaseObjectParser<BuntataUser>
	{
		public static final class Inst
		{
			private static final class InstanceHolder
			{
				private static final Parser INSTANCE = new Parser();
			}

			public static Parser get()
			{
				return Parser.Inst.InstanceHolder.INSTANCE;
			}
		}

		@Override
		public BuntataUser parse(DatabaseResult rs, boolean includeForeign)
			throws DatabaseException
		{
			return new BuntataUser(rs.getLong(DatabaseObject.ID), rs.getTimestamp(DatabaseObject.CREATED_ON), rs.getTimestamp(DatabaseObject.UPDATED_ON))
				.setUsername(rs.getString(BuntataUser.FIELD_USERNAME))
				.setPassword(rs.getString(BuntataUser.FIELD_PASSWORD));
		}
	}
}
