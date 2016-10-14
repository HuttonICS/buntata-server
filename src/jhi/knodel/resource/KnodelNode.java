package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelNode extends DatabaseObject
{
	public static final String TABLE_NAME          = "nodes";
	public static final String FIELD_DATASOURCE_ID = "datasource_id";
	public static final String FIELD_NAME          = "name";
	public static final String FIELD_DESCRIPTION   = "description";

	private Integer datasourceId;
	private String  name;
	private String  description;

	public KnodelNode()
	{
	}

	public KnodelNode(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelNode(int id, Date createdOn, Date updatedOn, Integer datasourceId, String name, String description)
	{
		super(id, createdOn, updatedOn);
		this.datasourceId = datasourceId;
		this.name = name;
		this.description = description;
	}

	public Integer getDatasourceId()
	{
		return datasourceId;
	}

	public KnodelNode setDatasourceId(Integer datasourceId)
	{
		this.datasourceId = datasourceId;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public KnodelNode setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public KnodelNode setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelNode{" +
				"datasourceId=" + datasourceId +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				"} " + super.toString();
	}
}
