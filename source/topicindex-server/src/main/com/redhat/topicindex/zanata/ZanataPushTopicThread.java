package com.redhat.topicindex.zanata;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;

import org.apache.commons.codec.binary.Hex;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ResourceType;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonstructures.StringToNodeCollection;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

public class ZanataPushTopicThread implements Runnable
{
	private List<Pair<Integer, Integer>> topics;
	private boolean overwrite = false;

	public ZanataPushTopicThread(final List<Pair<Integer, Integer>> topics, final boolean overwrite)
	{
		/* get a read only copy of the list */
		this.topics = Collections.unmodifiableList(topics);

		this.overwrite = overwrite;
	}

	@Override
	public void run()
	{
		/*
		 * Only one thread should be created to use the list of topics, but we
		 * synchronize anyway just in case.
		 */
		synchronized (topics)
		{
			TransactionManager transactionManager = null;
			EntityManager entityManager = null;

			try
			{
				final InitialContext initCtx = new InitialContext();
				final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
				transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
				transactionManager.begin();
				entityManager = entityManagerFactory.createEntityManager();
				final AuditReader reader = AuditReaderFactory.get(entityManager);

				final int total = topics.size();
				int current = 0;

				for (final Pair<Integer, Integer> topicDetails : topics)
				{
					++current;
					final int progress = (int) ((float) current / (float) total * 100.0f);
					System.out.println("Push To Zanata Progress - Topic ID " + topicDetails.getFirst() + ": " + current + " of " + total + " (" + progress + "%)");

					final Topic topic = reader.find(Topic.class, topicDetails.getFirst(), topicDetails.getSecond());
					topic.setTempRevisionNumber(topicDetails.getSecond());

					if (topic != null)
					{

						final String zanataId = topic.getZanataId();

						/*
						 * deleting existing resources is useful for debugging,
						 * but not for production
						 */
						final boolean zanataFileExists = ZanataInterface.getZanataResourceExists(zanataId);

						if (zanataFileExists)
						{
							if (overwrite)
							{
								System.out.println("Topic ID " + topic.getTopicId() + " revision " + topic.getTempRevisionNumber() + " already exists - Deleting.");
								ZanataInterface.deleteResource(zanataId);
							}
							else
							{
								System.out.println("Topic ID " + topic.getTopicId() + " revision " + topic.getTempRevisionNumber() + " already exists - Skipping.");
								continue;
							}
						}

						try
						{
							/*
							 * make sure the section title is the same as the
							 * topic title
							 */
							topic.syncXML();
							topic.initializeTempTopicXMLDoc();

							/* the historical object may have invalid xml */
							if (topic.getTempTopicXMLDoc() != null)
							{

								final Resource resource = new Resource();

								resource.setContentType(ContentType.TextPlain);
								resource.setLang(LocaleId.fromJavaName(topic.getTopicLocale()));
								resource.setName(zanataId);
								resource.setRevision(1);
								resource.setType(ResourceType.FILE);

								final List<StringToNodeCollection> translatableStrings = XMLUtilities.getTranslatableStrings(topic.getTempTopicXMLDoc(), false);

								for (final StringToNodeCollection translatableStringData : translatableStrings)
								{
									final String translatableString = translatableStringData.getTranslationString();
									if (!translatableString.trim().isEmpty())
									{										
										final TextFlow textFlow = new TextFlow();
										textFlow.setContent(translatableString);
										textFlow.setLang(LocaleId.fromJavaName(topic.getTopicLocale()));
										textFlow.setId(createId(topic, translatableString));
										textFlow.setRevision(1);

										resource.getTextFlows().add(textFlow);
									}
								}

								ZanataInterface.createFile(resource);

							}
							else
							{
								System.out.println("Topic ID " + topic.getTopicId() + " revision " + topic.getTempRevisionNumber() + " does not have valid XML - Skipping.");
							}
						}
						catch (final Exception ex)
						{
							SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieveing the Topic entity");
						}
					}
				}

				transactionManager.commit();
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieveing the Topic entity");
				try
				{
					transactionManager.rollback();
				}
				catch (final Exception ex2)
				{
					SkynetExceptionUtilities.handleException(ex2, false, "There was an issue rolling back the transaction");
				}

				throw new InternalServerErrorException("There was an error running the query");
			}
			finally
			{
				if (entityManager != null)
					entityManager.close();
			}
		}
	}

	private static String createId(final Topic topic, final String text)
	{
		final String sep = "\u0000";
		final String hashBase = text + sep + topic.getTopicId() + sep + topic.getTempRevisionNumber();
		return generateHash(hashBase);
	}

	private static String generateHash(final String key)
	{
		try
		{
			final MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			return new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
		}
		catch (Exception exc)
		{
			throw new RuntimeException(exc);
		}
	}
	
}
