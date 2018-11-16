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

import java.util.*;

import jhi.database.server.*;
import jhi.database.shared.exception.*;
import jhi.database.shared.util.*;

/**
 * {@link DatabaseObjectWriter} is an interface defining how a {@link DatabaseObject} should be written.
 *
 * @author Sebastian Raubach
 */

public abstract class DatabaseObjectWriter<T extends DatabaseObject>
{
	public abstract void write(T object, DatabaseStatement stmt)
		throws DatabaseException;

	public abstract void writeBatched(T object, DatabaseStatement stmt)
		throws DatabaseException;

	public abstract DatabaseStatement getStatement(Database database)
		throws DatabaseException;

	protected void setDate(int i, Date date, DatabaseStatement stmt)
		throws DatabaseException
	{
		Database.DatabaseType type = Database.getDbType();

		switch (type)
		{
			case SQLITE:
				stmt.setLong(i, date.getTime());
				break;
			case MYSQL:
			default:
				stmt.setTimestamp(i, date);
				break;
		}
	}
}
