package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.messaging.TopicRendererType;

@Entity
@Audited
@Table(name = "TranslatedTopicData", uniqueConstraints = @UniqueConstraint(columnNames = { "TranslatedTopicID", "TranslationLocale" }))
public class TranslatedTopicData extends AuditedEntity<TranslatedTopicData> implements java.io.Serializable
{
	private static final long serialVersionUID = 7470594104954257672L;

	public static final String SELECT_ALL_QUERY = "select translatedTopicData from TranslatedTopicData translatedTopicData";

	private Integer translatedTopicDataId;
	private TranslatedTopic translatedTopic;
	private String translatedXml;
	private String translatedXmlErrors;
	private String translatedXmlRendered;
	private String translationLocale;
	private Set<TranslatedTopicString> translatedTopicStrings = new HashSet<TranslatedTopicString>(0);
	private Date translatedXmlRenderedUpdated;
	private Integer translationPercentage = 0;
	
	public TranslatedTopicData()
	{
		super(TranslatedTopicData.class);
	}
	
	@Transient
	public Integer getId()
	{
		return this.translatedTopicDataId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TranslatedXMLRenderedUpdated", nullable = true, length = 0)
	public Date getTranslatedXmlRenderedUpdated()
	{
		return this.translatedXmlRenderedUpdated;
	}

	public void setTranslatedXmlRenderedUpdated(final Date translatedXmlRenderedUpdated)
	{
		this.translatedXmlRenderedUpdated = translatedXmlRenderedUpdated;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TranslatedTopicDataID", unique = true, nullable = false)
	public Integer getTranslatedTopicDataId()
	{
		return translatedTopicDataId;
	}

	public void setTranslatedTopicDataId(final Integer translatedTopicDataId)
	{
		this.translatedTopicDataId = translatedTopicDataId;
	}

	@ManyToOne
	@JoinColumn(name = "TranslatedTopicID", nullable = false)
	@NotNull
	public TranslatedTopic getTranslatedTopic()
	{
		return translatedTopic;
	}

	public void setTranslatedTopic(final TranslatedTopic translatedTopic)
	{
		this.translatedTopic = translatedTopic;
	}

	// @Column(name = "TranslatedXML", length = 65535)
	@Column(name = "TranslatedXML", columnDefinition = "MEDIUMTEXT")
	@Length(max = 16777215)
	public String getTranslatedXml()
	{
		return translatedXml;
	}

	public void setTranslatedXml(final String translatedXml)
	{
		this.translatedXml = translatedXml;
	}
	
	@Column(name = "TranslatedXMLErrors", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTranslatedXmlErrors()
	{
		return translatedXmlErrors;
	}

	public void setTranslatedXmlErrors(final String translatedXmlErrors)
	{
		this.translatedXmlErrors = translatedXmlErrors;
	}

	// @Column(name = "TranslatedXMLRendered", length = 65535)
	@Column(name = "TranslatedXMLRendered", columnDefinition = "MEDIUMTEXT")
	@Length(max = 16777215)
	public String getTranslatedXmlRendered()
	{
		return translatedXmlRendered;
	}

	public void setTranslatedXmlRendered(final String translatedXmlRendered)
	{
		this.translatedXmlRendered = translatedXmlRendered;
	}

	@Column(name = "TranslationLocale", nullable = false, length = 45)
	@NotNull
	@Length(max = 45)
	public String getTranslationLocale()
	{
		return translationLocale;
	}

	public void setTranslationLocale(final String translationLocale)
	{
		this.translationLocale = translationLocale;
	}
	
	@Column(name = "TranslationPercentage", nullable = false)
	@NotNull
	public Integer getTranslationPercentage() {
		return translationPercentage;
	}

	public void setTranslationPercentage(Integer translationPercentage) {
		this.translationPercentage = translationPercentage;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "translatedTopicData", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TranslatedTopicString> getTranslatedTopicStrings()
	{
		return translatedTopicStrings;
	}

	public void setTranslatedTopicStrings(final Set<TranslatedTopicString> translatedTopicStrings)
	{
		this.translatedTopicStrings = translatedTopicStrings;
	}

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
	@PostPersist
	private void render()
	{
		try
		{
			/* create the threads to rerender the TranslatedTopicData objects */
			final InitialContext initCtx = new InitialContext();
			final TransactionManager transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			final Transaction transaction = transactionManager.getTransaction();

			WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(translatedTopicDataId, TopicRendererType.TRANSLATEDTOPIC, transaction));
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue getting the transaction manager");
		}
	}
	
	@Transient
	public List<TranslatedTopicString> getTranslatedTopicDataStringsArray()
	{
		return CollectionUtilities.toArrayList(this.translatedTopicStrings);
	}
	
	@Transient
	public List<TranslatedTopicData> getOutgoingRelatedTranslatedTopicData(final EntityManager entityManager)
	{
		final Topic enversTopic = translatedTopic.getEnversTopic(entityManager);
		
		return getRelatedTranslatedTopicData(entityManager, enversTopic.getOutgoingRelatedTopicsArray());
	}
	
	@Transient
	public List<TranslatedTopicData> getIncomingRelatedTranslatedTopicData(final EntityManager entityManager)
	{
		final Topic enversTopic = translatedTopic.getEnversTopic(entityManager);
		
		return getRelatedTranslatedTopicData(entityManager, enversTopic.getIncomingRelatedTopicsArray());
	}
	
	@Transient
	private List<TranslatedTopicData> getRelatedTranslatedTopicData(final EntityManager entityManager, final List<Topic> relatedTopics)
	{
		final List<TranslatedTopicData> relatedTranslatedTopicDatas = new ArrayList<TranslatedTopicData>();
		for (Topic relatedTopic: relatedTopics)
		{
			/* Get the related TranslatedTopicData for the related TranslatedTopic */
			for (TranslatedTopicData relatedTranslation: relatedTopic.getTranslatedTopics(entityManager))
			{
				if (relatedTranslation.getTranslationLocale().equals(this.translationLocale)) {
					relatedTranslatedTopicDatas.add(relatedTranslation);
				}
			}
		}
		
		return relatedTranslatedTopicDatas;
	}
	
	@Transient
	public TranslatedTopicData getLatestRelatedTranslationDataByTopicID(final EntityManager entityManager, final int topicId)
	{
		final List<TranslatedTopicData> relatedOutgoingTranslatedTopicDatas = getOutgoingRelatedTranslatedTopicData(entityManager);
		TranslatedTopicData relatedTranslatedTopicData = null;
		if (relatedOutgoingTranslatedTopicDatas != null)
		{
			/* Loop through the related TranslatedTopicData to find the latest complete translation */
			for (final TranslatedTopicData translatedTopicData : relatedOutgoingTranslatedTopicDatas)
			{
				if (translatedTopicData.getTranslatedTopic().getTopicId().equals(topicId)
						/* Check that the translation is complete */
						&& translatedTopicData.getTranslationPercentage() >= 100
						/* Check to see if this TranslatedTopic revision is higher then the current revision */
						&& (relatedTranslatedTopicData == null || relatedTranslatedTopicData.getTranslatedTopic().getTopicRevision() < translatedTopicData.getTranslatedTopic().getTopicRevision()))
					relatedTranslatedTopicData = translatedTopicData;
			}
		}
		return relatedTranslatedTopicData;
	}
	
	@Transient
	public List<TranslatedTopicData> getOutgoingDummyFilledRelatedTranslatedTopicDatas(final EntityManager entityManager)
	{	
		final List<TranslatedTopicData> outgoingRelatedTranslatedTopicDatas = this.getOutgoingRelatedTranslatedTopicData(entityManager);
		
		return getDummyFilledRelatedTranslatedTopicData(outgoingRelatedTranslatedTopicDatas);
	}
	
	@Transient
	public List<TranslatedTopicData> getIncomingDummyFilledRelatedTranslatedTopicDatas(final EntityManager entityManager)
	{	
		final List<TranslatedTopicData> incomingRelatedTranslatedTopicDatas = this.getIncomingRelatedTranslatedTopicData(entityManager);
		
		return getDummyFilledRelatedTranslatedTopicData(incomingRelatedTranslatedTopicDatas);
	}
	
	@Transient
	private List<TranslatedTopicData> getDummyFilledRelatedTranslatedTopicData(final List<TranslatedTopicData> currentRelatedTranslatedTopicData)
	{
		final List<TranslatedTopicData> relationships = new ArrayList<TranslatedTopicData>();
		
		/* Get the latest complete versions of the translated topics */
		if (currentRelatedTranslatedTopicData != null)
		{
			Map<Integer, TranslatedTopicData> translatedTopics = new HashMap<Integer, TranslatedTopicData>();
			
			for (final TranslatedTopicData translatedTopicData : currentRelatedTranslatedTopicData)	
			{
				final Integer topicId = translatedTopicData.getTranslatedTopic().getTopicId();
				
				if (/* Check that the translation is complete */
						translatedTopicData.getTranslationPercentage() >= 100 
						/* Check that a related topic hasn't been set or the topics 
						 * revision is higher then the current topic revision */
						&& (!translatedTopics.containsKey(topicId) 
								|| translatedTopicData.getTranslatedTopic().getTopicRevision() > translatedTopics.get(topicId).getTranslatedTopic().getTopicRevision()))
					translatedTopics.put(topicId, translatedTopicData);
			}
			
			/* Loop through and create dummy relationships for topics that haven't been translated yet */
			for (final Topic topic: this.getTranslatedTopic().getEnversTopic().getOutgoingRelatedTopicsArray())
			{
				if (!translatedTopics.containsKey(topic.getId()))
				{
					final TranslatedTopicData dummyTranslation = createDummyTranslatedTopicData(topic);
					translatedTopics.put(topic.getId(), dummyTranslation);
				}
			}
			
			for (final Integer topicId: translatedTopics.keySet())
			{
				relationships.add(translatedTopics.get(topicId));
			}
		}
		return relationships;
	}
	
	@Transient
	public TranslatedTopicData createDummyTranslatedTopicData(final Topic topic)
	{
		final TranslatedTopicData translatedTopicData = new TranslatedTopicData();
		final TranslatedTopic translatedTopic = new TranslatedTopic();
		
		translatedTopic.setTopicId(topic.getId());
		
		/* find the revision for the related topic */
		Number topicRevision = 0;
		for (final Number revision: topic.getRevisions())
		{
			if (revision.longValue() <= this.translatedTopic.getTopicRevision().longValue() 
					&& revision.longValue() > topicRevision.longValue())
				topicRevision = revision;
		}
		
		translatedTopic.setTopicRevision(topicRevision.intValue());
		translatedTopicData.setTranslatedTopic(translatedTopic);
		
		translatedTopicData.setTranslatedTopicDataId(topic.getId() * -1);
		translatedTopicData.setTranslatedXml(topic.getTopicXML());
		translatedTopicData.setTranslationPercentage(100);
		translatedTopicData.setTranslatedXmlRendered(topic.getTopicRendered());
		translatedTopicData.setTranslationLocale(topic.getTopicLocale());
		
		return translatedTopicData;
	}
}
