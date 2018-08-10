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

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuntataNode extends DatabaseObject
{
	public static final String TABLE_NAME          = "nodes";
	public static final String FIELD_DATASOURCE_ID = "datasource_id";
	public static final String FIELD_NAME          = "name";
	public static final String FIELD_DESCRIPTION   = "description";

	private Integer                         datasourceId;
	private String                          name;
	private String                          description;
	private Map<String, List<BuntataMedia>> media;
	private List<BuntataAttributeValue>     attributeValues;
	private List<BuntataNode>               similarNodes;

	public BuntataNode()
	{
	}

	public BuntataNode(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public Integer getDatasourceId()
	{
		return datasourceId;
	}

	public BuntataNode setDatasourceId(Integer datasourceId)
	{
		this.datasourceId = datasourceId;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public BuntataNode setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public BuntataNode setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public Map<String, List<BuntataMedia>> getMedia()
	{
		return media;
	}

	public BuntataNode setMedia(Map<String, List<BuntataMedia>> media)
	{
		this.media = media;
		return this;
	}

	public List<BuntataAttributeValue> getAttributeValues()
	{
		return attributeValues;
	}

	public BuntataNode setAttributeValues(List<BuntataAttributeValue> attributeValues)
	{
		this.attributeValues = attributeValues;
		return this;
	}

	public List<BuntataNode> getSimilarNodes()
	{
		return similarNodes;
	}

	public BuntataNode setSimilarNodes(List<BuntataNode> similarNodes)
	{
		this.similarNodes = similarNodes;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataNode{" +
			"datasourceId=" + datasourceId +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", media=" + media +
			", attributeValues=" + attributeValues +
			", similarNodes=" + similarNodes +
			"} " + super.toString();
	}

	public void restrict()
	{
		if (media != null)
		{
			for (Map.Entry<String, List<BuntataMedia>> pair : media.entrySet())
			{
				for (BuntataMedia m : pair.getValue())
				{
					m.restrict();
				}
			}
		}
	}
}
