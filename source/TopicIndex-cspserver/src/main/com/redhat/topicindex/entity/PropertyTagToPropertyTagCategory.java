package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Audited
@Entity
@Table(name = "PropertyTagToPropertyTagCategory", uniqueConstraints = @UniqueConstraint(columnNames =
{ "PropertyTagID", "PropertyTagCategoryID" }))
public class PropertyTagToPropertyTagCategory extends AuditedEntity<PropertyTagToPropertyTagCategory> implements java.io.Serializable
{
	private static final long serialVersionUID = -4464431865504442832L;

	public static final String SELECT_ALL_QUERY = "select propertyTagToPropertyTagCategory from PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory";
	private Integer propertyTagToPropertyTagCategoryId;
	private PropertyTag propertyTag;
	private PropertyTagCategory propertyTagCategory;
	private Integer sorting;
	
	public PropertyTagToPropertyTagCategory()
	{
		super(PropertyTagToPropertyTagCategory.class);
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "PropertyTagToPropertyTagCategoryID", unique = true, nullable = false)
	public Integer getPropertyTagToPropertyTagCategoryId()
	{
		return propertyTagToPropertyTagCategoryId;
	}

	public void setPropertyTagToPropertyTagCategoryId(final Integer propertyTagToPropertyTagCategoryId)
	{
		this.propertyTagToPropertyTagCategoryId = propertyTagToPropertyTagCategoryId;
	}

	@ManyToOne
	@JoinColumn(name = "PropertyTagID", nullable = false)
	@NotNull
	public PropertyTag getPropertyTag()
	{
		return propertyTag;
	}

	public void setPropertyTag(final PropertyTag propertyTag)
	{
		this.propertyTag = propertyTag;
	}

	@ManyToOne
	@JoinColumn(name = "PropertyTagCategoryID", nullable = false)
	@NotNull
	public PropertyTagCategory getPropertyTagCategory()
	{
		return propertyTagCategory;
	}

	public void setPropertyTagCategory(final PropertyTagCategory propertyTagCategory)
	{
		this.propertyTagCategory = propertyTagCategory;
	}
	
	@Column(name = "Sorting")
	public Integer getSorting() 
	{
		return this.sorting;
	}

	public void setSorting(Integer sorting) 
	{
		this.sorting = sorting;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.propertyTagToPropertyTagCategoryId;
	}
}
