package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelDatasource
{
	private String name;
	private String description;
	private int    versionNumber;
	private String dataProvider;
	private String contact;
	private String icon;

	public String getName()
	{
		return name;
	}

	public KnodelDatasource setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public KnodelDatasource setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public int getVersionNumber()
	{
		return versionNumber;
	}

	public KnodelDatasource setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
		return this;
	}

	public String getDataProvider()
	{
		return dataProvider;
	}

	public KnodelDatasource setDataProvider(String dataProvider)
	{
		this.dataProvider = dataProvider;
		return this;
	}

	public String getContact()
	{
		return contact;
	}

	public KnodelDatasource setContact(String contact)
	{
		this.contact = contact;
		return this;
	}

	public String getIcon()
	{
		return icon;
	}

	public KnodelDatasource setIcon(String icon)
	{
		this.icon = icon;
		return this;
	}
}
