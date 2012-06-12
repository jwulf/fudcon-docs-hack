package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.ecs.commonutils.NotificationUtilities;
import com.redhat.topicindex.entity.BugzillaBug;
import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TopicSourceUrl;
import com.redhat.topicindex.entity.TopicToPropertyTag;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.BugzillaBugV1;
import com.redhat.topicindex.rest.entities.PropertyTagV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.TopicSourceUrlV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class TopicV1Factory extends RESTDataObjectFactory<TopicV1, Topic>
{
	TopicV1Factory()
	{
		super(Topic.class);
	}

	@Override
	TopicV1 createRESTEntityFromDBEntity(final Topic entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		assert expand != null : "Parameter expand can not be null";

		final TopicV1 retValue = new TopicV1();

		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(BaseRESTv1.TAGS_EXPANSION_NAME);
		expandOptions.add(BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME);
		expandOptions.add(BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME);
		expandOptions.add(BaseRESTv1.SOURCE_URLS_EXPANSION_NAME);
		expandOptions.add(TopicV1.BUGZILLABUGS_NAME);
		expandOptions.add(TopicV1.PROPERTIES_NAME);
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);

		retValue.setExpand(expandOptions);

		/* Set simple properties */
		retValue.setId(entity.getTopicId());
		//debug jwulf 2012-06-10
		//NotificationUtilities.dumpMessageToStdOut("I am " + retValue.getId());

		retValue.setTitle(entity.getTopicTitle());
		retValue.setDescription(entity.getTopicText());
		retValue.setXml(entity.getTopicXML());
		retValue.setHtml(entity.getTopicRendered());
		retValue.setLastModified(entity.getFixedLastModifiedDate());
		retValue.setRevision(entity.getLatestRevision());
		retValue.setCreated(entity.getTopicTimeStamp());
		retValue.setLocale(entity.getTopicLocale());
		retValue.setXmlErrors(entity.getTopicXMLErrors());

		/* Set collections */
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		//debug jwulf 2012-06-10
		//NotificationUtilities.dumpMessageToStdOut("Calling get tags");
		
		retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl));
		retValue.setOutgoingRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getOutgoingRelatedTopicsArray(), BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl));
		retValue.setIncomingRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getIncomingRelatedTopicsArray(), BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl));
		retValue.setProperties(new RESTDataObjectCollectionFactory<PropertyTagV1, TopicToPropertyTag>().create(new TopicPropertyTagV1Factory(), entity.getTopicToPropertyTagsArray(), BaseRESTv1.PROPERTIES_EXPANSION_NAME, dataType, expand, baseUrl));
		retValue.setSourceUrls_OTM(new RESTDataObjectCollectionFactory<TopicSourceUrlV1, TopicSourceUrl>().create(new TopicSourceUrlV1Factory(), entity.getTopicSourceUrls(), BaseRESTv1.SOURCE_URLS_EXPANSION_NAME, dataType, expand, baseUrl, false));
		retValue.setBugzillaBugs_OTM(new RESTDataObjectCollectionFactory<BugzillaBugV1, BugzillaBug>().create(new BugzillaBugV1Factory(), entity.getBugzillaBugs(), TopicV1.BUGZILLABUGS_NAME, dataType, expand, baseUrl, false));

		retValue.setLinks(baseUrl, BaseRESTv1.TOPIC_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Topic entity, final TopicV1 dataObject) throws InvalidParameterException
	{
		/* sync the basic properties */
		if (dataObject.isParameterSet(TopicV1.TITLE_NAME))
			entity.setTopicTitle(dataObject.getTitle());
		if (dataObject.isParameterSet(TopicV1.DESCRIPTION_NAME))
			entity.setTopicText(dataObject.getDescription());
		if (dataObject.isParameterSet(TopicV1.XML_NAME))
			entity.setTopicXML(dataObject.getXml());
		if (dataObject.isParameterSet(TopicV1.HTML_NAME))
			entity.setTopicRendered(dataObject.getHtml());
		if (dataObject.isParameterSet(TopicV1.LOCALE_NAME))
			entity.setTopicLocale(dataObject.getLocale());
		if (dataObject.isParameterSet(TopicV1.XML_ERRORS_NAME))
			entity.setTopicXMLErrors(dataObject.getXmlErrors());

		if (dataObject.isParameterSet(TopicV1.TAGS_NAME) && dataObject.getTags() != null && dataObject.getTags().getItems() != null)
		{
			for (final TagV1 tag : dataObject.getTags().getItems())
			{
				if (tag.getRemoveItem())
				{
					final Tag tagEntity = entityManager.find(Tag.class, tag.getId());
					if (tagEntity == null)
						throw new InvalidParameterException("No Tag entity was found with the primary key " + tag.getId());

					entity.removeTag(tag.getId());
				}
			}

			for (final TagV1 tag : dataObject.getTags().getItems())
			{
				if (tag.getAddItem())
				{
					final Tag tagEntity = entityManager.find(Tag.class, tag.getId());
					if (tagEntity == null)
						throw new InvalidParameterException("No Tag entity was found with the primary key " + tag.getId());

					entity.addTag(entityManager, tag.getId());
				}
			}
		}

		if (dataObject.isParameterSet(TopicV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null)
		{
			/* remove children first */
			for (final PropertyTagV1 restEntity : dataObject.getProperties().getItems())
			{
				if (restEntity.getRemoveItem())
				{
					final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
					if (dbEntity == null)
						throw new InvalidParameterException("No PropertyTag entity was found with the primary key " + restEntity.getId());
					entity.removePropertyTag(dbEntity, restEntity.getValue());
				}
			}

			/* add children second */
			for (final PropertyTagV1 restEntity : dataObject.getProperties().getItems())
			{
				if (restEntity.getAddItem())
				{
					final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
					if (dbEntity == null)
						throw new InvalidParameterException("No PropertyTag entity was found with the primary key " + restEntity.getId());

					entity.addPropertyTag(dbEntity, restEntity.getValue());
				}
			}
		}

		/*
		 * Persist the entity before adding anything else as they require an id
		 * for the topic
		 */
		entityManager.persist(entity);

		if (dataObject.isParameterSet(TopicV1.OUTGOING_NAME) && dataObject.getIncomingRelationships() != null && dataObject.getIncomingRelationships().getItems() != null)
		{
			for (final TopicV1 topic : dataObject.getIncomingRelationships().getItems())
			{
				if (topic.getRemoveItem())
				{
					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
					if (otherTopic == null)
						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());

					entity.removeRelationshipTo(topic.getId(), 1);

				}
			}

			for (final TopicV1 topic : dataObject.getIncomingRelationships().getItems())
			{
				if (topic.getAddItem())
				{
					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
					if (otherTopic == null)
						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());

					entity.addRelationshipTo(entityManager, topic.getId(), 1);

				}
			}
		}

		if (dataObject.isParameterSet(TopicV1.INCOMING_NAME) && dataObject.getIncomingRelationships() != null && dataObject.getIncomingRelationships().getItems() != null)
		{
			for (final TopicV1 topic : dataObject.getIncomingRelationships().getItems())
			{
				if (topic.getRemoveItem())
				{
					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
					if (otherTopic == null)
						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());

					otherTopic.removeRelationshipTo(entity.getTopicId(), 1);

				}
			}

			for (final TopicV1 topic : dataObject.getIncomingRelationships().getItems())
			{
				if (topic.getAddItem())
				{
					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
					if (otherTopic == null)
						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());

					entity.addRelationshipFrom(entityManager, otherTopic.getTopicId(), 1);

				}
			}
		}

		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(TopicV1.SOURCE_URLS_NAME) && dataObject.getSourceUrls_OTM() != null && dataObject.getSourceUrls_OTM().getItems() != null)
		{
			for (final TopicSourceUrlV1 url : dataObject.getSourceUrls_OTM().getItems())
			{
				if (url.getRemoveItem())
				{

					final TopicSourceUrl topicSourceUrl = entityManager.find(TopicSourceUrl.class, url.getId());
					if (topicSourceUrl == null)
						throw new InvalidParameterException("No TopicSourceUrl entity was found with the primary key " + url.getId());

					entity.removeTopicSourceUrl(url.getId());

				}
			}

			for (final TopicSourceUrlV1 url : dataObject.getSourceUrls_OTM().getItems())
			{
				if (url.getAddItem())
				{
					final TopicSourceUrl dbEntity = new TopicSourceUrlV1Factory().createDBEntityFromRESTEntity(entityManager, url);
					entityManager.persist(dbEntity);
					entity.addTopicSourceUrl(dbEntity);
				}
			}
		}

		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(TopicV1.BUGZILLABUGS_NAME) && dataObject.getBugzillaBugs_OTM() != null && dataObject.getBugzillaBugs_OTM().getItems() != null)
		{
			for (final BugzillaBugV1 restEntity : dataObject.getBugzillaBugs_OTM().getItems())
			{
				if (restEntity.getRemoveItem())
				{
					final BugzillaBug dbEntity = entityManager.find(BugzillaBug.class, restEntity.getId());
					if (dbEntity == null)
						throw new InvalidParameterException("No BugzillaBug entity was found with the primary key " + restEntity.getId());

					entity.removeBugzillaBug(restEntity.getId());
				}
			}

			for (final BugzillaBugV1 restEntity : dataObject.getBugzillaBugs_OTM().getItems())
			{
				if (restEntity.getAddItem())
				{
					final BugzillaBug dbEntity = new BugzillaBugV1Factory().createDBEntityFromRESTEntity(entityManager, restEntity);
					entityManager.persist(dbEntity);
					entity.addBugzillaBug(dbEntity);
				}
			}
		}
	}
}
