package com.redhat.topicindex.session;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.TranslatedTopicData;

import com.redhat.topicindex.filter.FilterQueryBuilder;
import com.redhat.topicindex.filter.TranslatedTopicDataFilterQueryBuilder;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

@Name("translatedTopicDataList")
public class TranslatedTopicDataList extends EntityQuery<TranslatedTopicData>
{
	private static final long serialVersionUID = 4265050133041173177L;
	/**
	 * A predefined query that overrides the URL parameters
	 */
	private String constructedEJBQL;

	public TranslatedTopicDataList()
	{
		construct(Constants.DEFAULT_ENVERS_PAGING_SIZE, null, new TranslatedTopicDataFilterQueryBuilder());
	}

	public TranslatedTopicDataList(final int limit)
	{
		construct(limit, null, new TranslatedTopicDataFilterQueryBuilder());
	}

	public TranslatedTopicDataList(final int limit, final String constructedEJBQL)
	{
		construct(limit, constructedEJBQL, null);
	}
	
	protected void construct(int limit, final String constructedEJBQL, final FilterQueryBuilder filterQueryBuilder)
	{
		if (constructedEJBQL == null)
		{
			// initialize filter home
			final Filter filter = EntityUtilities.populateFilter(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC, Constants.MATCH_LOCALE);

			/*
			 * the filter may be null if an invalid variable was passed in the
			 * URL
			 */
			if (filter != null)
			{
				// add the and and or categories clause to the default statement
				this.constructedEJBQL = filter.buildQuery(filterQueryBuilder);
			}
			else
			{
				this.constructedEJBQL = TranslatedTopicData.SELECT_ALL_QUERY;
			}
		}
		else
		{
			this.constructedEJBQL = constructedEJBQL;
		}

		if (limit != -1)
			setMaxResults(limit);

		setEjbql(this.constructedEJBQL);
	}
}
