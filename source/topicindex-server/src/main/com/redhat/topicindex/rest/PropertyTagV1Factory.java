package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.PropertyTagV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class PropertyTagV1Factory extends RESTDataObjectFactory<PropertyTagV1, PropertyTag>
{
	PropertyTagV1Factory()
	{
		super(PropertyTag.class);
	}
	
	@Override
	PropertyTagV1 createRESTEntityFromDBEntity(final PropertyTag entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final PropertyTagV1 retValue = new PropertyTagV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);
		
		retValue.setId(entity.getPropertyTagId());
		retValue.setName(entity.getPropertyTagName());
		retValue.setRegex(entity.getPropertyTagRegex());
		retValue.setUnique(entity.getPropertyTagIsUnique());
		
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<PropertyTagV1, PropertyTag>().create(new PropertyTagV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		retValue.setLinks(baseUrl, BaseRESTv1.PROPERTYTAG_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final PropertyTag entity, final PropertyTagV1 dataObject)
	{
		if (dataObject.isParameterSet(PropertyTagV1.DESCRIPTION_NAME))
			entity.setPropertyTagDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(PropertyTagV1.CANBENULL_NAME))
			entity.setPropertyTagCanBeNull(dataObject.isCanBeNull());
		if (dataObject.isParameterSet(PropertyTagV1.NAME_NAME))
			entity.setPropertyTagName(dataObject.getName());
		if (dataObject.isParameterSet(PropertyTagV1.REGEX_NAME))
			entity.setPropertyTagRegex(dataObject.getRegex());
		if (dataObject.isParameterSet(PropertyTagV1.ISUNIQUE_NAME))
			entity.setPropertyTagIsUnique(dataObject.isUnique());
		
		entityManager.persist(entity);
	}
}