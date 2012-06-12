package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import com.redhat.topicindex.entity.base.ToPropertyTag;
import com.redhat.topicindex.exceptions.CustomConstraintViolationException;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

@Audited
@Entity
@Table(name = "TopicToPropertyTag")
public class TopicToPropertyTag extends ToPropertyTag<TopicToPropertyTag>
{
	public static String SELECT_ALL_QUERY = "SELECT topicToPropertyTag FROM TopicToPropertyTag AS TopicToPropertyTag";
	private Integer TopicToPropertyTagID;
	private Topic topic;

	public TopicToPropertyTag()
	{
		super(TopicToPropertyTag.class);
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicToPropertyTagID", unique = true, nullable = false)
	public Integer getTopicToPropertyTagID()
	{
		return TopicToPropertyTagID;
	}

	public void setTopicToPropertyTagID(final Integer TopicToPropertyTagID)
	{
		this.TopicToPropertyTagID = TopicToPropertyTagID;
	}

	@ManyToOne
	@JoinColumn(name = "TopicID", nullable = false)
	@NotNull
	public Topic getTopic()
	{
		return topic;
	}

	public void setTopic(final Topic topic)
	{
		this.topic = topic;
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
		return this.TopicToPropertyTagID;
	}

	@Override
	protected boolean testUnique()
	{
		try
		{
			if (this.propertyTag.getPropertyTagIsUnique())
			{

				for (final TopicToPropertyTag mapping : this.propertyTag.getTopicToPropertyTags())
				{
					if (!mapping.getTopicToPropertyTagID().equals(this.getTopicToPropertyTagID()) && mapping.getValue().equals(this.getValue()))
						return false;
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error querying the this.propertyTag.getTopicToPropertyTags() property");
			return false;
		}
		
		return true;
	}
}
