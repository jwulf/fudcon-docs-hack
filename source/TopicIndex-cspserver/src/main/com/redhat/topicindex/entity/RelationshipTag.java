package com.redhat.topicindex.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import javax.persistence.Id;
import javax.persistence.Table;
import static javax.persistence.GenerationType.IDENTITY;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "RelationshipTag", uniqueConstraints = @UniqueConstraint(columnNames = {"RelationshipTagName" })) 
public class RelationshipTag extends AuditedEntity<RelationshipTag> implements java.io.Serializable
{
	private static final long serialVersionUID = 1882693752297919114L;

	public static final String SELECT_ALL_QUERY = "select relationshipTag from RelationshipTag relationshipTag";
	
	private Integer relationshipTagId;
	private String relationshipTagName;
	private String relationshipTagDescription;

	public RelationshipTag()
	{
		super(RelationshipTag.class);
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "RelationshipTagId", unique = true, nullable = false)
	public Integer getRelationshipTagId()
	{
		return this.relationshipTagId;
	}

	public void setRelationshipTagId(final Integer relationshipTagId)
	{
		this.relationshipTagId = relationshipTagId;
	}

	@Column(name = "RelationshipTagName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getRelationshipTagName()
	{
		return this.relationshipTagName;
	}

	public void setRelationshipTagName(final String relationshipTagName)
	{
		this.relationshipTagName = relationshipTagName;
	}

	// @Column(name = "RelationshipTagDescription", length = 65535)
	@Column(name = "RelationshipTagDescription", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getRelationshipTagDescription()
	{
		return this.relationshipTagDescription;
	}

	public void setRelationshipTagDescription(final String relationshipTagDescription)
	{
		this.relationshipTagDescription = relationshipTagDescription;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.relationshipTagId;
	}
}
