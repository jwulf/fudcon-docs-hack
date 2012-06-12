package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.User;

public class UserNameComparator implements Comparator<User>
{
	@Override
	public int compare(final User o1, final User o2) 
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.getUserName() == null && o2.getUserName() == null)
			return 0;
		if (o1.getUserName() == null)
			return -1;
		if (o2.getUserName() == null)
			return 1;
		
		return o1.getUserName().compareTo(o2.getUserName());
	}
}
