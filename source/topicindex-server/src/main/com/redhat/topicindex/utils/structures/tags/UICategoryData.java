package com.redhat.topicindex.utils.structures.tags;

import java.util.ArrayList;
import java.util.List;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.utils.Constants;

/**
	This class represents a category of tags that can be applied to a topic 
 */
public class UICategoryData extends UITagDataBase implements Comparable<UICategoryData>
{
	/** A collection of tags that belong to this category */
	private List<UITagData> tags = new ArrayList<UITagData>();
	/** true if this category is mutually exclusive 9i.e. only one tag can be selected */
	private boolean mutuallyExclusive = false;
	/** The ID of the selected tag, or null if no tag is selected */
	private Integer selectedTag = null;
	/** The internal logic applied to the tags within a category */
	private String internalLogic;
	/** The external logic applied to categories */
	private String externalLogic;
	
	public List<UITagData> getTags() 
	{
		return tags;
	}

	public void setTags(final List<UITagData> tags) 
	{
		this.tags = tags;
	}

	public boolean isMutuallyExclusive() 
	{
		return mutuallyExclusive;
	}

	public void setMutuallyExclusive(final boolean mutuallyExclusive) 
	{
		this.mutuallyExclusive = mutuallyExclusive;
	}

	public Integer getSelectedTag() 
	{
		return selectedTag;
	}

	public void setSelectedTag(final Integer selectedTag) 
	{
		this.selectedTag = selectedTag;
	}
	
	public UICategoryData(
		final String name, 
		final String description,
		final Integer id, 
		final Integer sort,
		final boolean selected, 
		final boolean notSelected,
		final boolean groupBy,
		final boolean mutuallyExclusive)
	{
		super(name, description, id, sort, selected, notSelected, groupBy);
		this.mutuallyExclusive = mutuallyExclusive;
	}
	
	public UICategoryData(
			final String name, 
			final String description,
			final Integer id, 
			final Integer sort,
			final boolean mutuallyExclusive)
		{
			super(name, description, id, sort, false, false, false);
			this.mutuallyExclusive = mutuallyExclusive;
			this.internalLogic = Constants.OR_LOGIC;
			this.externalLogic = Constants.AND_LOGIC;
		}
	
	@Override
	public int compareTo(final UICategoryData o) 
	{
		final Integer sortCompare = CollectionUtilities.getSortOrder(this.sort, o.sort);
		if (sortCompare != null && sortCompare != 0) return sortCompare;
		
		final Integer nameCompare = CollectionUtilities.getSortOrder(this.name, o.name);
		if (nameCompare != null && nameCompare != 0) return nameCompare;
		
		final Integer descriptionCompare = CollectionUtilities.getSortOrder(this.description, o.description);
		if (descriptionCompare != null && descriptionCompare != 0) return descriptionCompare;
		
		final Integer idCompare = CollectionUtilities.getSortOrder(this.id, o.id);
		if (idCompare != null && idCompare != 0) return idCompare;
		
		final Integer mutuallyExclusiveCompare = CollectionUtilities.getSortOrder(this.mutuallyExclusive, o.mutuallyExclusive);
		if (mutuallyExclusiveCompare != null && mutuallyExclusiveCompare != 0) return mutuallyExclusiveCompare;
		
		final Integer selectedTagCompare = CollectionUtilities.getSortOrder(this.selectedTag, o.selectedTag);
		if (selectedTagCompare != null && selectedTagCompare != 0) return selectedTagCompare;
		
		return 0;			
	}

	public String getInternalLogic()
	{
		return internalLogic;
	}

	public void setInternalLogic(String internalLogic)
	{
		this.internalLogic = internalLogic;
	}

	public String getExternalLogic()
	{
		return externalLogic;
	}

	public void setExternalLogic(String externalLogic)
	{
		this.externalLogic = externalLogic;
	}
	
	public void addTag(final UITagData uiTagData)
	{
		this.tags.add(uiTagData);
	}
}


