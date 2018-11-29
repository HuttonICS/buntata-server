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

import org.restlet.data.Status;
import org.restlet.resource.*;

import java.util.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link ServerResource} handling {@link Node} requests.
 *
 * @author Sebastian Raubach
 */
public class Node extends ServerResource
{
	public static final String PARAM_DATASOURCE_ID  = "datasourceId";
	public static final String PARAM_NODE_PARENT_ID = "nodeParentId";
	public static final String PARAM_SEARCH_TERM    = "searchTerm";

	private final NodeDAO dao          = new NodeDAO();
	private       Long    id           = null;
	private       Long    datasourceId = null;
	private       Long    nodeParentId = null;
	private       String  searchTerm   = null;

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

		try
		{
			nodeParentId = Long.parseLong(getQueryValue(PARAM_NODE_PARENT_ID));
		}
		catch (NullPointerException | NumberFormatException e)
		{
		}

		try
		{
			datasourceId = Long.parseLong(getQueryValue(PARAM_DATASOURCE_ID));
		}
		catch (NullPointerException | NumberFormatException e)
		{
		}

		searchTerm = getQueryValue(PARAM_SEARCH_TERM);
	}

	@Post("json")
	public Long postJson(BuntataNode node)
	{
		if (id != null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		else
			return dao.add(node);
	}

	@Put("json")
	public Long putJson(BuntataNode node)
	{
		if (id == null)
		{
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}
		else
		{
			BuntataNode nd = dao.get(id);

			if (nd != null)
			{
				dao.update(node);
				return node.getId();
			}
			else
			{
				return dao.add(node);
			}
		}
	}

	@Get("json")
	public List<BuntataNode> getJson()
	{
		List<BuntataNode> result = new ArrayList<>();
		if (id != null)
		{
			BuntataNode ds = dao.get(id);
			if (ds != null)
				result.add(ds);
		}
		else if (nodeParentId != null)
		{
			result.addAll(dao.getAllForParent(nodeParentId));
		}
		else if (searchTerm != null)
		{
			if (datasourceId != null)
				result.addAll(dao.getAllForSearch(datasourceId, searchTerm));
			else
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		else
		{
			result.addAll(dao.getAll());
		}

		// Restrict paths to just the filename
		result.forEach(BuntataNode::restrict);

		return result;
	}
}
