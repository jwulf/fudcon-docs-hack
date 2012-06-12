package com.redhat.topicindex.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.jboss.seam.Component;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.BugzillaBug;
import com.redhat.topicindex.entity.TranslatedTopic;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TranslatedTopicData;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

/**
 * Provides the query elements required by Filter.buildQuery() to get a list of
 * TranslatedTopicData elements
 */
public class TranslatedTopicDataFilterQueryBuilder implements FilterQueryBuilder
{
	private DateTime startEditDate;
	private DateTime endEditDate;
	private DateTime startCreateDate;
	private DateTime endCreateDate;
	private String filterFieldsLogic = "AND";
	private List<String> fields = new ArrayList<String>();
	private Map<TranslatedTopic, Topic> topicMap;

	public TranslatedTopicDataFilterQueryBuilder()
	{
		topicMap = EntityUtilities.getHistoricalTopicsFromTranslatedTopics();
	}
	
	public Map<TranslatedTopic, Topic> getTopicMap() {
		return topicMap;
	}

	public void setTopicMap(Map<TranslatedTopic, Topic> topicMap) {
		this.topicMap = topicMap;
	}

	@Override
	public String getSelectAllQuery()
	{
		return TranslatedTopicData.SELECT_ALL_QUERY;
	}
	
	@Override
	public String getMatchingLocalString(final String locale)
	{
		if (locale == null)
			return "";
		
		final String entityName = "translatedTopicData";
		
		final String defaultLocale = System.getProperty(CommonConstants.DEFAULT_LOCALE_PROPERTY);
		
		if (defaultLocale != null && defaultLocale.toLowerCase().equals(locale.toLowerCase()))
			return "(" + entityName + ".translationLocale = '" + locale + "' OR " + entityName + ".translationLocale is null)";
		
		return entityName + ".translationLocale = '" + locale + "'";
	}
	
	@Override
	public String getNotMatchingLocalString(final String locale)
	{
		if (locale == null)
			return "";
		
		final String entityName = "translatedTopicData";
		
		final String defaultLocale = System.getProperty(CommonConstants.DEFAULT_LOCALE_PROPERTY);
		
		if (defaultLocale != null && defaultLocale.toLowerCase().equals(locale.toLowerCase()))
			return "(" + entityName + ".translationLocale != '" + locale + "' AND " + entityName + ".translationLocale is not null)";
		
		return entityName + ".translationLocale != '" + locale + "'";
	}

	@Override
	public String getMatchTagString(final Integer tagId)
	{
		/*
		 * maintain a list of the TranslatedTopics that reference a Envers Topic that
		 * contains this tag
		 */
		final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();

		/*
		 * Loop over all the TranslatedTopics, get the associated Envers object, and
		 * check to see is that topics contains the tag we are looking for
		 */
		for (final TranslatedTopic translatedTopic : topicMap.keySet())
		{
			final Topic topic = topicMap.get(translatedTopic);
			if (topic.getTagIDs().contains(tagId))
				matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
		}

		if (matchingTranslatedTopics.size() != 0)
		{
			return "translatedTopicData.translatedTopic in (" + CollectionUtilities.toSeperatedString(matchingTranslatedTopics) + ")";
		}
		else
		{
			return "translatedTopicData.translatedTopic in (-1)";
		}
	}

	@Override
	public String getNotMatchTagString(final Integer tagId)
	{
		/*
		 * maintain a list of the TranslatedTopics that reference a Envers Topic that
		 * contains this tag
		 */
		final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();

		/*
		 * Loop over all the TranslatedTopics, get the associated Envers object, and
		 * check to see is that topics contains the tag we are looking for
		 */
		for (final TranslatedTopic translatedTopic : topicMap.keySet())
		{
			final Topic topic = topicMap.get(translatedTopic);
			if (!topic.getTagIDs().contains(tagId))
				matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
		}

		if (matchingTranslatedTopics.size() != 0)
		{
			return "translatedTopicData.translatedTopic in (" + CollectionUtilities.toSeperatedString(matchingTranslatedTopics) + ")";
		}
		else
		{
			return "translatedTopicData.translatedTopic in (-1)";
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
				final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
				for (final TranslatedTopic translatedTopic : topicMap.keySet())
				{
					final Topic topic = topicMap.get(translatedTopic);
					if (topic.getTopicTimeStamp().after(startCreateDate.toDate()) || topic.getTopicTimeStamp().equals(startCreateDate.toDate()))
						matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
				}

				if (matchingTranslatedTopics.size() != 0)
				{
					thisRestriction += "translatedTopicData.translatedTopic in (" + CollectionUtilities.toSeperatedString(matchingTranslatedTopics) + ")";
				}
				else
				{
					thisRestriction += "translatedTopicData.translatedTopic in (-1)";
				}
			}

			if (endCreateDate != null)
			{
				if (startCreateDate != null)
					thisRestriction += " and ";

				final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
				for (final TranslatedTopic translatedTopic : topicMap.keySet())
				{
					final Topic topic = topicMap.get(translatedTopic);
					if (topic.getTopicTimeStamp().before(endCreateDate.toDate()) || topic.getTopicTimeStamp().equals(endCreateDate.toDate()))
						matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
				}

				if (matchingTranslatedTopics.size() != 0)
				{
					thisRestriction += "translatedTopicData.translatedTopic in (" + CollectionUtilities.toSeperatedString(matchingTranslatedTopics) + ")";
				}
				else
				{
					thisRestriction += "translatedTopicData.translatedTopic in (-1)";
				}
			}

			fields.add(thisRestriction);
		}

