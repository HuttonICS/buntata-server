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
 * {@link ServerResource} handling {@link AttributeValue} requests.
 *
 * @author Sebastian Raubach
 */
public class AttributeValue extends ServerResource
{
	private final AttributeDAO      attributeDao = new AttributeDAO();
	private final AttributeValueDAO valueDao     = new AttributeValueDAO();

	private Long id = null;

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

	@Put("json")
	public Long putJson(BuntataAttributeValue av)
	{
		BuntataAttribute attr = attributeDao.getByName(av.getAttribute().getName());

		if (attr == null)
		{
			attributeDao.add(av.getAttribute());
			attr = av.getAttribute();
		}
		av.setAttributeId(attr.getId());
		av.setAttribute(attr);

		BuntataAttributeValue exists = valueDao.getFor(av.getNodeId(), attr.getId());

		if (exists != null)
		{
			valueDao.update(av);
			return av.getId();
		}
		else
		{
			return valueDao.add(av);
		}
	}

	@Post("json")
	public Long postJson(BuntataAttributeValue av)
	{
		BuntataAttribute attr = attributeDao.getByName(av.getAttribute().getName());

		if (attr == null)
		{
			attributeDao.add(av.getAttribute());
			attr = av.getAttribute();
		}
		av.setAttributeId(attr.getId());
		av.setAttribute(attr);

		BuntataAttributeValue exists = valueDao.getFor(av.getNodeId(), attr.getId());

		if (exists != null)
		{
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
		}
		else
		{
			return valueDao.add(av);
		}
	}

	@Delete("json")
	public boolean deleteJson()
	{
		if (id == null)
		{
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
		else
		{
			BuntataAttributeValue av = valueDao.get(id);

			if (av != null)
			{
				valueDao.delete(av.getId());
				return true;
			}
			else
			{
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}
	}
}
