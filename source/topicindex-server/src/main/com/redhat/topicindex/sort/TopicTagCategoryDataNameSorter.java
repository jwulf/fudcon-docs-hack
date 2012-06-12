package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;

public class TopicTagCategoryDataNameSorter implements Comparator<UICategoryData>
{
	@Override
	public int compare(final UICategoryData o1, final UICategoryData o2) 
	{
		final Integer nameSort = CollectionUtilities.getSortOrder(o1.getName(), o2.getName());
		if (nameSort != null) return nameSort;
		
		final Integer descriptionSort = CollectionUtilities.getSortOrder(o1.getDescription(), o2.getDescription());
		if (descriptionSort != null) return descriptionSort;
		
		final Integer idSort = CollectionUtilities.getSortOrder(o1.getId(), o2.getId());
		if (idSort != null) return idSort;
		
		return 0;
	} 

}
