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

/**
 * {@link ServerResource} handling {@link NodeDAO} {@link BuntataMedia} object requests.
 *
 * @author Sebastian Raubach
 */
public class NodeMedia extends ServerResource
{
	private final NodeDAO  nodeDAO  = new NodeDAO();
	private final MediaDAO mediaDAO = new MediaDAO();
	private       Long     id       = null;

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

	@Get("json")
	public Map<String, List<BuntataMedia>> getMedia()
	{
		BuntataNode node = nodeDAO.get(id);

		Map<String, List<BuntataMedia>> result = new HashMap<>();

		if (node != null)
			result = mediaDAO.getAllForNode(node.getId(), false);

		// Restrict paths to just the filename
		for (Map.Entry<String, List<BuntataMedia>> e : result.entrySet())
			e.getValue().forEach(BuntataMedia::restrict);

		return result;
	}
}
