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
public class BuntataNodeMedia extends DatabaseObject
{
	public static final String TABLE_NAME     = "nodemedia";
	public static final String FIELD_NODE_ID  = "node_id";
	public static final String FIELD_MEDIA_ID = "media_id";

	private Long nodeId;
	private Long mediaId;

	public BuntataNodeMedia()
	{
	}

	public BuntataNodeMedia(Long id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public Long getNodeId()
	{
		return nodeId;
	}

	public BuntataNodeMedia setNodeId(Long nodeId)
	{
		this.nodeId = nodeId;
		return this;
	}

	public Long getMediaId()
	{
		return mediaId;
	}

	public BuntataNodeMedia setMediaId(Long mediaId)
	{
		this.mediaId = mediaId;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataNodeMedia{" +
			"nodeId=" + nodeId +
			", mediaId=" + mediaId +
			"} " + super.toString();
	}
}
