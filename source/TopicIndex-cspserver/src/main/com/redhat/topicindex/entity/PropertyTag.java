package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "PropertyTag", uniqueConstraints = @UniqueConstraint(columnNames = { "PropertyTagName" }))
public class PropertyTag extends AuditedEntity<PropertyTag> implements java.io.Serializable
{
	private static final long serialVersionUID = -9064491060913710869L;
	public static final String SELECT_ALL_QUERY = "select propertyTag from PropertyTag propertyTag";
	private Integer propertyTagId;
	private String propertyTagName;
	private String propertyTagDescription;
	private String propertyTagRegex;
	private boolean propertyTagCanBeNull;
	private Set<TagToPropertyTag> tagToPropertyTags = new HashSet<TagToPropertyTag>(0);
	private Set<TopicToPropertyTag> topicToPropertyTags = new HashSet<TopicToPropertyTag>(0);
	private Set<PropertyTagToPropertyTagCategory> propertyTagToPropertyTagCategories = new HashSet<PropertyTagToPropertyTagCategory>(0);
	private Boolean propertyTagIsUnique;
	
	public PropertyTag()
	{
		super(PropertyTag.class);
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "PropertyTagID", unique = true, nullable = false)
	public Integer getPropertyTagId()
	{
		return propertyTagId;
	}

	public void setPropertyTagId(final Integer propertyTagId)
	{
		this.propertyTagId = propertyTagId;
	}

	@Column(name = "PropertyTagName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getPropertyTagName()
	{
		return propertyTagName;
	}

	public void setPropertyTagName(final String propertyTagName)
	{
		this.propertyTagName = propertyTagName;
	}

	@Column(name = "PropertyTagDescription", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getPropertyTagDescription()
	{
		return propertyTagDescription;
	}

	public void setPropertyTagDescription(final String propertyTagDescription)
	{
		this.propertyTagDescription = propertyTagDescription;
	}

	@Column(name = "PropertyTagRegex", columnDefinition = "TEXT")
	@NotNull
	@Length(max = 65535)
	public String getPropertyTagRegex()
	{
		return propertyTagRegex;
	}

	public void setPropertyTagRegex(final String propertyTagRegex)
	{
		this.propertyTagRegex = propertyTagRegex;
	}

	@Column(name = "PropertyTagCanBeNull", unique = true, nullable = false)
	@NotNull
	public boolean isPropertyTagCanBeNull()
	{
		return propertyTagCanBeNull;
	}

	public void setPropertyTagCanBeNull(final boolean propertyTagCanBeNull)
	{
		this.propertyTagCanBeNull = propertyTagCanBeNull;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToPropertyTag> getTagToPropertyTags()
	{
		return tagToPropertyTags;
	}

	public void setTagToPropertyTags(final Set<TagToPropertyTag> tagToPropertyTags)
	{
		this.tagToPropertyTags = tagToPropertyTags;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToPropertyTag> getTopicToPropertyTags()
	{
		return topicToPropertyTags;
	}

	public void setTopicToPropertyTags(final Set<TopicToPropertyTag> topicToPropertyTags)
	{
		this.topicToPropertyTags = topicToPropertyTags;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<PropertyTagToPropertyTagCategory> getPropertyTagToPropertyTagCategories()
	{
		return propertyTagToPropertyTagCategories;
	}

	public void setPropertyTagToPropertyTagCategories(final Set<PropertyTagToPropertyTagCategory> propertyTagToPropertyTagCategories)
	{
		this.propertyTagToPropertyTagCategories = propertyTagToPropertyTagCategories;
	}
	
	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		this.propertyTagToPropertyTagCategories.clear();
		this.tagToPropertyTags.clear();
	}
	
	@Transient
	public boolean isInCategory(final PropertyTagCategory propertyTagCategory)
	{
		for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : this.propertyTagToPropertyTagCategories )
			if (propertyTagToPropertyTagCategory.getPropertyTagCategory().equals(propertyTagCategory))
				return true;
		
		return false;
	}
	
	@Transient
	public PropertyTagToPropertyTagCategory getCategory(final Integer categoryId)
	{
		for (final PropertyTagToPropertyTagCategory category : this.propertyTagToPropertyTagCategories)
			if (categoryId.equals(category.getPropertyTagCategory().getPropertyTagCategoryId()))
				return category;

		return null;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.propertyTagId;
	}
	
	@Column(name = "PropertyTagIsUnqiue", nullable = false)
	public Boolean getPropertyTagIsUnique()
	{
		return propertyTagIsUnique;
	}

	public void setPropertyTagIsUnique(final Boolean isUnique)
	{
		this.propertyTagIsUnique = isUnique;
	}
}
