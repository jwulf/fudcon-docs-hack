package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.Topic;

public class TopicIDComparator implements Comparator<Topic>
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
		
		if (o1.getTopicId() == null && o2.getTopicId() == null)
			return 0;
		if (o1.getTopicId() == null)
			return -1;
		if (o2.getTopicId() == null)
			return 1;
		
		return o1.getTopicId().compareTo(o2.getTopicId());
	}

}