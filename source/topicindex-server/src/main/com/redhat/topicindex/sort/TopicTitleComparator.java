package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.Topic;

public class TopicTitleComparator implements Comparator<Topic>
{
	@Override
	public int compare(final Topic o1, final Topic o2)
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.getTopicTitle() == null && o2.getTopicTitle() == null)
			return 0;
		if (o1.getTopicTitle() == null)
			return -1;
		if (o2.getTopicTitle() == null)
			return 1;
		
		return o1.getTopicTitle().compareTo(o2.getTopicTitle());
	}
}
