package com.redhat.topicindex.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

/**
 * Provides the query elements required by Filter.buildQuery() to get a list of
 * Topic elements
 */
public class TopicFilterQueryBuilder implements FilterQueryBuilder
{
	private DateTime startEditDate;
	private DateTime endEditDate;
	private String startCreateDate;
	private String endCreateDate;
	private String filterFieldsLogic = "AND";
	private List<String> fields = new ArrayList<String>();

	@Override
	public String getSelectAllQuery()
	{
		return Topic.SELECT_ALL_QUERY;
	}

	@Override
	public String getMatchTagString(final Integer tagId)
	{
		return "EXISTS (SELECT 1 FROM TopicToTag topicToTag WHERE topicToTag.topic = topic AND topicToTag.tag.tagId = " + tagId + ") ";
	}

	@Override
	public String getNotMatchTagString(final Integer tagId)
	{
		return "NOT EXISTS (SELECT 1 FROM TopicToTag topicToTag WHERE topicToTag.topic = topic AND topicToTag.tag.tagId = " + tagId + ") ";
	}

	@Override
	public String getFilterString()
	{
		if (startCreateDate != null || endCreateDate != null)
		{
			String thisRestriction = "";

			if (startCreateDate != null)
			{
				thisRestriction = "topic.topicTimeStamp >= '" + startCreateDate + "'";
			}

			if (endCreateDate != null)
			{
				if (startCreateDate != null)
					thisRestriction += " AND ";

				thisRestriction += "topic.topicTimeStamp <= '" + endCreateDate + "'";
			}

			fields.add(thisRestriction);

		}

		if (startEditDate != null || endEditDate != null)
		{
			final String editedTopics = EntityUtilities.getEditedEntitiesString(Topic.class, "topicId", startEditDate, endEditDate);
			fields.add("topic.topicId IN (" + editedTopics + ")");
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
			if (fieldValue.trim().length() != 0)
				fields.add("topic.topicId IN (" + fieldValue + ")");
		}
		
		else if (fieldName.equals(Constants.TOPIC_IS_INCLUDED_IN_SPEC))
		{
			try
			{
				final Integer topicId = Integer.parseInt(fieldValue);
				final String relatedTopics = EntityUtilities.getTopicsInContentSpec(topicId);

				fields.add("topic.topicId IN (" + relatedTopics + ")");
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, true, "An invalid Topic ID was stored for a Content Spec in the database");
			}
		}

		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
		{
			fields.add("LOWER(topic.topicTitle) LIKE LOWER('%" + fieldValue + "%')");
		}

		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
		{
			fields.add("LOWER(topic.topicTitle) LIKE LOWER('%" + fieldValue + "%')");
		}

		else if (fieldName.equals(Constants.TOPIC_XML_FILTER_VAR))
		{
			fields.add("LOWER(topic.topicXML) LIKE LOWER('%" + fieldValue + "%')");
		}

		else if (fieldName.equals(Constants.TOPIC_DESCRIPTION_FILTER_VAR))
		{
			fields.add("LOWER(topic.topicText) LIKE LOWER('%" + fieldValue + "%')");
		}

		else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
		{
			fields.add("topic.parentTopicToTopics.size >= 1");
		}

		else if (fieldName.equals(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS))
		{
			fields.add("topic.childTopicToTopics.size >= 1");
		}

		else if (fieldName.equals(Constants.TOPIC_RELATED_TO))
		{
			try
			{
				final Integer topicId = Integer.parseInt(fieldValue);
				final String relatedTopics = EntityUtilities.getIncomingRelationshipsTo(topicId);

				fields.add("topic.topicId IN (" + relatedTopics + ")");
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid Topic ID was stored for a Filter in the database");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_RELATED_FROM))
		{
			try
			{
				final Integer topicId = Integer.parseInt(fieldValue);
				final String relatedTopics = EntityUtilities.getOutgoingRelationshipsFrom(topicId);

				fields.add("topic.topicId IN (" + relatedTopics + ")");
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid Topic ID was stored for a Filter in the database");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR))
		{
			try
			{
				final List<Integer> matchingTopic = EntityUtilities.getTextSearchTopicMatch(fieldValue);

				fields.add("topic.topicId IN (" + CollectionUtilities.toSeperatedString(matchingTopic) + ")");
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "Could not get a list of topics matching a Lucene query");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_HAS_XML_ERRORS))
		{
			try
			{
				final Boolean hasXMLErrors = Boolean.valueOf(fieldValue);
				if (hasXMLErrors)
					fields.add("topic.topicSecondOrderData.topicXMLErrors IS NOT NULL");
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
				final DateTime date = new DateTime().minusDays(days);
				final String editedTopics = EntityUtilities.getEditedEntitiesString(Topic.class, "topicId", date, null);

				fields.add("topic.topicId IN (" + editedTopics + ")");
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "An invalid integer was stored for a Filter in the database");
			}
		}

		else if (fieldName.equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
		{
			startCreateDate = fieldValue;
		}
		else if (fieldName.equals(Constants.TOPIC_ENDDATE_FILTER_VAR))
		{
			endCreateDate = fieldValue;
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
					final String topics = EntityUtilities.getTopicsWithOpenBugsString();
					fields.add("topic.topicId IN (" + topics + ")");
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
					final String topics = EntityUtilities.getTopicsWithBugsString();
					fields.add("topic.topicId IN (" + topics + ")");
				}
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error querying the TopicToBugzillaBug table");
			}
		}
		else if (fieldName.startsWith(Constants.TOPIC_PROPERTY_TAG))
		{
			try
			{
				final Pattern pattern = Pattern.compile(CommonConstants.PROPERTY_TAG_SEARCH_RE);
				final Matcher matcher = pattern.matcher(fieldValue);
				
				while (matcher.find())
				{
					final String propertyTagIdString = matcher.group("PropertyTagID");
					final String propertyTagValue = matcher.group("PropertyTagValue");
					
					if (propertyTagIdString != null && propertyTagValue != null)
					{
						final Integer propertyTagIdInt = Integer.parseInt(propertyTagIdString);
						final String topics = EntityUtilities.getTopicsWithPropertyTag(propertyTagIdInt, propertyTagValue);
						fields.add("topic.topicId IN (" + topics + ")");					
					}
					
					/* should only match once */
					break;
				}
				
			}
			catch (final Exception ex)
			{
				// could not parse integer, so fail
				SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Property Tag' Topic ID");
			}
		}
	}
}
