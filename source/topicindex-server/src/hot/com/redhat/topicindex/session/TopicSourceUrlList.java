package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("topicSourceUrlList")
public class TopicSourceUrlList extends EntityQuery<TopicSourceUrl>
{
	private static final long serialVersionUID = 1252696089440198493L;

	private static final String EJBQL = "select topicSourceUrl from TopicSourceUrl topicSourceUrl";

	private static final String[] RESTRICTIONS = {
			"lower(topicSourceUrl.sourceUrl) like lower(concat(#{topicSourceUrlList.topicSourceUrl.sourceUrl},'%'))",
			"lower(topicSourceUrl.description) like lower(concat(#{topicSourceUrlList.topicSourceUrl.description},'%'))", };

	private TopicSourceUrl topicSourceUrl = new TopicSourceUrl();

	public TopicSourceUrlList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public TopicSourceUrl getTopicSourceUrl() {
		return topicSourceUrl;
	}
}
