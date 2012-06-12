package com.redhat.topicindex.session;

import java.util.List;

import javax.persistence.EntityManager;

import net.ser1.stomp.Client;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.messaging.DocbookRendererMessage;
import com.redhat.topicindex.messaging.TopicRendererType;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TranslatedTopicData;

@Name("globalDatabaseUtilities")
public class GlobalDatabaseUtilities
{
	public void indexEntireDatabase()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Session session = (Session) entityManager.getDelegate();
			final FullTextSession fullTextSession = Search.getFullTextSession(session);
			fullTextSession.createIndexer(Topic.class).startAndWait();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error reindexing the Lucene database");
		}
	}

	public void renderAllTopics()
	{
		Client client = null;
		try
		{
			final String messageServer = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_SYSTEM_PROPERTY);
			final String messageServerPort = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PORT_SYSTEM_PROPERTY);
			final String messageServerUser = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_USER_SYSTEM_PROPERTY);
			final String messageServerPass = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PASS_SYSTEM_PROPERTY);
			final String messageServerQueue = System.getProperty(Constants.STOMP_MESSAGE_SERVER_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY);

			if (messageServer != null && messageServerPort != null && messageServerUser != null && messageServerPass != null && messageServerQueue != null)
			{
				client = new Client(messageServer, Integer.parseInt(messageServerPort), messageServerUser, messageServerPass);

				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				@SuppressWarnings("unchecked")
				final List<Topic> topics = entityManager.createQuery(Topic.SELECT_ALL_QUERY).getResultList();
				
				final ObjectMapper mapper = new ObjectMapper();

				for (final Topic topic : topics)
				{
					client.send(messageServerQueue, mapper.writeValueAsString(new DocbookRendererMessage(topic.getTopicId(), TopicRendererType.TOPIC)));
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error while rerendering all the topics in the database");
		}
		finally
		{
			if (client != null)
				client.disconnect();
			client = null;
		}
	}

	public void renderAllTranslatedTopicDataEntities()
	{
		Client client = null;
		try
		{

			final String messageServer = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_SYSTEM_PROPERTY);
			final String messageServerPort = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PORT_SYSTEM_PROPERTY);
			final String messageServerUser = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_USER_SYSTEM_PROPERTY);
			final String messageServerPass = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PASS_SYSTEM_PROPERTY);
			final String messageServerQueue = System.getProperty(Constants.STOMP_MESSAGE_SERVER_TRANSLATED_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY);

			if (messageServer != null && messageServerPort != null && messageServerUser != null && messageServerPass != null && messageServerQueue != null)
			{
				client = new Client(messageServer, Integer.parseInt(messageServerPort), messageServerUser, messageServerPass);
				
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				@SuppressWarnings("unchecked")
				final List<TranslatedTopicData> entities = entityManager.createQuery(TranslatedTopicData.SELECT_ALL_QUERY).getResultList();

				final ObjectMapper mapper = new ObjectMapper();
				
				for (final TranslatedTopicData entity : entities)
				{
					client.send(messageServerQueue, mapper.writeValueAsString(new DocbookRendererMessage(entity.getId(), TopicRendererType.TRANSLATEDTOPIC)));
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error while rerendering all the topics in the database");
		}
		finally
		{
			if (client != null) {
				client.disconnect();
			}
			client = null;
		}
	}
}
