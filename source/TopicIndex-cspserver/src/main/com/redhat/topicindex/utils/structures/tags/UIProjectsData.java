package com.redhat.topicindex.utils.structures.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterCategory;
import com.redhat.topicindex.entity.FilterTag;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToProject;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class represents the collection of projects that form the top level of
 * the tagging structure, as used by the GUI. These classes differ from the
 * underlying entities in their relationships (e.g. the project and category
 * database entities have no relationship, while the UI tag display does have a
 * direct relationship between projects and categories), and include extra data
 * such as whether a tag/category has been selected or not.
 */
public class UIProjectsData
{
	/**
	 * A collection of UIProjectCategoriesData objects, which represent the
	 * categories that hold tags assigned to a project. A PriorityQueue is used
	 * to get automatic sorting.
	 */
	private List<UIProjectData> projectCategories = new ArrayList<UIProjectData>();

	public void populateTagTags(final Tag tag)
	{
		this.populateTags(tag.getTags(), null, true);
	}
	
	public void populateTagTags(final Category category)
	{
		this.populateTags(category.getTags(), null, true);
	}
	
	public List<UIProjectData> getProjectCategories()
	{
		return projectCategories;
	}

	public void setProjectCategories(final List<UIProjectData> categories)
	{
		this.projectCategories = categories;
	}

	public void clear()
	{
		projectCategories.clear();
	}

	/**
	 * @return A collection of Tags that were selected in the UI
	 */
	public List<Tag> getSelectedTags()
	{
		final List<Tag> selectedTagObjects = new ArrayList<Tag>();

		for (final UIProjectData project : this.getProjectCategories())
		{
			for (final UICategoryData cat : project.getCategories())
			{
				// find the selected tags
				for (final UITagData tagId : cat.getTags())
				{
					// if tag is selected
					if (tagId.isSelected())
						selectedTagObjects.add(EntityUtilities.getTagFromId(tagId.getId()));
				}
			}
		}

		return selectedTagObjects;
	}

	/**
	 * @return A collection of Tags that were selected in the UI, paired with a
	 *         UITagData object
	 */
	public List<Pair<Tag, UITagData>> getExtendedSelectedTags()
	{
		final List<Pair<Tag, UITagData>> selectedTagObjects = new ArrayList<Pair<Tag, UITagData>>();

		for (final UIProjectData project : this.getProjectCategories())
		{
			for (final UICategoryData cat : project.getCategories())
			{
				// find the selected tags
				for (final UITagData tagId : cat.getTags())
				{
					// if tag is selected
					if (tagId.isSelected())
						selectedTagObjects.add(Pair.newPair(EntityUtilities.getTagFromId(tagId.getId()), tagId));
				}
			}
		}

		return selectedTagObjects;
	}

	/**
	 * @param existingTagsExtended
	 *            A collection of the existing TagToCategory objects held by a
	 *            Category
	 * @return A collection of selected Tags paired with the UITagData (that
	 *         includes additional information such as the sorting order) that
	 *         either do not exist in the existingTagsExtended collection, or
	 *         exist in the existingTagsExtended collection with a different
	 *         sorting order
	 */
	public List<Pair<Tag, UITagData>> getAddedOrModifiedTags(final Set<TagToCategory> existingTagsExtended)
	{
		final List<Pair<Tag, UITagData>> selectedTags = getExtendedSelectedTags();

		// now make a note of the additions
		final List<Pair<Tag, UITagData>> addTags = new ArrayList<Pair<Tag, UITagData>>();
		for (final Pair<Tag, UITagData> selectedTagData : selectedTags)
		{
			final Tag selectedTag = selectedTagData.getFirst();
			final Integer sorting = selectedTagData.getSecond().getNewSort();

			/*
			 * Loop over the TagToCategory collection, and see if the tag we
			 * have selected exists with the same sorting order
			 */
			boolean found = false;
			for (final TagToCategory tagToCategory : existingTagsExtended)
			{
				if (tagToCategory.getTag().equals(selectedTag) && CollectionUtilities.isEqual(tagToCategory.getSorting(), sorting))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				addTags.add(selectedTagData);
			}
		}

		return addTags;
	}

