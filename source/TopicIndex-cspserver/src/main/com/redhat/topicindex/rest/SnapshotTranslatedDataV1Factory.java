package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.SnapshotRevision;
import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.SnapshotTranslatedString;
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

class SnapshotTranslatedDataV1Factory extends RESTDataObjectFactory<SnapshotTranslatedDataV1, SnapshotTranslatedData>
{
	SnapshotTranslatedDataV1Factory()
	{
		super(SnapshotTranslatedData.class);
	}

	@Override
	SnapshotTranslatedDataV1 createRESTEntityFromDBEntity(final SnapshotTranslatedData entity, final String baseUrl, String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences) throws InvalidParameterException
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final SnapshotTranslatedDataV1 retValue = new SnapshotTranslatedDataV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(SnapshotTranslatedDataV1.SNAPSHOTREVISION_NAME);
		expandOptions.add(SnapshotTranslatedDataV1.SNAPSHOTTOPIC_NAME);
		expandOptions.add(SnapshotTranslatedDataV1.TRANSLATEDSTRINGS_NAME);
		
		retValue.setExpand(expandOptions);	

		
		/* Set simple properties */
		retValue.setId(entity.getSnapshotTranslatedDataId());
		retValue.setLocale(entity.getTranslationLocale());
		retValue.setRenderedXml(entity.getTranslatedXmlRendered());
		retValue.setXml(entity.getTranslatedXml());
		
		/* Set complex properties */
		if (expandParentReferences)
		{
			retValue.setSnapshotTopic(new SnapshotTopicV1Factory().createRESTEntityFromDBEntity(entity.getSnapshotTopic(), baseUrl, dataType, expand.contains(SnapshotTranslatedDataV1.SNAPSHOTTOPIC_NAME)));
			retValue.setSnapshotRevision(new SnapshotRevisionV1Factory().createRESTEntityFromDBEntity(entity.getSnapshotRevision(), baseUrl, dataType, expand.contains(SnapshotTranslatedDataV1.SNAPSHOTREVISION_NAME)));
		}
		
		/* Set collections */
		retValue.setTranslatedStrings_OTM
		(
			new RESTDataObjectCollectionFactory<SnapshotTranslatedStringV1, SnapshotTranslatedString>().create
			(
				new SnapshotTranslatedStringV1Factory(), 
				entity.getSnapshotTranslatedStringsArray(), 
				SnapshotTranslatedDataV1.TRANSLATEDSTRINGS_NAME, 
				dataType, 
				expand, 
				baseUrl,
				false
			)
		);

		retValue.setLinks(baseUrl, BaseRESTv1.SNAPSHOTTRANSLATEDDATA_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final SnapshotTranslatedData entity, final SnapshotTranslatedDataV1 dataObject) throws InvalidParameterException
	{
		if (dataObject.isParameterSet(SnapshotTranslatedDataV1.DATE_NAME))
			entity.setTranslatedXmlRenderedUpdated(dataObject.getUpdated());
		if (dataObject.isParameterSet(SnapshotTranslatedDataV1.LOCALE_NAME))
			entity.setTranslationLocale(dataObject.getLocale());
		if (dataObject.isParameterSet(SnapshotTranslatedDataV1.RENDEREDXML_NAME))
			entity.setTranslatedXmlRendered(dataObject.getRenderedXml());
		if (dataObject.isParameterSet(SnapshotTranslatedDataV1.XML_NAME))
			entity.setTranslatedXml(dataObject.getXml());
		
		entityManager.persist(entity);

		if (dataObject.isParameterSet(SnapshotTranslatedDataV1.SNAPSHOTREVISION_NAME))
		{
			final SnapshotRevision snapshotRevision = entityManager.find(SnapshotRevision.class, dataObject.getSnapshotRevision().getId());
			if (snapshotRevision == null)
				throw new BadRequestException("No SnapshotTranslatedData entity was found with the primary key " + dataObject.getSnapshotRevision().getId());
			entity.setSnapshotRevision(snapshotRevision);
		}
		
		if (dataObject.isParameterSet(SnapshotTranslatedDataV1.SNAPSHOTTOPIC_NAME))
		{
			final SnapshotTopic snapshotTopic = entityManager.find(SnapshotTopic.class, dataObject.getSnapshotTopic().getId());
			if (snapshotTopic == null)
				throw new BadRequestException("No SnapshotTranslatedData entity was found with the primary key " + dataObject.getSnapshotRevision().getId());
			entity.setSnapshotTopic(snapshotTopic);
		}
		
		/* One To Many - Add will create a child entity */
		if (dataObject.isParameterSet(SnapshotTranslatedDataV1.TRANSLATEDSTRINGS_NAME) && dataObject.getTranslatedStrings_OTM() != null && dataObject.getTranslatedStrings_OTM().getItems() != null)
		{
			for (final SnapshotTranslatedStringV1 restEntity : dataObject.getTranslatedStrings_OTM().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (restEntity.getAddItem())
					{
						final SnapshotTranslatedString dbEntity = new SnapshotTranslatedString();
						dbEntity.setSnapshotTranslatedData(entity);
						new SnapshotTranslatedStringV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
						entity.getSnapshotTranslatedStrings().add(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						final SnapshotTranslatedString dbEntity = entityManager.find(SnapshotTranslatedString.class, restEntity.getId());
						if (dbEntity == null)
							throw new InvalidParameterException("No SnapshotTranslatedString entity was found with the primary key " + restEntity.getId());
						entity.getSnapshotTranslatedStrings().remove(dbEntity);
					}
				}
			}
		}
	}

}