		if (startEditDate != null || endEditDate != null)
		{
			String thisRestriction = "";

			if (startEditDate != null)
			{
				final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
				for (final TranslatedTopic translatedTopic : topicMap.keySet())
				{
					final Date revisionDate = reader.getRevisionDate(translatedTopic.getTopicRevision());
					if (revisionDate.after(startEditDate.toDate()) || revisionDate.equals(startEditDate.toDate()))
						matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
				}

				if (matchingTranslatedTopics.size() != 0)
				{
					thisRestriction += "translatedTopicData.translatedTopic in (" + CollectionUtilities.toSeperatedString(matchingTranslatedTopics) + ")";
				}
				else
				{
					thisRestriction += "translatedTopicData.translatedTopic in (-1)";
				}
			}

			if (endCreateDate != null)
			{
				if (startCreateDate != null)
					thisRestriction += " and ";

				final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
				for (final TranslatedTopic translatedTopic : topicMap.keySet())
				{
					final Date revisionDate = reader.getRevisionDate(translatedTopic.getTopicRevision());
					if (revisionDate.before(startCreateDate.toDate()) || revisionDate.equals(startCreateDate.toDate()))
						matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
				}

				if (matchingTranslatedTopics.size() != 0)
				{
					thisRestriction += "translatedTopicData.translatedTopic in (" + CollectionUtilities.toSeperatedString(matchingTranslatedTopics) + ")";
				}
				else
				{
					thisRestriction += "translatedTopicData.translatedTopic in (-1)";
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
		else if (fieldName.equals(Constants.TOPIC_IDS_FILTER_VAR))
		{
			final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
			for (final TranslatedTopic translatedTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(translatedTopic);
				if (fieldValue.indexOf(topic.getTopicId().toString()) != -1)
					matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
			}

			addFieldQuery(matchingTranslatedTopics);
		}
		else if (fieldName.equals(Constants.TOPIC_IS_INCLUDED_IN_SPEC))
		{
			try
			{
				final Integer topicId = Integer.parseInt(fieldValue);
				final String relatedTopics = EntityUtilities.getTopicsInContentSpec(topicId);

				final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
				for (final TranslatedTopic translatedTopic : topicMap.keySet())
				{
					final Topic topic = topicMap.get(translatedTopic);
					if (relatedTopics.matches("(^|.*,)" + topic.getTopicId() + "(,.*|$)"))
						matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
				}
				addFieldQuery(matchingTranslatedTopics);
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, true, "An invalid Topic ID was stored for a Content Spec in the database");
			}
		}

		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
		{
			final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
			for (final TranslatedTopic translatedTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(translatedTopic);
				if (fieldValue.toLowerCase().indexOf(topic.getTopicTitle().toLowerCase()) != -1)
					matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
			}

			addFieldQuery(matchingTranslatedTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_XML_FILTER_VAR))
		{
			final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
			for (final TranslatedTopic translatedTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(translatedTopic);
				if (fieldValue.toLowerCase().indexOf(topic.getTopicXML().toLowerCase()) != -1)
					matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
			}

			addFieldQuery(matchingTranslatedTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_DESCRIPTION_FILTER_VAR))
		{
			final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
			for (final TranslatedTopic translatedTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(translatedTopic);
				if (fieldValue.toLowerCase().indexOf(topic.getTopicText().toLowerCase()) != -1)
					matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
			}

			addFieldQuery(matchingTranslatedTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
		{
			final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
			for (final TranslatedTopic translatedTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(translatedTopic);
				if (topic.getParentTopicToTopics().size() >= 1)
					matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
			}

			addFieldQuery(matchingTranslatedTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS))
		{
			final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
			for (final TranslatedTopic translatedTopic : topicMap.keySet())
			{
				final Topic topic = topicMap.get(translatedTopic);
				if (topic.getChildTopicToTopics().size() >= 1)
					matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
			}

			addFieldQuery(matchingTranslatedTopics);
		}

		else if (fieldName.equals(Constants.TOPIC_RELATED_TO) || fieldName.equals(Constants.TOPIC_RELATED_FROM))
		{
			try
			{
				final Integer topicId = Integer.parseInt(fieldValue);

				final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
				for (final TranslatedTopic translatedTopic : topicMap.keySet())
				{
					final Topic topic = topicMap.get(translatedTopic);
					if (topic.isRelatedTo(topicId))
						matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
				}

				addFieldQuery(matchingTranslatedTopics);
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
					final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
					for (final TranslatedTopic translatedTopic : topicMap.keySet())
					{
						final Topic topic = topicMap.get(translatedTopic);
						if (topic.getTopicSecondOrderData() != null && topic.getTopicSecondOrderData().getTopicXMLErrors() != null && !topic.getTopicSecondOrderData().getTopicXMLErrors().isEmpty())
							matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
					}

					addFieldQuery(matchingTranslatedTopics);
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

				final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
				for (final TranslatedTopic translatedTopic : topicMap.keySet())
				{
					final Date revisionDate = reader.getRevisionDate(translatedTopic.getTopicRevision());
					if (revisionDate.after(date) || revisionDate.equals(date))
						matchingTranslatedTopics.add(translatedTopic.getTranslatedTopicId());
				}

				addFieldQuery(matchingTranslatedTopics);
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
		
		else if (fieldName.equals(Constants.TOPIC_HAS_OPEN_BUGZILLA_BUGS))
		{
			try
			{
				final Boolean fieldValueBoolean = Boolean.parseBoolean(fieldValue);
				if (fieldValueBoolean)
				{
					final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
					for (final TranslatedTopic translatedTopic : topicMap.keySet())
					{
						final Topic topic = topicMap.get(translatedTopic);
						for (final BugzillaBug bugzillaBug : topic.getBugzillaBugs())
						{
							if (bugzillaBug.getBugzillaBugOpen())
								matchingTranslatedTopics.add(translatedTopic.getId());
						}
					}
					addFieldQuery(matchingTranslatedTopics);
				}
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error querying the TopicToBugzillaBug table");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_HAS_BUGZILLA_BUGS))
		{
			try
			{
				final Boolean fieldValueBoolean = Boolean.parseBoolean(fieldValue);
				if (fieldValueBoolean)
				{
					final List<Integer> matchingTranslatedTopics = new ArrayList<Integer>();
					for (final TranslatedTopic translatedTopic : topicMap.keySet())
					{
						final Topic topic = topicMap.get(translatedTopic);
						if (topic.getBugzillaBugs() != null && !topic.getBugzillaBugs().isEmpty())
							matchingTranslatedTopics.add(translatedTopic.getId());
					}
					addFieldQuery(matchingTranslatedTopics);
				}
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error querying the TopicToBugzillaBug table");
			}
		}
		else if (fieldName.equals(Constants.LATEST_TRANSLATIONS_FILTER_VAR))
		{
			final Boolean fieldValueBoolean = Boolean.parseBoolean(fieldValue);
			if (fieldValueBoolean) {
				/* Limit to fully translated topics */
				final String topics = EntityUtilities.getLatestTranslatedTopicsString();
				fields.add("translatedTopicData.translatedTopicDataId in (" + topics + ")");
			}
		}
		else if (fieldName.equals(Constants.LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR))
		{
			final Boolean fieldValueBoolean = Boolean.parseBoolean(fieldValue);
			if (fieldValueBoolean) {
				/* Limit to the latest completed translated topics */
				final String topics = EntityUtilities.getLatestCompletedTranslatedTopicsString();
				fields.add("translatedTopicData.translatedTopicDataId in (" + topics + ")");
			}
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
			return("translatedTopicData.translatedTopic in (" + CollectionUtilities.toSeperatedString(ids) + ")");
		}
		else
		{
			return("translatedTopicData.translatedTopic in (-1)");
		}
	}

}
