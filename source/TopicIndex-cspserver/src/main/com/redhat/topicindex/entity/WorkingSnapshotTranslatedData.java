package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
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
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.messaging.TopicRendererType;

@Entity
@Audited
@Table(name = "WorkingSnapshotTranslatedData", uniqueConstraints = @UniqueConstraint(columnNames = { "SnapshotTopicID", "TranslationLocale" }))
public class WorkingSnapshotTranslatedData extends AuditedEntity<WorkingSnapshotTranslatedData> implements java.io.Serializable, SnapshotTranslatedDataBase
{
	private static final long serialVersionUID = 7470594104954257672L;

	public static final String SELECT_ALL_QUERY = "select workingSnapshotTranslatedData from WorkingSnapshotTranslatedData workingSnapshotTranslatedData";

	private Integer snapshotTranslatedDataId;
	private SnapshotTopic snapshotTopic;
	private String translatedXml;
	private String translatedXmlRendered;
	private String translationLocale;
	private Set<WorkingSnapshotTranslatedString> workingSnapshotTranslatedStrings = new HashSet<WorkingSnapshotTranslatedString>(0);
	private Date translatedXmlRenderedUpdated;
	private Topic enversTopic;
	
	public WorkingSnapshotTranslatedData()
	{
		super(WorkingSnapshotTranslatedData.class);
	}
	
	@Transient
	public Integer getId()
	{
		return this.snapshotTranslatedDataId;
	}

	@Override
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TranslatedXMLRenderedUpdated", nullable = true, length = 0)
	public Date getTranslatedXmlRenderedUpdated()
	{
		return this.translatedXmlRenderedUpdated;
	}

	@Override
	public void setTranslatedXmlRenderedUpdated(final Date translatedXmlRenderedUpdated)
	{
		this.translatedXmlRenderedUpdated = translatedXmlRenderedUpdated;
	}

	@Override
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "WorkingSnapshotTranslatedDataID", unique = true, nullable = false)
	public Integer getSnapshotTranslatedDataId()
	{
		return snapshotTranslatedDataId;
	}

	@Override
	public void setSnapshotTranslatedDataId(final Integer snapshotTranslatedDataId)
	{
		this.snapshotTranslatedDataId = snapshotTranslatedDataId;
	}

	@Override
	@ManyToOne
	@JoinColumn(name = "SnapshotTopicID", nullable = false)
	@NotNull
	public SnapshotTopic getSnapshotTopic()
	{
		return snapshotTopic;
	}

	@Override
	public void setSnapshotTopic(final SnapshotTopic snapshotTopic)
	{
		this.snapshotTopic = snapshotTopic;
	}

	// @Column(name = "TranslatedXML", length = 65535)
	@Override
	@Column(name = "TranslatedXML", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTranslatedXml()
	{
		return translatedXml;
	}

	@Override
	public void setTranslatedXml(final String translatedXml)
	{
		this.translatedXml = translatedXml;
	}

	// @Column(name = "TranslatedXMLRendered", length = 65535)
	@Override
	@Column(name = "TranslatedXMLRendered", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTranslatedXmlRendered()
	{
		return translatedXmlRendered;
	}

	@Override
	public void setTranslatedXmlRendered(final String translatedXmlRendered)
	{
		this.translatedXmlRendered = translatedXmlRendered;
	}

	@Override
	@Column(name = "TranslationLocale", nullable = false, length = 45)
	@NotNull
	@Length(max = 45)
	public String getTranslationLocale()
	{
		return translationLocale;
	}

	@Override
	public void setTranslationLocale(final String translationLocale)
	{
		this.translationLocale = translationLocale;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "workingSnapshotTranslatedData", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<WorkingSnapshotTranslatedString> getWorkingSnapshotTranslatedStrings()
	{
		return workingSnapshotTranslatedStrings;
	}

	public void setWorkingSnapshotTranslatedStrings(final Set<WorkingSnapshotTranslatedString> workingSnapshotTranslatedStrings)
	{
		this.workingSnapshotTranslatedStrings = workingSnapshotTranslatedStrings;
	}

	@Transient
	public String getTopicTitle()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);
		final AuditQuery query = reader.createQuery().forEntitiesAtRevision(Topic.class, this.snapshotTopic.getTopicRevision()).add(AuditEntity.id().eq(this.snapshotTopic.getTopicId()));
		final Topic topic = (Topic) query.getSingleResult();
		if (topic != null)
			return topic.getTopicTitle();
		return null;
	}

	@Override
	@Transient
	public Topic getEnversTopic()
	{
		return enversTopic;
	}

	@Override
	public void setEnversTopic(final Topic enversTopic)
	{
		this.enversTopic = enversTopic;
	}

	@Override
	@Transient
	public String getTopicTags()
	{
		try
		{
			System.out.println("WorkingSnapshotTranslatedData.getTopicTags() START");

			if (getEnversTopic() != null)
			{
				return getEnversTopic().getTagsList(true);
			}

			return null;
		}
		finally
		{
			System.out.println("WorkingSnapshotTranslatedData.getTopicTags() FINISH");
		}
	}

	@Override
	@Transient
	public String getFormattedTranslatedXmlRenderedUpdated()
	{
		if (this.translatedXmlRenderedUpdated != null)
		{
			final SimpleDateFormat formatter = new SimpleDateFormat(CommonConstants.FILTER_DISPLAY_DATE_FORMAT);
			return formatter.format(this.translatedXmlRenderedUpdated);
		}

		return new String();
	}

	@SuppressWarnings("unused")
	@PostUpdate
	@PostPersist
	private void render()
	{
		try
		{
			/* create the threads to rerender the SnapshotTranslatedData objects */
			final InitialContext initCtx = new InitialContext();
			final TransactionManager transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			final Transaction transaction = transactionManager.getTransaction();

			WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(snapshotTranslatedDataId, TopicRendererType.WORKINGSNAPSHOTTOPIC, transaction));
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue getting the transaction manager");
		}
	}
	
	@Transient
	public List<WorkingSnapshotTranslatedString> getWorkingSnapshotTranslatedStringsArray()
	{
		return CollectionUtilities.toArrayList(this.workingSnapshotTranslatedStrings);
	}
}
