package jhi.knodel.resource;

import java.util.*;

/**
 * @author Sebastian Raubach
 */

public abstract class DatabaseObject
{
	public static final String FIELD_ID         = "id";
	public static final String FIELD_CREATED_ON = "created_on";
	public static final String FIELD_UPDATED_ON = "updated_on";

	protected int  id;
	protected Date createdOn;
	protected Date updatedOn;

	public DatabaseObject()
	{
	}

	public DatabaseObject(int id, Date createdOn, Date updatedOn)
	{
		this.id = id;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
	}

	public int getId()
	{
		return id;
	}

	public DatabaseObject setId(int id)
	{
		this.id = id;
		return this;
	}

	public Date getCreatedOn()
	{
		return createdOn;
	}

	public DatabaseObject setCreatedOn(Date createdOn)
	{
		this.createdOn = createdOn;
		return this;
	}

	public Date getUpdatedOn()
	{
		return updatedOn;
	}

	public DatabaseObject setUpdatedOn(Date updatedOn)
	{
		this.updatedOn = updatedOn;
		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DatabaseObject that = (DatabaseObject) o;

		return id == that.id;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return "DatabaseObject{" +
				"id=" + id +
				", createdOn=" + createdOn +
				", updatedOn=" + updatedOn +
				'}';
	}
}
