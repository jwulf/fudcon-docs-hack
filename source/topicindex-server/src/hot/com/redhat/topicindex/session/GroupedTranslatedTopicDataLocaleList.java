package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.filter.TranslatedTopicDataFilterQueryBuilder;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.GroupedList;

@Name("groupedTranslatedTopicDataLocaleList")
public class GroupedTranslatedTopicDataLocaleList extends GroupedLocaleListBase<GroupedTranslatedTopicDataList>
{
	
	/** A collection of EntityQuerys that make up the contents of the locale tabs */
	final Map<String, GroupedTranslatedTopicDataList> groupedLocales = new HashMap<String, GroupedTranslatedTopicDataList>();
	/** The locales that should be searched on */
	private List<String> locales = new ArrayList<String>();
	/** The first results to show for a query */
	private Integer firstResult = 0;
	/** The order column for the queries */
	private String orderColumn;
	/** The order direction for the queries */
	private String orderDirection;
	/** The query with the most results */
	private TranslatedTopicDataList pagingEntityQuery;
	
	@Create
	public void create()
	{
		final Map<String, String> urlParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		final Filter filter = EntityUtilities.populateFilter(urlParameters, Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG,
				Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC, Constants.MATCH_LOCALE);

		TranslatedTopicDataFilterQueryBuilder filterQueryBuilder = new TranslatedTopicDataFilterQueryBuilder();

		// get a map of variable names to variable values
		filterVars = filter.getUrlVariables();

		// get the heading to display over the list of topics
		searchTagHeading = filter.getFilterTitle();
		
		/*
		 * get a string that can be appended to a url that contains the url
		 * variables
		 */
		urlVars = filter.buildFilterUrlVars();
		
		/* Get the locales to be searched on */
		locales = this.getGroupedLocales();
		if (locales.isEmpty())locales = CommonConstants.LOCALES;
		
		for (String locale: locales) {
			final GroupedTranslatedTopicDataList translatedTopicDataList = new GroupedTranslatedTopicDataList(locale, filterQueryBuilder);
			if (translatedTopicDataList.getResultCount() > 0)
			{
				groupedLocales.put(locale, translatedTopicDataList);
				
				if (pagingEntityQuery == null || pagingEntityQuery.getResultCount() < translatedTopicDataList.getResultCount())
				{
					pagingEntityQuery = translatedTopicDataList.pagingEntityQuery;
				}
			}
		}

		if (getLocales().size() != 0)
			this.localeTab = getLocales().get(0);
		else
			this.localeTab = locales.get(0);
	}
	
	public String doBackToSearchLink()
	{
		return "/TranslatedTopicSearch.seam?" + urlVars;
	}
	
	public List<String> getLocales() {
		List<String> locales = new ArrayList<String>(groupedLocales.keySet());
		Collections.sort(locales);
		return locales;
	}
	
	public GroupedTranslatedTopicDataList getTranslatedTopicDataList(String locale)
	{
		return groupedLocales.containsKey(locale) ? groupedLocales.get(locale) : null;
	}
	
	public List<GroupedList<TranslatedTopicDataList>> getLocaleGroupedLists(String locale)
	{
		return groupedLocales.containsKey(locale) ? groupedLocales.get(locale).getGroupedLists() : new ArrayList<GroupedList<TranslatedTopicDataList>>();
	}
	
	public Integer getFirstResult()
	{
		return firstResult;
	}

	public void setFirstResult(final Integer firstResult)
	{
		this.firstResult = firstResult;
		for (String locale: groupedLocales.keySet())
		{
			groupedLocales.get(locale).setFirstResult(firstResult);
		}
	}
	
	public String getOrderColumn()
	{
		return orderColumn;
	}

	public void setOrderColumn(final String orderColumn)
	{
		this.orderColumn = orderColumn;
		for (String locale: groupedLocales.keySet())
		{
			groupedLocales.get(locale).setOrderColumn(orderColumn);
		}
	}
	
	public String getOrderDirection()
	{
		return orderDirection;
	}

	public void setOrderDirection(final String orderDirection)
	{
		this.orderDirection = orderDirection;
		for (String locale: groupedLocales.keySet())
		{
			groupedLocales.get(locale).setOrderDirection(orderDirection);
		}
	}
	
	public String getSelectedTab()
	{
		final GroupedTranslatedTopicDataList translatedTopicDataList = getTranslatedTopicDataList(localeTab);
		return translatedTopicDataList != null ? translatedTopicDataList.tab : null;
	}
	
	public void setSelectedTab(final String selectedTab)
	{
		final GroupedTranslatedTopicDataList translatedTopicDataList = getTranslatedTopicDataList(localeTab);
		if (translatedTopicDataList != null)
			translatedTopicDataList.tab = selectedTab;
	}
	
	public Long getResultCount()
	{
		return pagingEntityQuery == null ? 0 : pagingEntityQuery.getResultCount();
	}
	
	public boolean isNextExists()
	{
		/* Note: The EntityQuery.isNextExists() fails for an unknown reason so this custom method exists */
		return pagingEntityQuery == null ||	pagingEntityQuery.getMaxResults() == null || pagingEntityQuery.getResultCount() == null ? false : 
				(((pagingEntityQuery.getFirstResult() == null ? 0 : pagingEntityQuery.getFirstResult()) + pagingEntityQuery.getMaxResults()) < pagingEntityQuery.getResultCount());
		//return pagingEntityQuery == null ? false : pagingEntityQuery.isNextExists();
	}
	
	public Integer getNextFirstResult()
	{
		return pagingEntityQuery == null ? 0 : pagingEntityQuery.getNextFirstResult();
	}
	
	public boolean isPreviousExists()
	{
		return pagingEntityQuery == null ? false : pagingEntityQuery.isPreviousExists();
	}
	
	public Integer getPreviousFirstResult()
	{
		return pagingEntityQuery == null ? 0 : pagingEntityQuery.getPreviousFirstResult();
	}
	
	public Long getLastFirstResult()
	{
		return pagingEntityQuery == null ? 0 : pagingEntityQuery.getLastFirstResult();
	}
	
	public void redirectToZanata()
	{
		final GroupedTranslatedTopicDataList translatedTopicDataList = getTranslatedTopicDataList(localeTab);
		translatedTopicDataList.redirectToZanata();
	}
	
	public void pullFromZanata()
	{
		final GroupedTranslatedTopicDataList translatedTopicDataList = getTranslatedTopicDataList(localeTab);
		translatedTopicDataList.pullFromZanata();
	}
}
