package com.redhat.topicindex.session;

import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;
import com.redhat.topicindex.utils.structures.tags.UITagData;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("categoryHome")
public class CategoryHome extends EntityHome<Category> implements DisplayMessageInterface
{
	private static final long serialVersionUID = 6943432713479412363L;

	private UIProjectsData selectedTags;
	/** The message to be displayed to the user */
	private String displayMessage;

	public void setCategoryCategoryId(Integer id)
	{
		setId(id);
	}

	public Integer getCategoryCategoryId()
	{
		return (Integer) getId();
	}

	@Override
	protected Category createInstance()
	{
		Category category = new Category();
		return category;
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
	}

	public boolean isWired()
	{
		return true;
	}

	public Category getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	public void populate()
	{
		selectedTags = new UIProjectsData();
		selectedTags.populateTagTags(this.getInstance());
		EntityUtilities.populateTagTagsSortingForCategory(this.getInstance(), selectedTags);
	}

	public UIProjectsData getSelectedTags()
	{
		return selectedTags;
	}

	public void setSelectedTags(UIProjectsData selectedTags)
	{
		this.selectedTags = selectedTags;
	}
	
	public String getExclusionArray(final Integer id)
	{
		return "[]";
	}
	
	private void updateTags()
	{
		final Category category = this.getInstance();

		if (category != null)
		{
			final List<Tag> existingTags = category.getTags();
			final Set<TagToCategory> existingTagsExtended = category.getTagToCategories();

			// make a note of the tags that were removed
			final List<Tag> removeTags = selectedTags.getRemovedTags(existingTags);
			// make a note of the tahs that were added
			final List<Pair<Tag, UITagData>> addTags = selectedTags.getAddedOrModifiedTags(existingTagsExtended);
			

			// only proceed if there are some changes to make
			if (removeTags.size() != 0 || addTags.size() != 0)
			{
				// remove the tags
				for (final Tag removeTag : removeTags)
				{
					category.removeTagRelationship(removeTag);
				}

				for (final Pair<Tag, UITagData> addTag : addTags)
				{
					category.addTagRelationship(addTag.getFirst(), addTag.getSecond().getNewSort());
				}
			}
		}
	}
	
	@Override
	public String persist()
	{
		try
		{
			updateTags();
			return super.persist();
		}
		catch (final PersistenceException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");	
			if (ex.getCause() instanceof ConstraintViolationException)
				this.displayMessage = "The category requires a unique name";
			else
				this.displayMessage = "The category could not be saved";
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error saving the entity");
			this.displayMessage = "The category could not be saved";
		}
		
		return null;
	}
	
	@Override
	public String update()
	{
		try
		{
			updateTags();
			return super.update();
		}
		catch (final PersistenceException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");	
			if (ex.getCause() instanceof ConstraintViolationException)
				this.displayMessage = "The category requires a unique name";
			else
				this.displayMessage = "The category could not be saved";
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error saving the entity");
			this.displayMessage = "The category could not be saved";
		}
		
		
		return null;
	}

	@Override
	public String getDisplayMessage()
	{
		return displayMessage;
	}
	
	@Override
	public String getDisplayMessageAndClear()
	{
		final String retValue = this.displayMessage;
		this.displayMessage = null;
		return retValue;
	}
}
