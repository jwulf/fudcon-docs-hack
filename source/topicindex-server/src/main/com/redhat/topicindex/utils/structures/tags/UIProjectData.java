package com.redhat.topicindex.utils.structures.tags;

import java.util.ArrayList;
import java.util.List;
import com.redhat.ecs.commonutils.CollectionUtilities;

/**
 * This class represents the collection of categories with tags that can be
 * selected and "not selected" (i.e. a tag that specifically set to not be
 * present in a collection).
 */
public class UIProjectData extends UITagDataBase implements Comparable<UIProjectData>
{
	/**
	 * The categories that hold tags assigned to this project.
	 */
	private List<UICategoryData> categories = new ArrayList<UICategoryData>();

	public List<UICategoryData> getCategories()
	{
		return categories;
	}

	public void setCategories(final List<UICategoryData> categories)
	{
		this.categories = categories;
	}

	public UIProjectData(final String name, final String description, final Integer id)
	{
		super(name, description, id, 0, false, false, false);
	}

	public void clear()
	{
		categories.clear();
	}

	@Override
	public int compareTo(final UIProjectData other)
	{
		if (other == null)
			return 1;
		
		final Integer nameSort = CollectionUtilities.getSortOrder(this.name, other.name);
		if (nameSort != null && nameSort != 0) return nameSort;
		
		return 0;
	}
}
