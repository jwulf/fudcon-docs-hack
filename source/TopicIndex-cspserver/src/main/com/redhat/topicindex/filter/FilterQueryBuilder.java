package com.redhat.topicindex.filter;

public interface FilterQueryBuilder
{
	String getMatchTagString(Integer tagId);
	String getNotMatchTagString(Integer tagId);
	void processFilterString(String fieldName, String fieldValue);
	String getFilterString();
	String getSelectAllQuery();
}
