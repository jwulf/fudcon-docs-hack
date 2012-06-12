package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.Snapshot;
import com.redhat.topicindex.entity.SnapshotRevision;
import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.SnapshotRevisionV1;
import com.redhat.topicindex.rest.entities.SnapshotTopicV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedDataV1;
import com.redhat.topicindex.rest.entities.SnapshotTranslatedStringV1;
import com.redhat.topicindex.rest.entities.SnapshotV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class SnapshotV1Factory extends RESTDataObjectFactory<SnapshotV1, Snapshot>
{
	SnapshotV1Factory()
	{
		super(Snapshot.class);
	}

	@Override
	SnapshotV1 createRESTEntityFromDBEntity(final Snapshot entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final SnapshotV1 retValue = new SnapshotV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(SnapshotV1.SNAPSHOTREVISIONS_NAME);
		expandOptions.add(SnapshotV1.SNAPSHOTTOPICS_NAME);
		
		retValue.setExpand(expandOptions);	
		
		/* Set simple properties */
		retValue.setId(entity.getSnapshotId());
		retValue.setName(entity.getSnapshotName());
		retValue.setDate(entity.getSnapshotDate());
		
		/* Set collections */
		retValue.setSnapshotRevisions_OTM
		(
			new RESTDataObjectCollectionFactory<SnapshotRevisionV1, SnapshotRevision>().create
			(
				new SnapshotRevisionV1Factory(), 
				entity.getSnapshotRevisionsArray(), 
				SnapshotV1.SNAPSHOTREVISIONS_NAME, 
				dataType,
				expand, 
				baseUrl,
				false
			)
		);
		retValue.setSnaphotTopics(new RESTDataObjectCollectionFactory<SnapshotTopicV1, SnapshotTopic>().create(new SnapshotTopicV1Factory(), entity.getSnapshotTopicsArray(), SnapshotV1.SNAPSHOTTOPICS_NAME, dataType, expand, baseUrl));

		retValue.setLinks(baseUrl, BaseRESTv1.SNAPSHOT_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(EntityManager entityManager, Snapshot entity, SnapshotV1 dataObject) throws InvalidParameterException
	{
		if (dataObject.isParameterSet(SnapshotV1.DATE_NAME))
			entity.setSnapshotDate(dataObject.getDate());
		if (dataObject.isParameterSet(SnapshotV1.NAME_NAME))
			entity.setSnapshotName(dataObject.getName());
		
		entityManager.persist(entity);
		
		if (dataObject.isParameterSet(SnapshotV1.SNAPSHOTTOPICS_NAME) && dataObject.getSnaphotTopics() != null)
		{
			for (final SnapshotTopicV1 restEntity : dataObject.getSnaphotTopics().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					final SnapshotTopic dbEntity = entityManager.find(SnapshotTopic.class, restEntity.getId());
					if (dbEntity == null)
						throw new InvalidParameterException("No SnapshotTopic entity was found with the primary key " + restEntity.getId());

					if (restEntity.getAddItem())
					{
						entity.addSnapshotTopic(entityManager, dbEntity.getSnapshotTopicId());
					}
					else if (restEntity.getRemoveItem())
					{
						entity.removeSnapshotTopic(dbEntity.getSnapshotTopicId());
					}
				}
			}
		}

		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(SnapshotV1.SNAPSHOTREVISIONS_NAME) && dataObject.getSnapshotRevisions_OTM() != null && dataObject.getSnapshotRevisions_OTM().getItems() != null)
		{
			for (final SnapshotRevisionV1 restEntity : dataObject.getSnapshotRevisions_OTM().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (restEntity.getAddItem())
					{
						final SnapshotRevision snapshotRevision = new SnapshotRevision();
						snapshotRevision.setSnapshot(entity);
						new SnapshotRevisionV1Factory().syncDBEntityWithRESTEntity(entityManager, snapshotRevision, restEntity);
						entity.getSnapshotRevisions().add(snapshotRevision);
					}
					else if (restEntity.getRemoveItem())
					{
						final SnapshotRevision dbEntity = entityManager.find(SnapshotRevision.class, restEntity.getId());
						if (dbEntity == null)
							throw new InvalidParameterException("No SnapshotRevision entity was found with the primary key " + restEntity.getId());
						
						entity.getSnapshotRevisions().remove(dbEntity);
					}
				}
			}
		}
		
	}

}
