package com.redhat.topicindex.rest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.TransactionManager;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.InternalServerErrorException;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.filter.FilterQueryBuilder;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.exceptions.InternalProcessingException;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class provides the functions that retrieve, update, create and delete
 * entities. It is expected that other classes will extend BaseRESTv1 to provide
 * expose REST functions.
 */
public class BaseRESTv1
{
	protected static final String REST_DATE_FORMAT = "dd-MMM-yyyy";

	public static final String TOPICS_EXPANSION_NAME = "topics";
	public static final String IMAGES_EXPANSION_NAME = "images";
	public static final String TAGS_EXPANSION_NAME = "tags";
	public static final String PROJECTS_EXPANSION_NAME = "projects";
	public static final String USERS_EXPANSION_NAME = "users";
	public static final String BLOBCONSTANTS_EXPANSION_NAME = "blobconstants";
	public static final String STRINGCONSTANTS_EXPANSION_NAME = "stringconstants";
	public static final String PROPERTYTAGS_EXPANSION_NAME = "propertytags";
	public static final String ROLES_EXPANSION_NAME = "propertytags";
	public static final String TRANSLATEDTOPICS_EXPANSION_NAME = "translatedtopics";
	public static final String TRANSLATEDTOPICSTRINGS_EXPANSION_NAME = "translatedtopicstrings";

	public static final String CATEGORIES_EXPANSION_NAME = "categories";
	public static final String TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME = "incomingRelationships";
	public static final String TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME = "outgoingRelationships";
	public static final String PROPERTIES_EXPANSION_NAME = "properties";
	public static final String SOURCE_URLS_EXPANSION_NAME = "sourceUrls";

	public static final String TOPIC_URL_NAME = "topic";
	public static final String TOPICSOURCEURL_URL_NAME = "topicsourceurl";
	public static final String BUGZILLABUG_URL_NAME = "bugzillabug";
	public static final String TRANSLATEDTOPIC_URL_NAME = "translatedtopic";
	public static final String TRANSLATEDTOPICDATA_URL_NAME = "translatedtopicdata";
	public static final String TRANSLATEDTOPICSTRING_URL_NAME = "translatedtopicstring";
	public static final String PROJECT_URL_NAME = "project";
	public static final String TAG_URL_NAME = "tag";
	public static final String CATEGORY_URL_NAME = "category";
	public static final String USER_URL_NAME = "user";
	public static final String BLOBCONSTANT_URL_NAME = "blobconstant";
	public static final String STRINGCONSTANT_URL_NAME = "stringconstant";

	public static final String IMAGE_URL_NAME = "image";
	public static final String PROPERTYTAG_URL_NAME = "propertytag";
	public static final String ROLE_URL_NAME = "role";

	public static final String JSON_URL = "json";
	public static final String XML_URL = "json";
	
	private final ObjectMapper mapper = new ObjectMapper();

	private @Context
	UriInfo uriInfo;

	protected String getBaseUrl()
	{
		final String fullPath = uriInfo.getAbsolutePath().toString();
		final int index = fullPath.indexOf(Constants.BASE_REST_PATH);
		if (index != -1)
			return fullPath.substring(0, index + Constants.BASE_REST_PATH.length());

		return null;
	}

	protected String getUrl()
	{
		return uriInfo.getAbsolutePath().toString();
	}

