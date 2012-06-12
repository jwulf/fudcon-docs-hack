package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.TranslatedTopicString;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.TranslatedTopicStringV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class TranslatedTopicStringV1Factory extends RESTDataObjectFactory<TranslatedTopicStringV1, TranslatedTopicString>
{
	TranslatedTopicStringV1Factory()
	{
		super(TranslatedTopicString.class);
	}

	@Override
	TranslatedTopicStringV1 createRESTEntityFromDBEntity(final TranslatedTopicString entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences) throws InvalidParameterException
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		assert expand != null : "Parameter expand can not be null";
		
		final TranslatedTopicStringV1 retValue = new TranslatedTopicStringV1();

		/* Set the expansion options */
		final List<String> expandOptions = new ArrayList<String>();

		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);
		
		/* Set the simple values */ 
		retValue.setId(entity.getTranslatedTopicStringID());
		retValue.setOriginalString(entity.getOriginalString());
		retValue.setTranslatedString(entity.getTranslatedString());

		/* Set the object references */
		if (expandParentReferences)
		{
			retValue.setTranslatedTopic(new TranslatedTopicV1Factory().createRESTEntityFromDBEntity(entity.getTranslatedTopicData(), baseUrl, dataType, expand.contains(TranslatedTopicStringV1.TRANSLATEDTOPIC_NAME)));
		}
		
		if (!isRevision) 
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<TranslatedTopicStringV1, TranslatedTopicString>().create(new TranslatedTopicStringV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		retValue.setLinks(baseUrl, BaseRESTv1.TRANSLATEDTOPICSTRING_URL_NAME, dataType, retValue.getId());
				
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final TranslatedTopicString entity, final TranslatedTopicStringV1 dataObject)
	{
		if (dataObject.isParameterSet(TranslatedTopicStringV1.ORIGINALSTRING_NAME))
			entity.setOriginalString(dataObject.getOriginalString());
		if (dataObject.isParameterSet(TranslatedTopicStringV1.TRANSLATEDSTRING_NAME))
			entity.setTranslatedString(dataObject.getTranslatedString());
		
		entityManager.persist(entity);		
	}
}
