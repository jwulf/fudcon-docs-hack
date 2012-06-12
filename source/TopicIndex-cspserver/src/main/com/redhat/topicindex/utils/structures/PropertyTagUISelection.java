package com.redhat.topicindex.utils.structures;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.PropertyTagCategory;
import com.redhat.topicindex.entity.PropertyTagToPropertyTagCategory;
import com.redhat.topicindex.utils.Constants;

public class PropertyTagUISelection
{
	/** A list of the available PropertyTags */
	private List<SelectItem> properties;
	
	public List<SelectItem> getProperties()
	{
		return properties;
	}

	public PropertyTagUISelection(final EntityManager entityManager)
	{
		properties = new ArrayList<SelectItem>();
		@SuppressWarnings("unchecked")
		final List<PropertyTag> propertyTags = entityManager.createQuery(PropertyTag.SELECT_ALL_QUERY).getResultList();
		@SuppressWarnings("unchecked")
		final List<PropertyTagCategory> propertyTagCategories = entityManager.createQuery(PropertyTagCategory.SELECT_ALL_QUERY).getResultList();

		/* get the uncategorised tags */
		final List<SelectItem> uncategorisedPropertyTags = new ArrayList<SelectItem>();
		for (final PropertyTag propertyTag : propertyTags)
		{
			if (propertyTag.getPropertyTagToPropertyTagCategories().size() == 0)
				uncategorisedPropertyTags.add(new SelectItem(Constants.PROPERTY_TAG_SELECT_ITEM_VALUE_PREFIX + propertyTag.getPropertyTagId(), Constants.PROPERTY_TAG_SELECT_LABEL_PREFIX + propertyTag.getPropertyTagName()));
		}

		if (uncategorisedPropertyTags.size() != 0)
		{
			properties.add(new SelectItem(Constants.UNCATEGORISED_PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE, Constants.UNCATEGORISED_PROPERTY_TAG_CATEGORY_SELECT_ITEM_LABEL));
			properties.addAll(uncategorisedPropertyTags);
		}

		/* add the categorised tags */
		for (final PropertyTagCategory propertyTagCategory : propertyTagCategories)
		{
			if (propertyTagCategory.getPropertyTagToPropertyTagCategories().size() != 0)
			{
				properties.add(new SelectItem(Constants.PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE_PREFIX + propertyTagCategory.getPropertyTagCategoryId(), propertyTagCategory.getPropertyTagCategoryName()));

				for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : propertyTagCategory.getPropertyTagToPropertyTagCategories())
				{
					final PropertyTag propertyTag = propertyTagToPropertyTagCategory.getPropertyTag();
					properties.add(new SelectItem(Constants.PROPERTY_TAG_SELECT_ITEM_VALUE_PREFIX + propertyTag.getPropertyTagId(), Constants.PROPERTY_TAG_SELECT_LABEL_PREFIX + propertyTag.getPropertyTagName()));
				}
			}
		}
	}
}
