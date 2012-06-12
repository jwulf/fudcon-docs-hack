package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.Snapshot;
import com.redhat.topicindex.entity.SnapshotRevision;
import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.SnapshotTranslatedString;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.RoleV1;
import com.redhat.topicindex.rest.entities.SnapshotRevisionV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedDataV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedStringV1;
import com.redhat.topicindex.rest.entities.SnapshotV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class SnapshotRevisionV1Factory extends RESTDataObjectFactory<SnapshotRevisionV1, SnapshotRevision>
{
	SnapshotRevisionV1Factory()
	{
		super(SnapshotRevision.class);
	}

	@Override
	SnapshotRevisionV1 createRESTEntityFromDBEntity(final SnapshotRevision entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences) throws InvalidParameterException
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final SnapshotRevisionV1 retValue = new SnapshotRevisionV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(SnapshotRevisionV1.SNAPSHOT_NAME);
		expandOptions.add(SnapshotRevisionV1.SNAPSHOTTRANSLATEDDATAENTITIES_NAME);
		expandOptions.add(RoleV1.PARENTROLES_NAME);
		
		retValue.setExpand(expandOptions);	
		
		/* Set simple properties */
		retValue.setId(entity.getSnapshotRevisionID());
		retValue.setName(entity.getSnapshotRevisionName());
		retValue.setDate(entity.getSnapshotRevisionDate());
		
		/* Set complex properties */
		if (expandParentReferences)
		{
			retValue.setSnapshot(new SnapshotV1Factory().createRESTEntityFromDBEntity(entity.getSnapshot(), baseUrl, dataType, expand.contains(SnapshotRevisionV1.SNAPSHOT_NAME)));
		}
		
		/* Set collections */
		retValue.setSnapshotTranslatedDataEntities_OTM
		(
			new RESTDataObjectCollectionFactory<SnapshotTranslatedDataV1, SnapshotTranslatedData>().create
			(
				new SnapshotTranslatedDataV1Factory(), 
				entity.getSnapshotTranslatedDataEntitiesArray(), 
				SnapshotRevisionV1.SNAPSHOTTRANSLATEDDATAENTITIES_NAME, 
				dataType, 
				expand.contains(SnapshotRevisionV1.SNAPSHOTTRANSLATEDDATAENTITIES_NAME), 
				baseUrl,
				false
			)
		);
		
		retValue.setLinks(baseUrl, BaseRESTv1.SNAPSHOTREVISION_URL_NAME, dataType, retValue.getId());
				
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final SnapshotRevision entity, final SnapshotRevisionV1 dataObject) throws InvalidParameterException
	{
		if (dataObject.isParameterSet(SnapshotRevisionV1.DATE_NAME))
			entity.setSnapshotRevisionDate(dataObject.getDate());
		if (dataObject.isParameterSet(SnapshotRevisionV1.NAME_NAME))
			entity.setSnapshotRevisionName(dataObject.getName());
		
		entityManager.persist(entity);

		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(SnapshotRevisionV1.SNAPSHOTTRANSLATEDDATAENTITIES_NAME) && dataObject.getSnapshotTranslatedDataEntities_OTM() != null && dataObject.getSnapshotTranslatedDataEntities_OTM().getItems() != null)
		{
			for (final SnapshotTranslatedDataV1 restEntity : dataObject.getSnapshotTranslatedDataEntities_OTM().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (restEntity.getAddItem())
					{
						final SnapshotTranslatedData dbEntity = new SnapshotTranslatedData();
						dbEntity.setSnapshotRevision(entity);						
						new SnapshotTranslatedDataV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
						entity.getSnapshotTranslatedDataEntities().add(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						final SnapshotTranslatedData dbEntity = entityManager.find(SnapshotTranslatedData.class, restEntity.getId());
						if (dbEntity == null)
							throw new BadRequestException("No SnapshotTranslatedData entity was found with the primary key " + restEntity.getId());
						
						entity.getSnapshotTranslatedDataEntities().remove(dbEntity);
					}
				}
			}
		}
		
	}

}
