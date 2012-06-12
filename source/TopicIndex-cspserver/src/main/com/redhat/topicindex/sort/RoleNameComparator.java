package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.Role;

public class RoleNameComparator implements Comparator<Role>
{
	@Override
	public int compare(final Role o1, final Role o2) 
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.getRoleName() == null && o2.getRoleName() == null)
			return 0;
		if (o1.getRoleName() == null)
			return -1;
		if (o2.getRoleName() == null)
			return 1;
		
		return o1.getRoleName().compareTo(o2.getRoleName());
	}
}
