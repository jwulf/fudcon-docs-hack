package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.Topic;

public class TopicRelativePriorityComparator implements Comparator<Topic>
{
	@Override
	public int compare(final Topic o1, final Topic o2) 
	{
		final Integer otherRelativePriority = o2.getTempRelativePriority();
		
		if (o1.getTempRelativePriority() == null && otherRelativePriority == null)
			return 0;
		
		if (o1.getTempRelativePriority() == null)
			return -1;
		
		if (otherRelativePriority == null)
			return 1;
		
		return o1.getTempRelativePriority() - otherRelativePriority;  
	}

}