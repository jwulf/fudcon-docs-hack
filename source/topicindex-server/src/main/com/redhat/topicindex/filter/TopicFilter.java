package com.redhat.topicindex.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class represents the options used by the objects that extend the
 * ExtendedTopicList class to filter a query to retrieve Topic entities.
 */
public class TopicFilter
{
	private List<Integer> topicIds;
	private Integer topicRelatedTo;
	private Integer topicRelatedFrom;
	private String topicTitle;
	private String topicText;
	private DateTime startCreateDate;
	private DateTime endCreateDate;
	private String topicRendered;
	private String topicXML;
	private String topicProduct;
	private String logic;
	private Boolean hasRelationships;
	private Integer minimumRelationshipCount;
	private Boolean hasIncomingRelationships;
	private Integer minimumIncomingRelationshipCount;
	private String topicTextSearch;
	private Boolean hasXMLErrors;
	private DateTime startEditDate;
	private DateTime endEditDate;
	private Integer editedInLastDays;
	private Boolean hasOpenBugzillaBugs;
	private Boolean hasBugzillaBugs;
	private List<String> propertyTags;
	private String topicIncludedInSpec;
	private Boolean latestTranslations;
	private Boolean latestCompletedTranslations;

	private void resetAllValues()
	{
		topicIds = null;
		topicRelatedTo = null;
		topicRelatedFrom = null;
		topicTitle = null;
		topicText = null;
		startCreateDate = null;
		endCreateDate = null;
		topicRendered = null;
		topicXML = null;
		topicProduct = null;
		logic = null;
		hasRelationships = null;
		minimumRelationshipCount = null;
		hasIncomingRelationships = null;
		minimumIncomingRelationshipCount = null;
		topicTextSearch = null;
		hasXMLErrors = null;
		startEditDate = null;
		endEditDate = null;
		editedInLastDays = null;
		hasOpenBugzillaBugs = null;
		hasBugzillaBugs = null;
		propertyTags = null;
		topicIncludedInSpec = null;
		latestTranslations = null;
		latestCompletedTranslations = null;
	}

	public List<Integer> getRelatedTopicIDs()
	{
		final List<Integer> retValue = EntityUtilities.getOutgoingRelatedTopicIDs(this.topicRelatedFrom);
		/*
		 * The EntityQuery class treats an empty list as null, and will not
		 * attempt to apply a restriction against it. If there are no topics
		 * returned, add -1 to the list to ensure that no topics are matched,
		 * but the list is not empty and therefore counted as a restriction.
		 */
		if (retValue != null && retValue.size() == 0)
			retValue.add(-1);
		return retValue;
	}

	public List<Integer> getIncomingRelatedTopicIDs()
	{
		final List<Integer> retValue = EntityUtilities.getIncomingRelatedTopicIDs(this.topicRelatedTo);
		if (retValue != null && retValue.size() == 0)
			retValue.add(-1);
		return retValue;
	}

	public List<Integer> getEditedTopics()
	{
		final List<Integer> retValue = EntityUtilities.getEditedEntities(Topic.class, "topicId", this.startEditDate, this.endEditDate);
		return retValue;
	}

	public void setTopicRelatedTo(final Integer topicRelatedTo)
	{
		this.topicRelatedTo = topicRelatedTo;
	}

	public Integer getTopicRelatedTo()
	{
		return topicRelatedTo;
	}

	public void setHasRelationships(final Boolean hasRelationships)
	{
		if (hasRelationships == null || !hasRelationships)
		{
			this.minimumRelationshipCount = null;
			this.hasRelationships = null;
		}
		else
		{
			this.minimumRelationshipCount = 1;
			this.hasRelationships = true;
		}
	}

	public Boolean getHasRelationships()
	{
		return hasRelationships;
	}

	public void setMinimumRelationshipCount(Integer minimumRelationshipCount)
	{
		this.minimumRelationshipCount = minimumRelationshipCount;
	}

	public Integer getMinimumRelationshipCount()
	{
		return minimumRelationshipCount;
	}

	public void setTopicTitle(final String topicTitle)
	{
		this.topicTitle = topicTitle;
	}

	public String getTopicTitle()
	{
		return topicTitle;
	}

