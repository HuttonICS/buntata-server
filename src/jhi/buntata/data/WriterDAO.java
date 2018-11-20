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

import jhi.database.server.*;
import jhi.database.shared.exception.*;
import jhi.database.shared.util.*;

/**
 * @author Sebastian Raubach
 */
public abstract class WriterDAO<T extends DatabaseObject>
{
	public boolean add(T object)
	{
		try
		{
			Database database = Database.connect();
			DatabaseObjectWriter<T> writer = getWriter();
			writer.write(object, writer.getStatement(database));
			database.close();
			return true;
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	protected abstract DatabaseObjectWriter<T> getWriter();
}
