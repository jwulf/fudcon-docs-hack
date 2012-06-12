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

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.messaging.TopicRendererType;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;

@Entity
@Audited
@Table(name = "SnapshotTranslatedData", uniqueConstraints = @UniqueConstraint(columnNames = { "SnapshotTopicID", "TranslationLocale" }))
public class SnapshotTranslatedData extends AuditedEntity<SnapshotTranslatedData> implements java.io.Serializable, SnapshotTranslatedDataBase
{
	private static final long serialVersionUID = 7470594104954257672L;

	public static final String SELECT_ALL_QUERY = "select snapshotTranslatedData from SnapshotTranslatedData snapshotTranslatedData";

	private Integer snapshotTranslatedDataId;
	private SnapshotTopic snapshotTopic;
	private SnapshotRevision snapshotRevision;
	private String translatedXml;
	private String translatedXmlRendered;
	private String translationLocale;
	private Set<SnapshotTranslatedString> snapshotTranslatedStrings = new HashSet<SnapshotTranslatedString>(0);
	private Date translatedXmlRenderedUpdated;
	/** Transient reference to an historical version of the topic */
	private Topic enversTopic;
	
	public SnapshotTranslatedData()
	{
		super(SnapshotTranslatedData.class);
	}
	
	@Transient
	public Integer getId()
	{
		return this.snapshotTranslatedDataId;
	}

	@Override
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "SnapshotTranslatedDataID", unique = true, nullable = false)
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
	public void setSnapshotTopic(SnapshotTopic snapshotTopic)
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
	public void setTranslatedXml(String translatedXml)
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
	public void setTranslatedXmlRendered(String translatedXmlRendered)
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
	public void setTranslationLocale(String translationLocale)
	{
		this.translationLocale = translationLocale;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "snapshotTranslatedData", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<SnapshotTranslatedString> getSnapshotTranslatedStrings()
	{
		return snapshotTranslatedStrings;
	}

	public void setSnapshotTranslatedStrings(final Set<SnapshotTranslatedString> snapshotTranslatedStrings)
	{
		this.snapshotTranslatedStrings = snapshotTranslatedStrings;
	}

	@ManyToOne
	@JoinColumn(name = "SnapshotRevisionID", nullable = false)
	@NotNull
	public SnapshotRevision getSnapshotRevision()
	{
		return snapshotRevision;
	}

	public void setSnapshotRevision(final SnapshotRevision snapshotRevision)
	{
		this.snapshotRevision = snapshotRevision;
	}

	@Transient
	public String getTopicTitle()
	{
		if (getEnversTopic() != null)
			return getEnversTopic().getTopicTitle();
		return null;
	}

	@Override
	@Transient
	public String getTopicTags()
	{
		try
		{
			System.out.println("SnapshotTranslatedData.getTopicTags() START");

			if (getEnversTopic() != null)
			{
				return getEnversTopic().getTagsList(true);
			}

			return null;
		}
		finally
		{
			System.out.println("SnapshotTranslatedData.getTopicTags() FINISH");
		}
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

			WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(snapshotTranslatedDataId, TopicRendererType.SNAPSHOTTOPIC, transaction));
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue getting the transaction manager");
		}
	}
	
	@Transient
	public List<SnapshotTranslatedString> getSnapshotTranslatedStringsArray()
	{
		return CollectionUtilities.toArrayList(snapshotTranslatedStrings);
	}

}
