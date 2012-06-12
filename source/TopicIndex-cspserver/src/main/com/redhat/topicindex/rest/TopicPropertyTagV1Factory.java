package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.TopicToPropertyTag;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.PropertyTagV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class TopicPropertyTagV1Factory extends RESTDataObjectFactory<PropertyTagV1, TopicToPropertyTag>
{
	TopicPropertyTagV1Factory()
	{
		super(TopicToPropertyTag.class);
	}
	
	@Override
	PropertyTagV1 createRESTEntityFromDBEntity(final TopicToPropertyTag entity, final String baseUrl, String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final PropertyTagV1 retValue = new PropertyTagV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);

		retValue.setId(entity.getPropertyTag().getPropertyTagId());
		retValue.setDescription(entity.getPropertyTag().getPropertyTagDescription());
		retValue.setName(entity.getPropertyTag().getPropertyTagName());
		retValue.setRegex(entity.getPropertyTag().getPropertyTagRegex());
		retValue.setValid(entity.isValid());
		retValue.setValue(entity.getValue());
		
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<PropertyTagV1, TopicToPropertyTag>().create(new TopicPropertyTagV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final TopicToPropertyTag entity, final PropertyTagV1 dataObject)
	{
		if (dataObject.isParameterSet(PropertyTagV1.VALUE_NAME))
			entity.setValue(dataObject.getValue());
		
		entityManager.persist(entity);
	}
}
