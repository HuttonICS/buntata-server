package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelRelationship extends DatabaseObject
{
	public static final String TABLE_NAME   = "relationships";
	public static final String FIELD_PARENT = "parent";
	public static final String FIELD_CHILD  = "child";

	private Integer parent;
	private Integer child;

	public KnodelRelationship()
	{
	}

	public KnodelRelationship(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelRelationship(int id, Date createdOn, Date updatedOn, Integer parent, Integer child)
	{
		super(id, createdOn, updatedOn);
		this.parent = parent;
		this.child = child;
	}

	public Integer getParent()
	{
		return parent;
	}

	public KnodelRelationship setParent(Integer parent)
	{
		this.parent = parent;
		return this;
	}

	public Integer getChild()
	{
		return child;
	}

	public KnodelRelationship setChild(Integer child)
	{
		this.child = child;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelRelationship{" +
				"parent=" + parent +
				", child=" + child +
				"} " + super.toString();
	}
}
