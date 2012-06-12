package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.utils.Constants;

@SuppressWarnings("rawtypes")
public class GroupedLocaleListBase<T extends GroupedListBase>
{
	/** Provides the heading that identifies the currently selected filter tags */
	protected String searchTagHeading;
	/** A query that returns all of the elements displayed by all of the tabs. Used for group operations */
	protected String getAllQuery;
	/** holds the URL variables that define a filters options */
	protected String urlVars;
	/**
	 * The query used to find the entities in the list is set in the constructor
	 * using setEjbql. Because we modify this query based on the tags in the
	 * url, the url needs to have all the variables for the tags and categories.
	 * To ensure that the url always has these variables, we parse them out and
	 * save them in the filterVars collection, which is then read with a jstl
	 * foreach tag to place the required params into any link (like next,
	 * previous, first page, last page) that may require a new instance of this
	 * object to be constructed.
	 */
	protected HashMap<String, String> filterVars;
	/** The selected locale tab */
	protected String localeTab;
	
	public String getLocaleTab() {
		return localeTab;
	}

	public void setLocaleTab(String localeTab) {
		this.localeTab = localeTab;
	}
	
	public HashMap<String, String> getFilterVars()
	{
		return filterVars;
	}

	public void setFilterVars(final HashMap<String, String> filterVars)
	{
		this.filterVars = filterVars;
	}
	
	public void setUrlVars(final String urlVars)
	{
		this.urlVars = urlVars;
	}

	public String getUrlVars()
	{
		return urlVars;
	}
	
	public String getSearchTagHeading()
	{
		return searchTagHeading;
	}

	public void setSearchTagHeading(final String value)
	{
		searchTagHeading = value;
	}
	
	protected List<String> getGroupedLocales()
	{
		final List<String> matchedLocales = new ArrayList<String>();
		final List<String> matchedNotLocales = new ArrayList<String>();
		
		/* Get the locales from the url parameters  in the filter */
		for (final String urlParam : filterVars.keySet())
		{
			if (urlParam.matches(Constants.MATCH_LOCALE + "\\d+"))
			{
				final String urlParamValue = filterVars.get(urlParam);
				
				final String localeName = urlParamValue.replaceAll("\\d", "");
				final Integer state = Integer.parseInt(urlParamValue.replaceAll("[^\\d]", ""));
				
				if (state == Constants.MATCH_TAG_STATE)
					matchedLocales.add(localeName);
				else if (state == Constants.NOT_MATCH_TAG_STATE)
					matchedNotLocales.add(localeName);
			}
		}
		
		/* 
		 * If no matched locales were specified then use all the locales 
		 * and remove the "not" locales. 
		 */
		if (matchedLocales.isEmpty())
		{
			for (final String locale : CommonConstants.LOCALES)
			{
				if (!matchedNotLocales.contains(locale))
					matchedLocales.add(locale);
			}
		}
		
		return matchedLocales;
	}
}
