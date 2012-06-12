package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.StringConstants;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.StringConstantV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class StringConstantV1Factory extends RESTDataObjectFactory<StringConstantV1, StringConstants>
{
	StringConstantV1Factory()
	{
		super(StringConstants.class);
	}
	
	@Override
	StringConstantV1 createRESTEntityFromDBEntity(final StringConstants entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final StringConstantV1 retValue = new StringConstantV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);
		
		retValue.setId(entity.getStringConstantsId());
		retValue.setName(entity.getConstantName());
		retValue.setValue(entity.getConstantValue());
		
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<StringConstantV1, StringConstants>().create(new StringConstantV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		retValue.setLinks(baseUrl, BaseRESTv1.STRINGCONSTANT_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final StringConstants entity, final StringConstantV1 dataObject)
	{
		if (dataObject.isParameterSet(StringConstantV1.NAME_NAME))
			entity.setConstantName(dataObject.getName());
		
		if (dataObject.isParameterSet(StringConstantV1.VALUE_NAME))
			entity.setConstantValue(dataObject.getValue());
		
		entityManager.persist(entity);
	}
}
