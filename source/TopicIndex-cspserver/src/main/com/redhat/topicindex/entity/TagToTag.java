package com.redhat.topicindex.entity;

// Generated Aug 8, 2011 11:54:01 AM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

/**
 * TagToTag generated by hbm2java
 */
@Audited
@Entity
@Table(name = "TagToTag", uniqueConstraints = @UniqueConstraint(columnNames =
{ "PrimaryTagID", "SecondaryTagID", "RelationshipType" }))
public class TagToTag extends AuditedEntity<TagToTag> implements java.io.Serializable
{
	private static final long serialVersionUID = -7025237939786775336L;
	private Integer tagToTagId;
	private TagToTagRelationship tagToTagRelationship;
	private Tag primaryTag;
	private Tag secondaryTag;

	public TagToTag()
	{
		super(TagToTag.class);
	}

	public TagToTag(final TagToTagRelationship tagToTagRelationship, final Tag primaryTag, final Tag secondaryTag)
	{
		super(TagToTag.class);
		this.tagToTagRelationship = tagToTagRelationship;
		this.primaryTag = primaryTag;
		this.secondaryTag = secondaryTag;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TagToTagID", unique = true, nullable = false)
	public Integer getTagToTagId()
	{
		return this.tagToTagId;
	}

	public void setTagToTagId(final Integer tagToTagId)
	{
		this.tagToTagId = tagToTagId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RelationshipType", nullable = false)
	@NotNull
	public TagToTagRelationship getTagToTagRelationship()
	{
		return this.tagToTagRelationship;
	}

	public void setTagToTagRelationship(final TagToTagRelationship tagToTagRelationship)
	{
		this.tagToTagRelationship = tagToTagRelationship;
	}

	@ManyToOne
	@JoinColumn(name = "PrimaryTagID", nullable = false)
	@NotNull
	public Tag getPrimaryTag()
	{
		return this.primaryTag;
	}

	public void setPrimaryTag(final Tag primaryTag)
	{
		this.primaryTag = primaryTag;
	}

	@ManyToOne
	@JoinColumn(name = "SecondaryTagID", nullable = false)
	@NotNull
	public Tag getSecondaryTag()
	{
		return this.secondaryTag;
	}

	public void setSecondaryTag(final Tag secondaryTag)
	{
		this.secondaryTag = secondaryTag;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.tagToTagId;
	}

}
