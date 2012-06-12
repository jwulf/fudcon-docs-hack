package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedString;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.WorkingSnapshotTranslatedDataV1;
import com.redhat.topicindex.rest.entities.WorkingSnapshotTranslatedStringV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class WorkingSnapshotTranslatedStringV1Factory extends RESTDataObjectFactory<WorkingSnapshotTranslatedStringV1, WorkingSnapshotTranslatedString>
{
	WorkingSnapshotTranslatedStringV1Factory()
	{
		super(WorkingSnapshotTranslatedString.class);
	}

	@Override
	WorkingSnapshotTranslatedStringV1 createRESTEntityFromDBEntity(final WorkingSnapshotTranslatedString entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences) throws InvalidParameterException
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		assert expand != null : "Parameter expand can not be null";

		final WorkingSnapshotTranslatedStringV1 retValue = new WorkingSnapshotTranslatedStringV1();

		/* Set the expansion options */
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(WorkingSnapshotTranslatedStringV1.WORKINGSNAPSHOTTRANSLATEDDATA_NAME);

		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);
		
		/* Set the simple values */ 
		retValue.setId(entity.getWorkingSnapshotTranslatedStringID());
		retValue.setOriginalString(entity.getWorkingShapshotOriginalString());
		retValue.setTranslatedString(entity.getWorkingSnapshotTranslatedString());

		/* Set the object references */
		if (expandParentReferences)
		{
			retValue.setWorkingSnapshotTranslatedData(new WorkingSnapshotTranslatedDataV1Factory().createRESTEntityFromDBEntity(entity.getWorkingSnapshotTranslatedData(), baseUrl, dataType, expand.contains(WorkingSnapshotTranslatedStringV1.WORKINGSNAPSHOTTRANSLATEDDATA_NAME)));
		}
		
		retValue.setLinks(baseUrl, BaseRESTv1.WORKINGSNAPSHOTTRANSLATEDSTRING_URL_NAME, dataType, retValue.getId());
				
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final WorkingSnapshotTranslatedString entity, final WorkingSnapshotTranslatedStringV1 dataObject)
	{
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedStringV1.ORIGINALSTRING_NAME))
			entity.setWorkingShapshotOriginalString(dataObject.getOriginalString());
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedStringV1.TRANSLATEDSTRING_NAME))
			entity.setWorkingSnapshotTranslatedString(dataObject.getTranslatedString());
		
		entityManager.persist(entity);
		
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedStringV1.WORKINGSNAPSHOTTRANSLATEDDATA_NAME))
		{
			final WorkingSnapshotTranslatedData dbEntity = entityManager.find(WorkingSnapshotTranslatedData.class, dataObject.getWorkingSnapshotTranslatedData().getId());
			if (dbEntity == null)
				throw new BadRequestException("No WorkingSnapshotTranslatedData entity was found with the primary key " + dataObject.getWorkingSnapshotTranslatedData().getId());
			entity.setWorkingSnapshotTranslatedData(dbEntity);
		}		
	}
}
