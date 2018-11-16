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
	public static final String PARAM_TYPE = "type";

	private final NodeDAO dao = new NodeDAO();

	private Long     id   = null;
	private NodeType type = NodeType.ALL;

	public DatasourceNodeList()
	{
	}

	public void doInit()
	{
		super.doInit();

		try
		{
			this.id = Long.parseLong(this.getRequestAttributes().get("id").toString());
		}
		catch (NumberFormatException | NullPointerException var3)
		{
		}

		try
		{
			type = NodeType.getForName(getQueryValue(PARAM_TYPE));
		}
		catch (NullPointerException e)
		{
		}
	}

	@Get("json")
	public List<BuntataNode> getNodeList()
	{
		switch (type)
		{
			case ROOT:
				return dao.getAllForDatasourceRoot(id);
			case LEAF:
				return dao.getAllForDatasourceLeaf(id);
			case ALL:
			default:
				return dao.getAllForDatasource(id);
		}
	}

	private enum NodeType
	{
		LEAF("leaf"),
		ROOT("root"),
		ALL("all)");

		String name;

		NodeType(String name)
		{
			this.name = name;
		}

		public static NodeType getForName(String name)
		{
			for (NodeType type : values())
				if (Objects.equals(type.name, name))
					return type;

			return NodeType.ALL;
		}
	}
}