	protected Feed convertTopicsIntoFeed(final BaseRestCollectionV1<TopicV1> topics, final String title)
	{
		try
		{
			final Feed feed = new Feed();

			feed.setId(new URI(this.getUrl()));
			feed.setTitle(title);
			feed.setUpdated(new Date());

			if (topics.getItems() != null)
			{
				for (final TopicV1 topic : topics.getItems())
				{
					final String html = topic.getHtml();

					final Entry entry = new Entry();
					entry.setTitle(topic.getTitle());
					entry.setUpdated(topic.getLastModified());
					entry.setPublished(topic.getCreated());

					if (html != null)
					{
						final Content content = new Content();
						content.setType(MediaType.TEXT_HTML_TYPE);
						content.setText(fixHrefs(topic.getHtml()));
						entry.setContent(content);
					}

					feed.getEntries().add(entry);
				}
			}

			return feed;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "There was an error creating the ATOM feed");
			throw new InternalServerErrorException("There was an error creating the ATOM feed");
		}
	}

	private String fixHrefs(final String input)
	{
		return input.replaceAll("Topic\\.seam", CommonConstants.FULL_SERVER_URL + "/Topic.seam");
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> getJSONEntitiesUpdatedSince(final Class<U> type, final String idProperty, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand, final Date date)
	{
		return getEntitiesUpdatedSince(type, idProperty, dataObjectFactory, expandName, expand, JSON_URL, date);
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> getEntitiesUpdatedSince(final Class<U> type, final String idProperty, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand, final String dataType, final Date date)
	{
		assert date != null : "The date parameter can not be null";

		EntityManager entityManager = null;
		TransactionManager transactionManager = null;

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			if (transactionManager == null)
				throw new InternalServerErrorException("Could not find the TransactionManager");

			assert transactionManager != null : "transactionManager should not be null";
			assert entityManagerFactory != null : "entityManagerFactory should not be null";

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			/*
			 * get the list of topic ids that were edited after the selected
			 * date
			 */
			final AuditReader reader = AuditReaderFactory.get(entityManager);
			final AuditQuery query = reader.createQuery().forRevisionsOfEntity(type, true, false).addOrder(AuditEntity.revisionProperty("timestamp").asc()).add(AuditEntity.revisionProperty("timestamp").ge(date.getTime())).addProjection(AuditEntity.property("originalId." + idProperty).distinct());

			@SuppressWarnings("rawtypes")
			final List entityyIds = query.getResultList();

			/* now get the topics */
			final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			final CriteriaQuery<U> criteriaQuery = criteriaBuilder.createQuery(type);
			final Root<U> root = criteriaQuery.from(type);
			criteriaQuery.where(root.get(idProperty).in(entityyIds));

			final TypedQuery<U> jpaQuery = entityManager.createQuery(criteriaQuery);

			final List<U> entities = jpaQuery.getResultList();

			transactionManager.commit();

			/*
			 * convert the expand string from JSON to an instance of
			 * ExpandDataTrunk
			 */
			final ObjectMapper mapper = new ObjectMapper();
			final ExpandDataTrunk expandDataTrunk = mapper.readValue(expand, ExpandDataTrunk.class);

			final BaseRestCollectionV1<T> retValue = new RESTDataObjectCollectionFactory<T, U>().create(dataObjectFactory, entities, expandName, dataType, expandDataTrunk, getBaseUrl());

			return retValue;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue querying Envers");

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

	protected <T extends BaseRESTEntityV1<T>, U> T createEntity(final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U> factory, final String baseUrl, final String dataType, final String expand) throws InternalProcessingException, InvalidParameterException
	{
		return createOrUdpateEntity(type, restEntity, factory, DatabaseOperation.CREATE, baseUrl, dataType, expand);
	}

	protected <T extends BaseRESTEntityV1<T>, U> T updateEntity(final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U> factory, final String baseUrl, final String dataType, final String expand) throws InternalProcessingException, InvalidParameterException
	{
		return createOrUdpateEntity(type, restEntity, factory, DatabaseOperation.UPDATE, baseUrl, dataType, expand);
	}

	protected <U> U deleteEntity(final Class<U> type, final Integer id)
	{
		assert id != null : "id should not be null";

		TransactionManager transactionManager = null;
		EntityManager entityManager = null;

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			if (transactionManager == null)
				throw new InternalServerErrorException("Could not find the TransactionManager");

			assert transactionManager != null : "transactionManager should not be null";
			assert entityManagerFactory != null : "entityManagerFactory should not be null";

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			assert entityManager != null : "entityManager should not be null";

			final U entity = entityManager.find(type, id);

			if (entity == null)
				throw new BadRequestException("No entity was found with the id " + id);

			entityManager.remove(entity);
			entityManager.flush();
			transactionManager.commit();

			return entity;
		}
		catch (final Failure ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "There was an error looking up the required manager objects");
			throw ex;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error saving the entity");

			try
			{
				transactionManager.rollback();
			}
			catch (final Exception ex2)
			{
				SkynetExceptionUtilities.handleException(ex2, false, "There was an error rolling back the transaction");
			}

			throw new InternalServerErrorException("There was an error saving the entity");
		}
		finally
		{
			if (entityManager != null)
				entityManager.close();
		}
	}

	private <T extends BaseRESTEntityV1<T>, U> T createOrUdpateEntity(final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U> factory, final DatabaseOperation operation, final String baseUrl, final String dataType, final String expand) throws InternalProcessingException, InvalidParameterException
	{
		assert restEntity != null : "restEntity should not be null";
		assert factory != null : "factory should not be null";

		TransactionManager transactionManager = null;
		EntityManager entityManager = null;

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalProcessingException("Could not find the EntityManagerFactory");

			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			if (transactionManager == null)
				throw new InternalProcessingException("Could not find the TransactionManager");

			assert transactionManager != null : "transactionManager should not be null";
			assert entityManagerFactory != null : "entityManagerFactory should not be null";

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalProcessingException("Could not create an EntityManager");

			assert entityManager != null : "entityManager should not be null";

			/*
			 * The difference between creating or updating an entity is that we
			 * create a new instance of U, or find an existing instance of U.
			 */
			U entity = null;
			if (operation == DatabaseOperation.UPDATE)
			{
				/* Have to have an ID for the entity we are deleting or updating */
				if (restEntity.getId() == null)
					throw new InvalidParameterException("An id needs to be set for update operations");

				entity = entityManager.find(type, restEntity.getId());

				if (entity == null)
					throw new InvalidParameterException("No entity was found with the primary key " + restEntity.getId());

				factory.syncDBEntityWithRESTEntity(entityManager, entity, restEntity);

			}
			else if (operation == DatabaseOperation.CREATE)
			{
				entity = factory.createDBEntityFromRESTEntity(entityManager, restEntity);

				if (entity == null)
					throw new InvalidParameterException("The entity could not be created");
			}

			assert entity != null : "entity should not be null";

			entityManager.flush();
			transactionManager.commit();
			
			return factory.createRESTEntityFromDBEntity(entity, this.getBaseUrl(), JSON_URL, expand);
		}
		catch (final InternalProcessingException ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "There was an error looking up the required manager objects");
			throw ex;
		}
		catch (final InvalidParameterException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "There was an error looking up the entities");
			throw ex;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error saving the entity");

			try
			{
				transactionManager.rollback();
			}
			catch (final Exception ex2)
			{
				SkynetExceptionUtilities.handleException(ex2, false, "There was an error rolling back the transaction");
			}

			throw new InternalProcessingException("There was an error saving the entity");
		}
		finally
		{
			if (entityManager != null)
				entityManager.close();
		}
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> createEntities(final Class<U> type, final BaseRestCollectionV1<T> entities, final RESTDataObjectFactory<T, U> factory, final String expandName, final String dataType, final String expand, final String baseUrl)
	{
		return createOrUdpateEntities(type, factory, entities, DatabaseOperation.CREATE, expandName, dataType, expand, baseUrl);
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> updateEntities(final Class<U> type, final BaseRestCollectionV1<T> entities, final RESTDataObjectFactory<T, U> factory, final String expandName, final String dataType, final String expand, final String baseUrl)
	{
		return createOrUdpateEntities(type, factory, entities, DatabaseOperation.UPDATE, expandName, dataType, expand, baseUrl);
	}

	protected <T extends BaseRESTEntityV1<T>, U> List<U> deleteEntities(final Class<U> type, final Set<String> ids, final RESTDataObjectFactory<T, U> factory) throws InvalidParameterException, InternalProcessingException
	{
		assert type != null : "type should not be null";
		assert ids != null : "ids should not be null";
		assert factory != null : "factory should not be null";

		TransactionManager transactionManager = null;
		EntityManager entityManager = null;

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalProcessingException("Could not find the EntityManagerFactory");

			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			if (transactionManager == null)
				throw new InternalProcessingException("Could not find the TransactionManager");

			assert transactionManager != null : "transactionManager should not be null";
			assert entityManagerFactory != null : "entityManagerFactory should not be null";

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalProcessingException("Could not create an EntityManager");

			assert entityManager != null : "entityManager should not be null";

			final List<U> retValue = new ArrayList<U>();
			for (final String id : ids)
			{
				/*
				 * The ids are passed as strings into a PathSegment. We need to
				 * change these into Integers
				 */
				Integer idInt = null;
				try
				{
					idInt = Integer.parseInt(id);
				}
				catch (final Exception ex)
				{
					throw new InvalidParameterException("The id " + id + " was not a valid Integer");
				}

				final U entity = entityManager.find(type, idInt);

				if (entity == null)
					throw new InvalidParameterException("No entity was found with the primary key " + id);

				entityManager.remove(entity);
				entityManager.flush();

				retValue.add(entity);
			}

			transactionManager.commit();

			return retValue;
		}
		catch (final InvalidParameterException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "There was an error looking up the database entities");
			throw ex;
		}
		catch (final InternalProcessingException ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "There was an error looking up the required manager objects");
			throw ex;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error saving the entity");

			try
			{
				transactionManager.rollback();
			}
			catch (final Exception ex2)
			{
				SkynetExceptionUtilities.handleException(ex2, false, "There was an error rolling back the transaction");
			}

			throw new InternalServerErrorException("There was an error saving the entity");
		}
		finally
		{
			if (entityManager != null)
				entityManager.close();
		}
	}

	/**
	 * Takes a collection of REST entities, updates or creates the corresponding
	 * database entities, and returns those database entities in a collection
	 */
	private <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> createOrUdpateEntities(final Class<U> type, final RESTDataObjectFactory<T, U> factory, final BaseRestCollectionV1<T> entities, final DatabaseOperation operation, final String expandName, final String dataType, final String expand, final String baseUrl)
	{
		assert entities != null : "dataObject should not be null";
		assert factory != null : "factory should not be null";

		TransactionManager transactionManager = null;
		EntityManager entityManager = null;

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			if (transactionManager == null)
				throw new InternalServerErrorException("Could not find the TransactionManager");

			assert transactionManager != null : "transactionManager should not be null";
			assert entityManagerFactory != null : "entityManagerFactory should not be null";

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			assert entityManager != null : "entityManager should not be null";

			final List<U> retValue = new ArrayList<U>();
			for (final T restEntity : entities.getItems())
			{

				/*
				 * The difference between creating or updating an entity is that
				 * we create a new instance of U, or find an existing instance
				 * of U.
				 */
				U entity = null;
				if (operation == DatabaseOperation.UPDATE)
				{
					/*
					 * Have to have an ID for the entity we are deleting or
					 * updating
					 */
					if (restEntity.getId() == null)
						throw new BadRequestException("An id needs to be set for update operations");

					entity = entityManager.find(type, restEntity.getId());

					if (entity == null)
						throw new BadRequestException("No entity was found with the primary key " + restEntity.getId());

					factory.syncDBEntityWithRESTEntity(entityManager, entity, restEntity);
				}
				else if (operation == DatabaseOperation.CREATE)
				{
					entity = factory.createDBEntityFromRESTEntity(entityManager, restEntity);

					if (entity == null)
						throw new BadRequestException("The entity could not be created");
				}

				assert entity != null : "entity should not be null";

				entityManager.persist(entity);
				entityManager.flush();

				retValue.add(entity);
			}

			transactionManager.commit();

			return new RESTDataObjectCollectionFactory<T, U>().create(factory, retValue, expandName, dataType, expand, baseUrl);
		}
		catch (final Failure ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "There was an error looking up the required manager objects");
			throw ex;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error saving the entity");

			try
			{
				transactionManager.rollback();
			}
			catch (final Exception ex2)
			{
				SkynetExceptionUtilities.handleException(ex2, false, "There was an error rolling back the transaction");
			}

			throw new InternalServerErrorException("There was an error saving the entity");
		}
		finally
		{
			if (entityManager != null)
				entityManager.close();
		}
	}
	
	protected <T> String convertObjectToJSON(final T object) throws JsonGenerationException, JsonMappingException, IOException
	{		
		return mapper.writeValueAsString(object);
	}
	
	protected String wrapJsonInPadding(final String padding, final String json)
	{
		return padding + "(" + json + ")";
	}
	
	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> T getJSONResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResource(type, dataObjectFactory, id, null, expand);
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> T getJSONResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final Number revision, final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getResource(type, dataObjectFactory, id, revision, expand, JSON_URL);
	}
	
	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> T getXMLResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getXMLResource(type, dataObjectFactory, id, null, expand);
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> T getXMLResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final Number revision, final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getResource(type, dataObjectFactory, id, revision, expand, XML_URL);
	}

	private <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> T getResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final Number revision, final String expand, final String dataType) throws InvalidParameterException, InternalProcessingException
	{
		assert type != null : "The type parameter can not be null";
		assert id != null : "The id parameter can not be null";
		assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";
		
		boolean usingRevisions = revision != null;

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			final U entity;
			
			if (usingRevisions)
			{
				/*
				 * Looking up an Envers previous version is an
				 * expensive operation. So instead of getting a
				 * complete collection and only adding those we need
				 * to the REST collection (like we do with standard
				 * related entities in the database), when it comes
				 * to Envers we only retrieve the previous versions
				 * when they are specifically requested.
				 * 
				 * This means that we only have to request the list
				 * of revision numbers (supplied to us via the
				 * revisions parameter) instead of having to request
				 * every revision.
				 */
				final AuditedEntity<U> parentAuditedEntity = (AuditedEntity<U>) entityManager.find(type, id);
				entity = parentAuditedEntity.getRevision(revision);
				
			} else {
				entity = entityManager.find(type, id);
			}
			if (entity == null)
				throw new BadRequestException("No entity was found with the primary key " + id);

			/* create the REST representation of the topic */
			final T restRepresentation = dataObjectFactory.createRESTEntityFromDBEntity(entity, this.getBaseUrl(), dataType, expand, usingRevisions);
			
			/*
			 * if the entities keyset relates to the revision
			 * numbers, copy that data across
			 */
			if (usingRevisions) {
				restRepresentation.setRevision(revision);
			}

			return restRepresentation;
		}
		catch (final NamingException ex)
		{
			throw new InternalProcessingException("Could not find the EntityManagerFactory");
		}
	}

	protected <U> U getEntity(final Class<U> type, final Object id)
	{
		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			final U entity = entityManager.find(type, id);
			if (entity == null)
				throw new BadRequestException("No entity was found with the primary key " + id);

			return entity;
		}
		catch (final NamingException ex)
		{
			throw new InternalServerErrorException("Could not find the EntityManagerFactory");
		}
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> getXMLResources(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getResources(type, dataObjectFactory, expandName, expand, XML_URL);
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> getJSONResources(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getResources(type, dataObjectFactory, expandName, expand, JSON_URL);
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> getResources(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand, final String dataType) throws InvalidParameterException, InternalProcessingException
	{
		assert type != null : "The type parameter can not be null";
		assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			final CriteriaQuery<U> criteriaQuery = criteriaBuilder.createQuery(type);

			criteriaQuery.from(type);

			final TypedQuery<U> query = entityManager.createQuery(criteriaQuery);

			final List<U> result = query.getResultList();

			/*
			 * convert the expand string from JSON to an instance of
			 * ExpandDataTrunk
			 */
			ExpandDataTrunk expandDataTrunk = null;
			if (expand != null && !expand.trim().isEmpty())
			{
				final ObjectMapper mapper = new ObjectMapper();
				expandDataTrunk = mapper.readValue(expand, ExpandDataTrunk.class);
			}

			final BaseRestCollectionV1<T> retValue = new RESTDataObjectCollectionFactory<T, U>().create(dataObjectFactory, result, expandName, dataType, expandDataTrunk, getBaseUrl());

			return retValue;
		}
		catch (final NamingException ex)
		{
			throw new InternalProcessingException("Could not find the EntityManagerFactory");
		}
		catch (final JsonParseException ex)
		{
			throw new InvalidParameterException("Could not convert expand data from JSON to an instance of ExpandDataTrunk");
		}
		catch (final JsonMappingException ex)
		{
			throw new InvalidParameterException("Could not convert expand data from JSON to an instance of ExpandDataTrunk");
		}
		catch (final IOException ex)
		{
			throw new InvalidParameterException("Could not convert expand data from JSON to an instance of ExpandDataTrunk");
		}
		catch (final Exception ex)
		{
			throw new InvalidParameterException("Internal processing error");
		}
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> getJSONTopicsFromQuery(final MultivaluedMap<String, String> queryParams, final FilterQueryBuilder filterQueryBuilder, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand) throws InternalProcessingException
	{
		return getTopicsFromQuery(queryParams, filterQueryBuilder, dataObjectFactory, expandName, expand, JSON_URL);
	}

	protected <T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>> BaseRestCollectionV1<T> getTopicsFromQuery(final MultivaluedMap<String, String> queryParams, final FilterQueryBuilder filterQueryBuilder, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand, final String dataType) throws InternalProcessingException
	{
		assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";
		assert uriInfo != null : "uriInfo can not be null";

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalProcessingException("Could not find the EntityManagerFactory");

			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalProcessingException("Could not create an EntityManager");

			// build up a Filter object from the URL variables
			final Filter filter = EntityUtilities.populateFilter(queryParams, Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC, Constants.MATCH_LOCALE);

			final String query = filter.buildQuery(filterQueryBuilder);

			@SuppressWarnings("unchecked")
			final List<U> result = entityManager.createQuery(query).getResultList();

			/*
			 * convert the expand string from JSON to an instance of
			 * ExpandDataTrunk
			 */
			ExpandDataTrunk expandDataTrunk = null;
			if (expand != null && !expand.trim().isEmpty())
			{
				final ObjectMapper mapper = new ObjectMapper();
				expandDataTrunk = mapper.readValue(expand, ExpandDataTrunk.class);
			}

			final BaseRestCollectionV1<T> retValue = new RESTDataObjectCollectionFactory<T, U>().create(dataObjectFactory, result, expandName, dataType, expandDataTrunk, getBaseUrl());

			return retValue;
		}
		catch (final NamingException ex)
		{
			throw new InternalServerErrorException("Could not find the EntityManagerFactory");
		}
		catch (final JsonParseException ex)
		{
			throw new InternalServerErrorException("Could not convert expand data from JSON to an instance of ExpandDataTrunk");
		}
		catch (final JsonMappingException ex)
		{
			throw new InternalServerErrorException("Could not convert expand data from JSON to an instance of ExpandDataTrunk");
		}
		catch (final IOException ex)
		{
			throw new InternalServerErrorException("Could not convert expand data from JSON to an instance of ExpandDataTrunk");
		}
	}
}
