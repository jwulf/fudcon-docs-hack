package com.redhat.topicindex.zanata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.w3c.dom.Document;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedString;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.messaging.TopicRendererType;

public class ZanataPullTopicThread implements Runnable
{
	private List<Integer> snapshotTopics;

	public ZanataPullTopicThread(final List<Integer> snapshotTopics)
	{
		this.snapshotTopics = Collections.unmodifiableList(snapshotTopics);
	}

	@Override
	public void run()
	{
		synchronized (snapshotTopics)
		{

			final int total = snapshotTopics.size();
			int current = 0;

			TransactionManager transactionManager = null;

			try
			{
				final InitialContext initCtx = new InitialContext();
				final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
				transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
				
				final List<Integer> processedIds = new ArrayList<Integer>();

				/* for each snapshot topic... */
				for (final Integer topicId : this.snapshotTopics)
				{
					++current;
					final int progress = (int) ((float) current / (float) total * 100.0f);
					System.out.println("Pull from Zanata Progress - Topic ID " + topicId + ": " + current + " of " + total + " (" + progress + "%)");

					/* ... and each locale */
					for (final String locale : Constants.LOCALES)
					{
						/* attempt to pull down the latest translations */
						final Integer workingSnapshotTranslatedDataId = processTopic(transactionManager, entityManagerFactory, topicId, locale);
						if (workingSnapshotTranslatedDataId != null)
						{
							processedIds.add(workingSnapshotTranslatedDataId);							
						}
					}
				}
				
				for (final Integer id : processedIds)
				{
					WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(id, TopicRendererType.SNAPSHOTTOPIC));
				}
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error looking up the EntityManagerFactory or the TransactionManager");
			}

		}
	}

	private Integer processTopic(final TransactionManager transactionManager, final EntityManagerFactory entityManagerFactory, final Integer snapshotTopicId,
			final String locale)
	{
		EntityManager entityManager = null;

		try
		{
			Integer retValue = null;

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			final AuditReader reader = AuditReaderFactory.get(entityManager);

			/* find the original SnapshotTopic */
			final SnapshotTopic topic = entityManager.find(SnapshotTopic.class, snapshotTopicId);
			
			if (topic != null)
			{
				/* ... find the matching historical envers topic */
				final Topic historicalTopic = reader.find(Topic.class, topic.getTopicId(), topic.getTopicRevision());

				/* find a translation */
				final TranslationsResource translationsResource = ZanataInterface.getTranslations(topic.getZanataId(), LocaleId.fromJavaName(locale));
				/* and find the original resource */
				final Resource originalTextResource = ZanataInterface.getZanataResource(topic.getZanataId());

				if (translationsResource != null && originalTextResource != null)
				{
					final Set<WorkingSnapshotTranslatedData> workingSnapshotTranslatedDataEntities = topic.getWorkingSnapshotTranslatedDataEntities();

					/* attempt to find an existing SnapshotTranslatedData entity */
					WorkingSnapshotTranslatedData workingSnapshotTranslatedData = null;

					for (final WorkingSnapshotTranslatedData mySnapshotTranslatedData : workingSnapshotTranslatedDataEntities)
					{
						if (mySnapshotTranslatedData.getTranslationLocale().equals(locale))
						{
							workingSnapshotTranslatedData = mySnapshotTranslatedData;
							break;
						}
					}

					/*
					 * if an existing SnapshotTranslatedData entity does not
					 * exist, create one
					 */
					if (workingSnapshotTranslatedData == null)
					{
						workingSnapshotTranslatedData = new WorkingSnapshotTranslatedData();
						workingSnapshotTranslatedData.setSnapshotTopic(topic);
						workingSnapshotTranslatedData.setTranslationLocale(locale);
						workingSnapshotTranslatedDataEntities.add(workingSnapshotTranslatedData);
						
						/* Make sure the snapshotTranslatedData has an ID */
						//entityManager.persist(workingSnapshotTranslatedData);
					}

					/*
					 * a mapping of the original strings to their translations
					 */
					final Map<String, String> translations = new HashMap<String, String>();

					final List<TextFlowTarget> textFlowTargets = translationsResource.getTextFlowTargets();
					final List<TextFlow> textFlows = originalTextResource.getTextFlows();

					/* map the translation to the original resource */
					for (final TextFlowTarget textFlowTarget : textFlowTargets)
					{
						for (final TextFlow textFlow : textFlows)
						{
							if (textFlowTarget.getResId().equals(textFlow.getId()))
							{
								translations.put(textFlow.getContent(), textFlowTarget.getContent());
								break;
							}
						}
					}
					
					/* save the strings to WorkingSnapshotTranslatedString entities */					
					for (final String originalText : translations.keySet())
					{
						final String translation = translations.get(originalText);
						
						final WorkingSnapshotTranslatedString workingSnapshotTranslatedString = new WorkingSnapshotTranslatedString();
						workingSnapshotTranslatedString.setWorkingSnapshotTranslatedData(workingSnapshotTranslatedData);
						workingSnapshotTranslatedString.setWorkingShapshotOriginalString(originalText);
						workingSnapshotTranslatedString.setWorkingSnapshotTranslatedString(translation);
						
						//entityManager.persist(workingSnapshotTranslatedString);
						
						workingSnapshotTranslatedData.getWorkingSnapshotTranslatedStrings().add(workingSnapshotTranslatedString);
					}

					/* get a Document from the stored historical XML */
					historicalTopic.syncXML();
					historicalTopic.initializeTempTopicXMLDoc();
					final Document xml = historicalTopic.getTempTopicXMLDoc();

					/*
					 * replace the translated strings, and save the result into
					 * the SnapshotTranslatedData entity
					 */
					if (xml != null)
					{
						XMLUtilities.replaceTranslatedStrings(xml, translations);
						workingSnapshotTranslatedData.setTranslatedXml(XMLUtilities.convertDocumentToString(xml, Constants.XML_ENCODING));
					}

					/* persist the changes */
					entityManager.persist(workingSnapshotTranslatedData);

					/*
					 * make a note of the SnapshotTranslatedData entities that
					 * have been changed, so we can render them
					 */
					retValue = workingSnapshotTranslatedData.getSnapshotTranslatedDataId();
				}
			}

			transactionManager.commit();

			return retValue;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error looking up the manager objects");

			try
			{
				transactionManager.rollback();
			}
			catch (final Exception ex2)
			{
				SkynetExceptionUtilities.handleException(ex2, false, "There was an exception rolling back the transaction");
			}
		}
		finally
		{
			if (entityManager != null)
				entityManager.close();
		}

		return null;
	}

}
