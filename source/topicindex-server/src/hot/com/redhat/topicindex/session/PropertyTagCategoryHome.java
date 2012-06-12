package com.redhat.topicindex.session;

import java.util.List;
import java.util.Set;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.structures.tags.UICategoriesData;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("propertyTagCategoryHome")
public class PropertyTagCategoryHome extends EntityHome<PropertyTagCategory>
{
	private static final long serialVersionUID = 7143235180750459258L;
	private UICategoriesData uiCategoriesData = new UICategoriesData();
	
	public UICategoriesData getUiCategoriesData()
	{
		return uiCategoriesData;
	}

	public void setUiCategoriesData(final UICategoriesData uiCategoriesData)
	{
		this.uiCategoriesData = uiCategoriesData;
	}

	public void setPropertyTagCategoryPropertyTagCategoryId(Integer id)
	{
		setId(id);
	}

	public Integer getPropertyTagCategoryPropertyTagCategoryId()
	{
		return (Integer) getId();
	}

	@Override
	protected PropertyTagCategory createInstance()
	{
		final PropertyTagCategory propertytagcategory = new PropertyTagCategory();
		return propertytagcategory;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public void wire()
	{
		getInstance();
		uiCategoriesData.populateTopicTags(this.getInstance());
	}

	public boolean isWired()
	{
		return true;
	}

	public PropertyTagCategory getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}
	
	@Override
	public String update()
	{
		updateTags();
		return super.update();
	}
	
	@Override
	public String persist()
	{
		updateTags();
		return super.persist();
	}

	private void updateTags()
	{
		final Set<PropertyTagToPropertyTagCategory> propertyTagToPropertyTagCategories = this.getInstance().getPropertyTagToPropertyTagCategories();
		final List<PropertyTag> addedTags = uiCategoriesData.getAddedTags(propertyTagToPropertyTagCategories);
		final List<PropertyTag> removedTags = uiCategoriesData.getRemovedTags(propertyTagToPropertyTagCategories);
		
		for (final PropertyTag propertyTag : addedTags)
		{
			this.getInstance().addPropertyTag(propertyTag);
		}
		
		for (final PropertyTag propertyTag : removedTags)
		{
			this.getInstance().removePropertyTag(propertyTag);
		}
	}

}
