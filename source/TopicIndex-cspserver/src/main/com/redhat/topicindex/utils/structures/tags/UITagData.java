package com.redhat.topicindex.utils.structures.tags;

import com.redhat.ecs.commonutils.CollectionUtilities;

/**
	Represents a tag that can be applied to, or used to search for, a topic.
 */
public class UITagData extends UITagDataBase implements Comparable<UITagData>
{
	/** A list of the tags that this tag encompasses, used for tooltips */
	private String childrenList = "";
	/** A list of tags that this tag is part of, used for tooltips */
	private String parentList = "";
	/** A list of property tags assigned to this tag, used for tooltips */
	private String properties;

	public String getProperties()
	{
		return properties;
	}

	public void setProperties(final String properties)
	{
		this.properties = properties;
	}

	public String getChildrenList() 
	{
		return childrenList;
	}

	public void setChildrenList(final String childrenList) 
	{
		this.childrenList = childrenList;
	}

	public String getParentList() 
	{
		return parentList;
	}

	public void setParentList(final String parentList) 
	{
		this.parentList = parentList;
	}

	public UITagData(
		final String name, 
		final String description, 
		final Integer id, 
		final Integer sort, 
		final boolean selected, 
		final boolean notSelected,
		final boolean groupBy,
		final String parentList,
		final String childrenList, 
		final String properties)
	{
		super(name, description, id, sort, selected, notSelected, groupBy);
		this.childrenList = childrenList;
		this.parentList = parentList;
		this.properties = properties;
	}
	
	public UITagData(
		final String name, 
		final String description, 
		final Integer id, 
		final Integer sort, 
		final String parentList,
		final String childrenList)
	{
		super(name, description, id, sort, false, false, false);
		this.childrenList = childrenList;
		this.parentList = parentList;
	}
	
	public UITagData(
		final String name, 
		final Integer id)
	{
		super(name, "", id, 0, false, false, false);
		this.childrenList = "";
		this.parentList = "";
	}
	
	@Override
	public int compareTo(final UITagData o) 
	{
		final Integer sortCompare = CollectionUtilities.getSortOrder(this.sort, o.sort);
		if (sortCompare != null && sortCompare != 0) return sortCompare;
		
		final Integer nameCompare = CollectionUtilities.getSortOrder(this.name, o.name);
		if (nameCompare != null && nameCompare != 0) return nameCompare;
		
		final Integer descriptionCompare = CollectionUtilities.getSortOrder(this.description, o.description);
		if (descriptionCompare != null && descriptionCompare != 0) return descriptionCompare;
		
		final Integer idCompare = CollectionUtilities.getSortOrder(this.id, o.id);
		if (idCompare != null && idCompare != 0) return idCompare;
		
		final Integer selectedCompare = CollectionUtilities.getSortOrder(this.selected, o.selected);
		if (selectedCompare != null && selectedCompare != 0) return selectedCompare;
		
		final Integer notSelectedCompare = CollectionUtilities.getSortOrder(this.notSelected, o.notSelected);
		if (notSelectedCompare != null && notSelectedCompare != 0) return notSelectedCompare;
		
		return 0;			
	}

	public String getToolTip()
	{
		String retValue = "";
		if (this.description != null && !this.description.isEmpty())
			retValue += this.description;
		
		if (!retValue.isEmpty())
			retValue += " \n";
					
		retValue += "Tag ID: " + this.getId();
					
		if (this.parentList != null && !this.parentList.isEmpty())
			retValue += " \nEncompasses: " + parentList;
		if (this.childrenList != null && !this.childrenList.isEmpty())
			retValue += " \nEncompassed By: " + childrenList;
		if (this.properties != null && !this.properties.isEmpty())
			retValue += " \n" + properties;
		return retValue;
	}

}
