package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelMedia extends DatabaseObject
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

	public KnodelMedia()
	{
	}

	public KnodelMedia(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelMedia(int id, Date createdOn, Date updatedOn, Integer mediaTypeId, String name, String description, String internalLink, String externalLink, String externalLinkDescription, String copyright)
	{
		super(id, createdOn, updatedOn);
		this.mediaTypeId = mediaTypeId;
		this.name = name;
		this.description = description;
		this.internalLink = internalLink;
		this.externalLink = externalLink;
		this.externalLinkDescription = externalLinkDescription;
		this.copyright = copyright;
	}

	public Integer getMediaTypeId()
	{
		return mediaTypeId;
	}

	public KnodelMedia setMediaTypeId(Integer mediaTypeId)
	{
		this.mediaTypeId = mediaTypeId;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public KnodelMedia setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public KnodelMedia setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getInternalLink()
	{
		return internalLink;
	}

	public KnodelMedia setInternalLink(String internalLink)
	{
		this.internalLink = internalLink;
		return this;
	}

	public String getExternalLink()
	{
		return externalLink;
	}

	public KnodelMedia setExternalLink(String externalLink)
	{
		this.externalLink = externalLink;
		return this;
	}

	public String getExternalLinkDescription()
	{
		return externalLinkDescription;
	}

	public KnodelMedia setExternalLinkDescription(String externalLinkDescription)
	{
		this.externalLinkDescription = externalLinkDescription;
		return this;
	}

	public String getCopyright()
	{
		return copyright;
	}

	public KnodelMedia setCopyright(String copyright)
	{
		this.copyright = copyright;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelMedia{" +
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