	/**
	 * @param existingTags
	 *            A collection of the existing tags
	 * @return A collection of the tags that are were selected in the UI and do
	 *         not exist in the existingTags collection, paired with a UITagData
	 *         object that contains additional information such as sorting oder
	 */
	public List<Pair<Tag, UITagData>> getExtendedAddedTags(final List<Tag> existingTags)
	{
		final List<Pair<Tag, UITagData>> selectedTags = getExtendedSelectedTags();

		// now make a note of the additions
		final List<Pair<Tag, UITagData>> addTags = new ArrayList<Pair<Tag, UITagData>>();
		for (final Pair<Tag, UITagData> selectedTag : selectedTags)
		{
			if (!existingTags.contains(selectedTag))
			{
				addTags.add(selectedTag);
			}
		}

		return addTags;
	}

	/**
	 * @param existingTags
	 *            A collection of the existing tags
	 * @return A collection of the tags that are were selected in the UI and do
	 *         not exist in the existingTags collection
	 */
	public List<Tag> getAddedTags(final List<Tag> existingTags)
	{
		final List<Tag> selectedTags = getSelectedTags();

		// now make a note of the additions
		final ArrayList<Tag> addTags = new ArrayList<Tag>();
		for (final Tag selectedTag : selectedTags)
		{
			if (!existingTags.contains(existingTags))
			{
				addTags.add(selectedTag);
			}
		}

		return addTags;
	}

	/**
	 * @param existingTags
	 *            A collection of the existing tags
	 * @return A collection of the tags that are were not selected in the UI and
	 *         exist in the existingTags collection
	 */
	public List<Tag> getRemovedTags(final List<Tag> existingTags)
	{
		final List<Tag> selectedTags = getSelectedTags();

		// make a note of the tags that were removed
		final List<Tag> removeTags = new ArrayList<Tag>();
		for (final Tag existingTag : existingTags)
		{
			if (!selectedTags.contains(existingTag))
			{
				// add to external collection to avoid modifying a
				// collection while looping over it
				removeTags.add(existingTag);
			}
		}

		return removeTags;
	}

	public void populateTopicTags(final Topic topic)
	{
		populateTags(topic.getTags(), null, true);
	}

	public void populateTopicTags()
	{
		populateTags(null, null, true);
	}

	public void populateTopicTags(final Filter filter)
	{
		populateTags(null, filter, true);
	}

	public void populateTopicTags(final Filter filter, final boolean setSelectedItemInCategory)
	{
		populateTags(null, filter, setSelectedItemInCategory);
	}

