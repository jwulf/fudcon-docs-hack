package com.redhat.topicindex.session;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("projectHome")
public class ProjectHome extends EntityHome<Project>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -1054902573861759105L;
	private UIProjectsData selectedTags;

	public void setProjectProjectId(final Integer id)
	{
		setId(id);
	}

	public Integer getProjectProjectId()
	{
		return (Integer) getId();
	}

	@Override
	protected Project createInstance()
	{
		Project project = new Project();
		return project;
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

	public Project getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	public List<TagToProject> getTagToProjects()
	{
		return getInstance() == null ? null : new ArrayList<TagToProject>(getInstance().getTagToProjects());
	}

	public void populate()
	{
		selectedTags = new UIProjectsData();
		EntityUtilities.populateProjectTags(this.getInstance(), selectedTags);
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

	private Tag getTagFromId(final Integer tagId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.find(Tag.class, tagId);
		return tag;
	}

	private void updateTags()
	{
		final Project project = this.getInstance();

		if (project != null)
		{
			final ArrayList<Tag> selectedTagObjects = new ArrayList<Tag>();

			for (final UIProjectData myProject : selectedTags.getProjectCategories())
			{
				for (final UICategoryData cat : myProject.getCategories())
				{
					// find the selected tags
					for (final UITagData tagId : cat.getTags())
					{
						// if tag is selected
						if (tagId.isSelected())
							selectedTagObjects.add(getTagFromId(tagId.getId()));
					}
				}
			}

			// match up selected tags with existing tags
			final Set<TagToProject> tagToProjects = project.getTagToProjects();

			// make a note of the tags that were removed
			final ArrayList<Tag> removeTags = new ArrayList<Tag>();
			for (final TagToProject tagToProject : tagToProjects)
			{
				final Tag existingTag = tagToProject.getTag();

				if (!selectedTagObjects.contains(existingTag))
				{
					// add to external collection to avoid modifying a
					// collection while looping over it
					removeTags.add(existingTag);
				}
			}

			// now make a note of the additions
			final ArrayList<Tag> addTags = new ArrayList<Tag>();
			for (final Tag selectedTag : selectedTagObjects)
			{
				if (filter(having(on(TagToProject.class).getTag(), equalTo(selectedTag)), tagToProjects).size() == 0)
				{
					addTags.add(selectedTag);
				}
			}

			// only proceed if there are some changes to make
			if (removeTags.size() != 0 || addTags.size() != 0)
			{
				// remove the tags
				for (final Tag removeTag : removeTags)
				{
					removeTag.removeProjectRelationship(project);
				}

				for (final Tag addTag : addTags)
				{
					addTag.addProjectRelationship(project);
				}
			}
		}
	}

	public UIProjectsData getSelectedTags()
	{
		return selectedTags;
	}

	public void setSelectedTags(final UIProjectsData selectedTags)
	{
		this.selectedTags = selectedTags;
	}
	
    public String getExclusionArray(final Integer id)
	{
		return "[]";
	}
}
