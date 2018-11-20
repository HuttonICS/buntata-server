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

package jhi.buntata.server;

import org.restlet.resource.*;

import java.io.*;
import java.util.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link ServerResource} handling {@link Datasource} requests.
 *
 * @author Sebastian Raubach
 */
public class Datasource extends ServerResource
{
	private final DatasourceDAO dao = new DatasourceDAO();
	private       Long          id  = null;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.id = Long.parseLong(getRequestAttributes().get("id").toString());
		}
		catch (NullPointerException | NumberFormatException e)
		{
		}
	}

	@Post("json")
	public boolean postJson(BuntataDatasource datasource)
	{
		if (id != null)
		{
			throw new ResourceException(400);
		}
		else
		{
			return dao.add(datasource);
		}
	}

	@Put("json")
	public boolean putJson(BuntataDatasource datasource)
	{
		if (id == null)
		{
			throw new ResourceException(422);
		}
		else
		{
			BuntataDatasource ds = dao.get(id);

			if (ds != null)
			{
				throw new ResourceException(409);
			}
			else
			{
				return dao.add(datasource);
			}
		}
	}

	@Get("json")
	public List<BuntataDatasource> getJson()
	{
		List<BuntataDatasource> result = new ArrayList<>();
		if (id == null)
		{
			result = dao.getAll(false);
		}
		else
		{
			BuntataDatasource ds = dao.get(id);
			if (ds != null)
				result.add(ds);
		}

		result.forEach(ds -> {
			File icon = new File(ds.getIcon());

			ds.setIcon(icon.getName());
		});

		return result;
	}
}
