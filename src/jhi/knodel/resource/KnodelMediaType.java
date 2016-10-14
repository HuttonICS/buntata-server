package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelMediaType extends DatabaseObject
{
	public static final String TABLE_NAME = "mediatypes";
	public static final String FIELD_NAME = "name";

	private String name;

	public KnodelMediaType()
	{
	}

	public KnodelMediaType(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public String getName()
	{
		return name;
	}

	public KnodelMediaType setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelMediaType{" +
				"name='" + name + '\'' +
				"} " + super.toString();
	}
}
