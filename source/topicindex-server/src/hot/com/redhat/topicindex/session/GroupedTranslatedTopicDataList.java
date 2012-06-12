package com.redhat.topicindex.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TranslatedTopicData;
import com.redhat.topicindex.filter.TranslatedTopicDataFilterQueryBuilder;
import com.redhat.topicindex.sort.GroupedTopicListNameComparator;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.structures.GroupedList;
import com.redhat.topicindex.zanata.ZanataPullTopicThread;

@Name("groupedTranslatedTopicDataList")
public class GroupedTranslatedTopicDataList extends GroupedListBase<TranslatedTopicDataList>
{
	
	private String locale;
	
	private final TranslatedTopicDataFilterQueryBuilder filterQueryBuilder;
	
	public GroupedTranslatedTopicDataList()
	{
		filterQueryBuilder = new TranslatedTopicDataFilterQueryBuilder();
	}
	
	public GroupedTranslatedTopicDataList(final String locale, final TranslatedTopicDataFilterQueryBuilder filterQueryBuilder)
	{
		this.locale = locale;
		this.filterQueryBuilder = filterQueryBuilder;
		create();
	}
	
	@Create
	public void create()
	{
		final Map<String, String> urlParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		final Filter filter = EntityUtilities.populateFilter(urlParameters, Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG,
				Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC, Constants.MATCH_LOCALE);

		getAllQuery = filter.buildQuery(filterQueryBuilder);
		
		/* add this locale to the query */
		getAllQuery += (getAllQuery.toLowerCase().indexOf("where".toLowerCase()) != -1
				? " AND "
				: " WHERE ") + "translatedTopicData.translationLocale='" + locale + "'";

		// get a map of variable names to variable values
		filterVars = filter.getUrlVariables();

		// get the heading to display over the list of topics
		searchTagHeading = filter.getFilterTitle();

		/*
		 * get a string that can be appended to a url that contains the url
		 * variables
		 */
		urlVars = filter.buildFilterUrlVars();

		final List<Integer> groupedTags = buildTagGroups(filterQueryBuilder);

		buildUngroupedResults(filterQueryBuilder, groupedTags);

		if (groupedTags.size() != 0)
			this.tab = groupedLists.get(0).getGroup();
		else
			this.tab = Constants.UNGROUPED_RESULTS_TAB_NAME;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	private List<Integer> buildTagGroups(final TranslatedTopicDataFilterQueryBuilder filterQueryBuilder)
	{
		/* The tags included in the URL query parameters */
		final List<Tag> urlGroupedTags = getGroupedTags();
		/* The tag IDs that have some Topics tagged against them */
		final List<Integer> groupedTags = new ArrayList<Integer>();
		
		for (final Tag tag : urlGroupedTags)
		{

			/*
			 * Find those TranslatedTopic entities that reference an historical instance
			 * of a Topic that is tagged with the group tag
			 */

			final String groupedTagQuery = filterQueryBuilder.getMatchTagString(tag.getTagId());

			/* Get all the TranslatedTopicDataList */
			String query = getAllQuery;
			
			/* Add the tag search part of the query */
			if (!groupedTagQuery.isEmpty())
			{
				query += (query.toLowerCase().indexOf("where".toLowerCase()) != -1
				? " AND "
				: " WHERE ") + groupedTagQuery;
			}

			TranslatedTopicDataList translatedTopicDataList = new TranslatedTopicDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);

			if (translatedTopicDataList.getResultCount() != 0)
			{
				final GroupedList<TranslatedTopicDataList> groupedList = new GroupedList<TranslatedTopicDataList>();
				groupedList.setGroup(tag.getTagName());
				groupedList.setEntityList(translatedTopicDataList);

				groupedLists.add(groupedList);

				if (pagingEntityQuery == null || pagingEntityQuery.getResultCount() < translatedTopicDataList.getResultCount())
					pagingEntityQuery = translatedTopicDataList;

				groupedTags.add(tag.getTagId());
			}
		
			/* sort by tag name, and then add the unsorted topics on the end */
			Collections.sort(groupedLists, new GroupedTopicListNameComparator<TranslatedTopicDataList>());
		}
		
		return groupedTags;
	}
	
