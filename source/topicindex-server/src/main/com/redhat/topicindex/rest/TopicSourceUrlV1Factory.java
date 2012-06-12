package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.TopicSourceUrl;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.TopicSourceUrlV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class TopicSourceUrlV1Factory extends RESTDataObjectFactory<TopicSourceUrlV1, TopicSourceUrl>
{
	TopicSourceUrlV1Factory()
	{
		super(TopicSourceUrl.class);
	}
	
	@Override
	TopicSourceUrlV1 createRESTEntityFromDBEntity(final TopicSourceUrl entity, final String baseUrl, String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter entity can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final TopicSourceUrlV1 retValue = new TopicSourceUrlV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);

		retValue.setId(entity.getTopicSourceUrlid());
		retValue.setTitle(entity.getTitle());
		retValue.setDescription(entity.getDescription());
		retValue.setUrl(entity.getSourceUrl());
		
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<TopicSourceUrlV1, TopicSourceUrl>().create(new TopicSourceUrlV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		retValue.setLinks(baseUrl, BaseRESTv1.TOPICSOURCEURL_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final TopicSourceUrl entity, final TopicSourceUrlV1 dataObject)
	{
		if (dataObject.isParameterSet(TopicSourceUrlV1.TITLE_NAME))
			entity.setTitle(dataObject.getTitle());
		if (dataObject.isParameterSet(TopicSourceUrlV1.DESCRIPTION_NAME))
			entity.setDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(TopicSourceUrlV1.URL_NAME))
			entity.setSourceUrl(dataObject.getUrl());
		
		entityManager.persist(entity);
	}

}

