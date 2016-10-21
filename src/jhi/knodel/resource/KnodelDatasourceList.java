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

package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelDatasourceList
{
	private List<KnodelDatasource> list = new ArrayList<>();

	public KnodelDatasourceList()
	{
	}

	public KnodelDatasourceList add(KnodelDatasource item)
	{
		list.add(item);
		return this;
	}

	public List<KnodelDatasource> getList()
	{
		return list;
	}

	public KnodelDatasourceList setList(List<KnodelDatasource> list)
	{
		this.list = list;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelDatasourceList{" +
				"list=" + list +
				'}';
	}
}
