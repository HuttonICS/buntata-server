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

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link ServerResource} handling {@link Relationship} requests.
 *
 * @author Sebastian Raubach
 */
public class Relationship extends ServerResource
{
	private final RelationshipDAO dao = new RelationshipDAO();

	@Post("json")
	public Long postJson(BuntataRelationship relationship)
	{
		BuntataRelationship r = dao.getFor(relationship.getChild(), relationship.getParent());
		if (r != null)
		{
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
		}
		else
		{
			return dao.add(relationship);
		}
	}
}
