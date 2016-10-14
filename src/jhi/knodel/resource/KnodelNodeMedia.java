package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelNodeMedia extends DatabaseObject
{
	public static final String TABLE_NAME     = "nodemedia";
	public static final String FIELD_NODE_ID  = "node_id";
	public static final String FIELD_MEDIA_ID = "media_id";

	private Integer nodeId;
	private Integer mediaId;

	public KnodelNodeMedia()
	{
	}

	public KnodelNodeMedia(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelNodeMedia(int id, Date createdOn, Date updatedOn, Integer nodeId, Integer mediaId)
	{
		super(id, createdOn, updatedOn);
		this.nodeId = nodeId;
		this.mediaId = mediaId;
	}

	public Integer getNodeId()
	{
		return nodeId;
	}

	public KnodelNodeMedia setNodeId(Integer nodeId)
	{
		this.nodeId = nodeId;
		return this;
	}

	public Integer getMediaId()
	{
		return mediaId;
	}

	public KnodelNodeMedia setMediaId(Integer mediaId)
	{
		this.mediaId = mediaId;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelNodeMedia{" +
				"nodeId=" + nodeId +
				", mediaId=" + mediaId +
				"} " + super.toString();
	}
}
