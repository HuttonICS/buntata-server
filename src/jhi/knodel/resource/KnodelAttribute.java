package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelAttribute extends DatabaseObject
{
	public static final String TABLE_NAME = "attributes";
	public static final String FIELD_NAME = "name";

	private String name;

	public KnodelAttribute()
	{
	}

	public KnodelAttribute(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public String getName()
	{
		return name;
	}

	public KnodelAttribute setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelAttribute{" +
				"name='" + name + '\'' +
				"} " + super.toString();
	}
}