	public void setTopicText(final String topicText)
	{
		this.topicText = topicText;
	}

	public String getTopicText()
	{
		return topicText;
	}

	public void setStartCreateDate(final DateTime startCreateDate)
	{
		this.startCreateDate = startCreateDate;
	}

	public DateTime getStartCreateDate()
	{
		return startCreateDate;
	}

	public void setEndCreateDate(final DateTime endCreateDate)
	{
		this.endCreateDate = endCreateDate;
	}

	public DateTime getEndCreateDate()
	{
		return endCreateDate;
	}

	public void setTopicRendered(final String topicRendered)
	{
		this.topicRendered = topicRendered;
	}

	public String getTopicRendered()
	{
		return topicRendered;
	}

	public void setTopicXML(final String topicXML)
	{
		this.topicXML = topicXML;
	}

	public String getTopicXML()
	{
		return topicXML;
	}

	public void setTopicProduct(final String topicProduct)
	{
		this.topicProduct = topicProduct;
	}

	public String getTopicProduct()
	{
		return topicProduct;
	}

	public void setTopicIds(final List<Integer> topicIds)
	{
		this.topicIds = topicIds;
	}

	public List<Integer> getTopicIds()
	{
		return topicIds;
	}

	public void setTopicIdsString(final String topicIdsString)
	{
		this.topicIds = new ArrayList<Integer>();
		if (topicIdsString != null)
		{
			final String[] topicIdsArray = topicIdsString.split(",");
			for (final String topicId : topicIdsArray)
			{
				try
				{
					if (topicId.trim().length() != 0)
						this.topicIds.add(Integer.parseInt(topicId.trim()));
				}
				catch (final Exception ex)
				{
					SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query paramater option for the Topic IDs");
				}
			}
		}
	}

	public String getTopicIdsString()
	{
		if (this.topicIds != null && this.topicIds.size() != 0)
		{
			String retValue = "";
			for (final Integer topicId : this.topicIds)
			{
				if (retValue.length() != 0)
					retValue += ",";
				retValue += topicId.toString();
			}
			return retValue;
		}

		return null;
	}

	public void setStartCreateDateString(final String startDate)
	{
		this.startCreateDate = convertStringToDate(startDate);
	}

	public String getStartCreateDateString()
	{
		return convertDateToString(this.startCreateDate);
	}

	public void setEndCreateDateString(final String endDate)
	{
		this.endCreateDate = convertStringToDate(endDate);
	}

	public String getEndCreateDateString()
	{
		return convertDateToString(this.endCreateDate);
	}

	public void setStartEditDateString(final String startDate)
	{
		this.startEditDate = convertStringToDate(startDate);
	}

	public String getStartEditDateString()
	{
		return convertDateToString(this.startEditDate);
	}

	public void setEndEditDateString(final String endDate)
	{
		this.endEditDate = convertStringToDate(endDate);
	}

	public String getEndEditDateString()
	{
		return convertDateToString(this.endEditDate);
	}

	public void setLogic(String logic)
	{
		this.logic = logic;
	}

	public String getLogic()
	{
		return logic;
	}

	public void setStartCreateDatePlain(final Date startCreateDate)
	{
		this.startCreateDate = startCreateDate == null ? null : new DateTime(startCreateDate);
	}

	public Date getStartCreateDatePlain()
	{
		return this.startCreateDate == null ? null : this.startCreateDate.toDate();
	}

	public void setStartEditDatePlain(final Date startEditDate)
	{
		this.startEditDate = startEditDate == null ? null : new DateTime(startEditDate);
	}

	public Date getStartEditDatePlain()
	{
		return this.startEditDate == null ? null : this.startEditDate.toDate();
	}

	public void setEndCreateDatePlain(final Date endCreateDate)
	{
		this.endCreateDate = endCreateDate == null ? null : new DateTime(endCreateDate);
	}

	public Date getEndCreateDatePlain()
	{
		return this.endCreateDate == null ? null : this.endCreateDate.toDate();
	}

	public void setEndEditDatePlain(final Date endEditDate)
	{
		this.endEditDate = endEditDate == null ? null : new DateTime(endEditDate);
	}

