package com.redhat.topicindex.entity;

// Generated Jun 2, 2011 7:22:40 AM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

/**
 * TopicToTag generated by hbm2java
 */
@Entity
@Audited
@Table(name = "TopicToTag", uniqueConstraints = @UniqueConstraint(columnNames = { "TopicID", "TagID" }))
public class TopicToTag extends AuditedEntity<TopicToTag> implements java.io.Serializable
{
	private static final long serialVersionUID = -7516063608506037594L;
	private Integer topicToTagId;
	private Topic topic;
	private Tag tag;

	public TopicToTag()
	{
		super(TopicToTag.class);
	}

	public TopicToTag(final Topic topic, final Tag tag) 
	{
		super(TopicToTag.class);
		this.topic = topic;
		this.tag = tag;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicToTagID", unique = true, nullable = false)
	public Integer getTopicToTagId()
	{
		return this.topicToTagId;
	}

	public void setTopicToTagId(final Integer topicToTagId)
	{
		this.topicToTagId = topicToTagId;
	}

	@ManyToOne
	@JoinColumn(name = "TopicID", nullable = false)
	@NotNull
	public Topic getTopic()
	{
		return this.topic;
	}

	public void setTopic(final Topic topic)
	{
		this.topic = topic;
	}

	@ManyToOne
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
		return this.topicToTagId;
	}

}
