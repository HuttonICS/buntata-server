package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelAttributeValue extends DatabaseObject
{
	public static final String TABLE_NAME         = "attributevalues";
	public static final String FIELD_NODE_ID      = "node_id";
	public static final String FIELD_ATTRIBUTE_ID = "attribute_id";
	public static final String FIELD_VALUE        = "value";

	private Integer nodeId;
	private Integer attributeId;
	private String  value;

	public KnodelAttributeValue()
	{
	}

	public KnodelAttributeValue(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelAttributeValue(int id, Date createdOn, Date updatedOn, Integer nodeId, Integer attributeId, String value)
	{
		super(id, createdOn, updatedOn);
		this.nodeId = nodeId;
		this.attributeId = attributeId;
		this.value = value;
	}

	public Integer getNodeId()
	{
		return nodeId;
	}

	public KnodelAttributeValue setNodeId(Integer nodeId)
	{
		this.nodeId = nodeId;
		return this;
	}

	public Integer getAttributeId()
	{
		return attributeId;
	}

	public KnodelAttributeValue setAttributeId(Integer attributeId)
	{
		this.attributeId = attributeId;
		return this;
	}

	public String getValue()
	{
		return value;
	}

	public KnodelAttributeValue setValue(String value)
	{
		this.value = value;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelAttributeValue{" +
				"nodeId=" + nodeId +
				", attributeId=" + attributeId +
				", value='" + value + '\'' +
				"} " + super.toString();
	}
}