	public Date getEndEditDatePlain()
	{
		return this.endEditDate == null ? null : this.endEditDate.toDate();
	}

	public String getFieldValue(final String fieldName)
	{
		if (fieldName.equals(Constants.TOPIC_IDS_FILTER_VAR))
			return this.getTopicIdsString();
		else if (fieldName.equals(Constants.TOPIC_IS_INCLUDED_IN_SPEC))
			return this.topicIncludedInSpec;
		else if (fieldName.equals(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR))
			return this.getTopicTextSearch();
		else if (fieldName.equals(Constants.TOPIC_XML_FILTER_VAR))
			return this.getTopicXML();
		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
			return this.getTopicTitle();
		else if (fieldName.equals(Constants.TOPIC_DESCRIPTION_FILTER_VAR))
			return this.getTopicText();
		else if (fieldName.equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
			return this.getStartCreateDateString();
		else if (fieldName.equals(Constants.TOPIC_ENDDATE_FILTER_VAR))
			return this.getEndCreateDateString();
		else if (fieldName.equals(Constants.TOPIC_STARTEDITDATE_FILTER_VAR))
			return this.getStartEditDateString();
		else if (fieldName.equals(Constants.TOPIC_ENDEDITDATE_FILTER_VAR))
			return this.getEndEditDateString();
		else if (fieldName.equals(Constants.TOPIC_LOGIC_FILTER_VAR))
			return this.getLogic();
		else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
			return this.hasRelationships == null ? null : this.hasRelationships.toString();
		else if (fieldName.equals(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS))
			return this.hasIncomingRelationships == null ? null : this.hasIncomingRelationships.toString();
		else if (fieldName.equals(Constants.TOPIC_RELATED_TO))
			return this.topicRelatedTo == null ? null : this.topicRelatedTo.toString();
		else if (fieldName.equals(Constants.TOPIC_RELATED_FROM))
			return this.topicRelatedFrom == null ? null : this.topicRelatedFrom.toString();
		else if (fieldName.equals(Constants.TOPIC_HAS_XML_ERRORS))
			return this.getHasXMLErrorsString();
		else if (fieldName.equals(Constants.TOPIC_EDITED_IN_LAST_DAYS))
			return this.editedInLastDays == null ? null : editedInLastDays.toString();
		else if (fieldName.equals(Constants.TOPIC_HAS_OPEN_BUGZILLA_BUGS))
			return this.hasOpenBugzillaBugs == null ? null : hasOpenBugzillaBugs.toString();
		else if (fieldName.equals(Constants.TOPIC_HAS_BUGZILLA_BUGS))
			return this.hasBugzillaBugs == null ? null : hasBugzillaBugs.toString();
		else if (fieldName.equals(Constants.LATEST_TRANSLATIONS_FILTER_VAR))
			return this.getLatestTranslations() == null ? null : this.latestTranslations.toString();
		else if (fieldName.equals(Constants.LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR))
			return this.getLatestCompletedTranslations() == null ? null : this.latestCompletedTranslations.toString();
		else if (fieldName.startsWith(Constants.TOPIC_PROPERTY_TAG))
		{
			try
			{
				final String index = fieldName.replace(Constants.TOPIC_PROPERTY_TAG, "");

				/*
				 * index will be empty if the fieldName is just
				 * Constants.TOPIC_PROPERTY_TAG, which can happen when another
				 * object is looping over the getBaseFilterNames() keyset.
				 */
				if (!index.isEmpty())
				{
					final Integer indexInt = Integer.parseInt(index);

					/*
					 * propertyTags will be null unless one of the
					 * setPropertyTag() method is called
					 */
					if (this.propertyTags != null && this.propertyTags.size() < indexInt)
						return this.propertyTags.get(indexInt);
				}
				return null;
			}
			catch (final Exception ex)
			{
				// could not parse integer, so fail
				SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Property Tag' Topic ID");
				return null;
			}

		}

		return null;
	}

	public void setFieldValue(final String fieldName, final String fieldValue)
	{
		if (fieldName.equals(Constants.TOPIC_IDS_FILTER_VAR))
			this.setTopicIdsString(fieldValue);
		if (fieldName.equals(Constants.TOPIC_IS_INCLUDED_IN_SPEC))
			this.setTopicIncludedInSpec(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_XML_FILTER_VAR))
			this.setTopicXML(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR))
			this.setTopicTextSearch(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
			this.setTopicTitle(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_DESCRIPTION_FILTER_VAR))
			this.setTopicText(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
			this.setStartCreateDateString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_ENDDATE_FILTER_VAR))
			this.setEndCreateDateString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_STARTEDITDATE_FILTER_VAR))
			this.setStartEditDateString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_ENDEDITDATE_FILTER_VAR))
			this.setEndEditDateString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_LOGIC_FILTER_VAR))
			this.setLogic(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_HAS_XML_ERRORS))
			this.setHasXMLErrorsString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_EDITED_IN_LAST_DAYS))
			this.setEditedInLastDaysString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
			this.setHasRelationships(fieldValue == null ? null : fieldValue.equalsIgnoreCase("true"));
		else if (fieldName.equals(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS))
			this.setHasIncomingRelationships(fieldValue == null ? null : fieldValue.equalsIgnoreCase("true"));
		else if (fieldName.equals(Constants.LATEST_TRANSLATIONS_FILTER_VAR))
			this.setLatestTranslations(fieldValue == null ? null : fieldValue.equalsIgnoreCase("true"));
		else if (fieldName.equals(Constants.LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR))
			this.setLatestCompletedTranslations(fieldValue == null ? null : fieldValue.equalsIgnoreCase("true"));
		else if (fieldName.equals(Constants.TOPIC_RELATED_TO))
		{
			try
			{
				this.setTopicRelatedTo(fieldValue == null ? null : Integer.parseInt(fieldValue));
			}
			catch (final Exception ex)
			{
				// could not parse integer, so silently fail
				SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Related To' Topic ID");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_RELATED_FROM))
		{
			try
			{
				this.setTopicRelatedFrom(fieldValue == null ? null : Integer.parseInt(fieldValue));
			}
			catch (final Exception ex)
			{
				// could not parse integer, so silently fail
				SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Related From' Topic ID");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_HAS_OPEN_BUGZILLA_BUGS))
		{
			try
			{
				this.setHasOpenBugzillaBugs(fieldValue == null ? null : Boolean.parseBoolean(fieldValue));
			}
			catch (final Exception ex)
			{
				// could not parse integer, so silently fail
				SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Has Open Bugzilla Bugs' field");
			}
		}
		else if (fieldName.equals(Constants.TOPIC_HAS_BUGZILLA_BUGS))
		{
			try
			{
				this.setHasBugzillaBugs(fieldValue == null ? null : Boolean.parseBoolean(fieldValue));
			}
			catch (final Exception ex)
			{
				// could not parse integer, so silently fail
				SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Has Bugzilla Bugs' field");
			}
		}
		else if (fieldName.startsWith(Constants.TOPIC_PROPERTY_TAG))
		{
			try
			{
				final String index = fieldName.replace(Constants.TOPIC_PROPERTY_TAG, "");
				final Integer indexInt = Integer.parseInt(index);
				this.setPropertyTag(fieldValue, indexInt);
			}
			catch (final Exception ex)
			{
				// could not parse integer, so fail
				SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Property Tag' Topic ID");
			}

		}
	}

	public Map<String, String> getFilterValues()
	{
		final Map<String, String> retValue = new HashMap<String, String>();
		retValue.put(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR, this.getTopicTextSearch());
		retValue.put(Constants.TOPIC_IDS_FILTER_VAR, this.getTopicIdsString());
		retValue.put(Constants.TOPIC_IS_INCLUDED_IN_SPEC, this.topicIncludedInSpec);
		retValue.put(Constants.TOPIC_TITLE_FILTER_VAR, this.getTopicTitle());
		retValue.put(Constants.TOPIC_XML_FILTER_VAR, this.getTopicXML());
		retValue.put(Constants.TOPIC_DESCRIPTION_FILTER_VAR, this.getTopicText());
		retValue.put(Constants.TOPIC_STARTDATE_FILTER_VAR, this.getStartCreateDateString());
		retValue.put(Constants.TOPIC_ENDDATE_FILTER_VAR, this.getEndCreateDateString());
		retValue.put(Constants.TOPIC_STARTEDITDATE_FILTER_VAR, this.getStartEditDateString());
		retValue.put(Constants.TOPIC_ENDEDITDATE_FILTER_VAR, this.getEndEditDateString());
		retValue.put(Constants.TOPIC_LOGIC_FILTER_VAR, this.getLogic());
		retValue.put(Constants.TOPIC_HAS_RELATIONSHIPS, this.getHasRelationships() == null ? "" : this.getHasRelationships().toString());
		retValue.put(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS, this.getHasIncomingRelationships() == null ? "" : this.getHasIncomingRelationships().toString());
		retValue.put(Constants.TOPIC_RELATED_TO, this.getTopicRelatedTo() == null ? "" : this.getTopicRelatedTo().toString());
		retValue.put(Constants.TOPIC_RELATED_FROM, this.getTopicRelatedFrom() == null ? "" : this.getTopicRelatedFrom().toString());
		retValue.put(Constants.TOPIC_HAS_XML_ERRORS, this.getHasXMLErrorsString());
		retValue.put(Constants.TOPIC_EDITED_IN_LAST_DAYS, this.getEditedInLastDaysString());
		retValue.put(Constants.TOPIC_HAS_OPEN_BUGZILLA_BUGS, this.getHasOpenBugzillaBugs() == null ? null : this.getHasOpenBugzillaBugs().toString());
		retValue.put(Constants.TOPIC_HAS_BUGZILLA_BUGS, this.getHasBugzillaBugs() == null ? null : this.getHasBugzillaBugs().toString());
		retValue.put(Constants.LATEST_TRANSLATIONS_FILTER_VAR, this.getLatestTranslations() == null ? null : this.getLatestTranslations().toString());
		retValue.put(Constants.LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR, this.getLatestCompletedTranslations() == null ? null : this.getLatestCompletedTranslations().toString());
		
		int count = 1;
		for (final String propertyTag : propertyTags)
		{
			retValue.put(Constants.TOPIC_PROPERTY_TAG + " " + count, propertyTag);
			++count;
		}

		return retValue;
	}

	/**
	 * @return A map of the expanded filter field names (i.e. with regular
	 *         expressions) mapped to their descriptions
	 */
	private static Map<String, String> getFilterNames()
	{
		final Map<String, String> retValue = getSingleFilterNames();
		retValue.put(Constants.TOPIC_PROPERTY_TAG + "\\d+", Constants.TOPIC_PROPERTY_TAG_DESC);
		
		return retValue;
	}

	/**
	 * @return A map of the base filter field names (i.e. with no regular
	 *         expressions) mapped to their descriptions
	 */
	public static Map<String, String> getBaseFilterNames()
	{
		final Map<String, String> retValue = getSingleFilterNames();
		retValue.put(Constants.TOPIC_PROPERTY_TAG, Constants.TOPIC_PROPERTY_TAG_DESC);

		return retValue;
	}

	/**
	 * @return A map of the base filter field names that can not have multiple
	 *         mappings
	 */
	private static Map<String, String> getSingleFilterNames()
	{
		final Map<String, String> retValue = new HashMap<String, String>();
		retValue.put(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR, Constants.TOPIC_TEXT_SEARCH_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_IDS_FILTER_VAR, Constants.TOPIC_IDS_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_IS_INCLUDED_IN_SPEC, Constants.TOPIC_IS_INCLUDED_IN_SPEC_DESC);
		retValue.put(Constants.TOPIC_XML_FILTER_VAR, Constants.TOPIC_XML_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_TITLE_FILTER_VAR, Constants.TOPIC_TITLE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_DESCRIPTION_FILTER_VAR, Constants.TOPIC_DESCRIPTION_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_STARTDATE_FILTER_VAR, Constants.TOPIC_STARTDATE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_ENDDATE_FILTER_VAR, Constants.TOPIC_ENDDATE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_STARTEDITDATE_FILTER_VAR, Constants.TOPIC_STARTEDITDATE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_ENDEDITDATE_FILTER_VAR, Constants.TOPIC_ENDEDITDATE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_LOGIC_FILTER_VAR, Constants.TOPIC_LOGIC_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_HAS_RELATIONSHIPS, Constants.TOPIC_HAS_RELATIONSHIPS_DESC);
		retValue.put(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS, Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS_DESC);
		retValue.put(Constants.TOPIC_RELATED_TO, Constants.TOPIC_RELATED_TO_DESC);
		retValue.put(Constants.TOPIC_RELATED_FROM, Constants.TOPIC_RELATED_FROM_DESC);
		retValue.put(Constants.TOPIC_HAS_XML_ERRORS, Constants.TOPIC_HAS_XML_ERRORS_DESC);
		retValue.put(Constants.TOPIC_EDITED_IN_LAST_DAYS, Constants.TOPIC_EDITED_IN_LAST_DAYS_DESC);
		retValue.put(Constants.TOPIC_HAS_OPEN_BUGZILLA_BUGS, Constants.TOPIC_HAS_OPEN_BUGZILLA_BUGS_DESC);
		retValue.put(Constants.TOPIC_HAS_BUGZILLA_BUGS, Constants.TOPIC_HAS_BUGZILLA_BUGS_DESC);
		retValue.put(Constants.LATEST_TRANSLATIONS_FILTER_VAR, Constants.LATEST_TRANSLATIONS_FILTER_VAR_DESC);
		retValue.put(Constants.LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR, Constants.LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR_DESC);
		
		return retValue;
	}

	public static boolean hasFilterName(final String input)
	{
		boolean retValue = false;
		for (final String name : getFilterNames().keySet())
		{
			if (input.matches("^" + name + "$"))
			{
				retValue = true;
				break;
			}
		}

		return retValue;
	}

	public static String getFilterDesc(final String input)
	{
		for (final String name : getFilterNames().keySet())
		{
			if (input.matches("^" + name + "$"))
			{
				return getFilterNames().get(name);
			}
		}

		return "";
	}

	public Integer getTopicRelatedFrom()
	{
		return topicRelatedFrom;
	}

	public void setTopicRelatedFrom(final Integer topicRelatedFrom)
	{
		this.topicRelatedFrom = topicRelatedFrom;
	}

	public Boolean getHasIncomingRelationships()
	{
		return hasIncomingRelationships;
	}

	public void setHasIncomingRelationships(final Boolean hasIncomingRelationships)
	{
		if (hasIncomingRelationships == null || !hasIncomingRelationships)
		{
			this.minimumIncomingRelationshipCount = null;
			this.hasIncomingRelationships = null;
		}
		else
		{
			this.minimumIncomingRelationshipCount = 1;
			this.hasIncomingRelationships = true;
		}
	}

	public Integer getMinimumIncomingRelationshipCount()
	{
		return minimumIncomingRelationshipCount;
	}

	public void setMinimumIncomingRelationshipCount(final Integer minimumIncomingRelationshipCount)
	{
		this.minimumIncomingRelationshipCount = minimumIncomingRelationshipCount;
	}

	public String getTopicTextSearch()
	{
		return topicTextSearch;
	}

	public void setTopicTextSearch(final String topicTextSearch)
	{
		this.topicTextSearch = topicTextSearch == null ? null : topicTextSearch.trim();
	}

	public List<Integer> getTopicTextSearchIDs()
	{
		if (this.topicTextSearch == null || this.topicTextSearch.length() == 0)
			return null;

		return EntityUtilities.getTextSearchTopicMatch(this.topicTextSearch);
	}

	public void syncWithFilter(final Filter filter)
	{
		for (final FilterField field : filter.getFilterFields())
			this.setFieldValue(field.getField(), field.getValue());
	}

	public void loadFilterFields(final Filter filter)
	{
		resetAllValues();

		for (final FilterField filterField : filter.getFilterFields())
		{
			final String field = filterField.getField();
			final String value = filterField.getValue();

			this.setFieldValue(field, value);
		}
	}

	public Boolean getHasXMLErrors()
	{
		return hasXMLErrors;
	}

	public void setHasXMLErrors(final Boolean hasXMLErrors)
	{
		this.hasXMLErrors = hasXMLErrors == null || hasXMLErrors == false ? null : true;
	}

	public void setHasXMLErrorsString(final String hasXMLErrors)
	{
		try
		{
			this.setHasXMLErrors(hasXMLErrors == null ? null : Boolean.parseBoolean(hasXMLErrors));
		}
		catch (final Exception ex)
		{
			this.setHasXMLErrors(null);
			SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Has XML Errors' option");
		}
	}

	public String getHasXMLErrorsString()
	{
		return this.hasXMLErrors == null ? null : this.hasXMLErrors.toString();
	}

	public DateTime getStartEditDate()
	{
		return startEditDate;
	}

	public void setStartEditDate(final DateTime startEditDate)
	{
		this.startEditDate = startEditDate;
	}

	public DateTime getEndEditTime()
	{
		return endEditDate;
	}

	public void setEndEditTime(final DateTime endEditTime)
	{
		this.endEditDate = endEditTime;
	}

	private String convertDateToString(final DateTime date)
	{
		return date == null ? null : ISODateTimeFormat.dateTime().print(date);
	}

	private DateTime convertStringToDate(final String date)
	{
		try
		{
			if (date == null || date.length() == 0)
				return null;
			else
				return ISODateTimeFormat.dateTime().parseDateTime(date);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "Probably invalid input from a malformed URL query parameter");
		}

		return null;
	}

	public Integer getEditedInLastDays()
	{
		return editedInLastDays;
	}

	public void setEditedInLastDays(final Integer editedInLastDays)
	{
		this.editedInLastDays = editedInLastDays;
	}

	public String getEditedInLastDaysString()
	{
		return editedInLastDays == null ? null : editedInLastDays.toString();
	}

	public void setEditedInLastDaysString(final String editedInLastDays)
	{
		try
		{
			this.editedInLastDays = editedInLastDays == null || editedInLastDays.trim().length() == 0 ? null : Integer.parseInt(editedInLastDays);
		}
		catch (final Exception ex)
		{
			this.editedInLastDays = null;
			SkynetExceptionUtilities.handleException(ex, true, "Probably a malformed URL query parameter for the 'Edited In Last Days' option");
		}

	}

	public Boolean getHasOpenBugzillaBugs()
	{
		return this.hasOpenBugzillaBugs == null || hasOpenBugzillaBugs == false ? null : true;
	}

	public void setHasOpenBugzillaBugs(final Boolean hasOpenBugzillaBugs)
	{
		this.hasOpenBugzillaBugs = hasOpenBugzillaBugs;
	}

	public Boolean getHasBugzillaBugs()
	{
		return this.hasBugzillaBugs == null || hasBugzillaBugs == false ? null : true;
	}

	public void setHasBugzillaBugs(Boolean hasBugzillaBugs)
	{
		this.hasBugzillaBugs = hasBugzillaBugs;
	}

	public List<String> getPropertyTags()
	{
		return propertyTags;
	}

	public void setPropertyTags(final List<String> propertyTags)
	{
		this.propertyTags = propertyTags;
	}

	public void setPropertyTag(final String propertyTag, final int index)
	{
		if (this.propertyTags == null)
			this.propertyTags = new ArrayList<String>();

		if (this.propertyTags.size() < index)
		{
			final int start = this.propertyTags.size();
			for (int i = start; i < index; ++i)
			{
				this.propertyTags.add("");
			}
		}

		this.propertyTags.set(index, propertyTag);
	}

	public String getPropertyTag(final int index)
	{
		if (this.propertyTags == null)
			this.propertyTags = new ArrayList<String>();

		if (this.propertyTags.size() > index)
		{
			return this.propertyTags.get(index);
		}

		return null;
	}

	public String getTopicIncludedInSpec()
	{
		return topicIncludedInSpec;
	}

	public void setTopicIncludedInSpec(final String topicIncludedInSpec)
	{
		this.topicIncludedInSpec = topicIncludedInSpec;
	}

	public Boolean getLatestTranslations() {
		return latestTranslations;
	}

	public void setLatestTranslations(Boolean latestTranslations) {
		this.latestTranslations = latestTranslations;
	}

	public Boolean getLatestCompletedTranslations() {
		return latestCompletedTranslations;
	}

	public void setLatestCompletedTranslations(
			Boolean latestCompletedTranslations) {
		this.latestCompletedTranslations = latestCompletedTranslations;
	}
}
