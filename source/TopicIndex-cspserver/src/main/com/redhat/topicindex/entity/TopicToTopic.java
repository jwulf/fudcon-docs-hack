package com.redhat.topicindex.entity;

import javax.naming.InitialContext;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.messaging.TopicRendererType;

@Audited
@Entity
@Table(name = "TopicToTopic", uniqueConstraints = @UniqueConstraint(columnNames = { "MainTopicID", "RelatedTopicID" }))
public class TopicToTopic extends AuditedEntity<TopicToTopic> implements java.io.Serializable
{
	private static final long serialVersionUID = -589601408520832737L;
	private Integer topicToTopicId;
	private Topic mainTopic;
	private Topic relatedTopic;
	private RelationshipTag relationshipTag;

	public TopicToTopic()
	{
		super(TopicToTopic.class);
	}

	public TopicToTopic(final Topic mainTopic, final Topic relatedTopic, final RelationshipTag relationshipTag)
	{
		super(TopicToTopic.class);
		this.mainTopic = mainTopic;
		this.relatedTopic = relatedTopic;
		this.relationshipTag = relationshipTag;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicToTopicID", unique = true, nullable = false)
	public Integer getTopicToTopicId()
	{
		return this.topicToTopicId;
	}

	public void setTopicToTopicId(final Integer topicToTopicId)
	{
		this.topicToTopicId = topicToTopicId;
	}

	@ManyToOne
	@JoinColumn(name = "MainTopicID", nullable = false)
	@NotNull
	public Topic getMainTopic()
	{
		return this.mainTopic;
	}

	public void setMainTopic(final Topic mainTopic)
	{
		this.mainTopic = mainTopic;
	}

	@ManyToOne
	@JoinColumn(name = "RelatedTopicID", nullable = false)
	@NotNull
	public Topic getRelatedTopic()
	{
		return this.relatedTopic;
	}

	public void setRelatedTopic(final Topic relatedTopic)
	{
		this.relatedTopic = relatedTopic;
	}

	/**
	 * When a TopicToTopic mapping is created or destroyed, we rerender the main
	 * topic to update the generic injection lists.
	 */
	@SuppressWarnings("unused")
	@PostPersist
	@PostRemove
	private void reRender()
	{
		/*
		 * Send a message to the queue that this topic, and all those that have
		 * inbound relationships, need to be rerendered. We use the
		 * TopicQueueRenderer to send the topic id once the transaction has
		 * finished.
		 */
		try
		{
			if (this.mainTopic != null)
			{
				final InitialContext initCtx = new InitialContext();
				final TransactionManager transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
				final Transaction transaction = transactionManager.getTransaction();
				WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(mainTopic.getTopicId(), TopicRendererType.TOPIC, transaction));
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a STOMP messaging problem");
		}
	}

	@ManyToOne
	@JoinColumn(name = "RelationshipTagID", nullable = false)
	@NotNull
	public RelationshipTag getRelationshipTag()
	{
		return relationshipTag;
	}

	public void setRelationshipTag(final RelationshipTag relationshipTag)
	{
		this.relationshipTag = relationshipTag;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.topicToTopicId;
	}

}
