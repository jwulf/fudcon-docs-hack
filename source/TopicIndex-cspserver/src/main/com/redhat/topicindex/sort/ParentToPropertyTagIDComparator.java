package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.base.ToPropertyTag;

public class ParentToPropertyTagIDComparator implements Comparator<ToPropertyTag>
{

	@Override
	public int compare(final ToPropertyTag o1, final ToPropertyTag o2)
	{
		final PropertyTag o1Tag = o1.getPropertyTag() != null ? o1.getPropertyTag(): null;
		final PropertyTag o2Tag = o2.getPropertyTag() != null ? o2.getPropertyTag(): null;
		
		if (o1Tag == null && o2Tag == null)
			return 0;
		
		if (o1Tag == null)
			return -1;
		
		if (o2Tag == null)
			return 1;
		
		return o1Tag.getPropertyTagId().compareTo(o2Tag.getPropertyTagId()); 
	}

}
