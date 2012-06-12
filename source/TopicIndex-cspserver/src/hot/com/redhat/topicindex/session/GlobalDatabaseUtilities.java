package com.redhat.topicindex.session;

import java.util.List;

import javax.persistence.EntityManager;

import net.ser1.stomp.Client;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;

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

				for (final Topic topic : topics)
				{
					client.send(messageServerQueue, topic.getTopicId().toString());
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

	public void renderAllWorkingSnapshotTranslatedDataEntities()
	{
		try
		{

			final String messageServer = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_SYSTEM_PROPERTY);
			final String messageServerPort = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PORT_SYSTEM_PROPERTY);
			final String messageServerUser = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_USER_SYSTEM_PROPERTY);
			final String messageServerPass = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PASS_SYSTEM_PROPERTY);
			final String messageServerQueue = System.getProperty(Constants.STOMP_MESSAGE_SERVER_WORKING_SNAPHOT_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY);

			if (messageServer != null && messageServerPort != null && messageServerUser != null && messageServerPass != null && messageServerQueue != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				@SuppressWarnings("unchecked")
				final List<WorkingSnapshotTranslatedData> entities = entityManager.createQuery(WorkingSnapshotTranslatedData.SELECT_ALL_QUERY).getResultList();

				for (final WorkingSnapshotTranslatedData entity : entities)
				{

				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error while rerendering all the topics in the database");
		}
	}

	public void renderAllSnapshotTranslatedDataEntities()
	{
		try
		{
			final String messageServer = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_SYSTEM_PROPERTY);
			final String messageServerPort = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PORT_SYSTEM_PROPERTY);
			final String messageServerUser = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_USER_SYSTEM_PROPERTY);
			final String messageServerPass = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PASS_SYSTEM_PROPERTY);
			final String messageServerQueue = System.getProperty(Constants.STOMP_MESSAGE_SERVER_WORKING_SNAPHOT_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY);

			if (messageServer != null && messageServerPort != null && messageServerUser != null && messageServerPass != null && messageServerQueue != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				@SuppressWarnings("unchecked")
				final List<SnapshotTranslatedData> entities = entityManager.createQuery(SnapshotTranslatedData.SELECT_ALL_QUERY).getResultList();

				for (final SnapshotTranslatedData entity : entities)
				{
					//WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(entity.getSnapshotTranslatedDataId(), TopicRendererType.SNAPSHOTTOPIC));
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error while rerendering all the topics in the database");
		}
	}
}
