package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.utils.structures.GroupedList;

public class GroupedTopicListNameComparator<T> implements Comparator<GroupedList<T>>
{
	@Override
	public int compare(final GroupedList<T> o1, final GroupedList<T> o2)
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.getGroup() == null && o2.getGroup() == null)
			return 0;
		if (o1.getGroup() == null)
			return -1;
		if (o2.getGroup() == null)
			return 1;
		
		return o1.getGroup().compareTo(o2.getGroup());
	}
}
