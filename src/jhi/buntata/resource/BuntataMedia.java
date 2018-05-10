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
public class BuntataMedia extends DatabaseObject
{
	public static final String TABLE_NAME                      = "media";
	public static final String FIELD_MEDIATYPE_ID              = "mediatype_id";
	public static final String FIELD_NAME                      = "name";
	public static final String FIELD_DESCRIPTION               = "description";
	public static final String FIELD_INTERNAL_LINK             = "internal_link";
	public static final String FIELD_EXTERNAL_LINK             = "external_link";
	public static final String FIELD_EXTERNAL_LINK_DESCRIPTION = "external_link_description";
	public static final String FIELD_COPYRIGHT                 = "copyright";

	private Integer mediaTypeId;
	private String  name;
	private String  description;
	private String  internalLink;
	private String  externalLink;
	private String  externalLinkDescription;
	private String  copyright;

	public BuntataMedia()
	{
	}

	public BuntataMedia(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public Integer getMediaTypeId()
	{
		return mediaTypeId;
	}

	public BuntataMedia setMediaTypeId(Integer mediaTypeId)
	{
		this.mediaTypeId = mediaTypeId;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public BuntataMedia setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public BuntataMedia setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getInternalLink()
	{
		return internalLink;
	}

	public BuntataMedia setInternalLink(String internalLink)
	{
		this.internalLink = internalLink;
		return this;
	}

	public String getExternalLink()
	{
		return externalLink;
	}

	public BuntataMedia setExternalLink(String externalLink)
	{
		this.externalLink = externalLink;
		return this;
	}

	public String getExternalLinkDescription()
	{
		return externalLinkDescription;
	}

	public BuntataMedia setExternalLinkDescription(String externalLinkDescription)
	{
		this.externalLinkDescription = externalLinkDescription;
		return this;
	}

	public String getCopyright()
	{
		return copyright;
	}

	public BuntataMedia setCopyright(String copyright)
	{
		this.copyright = copyright;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataMedia{" +
				"mediaTypeId=" + mediaTypeId +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", internalLink='" + internalLink + '\'' +
				", externalLink='" + externalLink + '\'' +
				", externalLinkDescription='" + externalLinkDescription + '\'' +
				", copyright='" + copyright + '\'' +
				"} " + super.toString();
	}
}
