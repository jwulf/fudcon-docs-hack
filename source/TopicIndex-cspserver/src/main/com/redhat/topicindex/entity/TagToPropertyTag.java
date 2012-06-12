package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import com.redhat.topicindex.entity.base.ToPropertyTag;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

@Audited
@Entity
@Table(name = "TagToPropertyTag")
public class TagToPropertyTag extends ToPropertyTag<TagToPropertyTag>
{
	public static String SELECT_ALL_QUERY = "SELECT tagToPropertyTag FROM TagToPropertyTag AS TagToPropertyTag";
	
	public TagToPropertyTag()
	{
		super(TagToPropertyTag.class);
	}

	private Integer tagToPropertyTagID;
	private Tag tag;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TagToPropertyTagID", unique = true, nullable = false)
	public Integer getTagToPropertyTagID()
	{
		return tagToPropertyTagID;
	}

	public void setTagToPropertyTagID(final Integer tagToPropertyTagID)
	{
		this.tagToPropertyTagID = tagToPropertyTagID;
	}

	@ManyToOne
	@JoinColumn(name = "TagID", nullable = false)
	@NotNull
	public Tag getTag()
	{
		return tag;
	}

	public void setTag(final Tag tag)
	{
		this.tag = tag;
	}

	@Override
	@ManyToOne
	@JoinColumn(name = "PropertyTagID", nullable = false)
	@NotNull
	public PropertyTag getPropertyTag()
	{
		return propertyTag;
	}

	@Override
	public void setPropertyTag(final PropertyTag propertyTag)
	{
		this.propertyTag = propertyTag;
	}

	@Override
	@Column(name = "Value", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getValue()
	{
		return value;
	}

	@Override
	public void setValue(final String value)
	{
		this.value = value;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.tagToPropertyTagID;
	}
	
	@Override
	protected boolean testUnique()
	{
		try
		{
			if (this.propertyTag.getPropertyTagIsUnique())
			{

				for (final TagToPropertyTag mapping : this.propertyTag.getTagToPropertyTags())
				{
					if (!mapping.getTagToPropertyTagID().equals(this.getTagToPropertyTagID()) && mapping.getValue().equals(this.getValue()))
						return false;
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error querying the this.propertyTag.getTagToPropertyTags() property");
			return false;
		}
		
		return true;
	}
}
