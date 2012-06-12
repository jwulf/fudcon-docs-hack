package com.redhat.topicindex.utils.structures;

public class GroupedList<T>
{
	/** The tag that is used to group this list of results under a tab in the UI */
	private String group;
	private T entityList;

	public T getEntityList()
	{
		return entityList;
	}

	public void setEntityList(final T entityList)
	{
		this.entityList = entityList;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(final String group)
	{
		this.group = group;
	}
}
