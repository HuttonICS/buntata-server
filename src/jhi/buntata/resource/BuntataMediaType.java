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

package jhi.buntata.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

import jhi.database.shared.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuntataMediaType extends DatabaseObject
{
	public static final String TYPE_IMAGE = "Image";
	public static final String TYPE_VIDEO = "Video";

	public static final String TABLE_NAME = "mediatypes";
	public static final String FIELD_NAME = "name";

	private String name;

	public BuntataMediaType()
	{
	}

	public BuntataMediaType(Long id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public String getName()
	{
		return name;
	}

	public BuntataMediaType setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataMediaType{" +
				"name='" + name + '\'' +
				"} " + super.toString();
	}
}
