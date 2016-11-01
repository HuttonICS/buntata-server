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
public class BuntataDatasource extends DatabaseObject
{
	public static final String TABLE_NAME           = "datasources";
	public static final String FIELD_NAME           = "name";
	public static final String FIELD_DESCRIPTION    = "description";
	public static final String FIELD_VERSION_NUMBER = "version_number";
	public static final String FIELD_DATA_PROVIDER  = "data_provider";
	public static final String FIELD_CONTACT        = "contact";
	public static final String FIELD_ICON           = "icon";
	public static final String FIELD_SIZE_TOTAL     = "size_total";
	public static final String FIELD_SIZE_NO_VIDEO  = "size_no_video";

	private String name;
	private String description;
	private int    versionNumber;
	private String dataProvider;
	private String contact;
	private String icon;
	private long   sizeTotal;
	private long   sizeNoVideo;

	public BuntataDatasource()
	{
	}

	public BuntataDatasource(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public BuntataDatasource(int id, Date createdOn, Date updatedOn, String name, String description, int versionNumber, String dataProvider, String contact, String icon, long sizeTotal)
	{
		super(id, createdOn, updatedOn);
		this.name = name;
		this.description = description;
		this.versionNumber = versionNumber;
		this.dataProvider = dataProvider;
		this.contact = contact;
		this.icon = icon;
		this.sizeTotal = sizeTotal;
	}

	public String getName()
	{
		return name;
	}

	public BuntataDatasource setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public BuntataDatasource setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public int getVersionNumber()
	{
		return versionNumber;
	}

	public BuntataDatasource setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
		return this;
	}

	public String getDataProvider()
	{
		return dataProvider;
	}

	public BuntataDatasource setDataProvider(String dataProvider)
	{
		this.dataProvider = dataProvider;
		return this;
	}

	public String getContact()
	{
		return contact;
	}

	public BuntataDatasource setContact(String contact)
	{
		this.contact = contact;
		return this;
	}

	public String getIcon()
	{
		return icon;
	}

	public BuntataDatasource setIcon(String icon)
	{
		this.icon = icon;
		return this;
	}

	public long getSizeTotal()
	{
		return sizeTotal;
	}

	public BuntataDatasource setSizeTotal(long sizeTotal)
	{
		this.sizeTotal = sizeTotal;
		return this;
	}

	public long getSizeNoVideo()
	{
		return sizeNoVideo;
	}

	public BuntataDatasource setSizeNoVideo(long sizeNoVideo)
	{
		this.sizeNoVideo = sizeNoVideo;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataDatasource{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", versionNumber=" + versionNumber +
				", dataProvider='" + dataProvider + '\'' +
				", contact='" + contact + '\'' +
				", icon='" + icon + '\'' +
				", sizeTotal=" + sizeTotal +
				", sizeNoVideo=" + sizeNoVideo +
				"} " + super.toString();
	}
}