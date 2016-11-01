/*
 * Copyright (c) 2016 Information & Computational Sciences, The James Hutton Institute
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

package jhi.buntata.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuntataRelationship extends DatabaseObject
{
	public static final String TABLE_NAME   = "relationships";
	public static final String FIELD_PARENT = "parent";
	public static final String FIELD_CHILD  = "child";

	private Integer parent;
	private Integer child;

	public BuntataRelationship()
	{
	}

	public BuntataRelationship(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public BuntataRelationship(int id, Date createdOn, Date updatedOn, Integer parent, Integer child)
	{
		super(id, createdOn, updatedOn);
		this.parent = parent;
		this.child = child;
	}

	public Integer getParent()
	{
		return parent;
	}

	public BuntataRelationship setParent(Integer parent)
	{
		this.parent = parent;
		return this;
	}

	public Integer getChild()
	{
		return child;
	}

	public BuntataRelationship setChild(Integer child)
	{
		this.child = child;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataRelationship{" +
				"parent=" + parent +
				", child=" + child +
				"} " + super.toString();
	}
}