	private void buildUngroupedResults(final TranslatedTopicDataFilterQueryBuilder filterQueryBuilder, final List<Integer> groupedTags) {
		/*
		 * we didn't have any groups, so just find all the matching topics and dump
		 * them in the default group
		 */
		if (groupedTags.size() == 0)
		{

			/* Get all the TranslatedTopicDataList */
			final String query = getAllQuery;

			TranslatedTopicDataList translatedTopicDataList = new TranslatedTopicDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);
	
			if (translatedTopicDataList.getResultCount() != 0) {
				final GroupedList<TranslatedTopicDataList> groupedTopicList = new GroupedList<TranslatedTopicDataList>();
				groupedTopicList.setGroup(Constants.UNGROUPED_RESULTS_TAB_NAME);
				groupedTopicList.setEntityList(translatedTopicDataList);
		
				groupedLists.add(groupedTopicList);
				
				pagingEntityQuery = translatedTopicDataList;
			}
		}
		/*
		 * Find that topics that are part of the query, but couldn't be matched in
		 * any group
		 */
		else
		{
			String notQuery = "";
			for (final Integer tagID : groupedTags)
			{
				/*
				 * Find those TranslatedTopic entities that reference an historical
				 * instance of a Topic that is not tagged with the group tag
				 */
				final String notMatchGroupTagQuery = filterQueryBuilder.getNotMatchTagString(tagID);

				if (notQuery.length() != 0)
					notQuery += " AND ";
				notQuery += notMatchGroupTagQuery;
			}

			final String query = getAllQuery + (notQuery.isEmpty() ? "" : ((getAllQuery.toLowerCase().indexOf("where".toLowerCase()) != -1
					? " AND "
					: " WHERE ") + notQuery));

			TranslatedTopicDataList translatedTopicDataList = new TranslatedTopicDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);

			if (translatedTopicDataList.getResultCount() != 0)
			{
				final GroupedList<TranslatedTopicDataList> groupedTopicList = new GroupedList<TranslatedTopicDataList>();
				groupedTopicList.setGroup(Constants.UNGROUPED_RESULTS_TAB_NAME);
				groupedTopicList.setEntityList(translatedTopicDataList);

				groupedLists.add(groupedTopicList);
				

				if (pagingEntityQuery == null || pagingEntityQuery.getMaxResults() < translatedTopicDataList.getMaxResults())
					pagingEntityQuery = translatedTopicDataList;
			}
		}
	}
	
	public void redirectToZanata()
	{
		/* Get the zanata properties from the server properties */
		String zanataServerUrl = System.getProperty(CommonConstants.ZANATA_SERVER_PROPERTY);
		String zanataProject = System.getProperty(CommonConstants.ZANATA_PROJECT_PROPERTY);
		String zanataVersion = System.getProperty(CommonConstants.ZANATA_PROJECT_VERSION_PROPERTY);
		
		/* Create the zanata url for the translated topic and locale */
		String link = zanataServerUrl + "/webtrans/Application.html?project=" + zanataProject + "&iteration=" + zanataVersion + "&localeId=" + locale;
		
		/* Loop through and add the Zanata ID's to be displayed */
		for (GroupedList<TranslatedTopicDataList> groupedList: groupedLists) {
			if (groupedList.getGroup().equals(tab)) {
				for (Object o: groupedList.getEntityList().getResultList()) {
					TranslatedTopicData translatedTopicDataList = (TranslatedTopicData) o;
					link += "&doc=" + translatedTopicDataList.getTranslatedTopic().getZanataId();
				}
			}
		}
		
		/* Redirect the user to the zanata Url */
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		try 
		{
			externalContext.redirect(link);
			FacesContext.getCurrentInstance().responseComplete();
			return;
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void pullFromZanata()
	{
		/* Loop through and add the Topic ID's to be pulled from zanata */
		List<Integer> topicIds = new ArrayList<Integer>();
		for (GroupedList<TranslatedTopicDataList> groupedList: groupedLists) {
			if (groupedList.getGroup().equals(tab)) {
				for (Object o: groupedList.getEntityList().getResultList()) {
					TranslatedTopicData translatedTopicData = (TranslatedTopicData) o;
					topicIds.add(translatedTopicData.getTranslatedTopic().getTopicId());
				}
			}
		}
		
		if (topicIds.isEmpty()) return;
		
		try
		{
			final ZanataPullTopicThread zanataPullTopicThread = new ZanataPullTopicThread(topicIds);
			final Thread thread = new Thread(zanataPullTopicThread);
			thread.start();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Catch all exception handler");
		}
	}
	
	public String doBackToSearchLink()
	{
		return "/TranslatedTopicSearch.seam?" + urlVars;
	}
}