	/**
	 * This function is used to populate the data structures used to display
	 * categories and their tags.
	 * 
	 * @param topic
	 *            If this is not null, it is used to determine which tags are
	 *            currently selected
	 * @param selectedTags
	 *            This is a map of Category data to an ArrayList of Tag data.
	 *            When used to represent categories, the GuiInputData selected
	 *            field is used to indicate whether this is a mutually exclusive
	 *            category. If true, only one tag should be able to be selected.
	 *            If false, many tags can be selected. The decision to make a
	 *            category mutually exclusive is left up to a Drools rule file
	 *            in order to keep this code as process agnostic as possible.
	 *            This function will populate the category GuiInputData objects
	 *            (see the setSelectedItemInCategory param) so they can be used
	 *            either way.
	 * @param filter
	 *            This object represents the filter applied to the page. Like
	 *            topic, this is optionally used to preselect those tags that
	 *            are used in the filter. Either filter or topic can be not
	 *            null, but if both are supplied (and this shouldn't happen) the
	 *            topic is used.
	 * @param setSelectedItemInCategory
	 *            If this is false, the selectedID value in the Key of the
	 *            selectedTags parameter will not be modified to indicate which
	 *            of the children tags has been selected. This needs to be set
	 *            to false to avoid changing the equality of the keys in the
	 *            TreeMap as tag selections change.
	 */
	@SuppressWarnings("unchecked")
	public void populateTags(final List<Tag> checkedTags, final Filter filter, final boolean setSelectedItemInCategory)
	{
		try
		{
			/*
			 * this should be empty anyway, but make sure we start with a clean
			 * slate
			 */
			this.clear();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<Project> projectList = entityManager.createQuery(Project.SELECT_ALL_QUERY).getResultList();
			final List<TagToCategory> tagToCategoryList = entityManager.createQuery(TagToCategory.SELECT_ALL_QUERY).getResultList();

			/*
			 * First we create an entry for tags that have not been associated
			 * with a project. These will become common tags.
			 * 
			 * Use a HQL query to find those tags that have no associated
			 * project.
			 */
			final List<Tag> commonTagList = entityManager.createQuery(Tag.SELECT_ALL_QUERY + " where tag.tagToProjects is empty").getResultList();

			/* create a "common" project */
			final UIProjectData commonProjectDetails = new UIProjectData(Constants.COMMON_PROJECT_NAME, Constants.COMMON_PROJECT_DESCRIPTION, Constants.COMMON_PROJECT_ID);
			this.getProjectCategories().add(commonProjectDetails);

			/* get a list of the categories that the common tags fall into */
			final List<Category> commonCategories = new ArrayList<Category>();
			for (final Tag tag : commonTagList)
			{
				for (final TagToCategory tagToCategory : tag.getTagToCategories())
				{
					final Category category = tagToCategory.getCategory();
					if (!commonCategories.contains(category))
						commonCategories.add(category);
				}
			}

			/* create the categories under the common project */
			for (final Category category : commonCategories)
			{
				final Integer catID = category.getCategoryId();

				/* the object that represents the category */
				final UICategoryData commonCatDetails = createUICategoryData(category, null, filter);
				commonProjectDetails.getCategories().add(commonCatDetails);

				/*
				 * create an entry for all the tags that have no assigned
				 * project
				 */
				for (final TagToCategory tagToCategory : category.getTagToCategories())
				{
					final Tag tag = tagToCategory.getTag();
					if (tag.getTagToProjects().size() == 0)
					{
						final UITagData tagData = createUITagData(tag, catID, filter, checkedTags, tagToCategoryList);

						/*
						 * set the selected id in the category to the last
						 * selected tag this is used by the xhtml page when a
						 * category is marked as mutually exclusive, and ignored
						 * otherwise
						 */
						if (tagData.isSelected() && setSelectedItemInCategory)
							commonCatDetails.setSelectedTag(tag.getTagId());

						commonCatDetails.getTags().add(tagData);
					}
				}
			}

			/*
			 * This is a three step process: [1] we find the tags assigned to a
			 * product. [2] we find the categories assigned to the tags found in
			 * step 1 [3] we loop over the categories found in step 2, which we
			 * know contain tags assigned to the product, and pull out the tags
			 * that are associated with the product we are looking at.
			 * 
			 * This seems a little redundant, but it is necessary because there
			 * is no direct relationship between a category and a project - a
			 * category is only listed in a project if the tags contained in a
			 * category are assigned to a project.
			 */

			// loop through the projects
			for (final Project project : projectList)
			{
				// create a project
				final UIProjectData projectDetails = new UIProjectData(project.getProjectName(), project.getProjectDescription(), project.getProjectId());
				this.getProjectCategories().add(projectDetails);

				/*
				 * Step 1. find the tags assigned to a product.
				 */
				final Set<TagToProject> tags = project.getTagToProjects();

				/*
				 * Step 2: find the categories that the tags assigned to this
				 * product exist in
				 */
				final List<Category> projectCategories = new ArrayList<Category>();
				for (final TagToProject tagToProject : tags)
				{
					final Tag tag = tagToProject.getTag();
					final Set<TagToCategory> categories = tag.getTagToCategories();

					for (final TagToCategory tagToCategory : categories)
					{
						final Category category = tagToCategory.getCategory();
						if (!projectCategories.contains(category))
							projectCategories.add(category);
					}
				}

				/*
				 * Step 3: loop over the categories found in step 2, which we
				 * know contain tags assigned to the product, and pull out the
				 * tags that are associated with the product we are looking at
				 */
				for (final Category category : projectCategories)
				{
					final Integer catID = category.getCategoryId();

					// the key that represents the category
					final UICategoryData catDetails = createUICategoryData(category, project, filter);
					projectDetails.getCategories().add(catDetails);

					// sync category logic states with the filter
					if (filter != null)
					{
						final ArrayList<Integer> categoryStates = filter.hasCategory(category.getCategoryId());

						// override the default "or" state if the filter has
						// saved an "and" state
						if (categoryStates.contains(Constants.CATEGORY_INTERNAL_AND_STATE))
						{
							catDetails.setInternalLogic(Constants.AND_LOGIC);
						}

						// override the default external "and" state if the
						// filter has saved an "o" state
						if (categoryStates.contains(Constants.CATEGORY_EXTERNAL_OR_STATE))
						{
							catDetails.setExternalLogic(Constants.OR_LOGIC);
						}
					}

					// sync with the UI data object
					final Set<TagToCategory> tagsInCategory = category.getTagToCategories();
					for (final TagToCategory tagToCategory : tagsInCategory)
					{
						final Tag tag = tagToCategory.getTag();
						if (tag.isInProject(project))
						{
							final UITagData tagData = createUITagData(tag, catID, filter, checkedTags, tagToCategoryList);

							/*
							 * set the selected id in the category to the last
							 * selected tag this is used by the xhtml page when
							 * a category is marked as mutually exclusive, and
							 * ignored otherwise
							 */
							if (tagData.isSelected() && setSelectedItemInCategory)
								catDetails.setSelectedTag(tag.getTagId());

							catDetails.getTags().add(tagData);
						}
					}
				}
			}

			/* The final step is to order the collections */
			for (final UIProjectData project : this.getProjectCategories())
			{
				Collections.sort(project.getCategories());
				for (final UICategoryData category : project.getCategories())
				{
					Collections.sort(category.getTags());
				}

			}

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "A catch all Exception");
		}
	}

	static private UICategoryData createUICategoryData(final Category category, final Project project, final Filter filter)
	{
		final String catName = category.getCategoryName();
		final String catDesc = category.getCategoryDescription();
		final Integer catID = category.getCategoryId();
		final Integer catSort = category.getCategorySort();

		final UICategoryData retValue = new UICategoryData(catName, catDesc, catID, catSort == null ? 0 : catSort, false);

		/* sync the category logic with the filter */

		if (filter != null)
		{
			/*
			 * loop over all the categories, looking for a match with the
			 * project and category used to create the UI data
			 */
			for (final FilterCategory filterCategory : filter.getFilterCategories())
			{
				/*
				 * we have a match if a null project was supplied and a null
				 * project was found or the supplied project matches the filter
				 * project and the categories match.
				 * CollectionUtilities.isEqual() handles equality between null
				 * objects.
				 */

				final boolean projectsMatch = CollectionUtilities.isEqual(project, filterCategory.getProject());
				final boolean categoriesMatch = filterCategory.getCategory().equals(category);

				if (projectsMatch && categoriesMatch)
				{
					if (filterCategory.getCategoryState() == Constants.CATEGORY_INTERNAL_AND_STATE)
						retValue.setInternalLogic(Constants.AND_LOGIC);
					else if (filterCategory.getCategoryState() == Constants.CATEGORY_INTERNAL_OR_STATE)
						retValue.setInternalLogic(Constants.OR_LOGIC);
					else if (filterCategory.getCategoryState() == Constants.CATEGORY_EXTERNAL_AND_STATE)
						retValue.setExternalLogic(Constants.AND_LOGIC);
					if (filterCategory.getCategoryState() == Constants.CATEGORY_EXTERNAL_OR_STATE)
						retValue.setExternalLogic(Constants.OR_LOGIC);
				}
			}
		}

		return retValue;
	}

	@SuppressWarnings("javadoc")
	static private UITagData createUITagData(final Tag tag, final Integer catID, final Filter filter, final List<Tag> checkedTags, final List<TagToCategory> tagToCategoryList)
	{
		final Integer tagId = tag.getTagId();
		final String tagName = tag.getTagName();
		final String tagDescription = tag.getTagDescription();
		final String tagChildrenList = tag.getChildrenList();
		final String tagParentList = tag.getParentList();
		final String tagProperties = tag.getProperties();

		/*
		 * find out if this tag is already selected by the topic ...
		 */
		boolean selected = false;
		boolean selectedNot = false;
		boolean groupBy = false;

		if (checkedTags != null)
		{
			selected = checkedTags.contains(tag);
		}
		// ... or by the filter
		else
		{
			if (filter != null)
			{
				final List<Integer> tagStates = filter.hasTag(tagId);

				for (final Integer tagState : tagStates)
				{
					if (tagState == Constants.NOT_MATCH_TAG_STATE)
						selected = selectedNot = true;
					else if (tagState == Constants.MATCH_TAG_STATE)
						selected = true;
					else if (tagState == Constants.GROUP_TAG_STATE)
						groupBy = true;
				}
			}
		}

		// find the sorting order
		Integer sorting = null;
		for (final TagToCategory tagToCategory : tagToCategoryList)
		{
			if (tagToCategory.getCategory().getCategoryId() == catID && tagToCategory.getTag().getTagId() == tagId)
				sorting = tagToCategory.getSorting();
		}

		final UITagData retValue = new UITagData(tagName, tagDescription, tagId, sorting == null ? 0 : sorting, selected, selectedNot, groupBy, tagParentList, tagChildrenList, tagProperties);
		return retValue;
	}

	public void loadTagCheckboxes(final Filter filter)
	{
		// sync the Filter with the gui checkboxes
		for (final UIProjectData project : this.getProjectCategories())
		{
			for (final UICategoryData category : project.getCategories())
			{
				for (final UITagData tag : category.getTags())
				{
					boolean found = false;
					for (final FilterTag filterTag : filter.getFilterTags())
					{
						if (tag.getId().equals(filterTag.getTag().getTagId()))
						{
							final int tagState = filterTag.getTagState();
							
							if (tagState == Constants.MATCH_TAG_STATE)		
							{
								tag.setSelected(true);
							}
							else if (tagState == Constants.NOT_MATCH_TAG_STATE)
							{
								tag.setSelected(true);
								tag.setNotSelected(true);
							}
							else if (tagState == Constants.GROUP_TAG_STATE)
							{
								tag.setGroupBy(true);
							}
							
							found = true;
							break;
						}
					}

					if (!found)
					{
						tag.setSelected(false);
						tag.setNotSelected(false);
					}
				}
			}
		}
	}

	public void loadCategoryLogic(final Filter filter)
	{
		// sync the category logic states
		for (final UIProjectData project : this.getProjectCategories())
		{
			for (final UICategoryData category : project.getCategories())
			{
				for (final FilterCategory filterCategory : filter.getFilterCategories())
				{
					final Integer categoryId = filterCategory.getCategory().getCategoryId();

					if (categoryId.equals(category.getId()))
					{
						final int catgeoryState = filterCategory.getCategoryState();

						if (catgeoryState == Constants.CATEGORY_INTERNAL_AND_STATE)
							category.setInternalLogic(Constants.AND_LOGIC);
						if (catgeoryState == Constants.CATEGORY_INTERNAL_OR_STATE)
							category.setInternalLogic(Constants.OR_LOGIC);

						if (catgeoryState == Constants.CATEGORY_EXTERNAL_AND_STATE)
							category.setExternalLogic(Constants.AND_LOGIC);
						if (catgeoryState == Constants.CATEGORY_EXTERNAL_OR_STATE)
							category.setExternalLogic(Constants.OR_LOGIC);
					}
				}

			}
		}
	}
}
