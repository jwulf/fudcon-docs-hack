package com.redhat.topicindex.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.jboss.seam.Component;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.Snapshot;
import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.SnapshotTopicToSnapshot;
import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

/**
 * Provides the query elements required by Filter.buildQuery() to get a list of
 * SnapshotTranslatedData elements
 */
public class SnapshotTranslatedDataFilterQueryBuilder implements FilterQueryBuilder
{
	private DateTime startEditDate;
	private DateTime endEditDate;
	private DateTime startCreateDate;
	private DateTime endCreateDate;
	private String filterFieldsLogic = "AND";
	private List<String> fields = new ArrayList<String>();
	private Map<SnapshotTopic, Topic> topicMap;
	private boolean queryForRevisionTranslations = true;

	public SnapshotTranslatedDataFilterQueryBuilder(final boolean queryForRevisionTranslations)
	{
		this.queryForRevisionTranslations = queryForRevisionTranslations;
		topicMap = EntityUtilities.getHistoricalTopicsFromSnapshotTopics();
	}

	@Override
	public String getSelectAllQuery()
	{
		return queryForRevisionTranslations
				? SnapshotTranslatedData.SELECT_ALL_QUERY
				: WorkingSnapshotTranslatedData.SELECT_ALL_QUERY;
	}
	
	public String getMatchingLocalString(final String locale)
	{
		if (locale == null)
			return "";
		
		final String entityName = this.queryForRevisionTranslations ? "snapshotTranslatedData" : "workingSnapshotTranslatedData";
		
		final String defaultLocale = System.getProperty(CommonConstants.DEFAULT_LOCALE_PROPERTY);
		
		if (defaultLocale != null && defaultLocale.toLowerCase().equals(locale.toLowerCase()))
			return "(" + entityName + ".translationLocale = '" + locale + "'OR " + entityName + ".translationLocale is null)";
		
		return entityName + ".translationLocale = '" + locale + "'";
	}

