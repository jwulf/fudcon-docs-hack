package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.RoleV1;
import com.redhat.topicindex.rest.entities.SnapshotRevisionV1;
import com.redhat.topicindex.rest.entities.SnapshotTopicV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.entities.WorkingSnapshotTranslatedDataV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class SnapshotTopicV1Factory extends RESTDataObjectFactory<SnapshotTopicV1, SnapshotTopic>
{
	SnapshotTopicV1Factory()
	{
		super(SnapshotTopic.class);
	}
	
	@Override
	SnapshotTopicV1 createRESTEntityFromDBEntity(final SnapshotTopic entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final SnapshotTopicV1 retValue = new SnapshotTopicV1();

		retValue.setId(entity.getId());
		retValue.setTopicId(entity.getTopicId());
		retValue.setTopicRevision(entity.getTopicRevision());
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(SnapshotTopicV1.WORKINGTRANSLATEDDATA_NAME);
		
		retValue.setExpand(expandOptions);	

		retValue.setWorkingTranslatedData_OTM
		(
			new RESTDataObjectCollectionFactory<WorkingSnapshotTranslatedDataV1, WorkingSnapshotTranslatedData>().create
			(
				new WorkingSnapshotTranslatedDataV1Factory(), 
				entity.getWorkingSnapshotTranslatedDataEntitiesArray(), 
				SnapshotTopicV1.WORKINGTRANSLATEDDATA_NAME,
				dataType, 
				expand, 
				baseUrl,
				false
			)
		);

		retValue.setLinks(baseUrl, BaseRESTv1.SNAPSHOTTOPIC_URL_NAME, dataType, retValue.getId());

		return retValue;
	}
	
	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final SnapshotTopic entity, final SnapshotTopicV1 dataObject) throws InvalidParameterException
	{
		if (dataObject.isParameterSet(SnapshotTopicV1.TOPICID_NAME))
			entity.setTopicId(dataObject.getTopicId());
		if (dataObject.isParameterSet(SnapshotTopicV1.TOPICREVISION_NAME))
			entity.setTopicRevision(dataObject.getTopicRevision());
		
		/* we have to persist the entity here so the children entities have a valid entity to link to */
		entityManager.persist(entity);
		
		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(SnapshotTopicV1.WORKINGTRANSLATEDDATA_NAME) && dataObject.getWorkingTranslatedData_OTM() != null && dataObject.getWorkingTranslatedData_OTM().getItems() != null)
		{
			for (final WorkingSnapshotTranslatedDataV1 restEntity : dataObject.getWorkingTranslatedData_OTM().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{				
					if (restEntity.getAddItem())
					{
						final WorkingSnapshotTranslatedData dbEntity = new WorkingSnapshotTranslatedData();
						
						dbEntity.setSnapshotTopic(entity);
						new WorkingSnapshotTranslatedDataV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);

						entity.getWorkingSnapshotTranslatedDataEntities().add(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						final WorkingSnapshotTranslatedData dbEntity = entityManager.find(WorkingSnapshotTranslatedData.class, restEntity.getId());
						if (dbEntity == null)
							throw new InvalidParameterException("No WorkingSnapshotTranslatedData entity was found with the primary key " + restEntity.getId());
						
						entity.getWorkingSnapshotTranslatedDataEntities().remove(dbEntity);
					}
				}
			}
		}
	}
}
