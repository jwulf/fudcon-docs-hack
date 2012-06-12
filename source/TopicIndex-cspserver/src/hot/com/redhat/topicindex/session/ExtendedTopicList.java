package com.redhat.topicindex.session;

import javax.faces.context.FacesContext;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.filter.TopicFilterQueryBuilder;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This is a base class extended by other classes that need to display a list of
 * topics with an extended filter set.
 */
public class ExtendedTopicList extends TopicList
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -4553573868560054166L;

	protected String constructedEJBQL;

	public ExtendedTopicList()
	{
		this(null, null, null);
	}

	public ExtendedTopicList(final int limit)
	{
		this(limit, null, null);
	}

	public ExtendedTopicList(final Integer limit, final String constructedEJBQL)
	{
		this(limit, constructedEJBQL, null);
	}

	public ExtendedTopicList(final Integer limit, final String constructedEJBQL, final TopicFilter topic)
	{
		super(limit, constructedEJBQL == null ? constructEJBQL() : constructedEJBQL, topic, false);
	}

	private static String constructEJBQL()
	{
		// initialize filter home
		final Filter filter = EntityUtilities.populateFilter(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

		/*
		 * the filter may be null if an invalid variable was passed in the
		 * URL
		 */
		if (filter != null)
			// add the "and" and or "categories" clause to the default statement
			return filter.buildQuery(new TopicFilterQueryBuilder());
		
		return Topic.SELECT_ALL_QUERY;
	}

}
