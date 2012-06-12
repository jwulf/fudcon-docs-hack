package com.redhat.topicindex.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "PropertyTagCategory", uniqueConstraints = @UniqueConstraint(columnNames = { "PropertyTagCategoryName" }))
public class PropertyTagCategory extends AuditedEntity<PropertyTagCategory> implements java.io.Serializable
{
	private static final long serialVersionUID = -8850771550313264840L;
	public static final String SELECT_ALL_QUERY = "select propertyTagCategory from PropertyTagCategory propertyTagCategory";

	private Integer propertyTagCategoryId;
	private String propertyTagCategoryName;
	private String propertyTagCategoryDescription;
	private Set<PropertyTagToPropertyTagCategory> propertyTagToPropertyTagCategories = new HashSet<PropertyTagToPropertyTagCategory>(0);
	
	public PropertyTagCategory()
	{
		super(PropertyTagCategory.class);
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "PropertyTagCategoryID", unique = true, nullable = false)
	public Integer getPropertyTagCategoryId()
	{
		return propertyTagCategoryId;
	}

	public void setPropertyTagCategoryId(final Integer propertyTagCategoryId)
	{
		this.propertyTagCategoryId = propertyTagCategoryId;
	}

	@Column(name = "PropertyTagCategoryName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getPropertyTagCategoryName()
	{
		return propertyTagCategoryName;
	}

	public void setPropertyTagCategoryName(final String propertyTagCategoryName)
	{
		this.propertyTagCategoryName = propertyTagCategoryName;
	}

	@Column(name = "PropertyTagCategoryDescription", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getPropertyTagCategoryDescription()
	{
		return propertyTagCategoryDescription;
	}

	public void setPropertyTagCategoryDescription(final String propertyTagCategoryDescription)
	{
		this.propertyTagCategoryDescription = propertyTagCategoryDescription;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTagCategory", cascade = CascadeType.ALL, orphanRemoval = true)
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
	}
	
	public boolean hasPropertyTag(final PropertyTag propertyTag)
	{
		for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : this.propertyTagToPropertyTagCategories)
			if (propertyTagToPropertyTagCategory.getPropertyTag().equals(propertyTag))
				return true;
		return false;
	}
	
	public boolean removePropertyTag(final PropertyTag propertyTag)
	{
		for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : propertyTagToPropertyTagCategories)
		{
			if (propertyTagToPropertyTagCategory.getPropertyTag().equals(propertyTag))
			{
				propertyTagToPropertyTagCategories.remove(propertyTagToPropertyTagCategory);
				propertyTag.getPropertyTagToPropertyTagCategories().remove(propertyTagToPropertyTagCategory);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean addPropertyTag(final PropertyTag propertyTag)
	{
		boolean found = false;
		for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : propertyTagToPropertyTagCategories)
		{
			if (propertyTagToPropertyTagCategory.getPropertyTag().equals(propertyTag))
			{
				found = true;
				break;
			}			
		}
		
		if (!found)
		{
			final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory = new PropertyTagToPropertyTagCategory();
			propertyTagToPropertyTagCategory.setPropertyTag(propertyTag);
			propertyTagToPropertyTagCategory.setPropertyTagCategory(this);
			propertyTagToPropertyTagCategories.add(propertyTagToPropertyTagCategory);
			propertyTag.getPropertyTagToPropertyTagCategories().add(propertyTagToPropertyTagCategory);
		}
		
		return !found;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.propertyTagCategoryId;
	}
}