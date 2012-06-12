package com.redhat.topicindex.rest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;

import com.redhat.ecs.commonutils.DocBookUtilities;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TopicSourceUrl;
import com.redhat.topicindex.entity.TopicToPropertyTag;
import com.redhat.topicindex.entity.TranslatedTopic;
import com.redhat.topicindex.entity.TranslatedTopicData;
import com.redhat.topicindex.entity.TranslatedTopicString;

import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.PropertyTagV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.TopicSourceUrlV1;
import com.redhat.topicindex.rest.entities.TranslatedTopicStringV1;
import com.redhat.topicindex.rest.entities.TranslatedTopicV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

class TranslatedTopicV1Factory extends RESTDataObjectFactory<TranslatedTopicV1, TranslatedTopicData>
{

	TranslatedTopicV1Factory()
	{
		super(TranslatedTopicData.class);
	}

	@Override
	TranslatedTopicV1 createRESTEntityFromDBEntity(final TranslatedTopicData entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences) throws InvalidParameterException
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		assert expand != null : "Parameter expand can not be null";

		final TranslatedTopicV1 retValue = new TranslatedTopicV1();

		/* Set the expansion options */
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(TranslatedTopicV1.TRANSLATEDTOPICSTRING_NAME);
		expandOptions.add(TranslatedTopicV1.INCOMING_NAME);
		expandOptions.add(TranslatedTopicV1.OUTGOING_NAME);
		expandOptions.add(TranslatedTopicV1.ALL_LATEST_INCOMING_NAME);
		expandOptions.add(TranslatedTopicV1.ALL_LATEST_OUTGOING_NAME);
		expandOptions.add(TranslatedTopicV1.TAGS_NAME);
		expandOptions.add(TranslatedTopicV1.SOURCE_URLS_NAME);
		expandOptions.add(TranslatedTopicV1.PROPERTIES_NAME);

		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);
		
		/* Set the simple values */ 
		retValue.setId(entity.getTranslatedTopicDataId());
		Field translatedTopicId;
		try {
			translatedTopicId = retValue.getClass().getDeclaredField("translatedTopicId");
			translatedTopicId.setAccessible(true);
			translatedTopicId.set(retValue, entity.getTranslatedTopic().getId());
		} catch (Exception ex) {
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue setting the protected translated topic id field");
		}
		retValue.setTopicId(entity.getTranslatedTopic().getTopicId());
		retValue.setTopicRevision(entity.getTranslatedTopic().getTopicRevision());
		
		/* 
		 * Get the title from the XML or if the XML is null then use the
		 * original topics title.
		 */
		String title = DocBookUtilities.findTitle(entity.getTranslatedXml());
		if (title == null) title = entity.getTranslatedTopic().getEnversTopic().getTopicTitle();
		
		/* 
		 * Append the locale to the title if its a dummy translation to 
		 * show that it is missing the related translated topic
		 */
		if (entity.getId() < 0) title = "[" + entity.getTranslationLocale() + "] " + title;
		retValue.setTitle(title);
		
		retValue.setXml(entity.getTranslatedXml());
		retValue.setXmlErrors(entity.getTranslatedXmlErrors());
		retValue.setHtml(entity.getTranslatedXmlRendered());
		retValue.setLocale(entity.getTranslationLocale());
		retValue.setTranslationPercentage(entity.getTranslationPercentage());
		
		/* Set the object references */
		if (expandParentReferences)
		{
			retValue.setTopic(new TopicV1Factory().createRESTEntityFromDBEntity(entity.getTranslatedTopic().getEnversTopic(), baseUrl, dataType, expand.contains(TranslatedTopicV1.TOPIC_NAME)));
		}
		
		if (!isRevision) 
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<TranslatedTopicV1, TranslatedTopicData>().create(new TranslatedTopicV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		/* Set the collections */
		retValue.setTranslatedTopicString_OTM
		(
			new RESTDataObjectCollectionFactory<TranslatedTopicStringV1, TranslatedTopicString>().create
			(
				new TranslatedTopicStringV1Factory(), 
				entity.getTranslatedTopicDataStringsArray(), 
				TranslatedTopicV1.TRANSLATEDTOPICSTRING_NAME, 
				dataType, expand.contains(TranslatedTopicV1.TRANSLATEDTOPICSTRING_NAME), 
				baseUrl, 
				false	/* don't set the reference to this entity on the children */
			)
		);
		retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTranslatedTopic().getEnversTopic().getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl));
		retValue.setOutgoingTranslatedRelationships(new RESTDataObjectCollectionFactory<TranslatedTopicV1, TranslatedTopicData>().create(new TranslatedTopicV1Factory(), entity.getOutgoingRelatedTranslatedTopicData(entityManager), TranslatedTopicV1.OUTGOING_NAME, dataType, expand, baseUrl, true));
		retValue.setIncomingTranslatedRelationships(new RESTDataObjectCollectionFactory<TranslatedTopicV1, TranslatedTopicData>().create(new TranslatedTopicV1Factory(), entity.getIncomingRelatedTranslatedTopicData(entityManager), TranslatedTopicV1.INCOMING_NAME, dataType, expand, baseUrl, true));
		retValue.setOutgoingRelationships(new RESTDataObjectCollectionFactory<TranslatedTopicV1, TranslatedTopicData>().create(new TranslatedTopicV1Factory(), entity.getOutgoingDummyFilledRelatedTranslatedTopicDatas(entityManager), TranslatedTopicV1.ALL_LATEST_OUTGOING_NAME, dataType, expand, baseUrl, true));
		retValue.setIncomingRelationships(new RESTDataObjectCollectionFactory<TranslatedTopicV1, TranslatedTopicData>().create(new TranslatedTopicV1Factory(), entity.getIncomingDummyFilledRelatedTranslatedTopicDatas(entityManager), TranslatedTopicV1.ALL_LATEST_INCOMING_NAME, dataType, expand, baseUrl, true));
		retValue.setSourceUrls_OTM(new RESTDataObjectCollectionFactory<TopicSourceUrlV1, TopicSourceUrl>().create(new TopicSourceUrlV1Factory(), entity.getTranslatedTopic().getEnversTopic().getTopicSourceUrls(), BaseRESTv1.SOURCE_URLS_EXPANSION_NAME, dataType, expand, baseUrl, false));
		retValue.setProperties(new RESTDataObjectCollectionFactory<PropertyTagV1, TopicToPropertyTag>().create(new TopicPropertyTagV1Factory(), entity.getTranslatedTopic().getEnversTopic().getTopicToPropertyTagsArray(), BaseRESTv1.PROPERTIES_EXPANSION_NAME, dataType, expand, baseUrl));
		
		retValue.setLinks(baseUrl, BaseRESTv1.TRANSLATEDTOPIC_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(EntityManager entityManager, TranslatedTopicData entity, TranslatedTopicV1 dataObject) throws InvalidParameterException
	{
		/* Since this factory is the rare case where two entities are combined
		 * into one. Check if it has a parent, if not then check if one exists that 
		 * matches otherwise create one. If one exists then update it.
		 */
		TranslatedTopic translatedTopic = entity.getTranslatedTopic();
		if (translatedTopic == null)
		{
			try
			{
				final Query query = entityManager.createQuery(TranslatedTopic.SELECT_ALL_QUERY + " WHERE translatedTopic.topicId=" + dataObject.getTopicId() 
						+ " AND translatedTopic.topicRevision=" + dataObject.getTopicRevision());
				translatedTopic = (TranslatedTopic) query.getSingleResult();
			}
			catch (Exception e)
			{
				translatedTopic = new TranslatedTopic();
			}
		}
		if (dataObject.isParameterSet(TranslatedTopicV1.TOPICID_NAME))
			translatedTopic.setTopicId(dataObject.getTopicId());
		if (dataObject.isParameterSet(TranslatedTopicV1.TOPICREVISION_NAME))
			translatedTopic.setTopicRevision(dataObject.getTopicRevision());
		
		/* Save the changes done to the translated topic */
		entityManager.persist(translatedTopic);
		entity.setTranslatedTopic(translatedTopic);

		if (dataObject.isParameterSet(TranslatedTopicV1.HTML_UPDATED))
			entity.setTranslatedXmlRenderedUpdated(dataObject.getHtmlUpdated());
		if (dataObject.isParameterSet(TranslatedTopicV1.XML_ERRORS_NAME))
			entity.setTranslatedXmlErrors(dataObject.getXmlErrors());
		if (dataObject.isParameterSet(TranslatedTopicV1.XML_NAME))
			entity.setTranslatedXml(dataObject.getXml());
		if (dataObject.isParameterSet(TranslatedTopicV1.HTML_NAME))
			entity.setTranslatedXmlRendered(dataObject.getHtml());
		if (dataObject.isParameterSet(TranslatedTopicV1.LOCALE_NAME))
			entity.setTranslationLocale(dataObject.getLocale());
		if (dataObject.isParameterSet(TranslatedTopicV1.TRANSLATIONPERCENTAGE_NAME))
			entity.setTranslationPercentage(dataObject.getTranslationPercentage());
			
		entityManager.persist(entity);

		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(TranslatedTopicV1.TRANSLATEDTOPICSTRING_NAME) && dataObject.getTranslatedTopicString_OTM() != null && dataObject.getTranslatedTopicString_OTM().getItems() != null)
		{
			for (final TranslatedTopicStringV1 restEntity : dataObject.getTranslatedTopicString_OTM().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (restEntity.getAddItem())
					{
						final TranslatedTopicString dbEntity = new TranslatedTopicString();
						dbEntity.setTranslatedTopicData(entity);
						new TranslatedTopicStringV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
						entity.getTranslatedTopicStrings().add(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						final TranslatedTopicString dbEntity = entityManager.find(TranslatedTopicString.class, restEntity.getId());
						if (dbEntity == null)
							throw new InvalidParameterException("No TranslatedTopicString entity was found with the primary key " + restEntity.getId());
						entity.getTranslatedTopicStrings().remove(dbEntity);
					}
				}
			}
		}
	}

}
