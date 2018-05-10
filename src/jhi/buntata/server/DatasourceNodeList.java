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

package jhi.buntata.server;

import org.restlet.resource.*;

import java.util.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

public class DatasourceNodeList extends ServerResource
{
	public static final String PARAM_ROOT = "root";

	private final NodeDAO dao = new NodeDAO();

	private int     id       = -1;
	private boolean onlyRoot = false;

	public DatasourceNodeList()
	{
	}

	public void doInit()
	{
		super.doInit();

		try
		{
			this.id = Integer.parseInt(this.getRequestAttributes().get("id").toString());
		}
		catch (NumberFormatException | NullPointerException var3)
		{
		}

		try
		{
			this.onlyRoot = Boolean.parseBoolean(getQueryValue(PARAM_ROOT));
		}
		catch (NullPointerException e)
		{
		}
	}

	@Get("json")
	public List<BuntataNode> getNodeList()
	{
		if (onlyRoot)
			return dao.getAllForDatasourceRoot(id);
		else
			return dao.getAllForDatasource(id);
	}
}