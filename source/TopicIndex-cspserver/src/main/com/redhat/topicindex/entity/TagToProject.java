package com.redhat.topicindex.entity;

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

@Audited
@Entity
@Table(name = "TagToProject", uniqueConstraints = @UniqueConstraint(columnNames = { "ProjectID", "TagID" }))
public class TagToProject extends AuditedEntity<TagToProject> implements java.io.Serializable
{
	private static final long serialVersionUID = 8977075767446465613L;
	private Integer tagToProjectId;
	private Project project;
	private Tag tag;

	public TagToProject()
	{
		super(TagToProject.class);
	}

	public TagToProject(final Project project, final Tag tag)
	{
		super(TagToProject.class);
		this.project = project;
		this.tag = tag;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TagToProjectID", unique = true, nullable = false)
	public Integer getTagToProjectId()
	{
		return this.tagToProjectId;
	}

	public void setTagToProjectId(final Integer tagToProjectId)
	{
		this.tagToProjectId = tagToProjectId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ProjectID", nullable = false)
	@NotNull
	public Project getProject()
	{
		return this.project;
	}

	public void setProject(final Project project)
	{
		this.project = project;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TagID", nullable = false)
	@NotNull
	public Tag getTag()
	{
		return this.tag;
	}

	public void setTag(final Tag tag)
	{
		this.tag = tag;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.tagToProjectId;
	}

}
