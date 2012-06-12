package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.framework.EntityQuery;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.structures.GroupedList;

public class GroupedListBase<T extends EntityQuery>
{
	/** Points to the EntityQuery that will be used for the paging operations (i.e. the one with the most item) */
	protected T pagingEntityQuery;
	/** A collection of EntityQuerys that make up the contents of the tabs */
	protected List<GroupedList<T>> groupedLists = new ArrayList<GroupedList<T>>();
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
	protected String filterURLVars;
	/** Provides the heading that identifies the currently selected filter tags */
	protected String searchTagHeading;
	/** The selected tab */
	protected String tab;
	
	public String getSearchTagHeading()
	{
		return searchTagHeading;
	}

	public void setSearchTagHeading(final String value)
	{
		searchTagHeading = value;
	}
	
	public String getTab()
	{
		return tab;
	}

	public void setTab(final String tab)
	{
		this.tab = tab;
	}
	
	public void setUrlVars(final String urlVars)
	{
		this.urlVars = urlVars;
	}

	public String getUrlVars()
	{
		return urlVars;
	}

	public HashMap<String, String> getFilterVars()
	{
		return filterVars;
	}

	public void setFilterVars(final HashMap<String, String> filterVars)
	{
		this.filterVars = filterVars;
	}

	public List<GroupedList<T>> getGroupedLists()
	{
		return groupedLists;
	}

	public void setGroupedLists(List<GroupedList<T>> groupedLists)
	{
		this.groupedLists = groupedLists;
	}
	
	public boolean isNextExists()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.isNextExists();
		return false;
	}

	public boolean isPreviousExists()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.isPreviousExists();
		return false;
	}

	public Long getLastFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getLastFirstResult();
		return 0l;
	}

	public int getNextFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getNextFirstResult();
		return 0;
	}

	public int getPreviousFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getPreviousFirstResult();
		return 0;
	}

	public Integer getFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getFirstResult();
		return 0;
	}

	public void setFirstResult(final Integer firstResult)
	{
		for (final GroupedList<T> groupedTopicList : groupedLists)
			groupedTopicList.getEntityList().setFirstResult(firstResult);
	}

	public List<T> getResultList()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getResultList();
		return null;
	}

	public boolean isPaginated()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.isPaginated();
		return false;
	}

	public Long getResultCount()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getResultCount();
		return 0l;
	}

	public void setOrderColumn(final String orderColumn)
	{
		if (pagingEntityQuery != null)
			pagingEntityQuery.setOrderColumn(orderColumn);
	}

	public String getOrderColumn()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getOrderColumn();
		return null;
	}

	public void setOrderDirection(final String orderDirection)
	{
		if (pagingEntityQuery != null)
			pagingEntityQuery.setOrderDirection(orderDirection);
	}

	public String getOrderDirection()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getOrderDirection();
		return null;
	}
	
	protected List<String> getGroupedLocales()
	{
		final Map<String, String> urlParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		for (final String urlParam : urlParameters.keySet())
		{
			if (urlParam.equals(Constants.GROUP_LOCALE))
			{
				final String urlParamValue = urlParameters.get(Constants.GROUP_LOCALE);
				return CollectionUtilities.toArrayList(urlParamValue.split(","));
			}
		}
		
		return new ArrayList<String>();
	}
	
	protected List<Tag> getGroupedTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		final Map<String, String> urlParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		for (final String urlParam : urlParameters.keySet())
		{
			if (urlParam.startsWith(Constants.GROUP_TAG))
			{
				try
				{
					final Integer tagID = Integer.parseInt(urlParam.replace(Constants.GROUP_TAG, ""));
					final Integer state = Integer.parseInt(urlParameters.get(urlParam));
					if (state == Constants.GROUP_TAG_STATE)
					{
						final Tag tag = EntityUtilities.getTagFromId(tagID);
						if (tag != null)
						{
							retValue.add(tag);
						}
					}
				}
				catch (final Exception ex)
				{
					SkynetExceptionUtilities.handleException(ex, true, "Probably a bad url parameter passed as a grouping tag");
				}
			}
		}
		
		return retValue;
	}
}
