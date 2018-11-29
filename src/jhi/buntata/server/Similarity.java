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

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;

import org.restlet.data.Status;
import org.restlet.resource.*;

import java.util.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link ServerResource} handling {@link Similarity} requests.
 *
 * @author Sebastian Raubach
 */
public class Similarity extends ServerResource
{
	private final SimilarityDAO dao = new SimilarityDAO();

	@Delete("json")
	public boolean deleteJson(BuntataSimilarity similarity)
	{
		if (similarity == null)
		{
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
		else
		{
			BuntataSimilarity ds = dao.getFor(similarity.getNodeAId(), similarity.getNodeBId());

			if (ds != null)
			{
				dao.delete(ds.getId());
				return true;
			}
			else
			{
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}
	}

	@Post("json")
	public boolean postJson(String json)
	{
		List<BuntataSimilarity> similarities = fromJSON(new TypeReference<List<BuntataSimilarity>>()
		{
		}, json);

		boolean atLeastOne = false;

		for (BuntataSimilarity similarity : similarities)
		{
			BuntataSimilarity r = dao.getFor(similarity.getNodeAId(), similarity.getNodeBId());
			if (r == null)
			{
				atLeastOne |= (dao.add(similarity) != null);
			}
		}

		return atLeastOne;
	}

	public static <T> T fromJSON(final TypeReference<T> type, final String jsonPacket)
	{
		T data = null;

		try
		{
			data = new ObjectMapper().readValue(jsonPacket, type);
		}
		catch (Exception e)
		{
			// Handle the problem
		}
		return data;
	}
}
