package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.entity.Project;

public class ProjectIDComparator implements Comparator<Project>
{
	@Override
	public int compare(final Project o1, final Project o2) 
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.getProjectId() == null && o2.getProjectId() == null)
			return 0;
		if (o1.getProjectId() == null)
			return -1;
		if (o2.getProjectId() == null)
			return 1;
		
		return o1.getProjectId().compareTo(o2.getProjectId());
	}

}