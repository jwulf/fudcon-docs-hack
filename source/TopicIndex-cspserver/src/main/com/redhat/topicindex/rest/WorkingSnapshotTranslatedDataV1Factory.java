package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedString;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.SnapshotTopicV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedDataV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedStringV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.UserV1;
import com.redhat.topicindex.rest.entities.WorkingSnapshotTranslatedDataV1;
import com.redhat.topicindex.rest.entities.WorkingSnapshotTranslatedStringV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class WorkingSnapshotTranslatedDataV1Factory extends RESTDataObjectFactory<WorkingSnapshotTranslatedDataV1, WorkingSnapshotTranslatedData>
{

	WorkingSnapshotTranslatedDataV1Factory()
	{
		super(WorkingSnapshotTranslatedData.class);
	}

	@Override
	WorkingSnapshotTranslatedDataV1 createRESTEntityFromDBEntity(final WorkingSnapshotTranslatedData entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences) throws InvalidParameterException
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		assert expand != null : "Parameter expand can not be null";

		final WorkingSnapshotTranslatedDataV1 retValue = new WorkingSnapshotTranslatedDataV1();

		/* Set the expansion options */
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(WorkingSnapshotTranslatedDataV1.TRANSLATEDSTRINGS_NAME);
		expandOptions.add(WorkingSnapshotTranslatedDataV1.SNAPSHOTTOPIC_NAME);

		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);
		
		/* Set the simple values */ 
		retValue.setId(entity.getSnapshotTranslatedDataId());
		retValue.setTranslatedXml(entity.getTranslatedXml());
		retValue.setTranslatedXmlRendered(entity.getTranslatedXmlRendered());
		retValue.setTranslationLocale(entity.getTranslationLocale());
		retValue.setUpdated(entity.getTranslatedXmlRenderedUpdated());
		
		/* Set the object references */
		if (expandParentReferences)
		{
			retValue.setSnapshotTopic(new SnapshotTopicV1Factory().createRESTEntityFromDBEntity(entity.getSnapshotTopic(), baseUrl, dataType, expand.contains(WorkingSnapshotTranslatedDataV1.SNAPSHOTTOPIC_NAME)));
		}

		/* Set the collections */
		retValue.setTranslatedStrings_OTM
		(
			new RESTDataObjectCollectionFactory<WorkingSnapshotTranslatedStringV1, WorkingSnapshotTranslatedString>().create
			(
				new WorkingSnapshotTranslatedStringV1Factory(), 
				entity.getWorkingSnapshotTranslatedStringsArray(), 
				WorkingSnapshotTranslatedDataV1.TRANSLATEDSTRINGS_NAME, 
				dataType, expand.contains(WorkingSnapshotTranslatedDataV1.TRANSLATEDSTRINGS_NAME), 
				baseUrl, 
				false	/* don't set the reference to this entity on the children */
			)
		);
	
		retValue.setLinks(baseUrl, BaseRESTv1.WORKINGSNAPSHOTTRANSLATEDDATA_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(EntityManager entityManager, WorkingSnapshotTranslatedData entity, WorkingSnapshotTranslatedDataV1 dataObject) throws InvalidParameterException
	{
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedDataV1.DATE_NAME))
			entity.setTranslatedXmlRenderedUpdated(dataObject.getUpdated());
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedDataV1.TRANSLATEDXML_NAME))
			entity.setTranslatedXml(dataObject.getTranslatedXml());
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedDataV1.TRANSLATEDXMLRENDERED_NAME))
			entity.setTranslatedXmlRendered(dataObject.getTranslatedXmlRendered());
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedDataV1.TRANSLATIONLOCALE_NAME))
			entity.setTranslationLocale(dataObject.getTranslationLocale());
		
		entityManager.persist(entity);

		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(WorkingSnapshotTranslatedDataV1.TRANSLATEDSTRINGS_NAME) && dataObject.getTranslatedStrings_OTM() != null && dataObject.getTranslatedStrings_OTM().getItems() != null)
		{
			for (final WorkingSnapshotTranslatedStringV1 restEntity : dataObject.getTranslatedStrings_OTM().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (restEntity.getAddItem())
					{
						final WorkingSnapshotTranslatedString dbEntity = new WorkingSnapshotTranslatedString();
						dbEntity.setWorkingSnapshotTranslatedData(entity);
						new WorkingSnapshotTranslatedStringV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
						entity.getWorkingSnapshotTranslatedStrings().add(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						final WorkingSnapshotTranslatedString dbEntity = entityManager.find(WorkingSnapshotTranslatedString.class, restEntity.getId());
						if (dbEntity == null)
							throw new InvalidParameterException("No WorkingSnapshotTranslatedString entity was found with the primary key " + restEntity.getId());
						entity.getWorkingSnapshotTranslatedStrings().remove(dbEntity);
					}
				}
			}
		}
	}

}
