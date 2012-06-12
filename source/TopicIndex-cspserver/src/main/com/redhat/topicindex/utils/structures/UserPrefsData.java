package com.redhat.topicindex.utils.structures;

/**
 * This class holds details for user preferences
 */
public class UserPrefsData
{
	/** The filter used for the home link on the top of the screen */
	protected Integer homeFilter;

	public void setHomeFilter(final Integer homeFilter)
	{
		this.homeFilter = homeFilter;
	}

	public Integer getHomeFilter()
	{
		return homeFilter;
	}

}
