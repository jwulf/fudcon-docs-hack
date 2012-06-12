package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.expand.ExpandDataDetails;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

/**
 * A factory used to create collections of REST entity objects
 */
class RESTDataObjectCollectionFactory<T extends BaseRESTEntityV1<T>, U extends AuditedEntity<U>>
{
	BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final List<U> entities, final String expandName, final String dataType)
	{
		return create(dataObjectFactory, entities, expandName, dataType, "", null);
	}

	BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final List<U> entities, final String expandName, final String dataType, final String expand, final String baseUrl)
	{
		if (expand != null && !expand.isEmpty())
		{
			try
			{
				/*
				 * convert the expand string from JSON to an instance of
				 * ExpandDataTrunk
				 */
				final ObjectMapper mapper = new ObjectMapper();
				final ExpandDataTrunk expandDataTrunk = mapper.readValue(expand, ExpandDataTrunk.class);

				return this.create(dataObjectFactory, entities, expandName, dataType, expandDataTrunk, baseUrl);
			}
			catch (final Exception ex)
			{
				throw new BadRequestException("The expand parameter was not a valid JSON representation of a ExpandDataTrunk class");
			}
		}
		else
		{
			return this.create(dataObjectFactory, entities, expandName, dataType, (ExpandDataTrunk) null, baseUrl);
		}
	}

	BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final List<U> entities, final String expandName, final String dataType, final ExpandDataTrunk parentExpand, final String baseUrl)
	{
		return create(dataObjectFactory, entities, null, null, expandName, dataType, parentExpand, baseUrl, true);
	}
	
	BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final List<U> entities, final String expandName, final String dataType, final ExpandDataTrunk parentExpand, final String baseUrl, final boolean expandParentReferences)
	{
		return create(dataObjectFactory, entities, null, null, expandName, dataType, parentExpand, baseUrl, expandParentReferences);
	}

	BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final U parent, final List<Number> revisions, final String expandName, final String dataType, final ExpandDataTrunk parentExpand, final String baseUrl)
	{
		return create(dataObjectFactory, null, parent, revisions, expandName, dataType, parentExpand, baseUrl, true);
	}

	/**
	 * @param dataObjectFactory
	 *            The factory to convert the database entity to a REST entity
	 * @param entities
	 *            A collection of numbers mapped to database entities. If
	 *            isRevsionMap is true, these numbers are envers revision
	 *            numbers. If isRevsionMap is false, these numbers have no
	 *            meaning.
	 * @param parent
	 *            The parent from which to find previous versions
	 * @param revisions
	 *            A list of Envers revision numbers that we want to add to the
	 *            collection
	 * @param isRevsionMap
	 *            true if the entities keyset are related to envers revision
	 *            numbers. false if the entities keyset have no meaning.
	 * @param expandName
	 *            The name of the collection that we are working with
	 * @param dataType
	 *            The type of data that is returned through the REST interface
	 * @param parentExpand
	 *            The parent objects expansion details
	 * @param baseUrl
	 *            The base of the url that was used to access this collection
	 * @return a REST collection from a collection of database entities.
	 */
	BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final List<U> entities, final U parent, final List<Number> revisions, final String expandName, final String dataType, final ExpandDataTrunk parentExpand, final String baseUrl, final boolean expandParentReferences)
	{
		final BaseRestCollectionV1<T> retValue = new BaseRestCollectionV1<T>();

		if (dataObjectFactory == null)
			return retValue;

		/*
		 * either the entities collection needs to be set, or the revisions and
		 * parent
		 */
		if (!(entities != null || (revisions != null && parent != null)))
			return retValue;

		final boolean usingRevisions = entities == null;

		retValue.setSize(usingRevisions ? revisions.size() : entities.size());
		retValue.setExpand(expandName);

		final ExpandDataTrunk expand = parentExpand != null ? parentExpand.contains(expandName) : null;

		try
		{
			if (expand != null && expand.getTrunk().getName().equals(expandName))
			{
				assert baseUrl != null : "Parameter baseUrl can not be null if parameter expand is not null";

				final ExpandDataDetails indexes = expand.getTrunk();

				int start = 0;
				if (indexes.getStart() != null)
				{
					final int startIndex = indexes.getStart();
					if (startIndex < 0)
					{
						start = Math.max(0, entities.size() - startIndex);
					}
					else
					{
						start = Math.min(startIndex, entities.size() - 1);
					}
				}

				int end = usingRevisions ? revisions.size() : entities.size();
				if (indexes.getEnd() != null)
				{
					final int endIndex = indexes.getEnd();
					if (endIndex < 0)
					{
						end = Math.max(0, entities.size() - endIndex + 1);
					}
					else
					{
						end = Math.min(endIndex, entities.size());
					}
				}

				final int fixedStart = Math.min(start, end);
				final int fixedEnd = Math.max(start, end);

				retValue.setStartExpandIndex(fixedStart);
				retValue.setEndExpandIndex(fixedEnd);

				final List<T> restEntityArray = new ArrayList<T>();

				for (int i = fixedStart; i < fixedEnd; ++i)
				{
					U dbEntity = null;

					if (usingRevisions)
					{
						/*
						 * Looking up an Envers previous version is an
						 * expensive operation. So instead of getting a
						 * complete collection and only adding those we need
						 * to the REST collection (like we do with standard
						 * related entities in the database), when it comes
						 * to Envers we only retrieve the previous versions
						 * when they are specifically requested.
						 * 
						 * This means that we only have to request the list
						 * of revision numbers (supplied to us via the
						 * revisions parameter) instead of having to request
						 * every revision.
						 */
						final AuditedEntity<U> parentAuditedEntity = (AuditedEntity<U>) parent;
						dbEntity = parentAuditedEntity.getRevision(revisions.get(i));
						
					}
					else
					{
						dbEntity = entities.get(i);
					}

					if (dbEntity != null)
					{
						final T restEntity = dataObjectFactory.createRESTEntityFromDBEntity(dbEntity, baseUrl, dataType, expand, usingRevisions, expandParentReferences);

						/*
						 * if the entities keyset relates to the revision
						 * numbers, copy that data across
						 */
						if (usingRevisions) {
							restEntity.setRevision(revisions.get(i));
						}
						
						restEntityArray.add(restEntity);
					}

				}

				retValue.setItems(restEntityArray);
			}

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error creating or populating a BaseRestCollectionV1");
		}

		return retValue;
	}
}