	@Override
	public String getMatchTagString(final Integer tagId)
	{
		/*
		 * maintain a list of the SnapshotTopics that reference a Envers Topic that
		 * contains this tag
		 */
		final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();

		/*
		 * Loop over all the SnapshotTopics, get the associated Envers object, and
		 * check to see is that topics contains the tag we are looking for
		 */
		for (final SnapshotTopic snapshotTopic : topicMap.keySet())
		{
			final Topic topic = topicMap.get(snapshotTopic);
			if (topic.getTagIDs().contains(tagId))
				matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
		}

		if (matchingSnaphotTopics.size() != 0)
		{
			return queryForRevisionTranslations
					? "snapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")"
					: "workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")";
		}
		else
		{
			return queryForRevisionTranslations
					? "snapshotTranslatedData.snapshotTopic in (-1)"
					: "workingSnapshotTranslatedData.snapshotTopic in (-1)";
		}
	}

	@Override
	public String getNotMatchTagString(final Integer tagId)
	{
		/*
		 * maintain a list of the SnapshotTopics that reference a Envers Topic that
		 * contains this tag
		 */
		final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();

		/*
		 * Loop over all the SnapshotTopics, get the associated Envers object, and
		 * check to see is that topics contains the tag we are looking for
		 */
		for (final SnapshotTopic snapshotTopic : topicMap.keySet())
		{
			final Topic topic = topicMap.get(snapshotTopic);
			if (!topic.getTagIDs().contains(tagId))
				matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
		}

		if (matchingSnaphotTopics.size() != 0)
		{
			return queryForRevisionTranslations
					? "snapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")"
					: "workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")";
		}
		else
		{
			return queryForRevisionTranslations
					? "snapshotTranslatedData.snapshotTopic in (-1)"
					: "workingSnapshotTranslatedData.snapshotTopic in (-1)";
		}
	}

	@Override
	public String getFilterString()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);

		if (startCreateDate != null || endCreateDate != null)
		{
			String thisRestriction = "";

			if (startCreateDate != null)
			{
				final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
				for (final SnapshotTopic snapshotTopic : topicMap.keySet())
				{
					final Topic topic = topicMap.get(snapshotTopic);
					if (topic.getTopicTimeStamp().after(startCreateDate.toDate()) || topic.getTopicTimeStamp().equals(startCreateDate.toDate()))
						matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
				}

				if (matchingSnaphotTopics.size() != 0)
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")"
							: "workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")";
				}
				else
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (-1)"
							: "workingSnapshotTranslatedData.snapshotTopic in (-1)";
				}
			}

			if (endCreateDate != null)
			{
				if (startCreateDate != null)
					thisRestriction += " and ";

				final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
				for (final SnapshotTopic snapshotTopic : topicMap.keySet())
				{
					final Topic topic = topicMap.get(snapshotTopic);
					if (topic.getTopicTimeStamp().before(endCreateDate.toDate()) || topic.getTopicTimeStamp().equals(endCreateDate.toDate()))
						matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
				}

				if (matchingSnaphotTopics.size() != 0)
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")"
							: "workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")";
				}
				else
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (-1)"
							: "workingSnapshotTranslatedData.snapshotTopic in (-1)";
				}
			}

			fields.add(thisRestriction);
		}

		if (startEditDate != null || endEditDate != null)
		{
			String thisRestriction = "";

			if (startEditDate != null)
			{
				final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
				for (final SnapshotTopic snapshotTopic : topicMap.keySet())
				{
					final Date revisionDate = reader.getRevisionDate(snapshotTopic.getTopicRevision());
					if (revisionDate.after(startEditDate.toDate()) || revisionDate.equals(startEditDate.toDate()))
						matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
				}

				if (matchingSnaphotTopics.size() != 0)
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")"
							: "workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")";
				}
				else
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (-1)"
							: "workingSnapshotTranslatedData.snapshotTopic in (-1)";
				}
			}

			if (endCreateDate != null)
			{
				if (startCreateDate != null)
					thisRestriction += " and ";

				final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
				for (final SnapshotTopic snapshotTopic : topicMap.keySet())
				{
					final Date revisionDate = reader.getRevisionDate(snapshotTopic.getTopicRevision());
					if (revisionDate.before(startCreateDate.toDate()) || revisionDate.equals(startCreateDate.toDate()))
						matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
				}

				if (matchingSnaphotTopics.size() != 0)
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")"
							: "workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(matchingSnaphotTopics) + ")";
				}
				else
				{
					thisRestriction += queryForRevisionTranslations
							? "snapshotTranslatedData.snapshotTopic in (-1)"
							: "workingSnapshotTranslatedData.snapshotTopic in (-1)";
				}
			}

			fields.add(thisRestriction);
		}

		String retValue = "";
		for (final String field : fields)
		{
			if (retValue.length() != 0)
				retValue += filterFieldsLogic + " ";

			retValue += field;
		}

		return retValue;
	}

	@Override
	public void processFilterString(final String fieldName, final String fieldValue)
	{
		if (fieldName.equals(Constants.TOPIC_LOGIC_FILTER_VAR))
		{
			filterFieldsLogic = fieldValue;
		}

		else if (fieldName.equals(Constants.SNAPSHOT_REVISION_ID))
		{
			if (queryForRevisionTranslations)
				fields.add("snapshotTranslatedData.snapshotRevision = " + fieldValue);
		}

		else if (fieldName.equals(Constants.SNAPSHOT_ID))
		{
			if (!queryForRevisionTranslations)
			{
				try
				{
					final Integer snapshotId = Integer.parseInt(fieldValue);			
					final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
					final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

					final CriteriaQuery<Snapshot> criteriaQuery = criteriaBuilder.createQuery(Snapshot.class);
					final Root<Snapshot> root = criteriaQuery.from(Snapshot.class);
					final Predicate whereSnapshotId = criteriaBuilder.equal(root.get("snapshotId"), snapshotId);		
					criteriaQuery.where(whereSnapshotId);

					final TypedQuery<Snapshot> query = entityManager.createQuery(criteriaQuery);
					final Snapshot result = query.getSingleResult();
										
					if (result != null)
					{
						/* find all the snapshot topics that belong to this snapshot, and find the WorkingSnapshotTranslatedData entities that belong under that  */
						final List<Integer> matchingWorkingSnaphotTopics = new ArrayList<Integer>();
						for (final SnapshotTopicToSnapshot snapshotTopicToSnapshot : result.getSnapshotTopicToSnapshot())
						{
							final SnapshotTopic snapshotTopic = snapshotTopicToSnapshot.getSnapshotTopic();
							
							for (final WorkingSnapshotTranslatedData workingSnapshotTranslatedData : snapshotTopic.getWorkingSnapshotTranslatedDataEntities())
							{
								matchingWorkingSnaphotTopics.add(workingSnapshotTranslatedData.getSnapshotTranslatedDataId());
							}
							
						}
						
						if (matchingWorkingSnaphotTopics.size() != 0)
							fields.add("workingSnapshotTranslatedData in (" + CollectionUtilities.toSeperatedString(matchingWorkingSnaphotTopics) + ")");
						else
							fields.add("workingSnapshotTranslatedData in (-1)");
					}

				}
				catch (final Exception ex)
				{
					SkynetExceptionUtilities.handleException(ex, false, "An invalid Snapshot ID was stored for a Filter in the database");
				}
			}
		}

		else if (fieldName.equals(Constants.TOPIC_IDS_FILTER_VAR))
		{
			final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
			for (final SnapshotTopic snapshotTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(snapshotTopic);
				if (fieldValue.indexOf(topic.getTopicId().toString()) != -1)
					matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
			}

			addFieldQuery(matchingSnaphotTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
		{
			final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
			for (final SnapshotTopic snapshotTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(snapshotTopic);
				if (fieldValue.toLowerCase().indexOf(topic.getTopicTitle().toLowerCase()) != -1)
					matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
			}

			addFieldQuery(matchingSnaphotTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_XML_FILTER_VAR))
		{
			final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
			for (final SnapshotTopic snapshotTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(snapshotTopic);
				if (fieldValue.toLowerCase().indexOf(topic.getTopicXML().toLowerCase()) != -1)
					matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
			}

			addFieldQuery(matchingSnaphotTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_DESCRIPTION_FILTER_VAR))
		{
			final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
			for (final SnapshotTopic snapshotTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(snapshotTopic);
				if (fieldValue.toLowerCase().indexOf(topic.getTopicText().toLowerCase()) != -1)
					matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
			}

			addFieldQuery(matchingSnaphotTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
		{
			final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
			for (final SnapshotTopic snapshotTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(snapshotTopic);
				if (topic.getParentTopicToTopics().size() >= 1)
					matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
			}

			addFieldQuery(matchingSnaphotTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS))
		{
			final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
			for (final SnapshotTopic snapshotTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(snapshotTopic);
				if (topic.getChildTopicToTopics().size() >= 1)
					matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
			}

			addFieldQuery(matchingSnaphotTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_RELATED_TO) || fieldName.equals(Constants.TOPIC_RELATED_FROM))
		{
			try
			{
				final Integer topicId = Integer.parseInt(fieldValue);

				final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
				for (final SnapshotTopic snapshotTopic : topicMap.keySet())
				{
					final Topic topic = topicMap.get(snapshotTopic);
					if (topic.isRelatedTo(topicId))
						matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
				}

				addFieldQuery(matchingSnaphotTopics);
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid Topic ID was stored for a Filter in the database");
			}
		}

		else if (fieldName.equals(Constants.TOPIC_HAS_XML_ERRORS))
		{
			try
			{
				final Boolean hasXMLErrors = Boolean.valueOf(fieldValue);
				if (hasXMLErrors)
				{
					final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
					for (final SnapshotTopic snapshotTopic : topicMap.keySet())
					{
						final Topic topic = topicMap.get(snapshotTopic);
						if (topic.getTopicSecondOrderData() != null && topic.getTopicSecondOrderData().getTopicXMLErrors() != null)
							matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
					}

					addFieldQuery(matchingSnaphotTopics);
				}
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid boolean value was stored for a Filter in the database");
			}

		}

		else if (fieldName.equals(Constants.TOPIC_EDITED_IN_LAST_DAYS))
		{
			try
			{
				final Integer days = Integer.parseInt(fieldValue);
				final Date date = new DateTime().minusDays(days).toDate();

				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final AuditReader reader = AuditReaderFactory.get(entityManager);

				final List<Integer> matchingSnaphotTopics = new ArrayList<Integer>();
				for (final SnapshotTopic snapshotTopic : topicMap.keySet())
				{
					final Date revisionDate = reader.getRevisionDate(snapshotTopic.getTopicRevision());
					if (revisionDate.after(date) || revisionDate.equals(date))
						matchingSnaphotTopics.add(snapshotTopic.getSnapshotTopicId());
				}

				addFieldQuery(matchingSnaphotTopics);
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid integer was stored for a Filter in the database");
			}
		}

		else if (fieldName.equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
		{
			try
			{
				startCreateDate = ISODateTimeFormat.dateTime().parseDateTime(fieldValue);
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid DateTime string was stored by the Filter for the start create date");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_ENDDATE_FILTER_VAR))
		{
			try
			{
				endCreateDate = ISODateTimeFormat.dateTime().parseDateTime(fieldValue);
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid DateTime string was stored by the Filter for the end create date");
			}
		}

		else if (fieldName.equals(Constants.TOPIC_STARTEDITDATE_FILTER_VAR))
		{
			try
			{
				startEditDate = ISODateTimeFormat.dateTime().parseDateTime(fieldValue);
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid DateTime string was stored by the Filter for the start edit date");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_ENDEDITDATE_FILTER_VAR))
		{
			try
			{
				endEditDate = ISODateTimeFormat.dateTime().parseDateTime(fieldValue);
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid DateTime string was stored by the Filter for the end edit date");
			}
		}
		
		else if (fieldName.equals(Constants.LOCALE))
		{
				fields.add(getMatchingLocalString(fieldValue));
		}
	}

	private void addFieldQuery(final List<Integer> ids)
	{
		fields.add(getFieldQuery(ids));
	}
	
	private String getFieldQuery(final List<Integer> ids)
	{
		if (ids.size() != 0)
		{
			if (queryForRevisionTranslations)
				return("snapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(ids) + ")");
			else
				return("workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(ids) + ")");
		}
		else
		{
			
			if (queryForRevisionTranslations)
				return("snapshotTranslatedData.snapshotTopic in (-1)");
			else
				return("workingSnapshotTranslatedData.snapshotTopic in (" + CollectionUtilities.toSeperatedString(ids) + ")");
		}
	}

}
