/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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
public class BuntataSimilarity extends DatabaseObject
{
	public static final String TABLE_NAME      = "similarities";
	public static final String FIELD_NODE_A_ID = "node_a_id";
	public static final String FIELD_NODE_B_ID = "node_b_id";

	private Long nodeAId;
	private Long nodeBId;

	public BuntataSimilarity()
	{
	}

	public BuntataSimilarity(Long id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public Long getNodeAId()
	{
		return nodeAId;
	}

	public BuntataSimilarity setNodeAId(Long nodeAId)
	{
		this.nodeAId = nodeAId;
		return this;
	}

	public Long getNodeBId()
	{
		return nodeBId;
	}

	public BuntataSimilarity setNodeBId(Long nodeBId)
	{
		this.nodeBId = nodeBId;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataSimilarity{" +
			"nodeAId=" + nodeAId +
			", nodeBId=" + nodeBId +
			"} " + super.toString();
	}
}
