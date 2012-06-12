package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.TopicToTopic;

public class TopicToTopicRelatedTopicIDSort implements Comparator<TopicToTopic>
{
	@Override
	public int compare(final TopicToTopic o1, final TopicToTopic o2) 
	{
		final Integer thisID = o1.getRelatedTopic() != null ? o1.getRelatedTopic().getTopicId() : null;
		final Integer otherID = o2.getRelatedTopic() != null ? o2.getRelatedTopic().getTopicId() : null;
		
		if (thisID == null && otherID == null)
			return 0;
		
		if (thisID == null)
			return -1;
		
		if (otherID == null)
			return 1;
		
		return thisID - otherID;  
	}

}
