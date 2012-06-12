package com.redhat.topicindex.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.PreRemove;
import javax.persistence.Transient;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.ecs.commonstructures.NameIDSortMap;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.sort.TagIDComparator;

@Audited
@Entity
@Table(name = "Project")
public class Project extends AuditedEntity<Project> implements java.io.Serializable
{
	public static final String SELECT_ALL_QUERY = "select project from Project project";
	private static final long serialVersionUID = 7468160102030564523L;
	private Integer projectId;
	private String projectName;
	private String projectDescription;
	private Set<TagToProject> tagToProjects = new HashSet<TagToProject>(0);

	public Project()
	{
		super(Project.class);
	}

	public Project(final String projectName)
	{
		super(Project.class);
		this.projectName = projectName;
	}

	public Project(final String projectName, final String projectDescription, final Set<TagToProject> tagToProjects)
	{
		super(Project.class);
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		this.tagToProjects = tagToProjects;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ProjectID", unique = true, nullable = false)
	public Integer getProjectId()
	{
		return this.projectId;
	}

	public void setProjectId(final Integer projectId)
	{
		this.projectId = projectId;
	}

	@Column(name = "ProjectName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getProjectName()
	{
		return this.projectName;
	}

	public void setProjectName(final String projectName)
	{
		this.projectName = projectName;
	}

	// @Column(name = "ProjectDescription", length = 65535)
	@Column(name = "ProjectDescription", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getProjectDescription()
	{
		return this.projectDescription;
	}

	public void setProjectDescription(final String projectDescription)
	{
		this.projectDescription = projectDescription;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, orphanRemoval = true)
	public Set<TagToProject> getTagToProjects()
	{
		return this.tagToProjects;
	}

	public void setTagToProjects(final Set<TagToProject> tagToProjects)
	{
		this.tagToProjects = tagToProjects;
	}

	@Transient
	public String getTagsList()
	{
		return getTagsList(true);
	}

	/**
	 * Generates a HTML formatted and categorized list of the tags that are
	 * associated with this topic
	 * 
	 * @return A HTML String to display in a table
	 */
	@Transient
	public String getTagsList(final boolean brLineBreak)
	{
		// define the line breaks for html and for tooltips
		final String lineBreak = brLineBreak
				? "<br/>"
				: "\n";
		final String boldStart = brLineBreak
				? "<b>"
				: "";
		final String boldEnd = brLineBreak
				? "</b>"
				: "";

		final TreeMap<NameIDSortMap, ArrayList<String>> tags = new TreeMap<NameIDSortMap, ArrayList<String>>();

		for (final TagToProject tagToProject : this.tagToProjects)
		{
			final Tag tag = tagToProject.getTag();
			final Set<TagToCategory> tagToCategories = tag.getTagToCategories();

			if (tagToCategories.size() == 0)
			{
				NameIDSortMap categoryDetails = new NameIDSortMap("Uncatagorised", -1, 0);

				if (!tags.containsKey(categoryDetails))
					tags.put(categoryDetails, new ArrayList<String>());

				tags.get(categoryDetails).add(tag.getTagName());
			}
			else
			{
				for (final TagToCategory category : tagToCategories)
				{
					NameIDSortMap categoryDetails = new NameIDSortMap(category.getCategory().getCategoryName(), category.getCategory().getCategoryId(), category
							.getCategory().getCategorySort() == null
							? 0
							: category.getCategory().getCategorySort());

					if (!tags.containsKey(categoryDetails))
						tags.put(categoryDetails, new ArrayList<String>());

					tags.get(categoryDetails).add(tag.getTagName());
				}
			}
		}

		String tagsList = "";
		for (final NameIDSortMap key : tags.keySet())
		{
			// sort alphabetically
			Collections.sort(tags.get(key));

			if (tagsList.length() != 0)
				tagsList += lineBreak;

			tagsList += boldStart + key.getName() + boldEnd + ": ";

			String thisTagList = "";

			for (final String tag : tags.get(key))
			{
				if (thisTagList.length() != 0)
					thisTagList += ", ";

				thisTagList += tag;
			}

			tagsList += thisTagList + " ";
		}

		return tagsList;
	}

	@Transient
	public List<Tag> getTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TagToProject tag : this.tagToProjects)
			retValue.add(tag.getTag());

		Collections.sort(retValue, new TagIDComparator());

		return retValue;
	}

	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		for (final TagToProject mapping : tagToProjects)
			mapping.getTag().getTagToProjects().remove(mapping);
		
		this.tagToProjects.clear();
	}
	
	@Transient
	public boolean isRelatedTo(final Tag tag)
	{
		for (final TagToProject mapping : this.getTagToProjects())
			if (mapping.getTag().equals(tag))
				return true;

		return false;
	}
	
	public boolean addRelationshipTo(final Tag tag)
	{
		if (!this.isRelatedTo(tag))
		{
			final TagToProject mapping = new TagToProject(this, tag);
			this.getTagToProjects().add(mapping);
			tag.getTagToProjects().add(mapping);
			return true;
		}

		return false;
	}
	
	public boolean removeRelationshipTo(final Integer relatedTagId)
	{
		for (final TagToProject mapping : this.getTagToProjects())
		{
			final Tag tag = mapping.getTag();

			if (tag.getTagId().equals(relatedTagId))
			{
				/* remove the relationship from this project */
				this.getTagToProjects().remove(mapping);
				
				/* remove the relationship from the tag */
				tag.getTagToProjects().remove(mapping);

				return true;
			}
		}

		return false;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.projectId;
	}
}
