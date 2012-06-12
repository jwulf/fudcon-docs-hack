package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.utils.Constants;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import java.util.Arrays;
import java.util.List;

@Name("topicList")
public class TopicList extends EntityQuery<Topic>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 4132574852922621495L;

	private static final String EJBQL = "select topic from Topic topic";

	private static final String[] RESTRICTIONS =
	{
			// "topic.topicId like concat('%', #{topicList.topic.topicId}, '%')",
			"topic.topicId in (#{topicList.topic.topicIds})", "lower(topic.topicTitle) like lower(concat('%', #{topicList.topic.topicTitle}, '%'))", "lower(topic.topicText) like lower(concat('%', #{topicList.topic.topicText}, '%'))",
			"lower(topic.topicProduct) like lower(concat('%', #{topicList.topic.topicProduct}, '%'))", };

	protected TopicFilter topic = new TopicFilter();

	public TopicList(final Integer limit, final String constructedEJBQL, final TopicFilter topic, final boolean useRestrictions)
	{
		setEjbql(constructedEJBQL);

		if (useRestrictions)
			setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
		/* use the default paging size if no limit is specified */
		if (limit == null)
			setMaxResults(Constants.DEFAULT_PAGING_SIZE);
		/* if the limit is not -1 (or no paging), set the limit */
		else if (limit != -1)
			setMaxResults(limit);
		
		if (topic != null)
			this.topic = topic;
	}

	public TopicList(final int limit, final String constructedEJBQL)
	{
		this(limit, constructedEJBQL, null, true);
	}

	public TopicList(final int limit)
	{
		this(limit, EJBQL, null, true);
	}

	public TopicList()
	{
		this(null, EJBQL, null, true);
	}

	public TopicFilter getTopic()
	{
		return topic;
		
	}
	
	@Override
	public List<Topic> getResultList()
	{
		final List<Topic> retValue = super.getResultList();
		return retValue;
	}
} 
