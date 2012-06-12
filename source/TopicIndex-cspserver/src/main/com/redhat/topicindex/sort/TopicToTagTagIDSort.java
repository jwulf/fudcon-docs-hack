package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.TopicToTag;

public class TopicToTagTagIDSort implements Comparator<TopicToTag>
{
	@Override
	public int compare(final TopicToTag o1, final TopicToTag o2) 
	{
		final Integer thisTagID = o1.getTag() != null ? o1.getTag().getTagId() : null;
		final Integer otherTagID = o2.getTag() != null ? o2.getTag().getTagId() : null;
		
		if (thisTagID == null && otherTagID == null)
			return 0;
		
		if (thisTagID == null)
			return -1;
		
		if (otherTagID == null)
			return 1;
		
		return thisTagID - otherTagID;  
	}

}