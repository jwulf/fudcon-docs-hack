package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.FilterQueryBuilder;
import com.redhat.topicindex.filter.SnapshotTranslatedDataFilterQueryBuilder;
import com.redhat.topicindex.sort.GroupedTopicListNameComparator;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.GroupedList;

@Name("groupedSnapshotTranslatedDataList")
public class GroupedSnapshotTranslatedDataList extends GroupedListBase<SnapshotTranslatedDataListBase>
{
	private boolean snapshotRevision = false;
	
	public boolean isSnapshotRevision()
	{
		return snapshotRevision;
	}

	public void setSnapshotRevision(final boolean snapshotRevision)
	{
		this.snapshotRevision = snapshotRevision;
	}
	
	@Create
	public void create()
	{
		System.out.println("GroupedSnapshotTranslatedDataList.create() START");

		/*
		 * Get a collection of the Envers Topics that match those referenced by the
		 * SnapshotTopic entities
		 */
		final Map<SnapshotTopic, Topic> topicMap = EntityUtilities.getHistoricalTopicsFromSnapshotTopics();

		final Map<String, String> urlParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		final Filter filter = EntityUtilities.populateFilter(urlParameters, Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG,
				Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

		/*
		 * GroupedSnapshotTranslatedDataList is in an unusual position of having to
		 * display lists of two different types of entities: SnapshotTranslatedData
		 * and WorkingSnapshotTranslatedData.
		 * 
		 * The SnapshotTranslatedData and WorkingSnapshotTranslatedData both inherit
		 * SnapshotTranslatedDataBase, which provides a consistent interface for
		 * access their common fields, which means the EL code in the XHTML file can
		 * be assured that the property names are consistent.
		 * 
		 * The WorkingSnapshotTranslatedDataList and SnapshotTranslatedDataListBase
		 * classes extend SnapshotTranslatedDataListBase. This allows
		 * GroupedSnapshotTranslatedDataList to maintain collections of
		 * GroupedSnapshotTranslatedDataList objects, and populate them with either
		 * WorkingSnapshotTranslatedDataList or SnapshotTranslatedDataListBase
		 * objects depending on whether a snapshotRev query parameter is present.
		 * 
		 * If snapshotRev is present, we need to get a list of
		 * SnapshotTranslatedData entities. Otherwise we need to get
		 * WorkingSnapshotTranslatedData entities.
		 */
		snapshotRevision = urlParameters.containsKey(Constants.SNAPSHOT_REVISION_ID);

		SnapshotTranslatedDataFilterQueryBuilder filterQueryBuilder = null;
		if (snapshotRevision)
		{
			filterQueryBuilder = new SnapshotTranslatedDataFilterQueryBuilder(true);
		}
		else
		{
			filterQueryBuilder = new SnapshotTranslatedDataFilterQueryBuilder(false);
		}

		getAllQuery = filter.buildQuery(filterQueryBuilder);

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
		final List<String> groupedLocales = buildLocaleGroups(filterQueryBuilder);

		/* sort by tag name, and then add the unsorted topics on the end */
		Collections.sort(groupedLists, new GroupedTopicListNameComparator<SnapshotTranslatedDataListBase>());

		/*
		 * we didn't have any groups, so just find all the matching topics and dump
		 * them in the default group
		 */
		if (groupedTags.size() == 0 && groupedLocales.size() == 0)
		{
			System.out.println("Processing All SnapshotTranslatedData Entities");

			SnapshotTranslatedDataListBase snapshotTranslatedDataList = null;
			if (snapshotRevision)
				snapshotTranslatedDataList = new SnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, getAllQuery);
			else
				snapshotTranslatedDataList = new WorkingSnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, getAllQuery);

			final GroupedList<SnapshotTranslatedDataListBase> groupedTopicList = new GroupedList<SnapshotTranslatedDataListBase>();
			groupedTopicList.setGroup(Constants.UNGROUPED_RESULTS_TAB_NAME);
			groupedTopicList.setEntityList(snapshotTranslatedDataList);

			groupedLists.add(groupedTopicList);
			pagingEntityQuery = snapshotTranslatedDataList;
		}
		/*
		 * Find that topics that are part of the query, but couldn't be matched in
		 * any group
		 */
		else
		{
			System.out.println("Processing Ungrouped SnapshotTranslatedData Entities");

			String notQuery = "";
			for (final Integer tagID : groupedTags)
			{
				/*
				 * Find those SnapshotTopic entities that reference an historical
				 * instance of a Topic that is not tagged with the group tag
				 */
				final String notMatchGroupTagQuery = filterQueryBuilder.getNotMatchTagString(tagID);

				if (notQuery.length() != 0)
					notQuery += " AND ";
				notQuery += notMatchGroupTagQuery;
			}
			
			for (final String locale : groupedLocales)
			{
				/*
				 * Find those SnapshotTopic entities that reference an historical
				 * instance of a Topic that is not part of the locale
				 */
				final String matchLocaleQuery = filterQueryBuilder.getMatchingLocalString(locale);

				if (notQuery.length() != 0)
					notQuery += " AND ";
				notQuery += "NOT " + matchLocaleQuery;
			}

			final String query = getAllQuery + (getAllQuery.toLowerCase().indexOf("where".toLowerCase()) != -1
					? " AND "
					: " WHERE ") + notQuery;

			SnapshotTranslatedDataListBase snapshotTranslatedDataList = null;
			if (snapshotRevision)
				snapshotTranslatedDataList = new SnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);
			else
				snapshotTranslatedDataList = new WorkingSnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);

			if (snapshotTranslatedDataList.getResultCount() != 0)
			{
				final GroupedList<SnapshotTranslatedDataListBase> groupedTopicList = new GroupedList<SnapshotTranslatedDataListBase>();
				groupedTopicList.setGroup(Constants.UNGROUPED_RESULTS_TAB_NAME);
				groupedTopicList.setEntityList(snapshotTranslatedDataList);

				groupedLists.add(groupedTopicList);

				if (pagingEntityQuery == null || pagingEntityQuery.getMaxResults() < snapshotTranslatedDataList.getMaxResults())
					pagingEntityQuery = snapshotTranslatedDataList;
			}
		}

		if (groupedLists.size() != 0)
			this.tab = groupedLists.get(0).getGroup();

		System.out.println("GroupedSnapshotTranslatedDataList.create() FINISH");
	}

	public void populate()
	{
		for (final GroupedList<SnapshotTranslatedDataListBase> tab : this.groupedLists)
			tab.getEntityList().populate();
	}
	
	private List<Integer> buildTagGroups(final FilterQueryBuilder filterQueryBuilder)
	{
		/* The tags included in the URL query parameters */
		final List<Tag> urlGroupedTags = getGroupedTags();
		/* The tag IDs that have some Topics tagged against them */
		final List<Integer> groupedTags = new ArrayList<Integer>();

		for (final Tag tag : urlGroupedTags)
		{
			System.out.println("Processing Group Tag " + tag.getTagName());

			/*
			 * Find those SnapshotTopic entities that reference an historical instance
			 * of a Topic that is tagged with the group tag
			 */

			final String groupedTagQuery = filterQueryBuilder.getMatchTagString(tag.getTagId());

			/* Get all the SnapshotTranslatedDataList */
			final String query = getAllQuery + (getAllQuery.toLowerCase().indexOf("where".toLowerCase()) != -1
					? " AND "
					: " WHERE ") + groupedTagQuery;

			SnapshotTranslatedDataListBase snapshotTranslatedDataList = null;
			if (snapshotRevision)
				snapshotTranslatedDataList = new SnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);
			else
				snapshotTranslatedDataList = new WorkingSnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);

			if (snapshotTranslatedDataList.getResultCount() != 0)
			{
				final GroupedList<SnapshotTranslatedDataListBase> groupedList = new GroupedList<SnapshotTranslatedDataListBase>();
				groupedList.setGroup(tag.getTagName());
				groupedList.setEntityList(snapshotTranslatedDataList);

				groupedLists.add(groupedList);

				if (pagingEntityQuery == null || pagingEntityQuery.getResultCount() < snapshotTranslatedDataList.getResultCount())
					pagingEntityQuery = snapshotTranslatedDataList;

				groupedTags.add(tag.getTagId());
			}
		}
		
		return groupedTags;
	}
	
	private List<String> buildLocaleGroups(final SnapshotTranslatedDataFilterQueryBuilder filterQueryBuilder)
	{
		/* The tags included in the URL query parameters */
		final List<String> urlGroupedLocales = getGroupedLocales();
		/* The tag IDs that have some Topics tagged against them */
		final List<String> groupedLocales = new ArrayList<String>();

		for (final String locale : urlGroupedLocales)
		{
			/*
			 * Find those SnapshotTopic entities that reference an historical instance
			 * of a Topic that has the group locale
			 */
			final String groupedQuery = filterQueryBuilder.getMatchingLocalString(locale);

			/* Get all the SnapshotTranslatedDataList */
			final String query = getAllQuery + (getAllQuery.toLowerCase().indexOf("where".toLowerCase()) != -1
					? " AND "
					: " WHERE ") + groupedQuery;

			SnapshotTranslatedDataListBase snapshotTranslatedDataList = null;
			if (snapshotRevision)
				snapshotTranslatedDataList = new SnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);
			else
				snapshotTranslatedDataList = new WorkingSnapshotTranslatedDataList(Constants.DEFAULT_ENVERS_PAGING_SIZE, query);

			if (snapshotTranslatedDataList.getResultCount() != 0)
			{
				final GroupedList<SnapshotTranslatedDataListBase> groupedList = new GroupedList<SnapshotTranslatedDataListBase>();
				groupedList.setGroup(locale);
				groupedList.setEntityList(snapshotTranslatedDataList);

				groupedLists.add(groupedList);

				if (pagingEntityQuery == null || pagingEntityQuery.getResultCount() < snapshotTranslatedDataList.getResultCount())
					pagingEntityQuery = snapshotTranslatedDataList;

				groupedLocales.add(locale);
			}
		}
		
		return groupedLocales;
	}
}
