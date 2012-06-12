package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.BlobConstants;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.BlobConstantV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class BlobConstantV1Factory extends RESTDataObjectFactory<BlobConstantV1, BlobConstants>
{
	BlobConstantV1Factory()
	{
		super(BlobConstants.class);
	}
	
	@Override
	BlobConstantV1 createRESTEntityFromDBEntity(final BlobConstants entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final BlobConstantV1 retValue = new BlobConstantV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);
		
		retValue.setId(entity.getBlobConstantsId());
		retValue.setName(entity.getConstantName());
		retValue.setValue(entity.getConstantValue());
		
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<BlobConstantV1, BlobConstants>().create(new BlobConstantV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		retValue.setLinks(baseUrl, BaseRESTv1.BLOBCONSTANT_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final BlobConstants entity, final BlobConstantV1 dataObject)
	{
		if (dataObject.isParameterSet(BlobConstantV1.NAME_NAME))
			entity.setConstantName(dataObject.getName());
		
		if (dataObject.isParameterSet(BlobConstantV1.VALUE_NAME))
			entity.setConstantValue(dataObject.getValue());
		
		entityManager.persist(entity);
	}
}
