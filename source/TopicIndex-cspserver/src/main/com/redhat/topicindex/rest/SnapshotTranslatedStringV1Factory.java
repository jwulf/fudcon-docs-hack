package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.SnapshotTranslatedString;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedDataV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedStringV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class SnapshotTranslatedStringV1Factory extends RESTDataObjectFactory<SnapshotTranslatedStringV1, SnapshotTranslatedString>
{
	SnapshotTranslatedStringV1Factory()
	{
		super(SnapshotTranslatedString.class);
	}

	@Override
	SnapshotTranslatedStringV1 createRESTEntityFromDBEntity(final SnapshotTranslatedString entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences) throws InvalidParameterException
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final SnapshotTranslatedStringV1 retValue = new SnapshotTranslatedStringV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(SnapshotTranslatedStringV1.SNAPSHOTTRANSLATEDDATA_NAME);
		
		retValue.setExpand(expandOptions);	
		
		/* Set simple properties */
		retValue.setId(entity.getSnapshotTranslatedStringID());
		retValue.setOriginalString(entity.getShapshotOriginalString());
		retValue.setTranslatedString(entity.getSnapshotTranslatedString());
		
		/* Set complex properties */
		if (expandParentReferences)
		{
			retValue.setSnapshotTranslatedData(new SnapshotTranslatedDataV1Factory().createRESTEntityFromDBEntity(entity.getSnapshotTranslatedData(), baseUrl, dataType, expand.contains(SnapshotTranslatedStringV1.SNAPSHOTTRANSLATEDDATA_NAME)));
		}

		retValue.setLinks(baseUrl, BaseRESTv1.SNAPSHOTTRANSLATEDSTRING_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final SnapshotTranslatedString entity, final SnapshotTranslatedStringV1 dataObject)
	{
		if (dataObject.isParameterSet(SnapshotTranslatedStringV1.ORIGINALSTRING_NAME))
			entity.setShapshotOriginalString(dataObject.getOriginalString());
		if (dataObject.isParameterSet(SnapshotTranslatedStringV1.TRANSLATEDSTRING_NAME))
			entity.setSnapshotTranslatedString(dataObject.getTranslatedString());
		
		entityManager.persist(entity);
		
		if (dataObject.isParameterSet(SnapshotTranslatedStringV1.SNAPSHOTTRANSLATEDDATA_NAME))
		{
			final SnapshotTranslatedData dbEntity = entityManager.find(SnapshotTranslatedData.class, dataObject.getSnapshotTranslatedData().getId());
			if (dbEntity == null)
				throw new BadRequestException("No SnapshotTranslatedData entity was found with the primary key " +  dataObject.getSnapshotTranslatedData().getId());
			entity.setSnapshotTranslatedData(dbEntity);
		}		
	}	
}