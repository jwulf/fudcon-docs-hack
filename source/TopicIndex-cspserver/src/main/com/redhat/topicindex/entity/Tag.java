package com.redhat.topicindex.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.UniqueConstraint;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hamcrest.Matchers.equalTo;

import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.entity.base.ParentToPropertyTag;
import com.redhat.topicindex.entity.base.ToPropertyTag;
import com.redhat.topicindex.sort.CategoryIDComparator;
import com.redhat.topicindex.sort.ProjectIDComparator;
import com.redhat.topicindex.sort.TagIDComparator;

@Entity
@Audited
@Table(name = "Tag", uniqueConstraints = @UniqueConstraint(columnNames = { "TagName" }))
public class Tag extends ParentToPropertyTag<Tag> implements java.io.Serializable
{
	public static final String SELECT_ALL_QUERY = "select tag from Tag tag";
	private static final long serialVersionUID = 2841080567638275194L;
	private Integer tagId;
	private String tagName;
	private String tagDescription;
	private Set<TopicToTag> topicToTags = new HashSet<TopicToTag>(0);
	private Set<Tag> excludedTags = new HashSet<Tag>(0);
	private Set<TagToCategory> tagToCategories = new HashSet<TagToCategory>(0);
	private Set<TagToTag> childrenTagToTags = new HashSet<TagToTag>(0);
	private Set<TagToTag> parentTagToTags = new HashSet<TagToTag>(0);
	private Set<TagToProject> tagToProjects = new HashSet<TagToProject>(0);
	private Set<TagToPropertyTag> tagToPropertyTags = new HashSet<TagToPropertyTag>(0);

	@Override
	@Transient
	public Integer getId()
	{
		return this.tagId;
	}

	public Tag()
	{
		super(Tag.class);
	}

	public Tag(String tagName)
	{
		super(Tag.class);
		this.tagName = tagName;
	}

	public Tag(final String tagName, final String tagDescription, final Set<TopicToTag> topicToTags, final Set<TagToCategory> tagToCategories)
	{
		super(Tag.class);
		this.tagName = tagName;
		this.tagDescription = tagDescription;
		this.topicToTags = topicToTags;
		this.tagToCategories = tagToCategories;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToProject> getTagToProjects()
	{
		return this.tagToProjects;
	}

	public void setTagToProjects(final Set<TagToProject> tagToProjects)
	{
		this.tagToProjects = tagToProjects;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "secondaryTag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToTag> getParentTagToTags()
	{
		return this.parentTagToTags;
	}

	public void setParentTagToTags(final Set<TagToTag> parentTagToTags)
	{
		this.parentTagToTags = parentTagToTags;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "primaryTag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToTag> getChildrenTagToTags()
	{
		return this.childrenTagToTags;
	}

	public void setChildrenTagToTags(final Set<TagToTag> childrenTagToTags)
	{
		this.childrenTagToTags = childrenTagToTags;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToCategory> getTagToCategories()
	{
		return this.tagToCategories;
	}

	public void setTagToCategories(final Set<TagToCategory> tagToCategories)
	{
		this.tagToCategories = tagToCategories;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TagID", unique = true, nullable = false)
	public Integer getTagId()
	{
		return this.tagId;
	}

	public void setTagId(Integer tagId)
	{
		this.tagId = tagId;
	}

	@Column(name = "TagName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getTagName()
	{
		return this.tagName;
	}

	public void setTagName(String tagName)
	{
		this.tagName = tagName;
	}

	// @Column(name = "TagDescription", length = 65535)
	@Column(name = "TagDescription", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTagDescription()
	{
		return this.tagDescription;
	}

	public void setTagDescription(String tagDescription)
	{
		this.tagDescription = tagDescription;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToTag> getTopicToTags()
	{
		return this.topicToTags;
	}

	public void setTopicToTags(final Set<TopicToTag> topicToTags)
	{
		this.topicToTags = topicToTags;
	}

	@Transient
	public String getCategoriesList()
	{
		String categoriesList = "";
		for (final TagToCategory category : this.getTagToCategories())
		{
			if (categoriesList.length() != 0)
				categoriesList += " ";
			categoriesList += category.getCategory().getCategoryName();
		}
		return categoriesList;
	}

	@Transient
	public List<Category> getCategories()
	{
		final List<Category> retValue = new ArrayList<Category>();
		for (final TagToCategory category : this.getTagToCategories())
			retValue.add(category.getCategory());

		Collections.sort(retValue, new CategoryIDComparator());

		return retValue;
	}
	
	@Transient
	public List<Project> getProjects()
	{
		final List<Project> retValue = new ArrayList<Project>();
		for (final TagToProject mapping : this.tagToProjects)
			retValue.add(mapping.getProject());

		Collections.sort(retValue, new ProjectIDComparator());

		return retValue;
	}
	
	@Transient
	public String getProjectsList()
	{
		String retValue = "";
		for (final TagToProject mapping : this.tagToProjects)
		{
			if (retValue.length() != 0)
				retValue += " ";
			retValue += mapping.getProject().getProjectName();
		}
		return retValue;
	}

	@Transient
	public List<Tag> getParentTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TagToTag mapping : this.getParentTagToTags())
			retValue.add(mapping.getPrimaryTag());

		Collections.sort(retValue, new TagIDComparator());

		return retValue;
	}

	@Transient
	public List<Tag> getChildTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TagToTag mapping : this.getParentTagToTags())
			retValue.add(mapping.getSecondaryTag());

		Collections.sort(retValue, new TagIDComparator());

		return retValue;
	}

	@Transient
	public String getChildrenList()
	{
		String retValue = "";
		for (final TagToTag tag : this.getChildrenTagToTags())
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += tag.getSecondaryTag().getTagName();
		}
		return retValue;
	}

	@Transient
	public String getParentList()
	{
		String retValue = "";
		for (final TagToTag tag : this.getParentTagToTags())
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += tag.getPrimaryTag().getTagName();
		}
		return retValue;
	}

	@Transient
	public ArrayList<Integer> getCategoriesIDList()
	{
		ArrayList<Integer> categoriesList = new ArrayList<Integer>();
		for (final TagToCategory category : this.getTagToCategories())
			categoriesList.add(category.getCategory().getCategoryId());
		return categoriesList;
	}

	@Transient
	public ArrayList<Integer> getExclusionTagIDs()
	{
		ArrayList<Integer> tagList = new ArrayList<Integer>();
		for (final Tag tag : this.getExcludedTags())
			tagList.add(tag.getTagId());
		return tagList;
	}

	@Transient
	public boolean isInCategory(final Integer categoryId)
	{
		for (final TagToCategory category : this.getTagToCategories())
			if (categoryId.equals(category.getCategory().getCategoryId()))
				return true;

		return false;
	}

	@Transient
	public boolean isInCategory(final Category category)
	{
		for (final TagToCategory myCategory : this.getTagToCategories())
			if (myCategory.getCategory().equals(category))
				return true;

		return false;
	}

	@Transient
	public TagToCategory getCategory(final Integer categoryId)
	{
		for (final TagToCategory category : this.getTagToCategories())
			if (categoryId.equals(category.getCategory().getCategoryId()))
				return category;

		return null;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "TagExclusion", joinColumns = { @JoinColumn(name = "Tag1ID", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "Tag2ID", nullable = false, updatable = false) })
	public Set<Tag> getExcludedTags()
	{
		return this.excludedTags;
	}

	public void setExcludedTags(Set<Tag> excludedTags)
	{
		this.excludedTags = excludedTags;
	}

	/**
	 * @return Returns the list of revision numbers for this entity, as
	 *         maintained by Envers
	 */
	@Transient
	public List<Number> getRevisions()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);
		final List<Number> retValue = reader.getRevisions(Tag.class, this.tagId);
		Collections.sort(retValue, Collections.reverseOrder());
		return retValue;
	}

	/*
	 * @Override public int hashCode() { int result; result = (tagId != null ?
	 * tagId.hashCode() : 0); return result; }
	 */

	@PrePersist
	protected void onCreate()
	{
		validate();
	}

	@PreUpdate
	protected void onUpdate()
	{
		validate();
	}

	protected void validate()
	{
		// a tag can be a parent, or be a child, but not both
		if (this.getChildrenTagToTags().size() != 0)
			this.getParentTagToTags().clear();
	}

	public boolean removeTagRelationship(final Tag childTag)
	{
		final List<TagToTag> children = filter(having(on(TagToTag.class).getSecondaryTag(), equalTo(childTag)), this.getChildrenTagToTags());
		for (final TagToTag child : children)
		{
			this.getChildrenTagToTags().remove(child);
			childTag.getParentTagToTags().remove(child);
		}

		return children.size() != 0;
	}

	public boolean addTagRelationship(final Tag childTag)
	{
		final List<TagToTag> children = filter(having(on(TagToTag.class).getSecondaryTag(), equalTo(childTag)), this.getChildrenTagToTags());
		if (children.size() == 0)
		{
			final TagToTag tagToTag = new TagToTag(new TagToTagRelationship(1), this, childTag);
			this.getChildrenTagToTags().add(tagToTag);
			childTag.getParentTagToTags().add(tagToTag);
			return true;
		}

		return false;
	}

	public boolean removeProjectRelationship(final Project project)
	{
		final List<TagToProject> children = filter(having(on(TagToProject.class).getProject(), equalTo(project)), this.getTagToProjects());
		for (final TagToProject child : children)
		{
			this.getTagToProjects().remove(child);
			child.getProject().getTagToProjects().remove(child);
		}

		return children.size() != 0;
	}

	public boolean addProjectRelationship(final Project project)
	{
		final List<TagToProject> children = filter(having(on(TagToProject.class).getProject(), equalTo(project)), this.getTagToProjects());
		if (children.size() == 0)
		{
			final TagToProject tagToProject = new TagToProject(project, this);
			this.getTagToProjects().add(tagToProject);
			project.getTagToProjects().add(tagToProject);
			return true;
		}

		return false;
	}

	@Transient
	/**
	 * @param project Is the project to test the tags association with. A null project 
	 * 		test that there is no association with any project, or in other words, to test 
	 * 		if this is a common tag
	 * @return true if this tag has been assigned to the project, and false otherwise
	 */
	public boolean isInProject(final Project project)
	{
		if (this.tagToProjects.size() == 0 && project == null)
			return true;

		for (final TagToProject tagToProject : this.tagToProjects)
		{
			if (tagToProject.getProject().equals(project))
				return true;
		}

		return false;
	}

	@Transient
	public List<Tag> getTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TagToTag tag : this.childrenTagToTags)
			retValue.add(tag.getSecondaryTag());

		return retValue;
	}

	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		for (final TopicToTag topicToTag : topicToTags)
			topicToTag.getTopic().getTopicToTags().remove(topicToTag);
		
		for (final Tag tag : excludedTags)
			tag.getExcludedTags().remove(this);
		
		for (final TagToCategory tagToCategory : tagToCategories)
			tagToCategory.getCategory().getTagToCategories().remove(tagToCategory);
		
		for (final TagToProject tagToProject : tagToProjects)
			tagToProject.getProject().getTagToProjects().remove(tagToProject);
		
		for (final TagToPropertyTag tagToPropertyTag : tagToPropertyTags)
			tagToPropertyTag.getPropertyTag().getTagToPropertyTags().remove(tagToPropertyTag);
		
		for (final TagToTag tagToTag : childrenTagToTags)
			tagToTag.getSecondaryTag().getParentTagToTags().remove(tagToTag);
		
		for (final TagToTag tagToTag : parentTagToTags)
			tagToTag.getPrimaryTag().getChildrenTagToTags().remove(tagToTag);

		topicToTags.clear();
		excludedTags.clear();
		tagToCategories.clear();
		tagToProjects.clear();
		tagToPropertyTags.clear();
		childrenTagToTags.clear();
		parentTagToTags.clear();
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToPropertyTag> getTagToPropertyTags()
	{
		return tagToPropertyTags;
	}

	public void setTagToPropertyTags(Set<TagToPropertyTag> tagToPropertyTags)
	{
		this.tagToPropertyTags = tagToPropertyTags;
	}

	public void removePropertyTag(final TagToPropertyTag tagToPropertyTag)
	{
		this.tagToPropertyTags.remove(tagToPropertyTag);
		tagToPropertyTag.getPropertyTag().getTagToPropertyTags().remove(tagToPropertyTag);
	}

	public void removePropertyTag(final PropertyTag propertyTag, final String value)
	{
		final List<TagToPropertyTag> removeList = new ArrayList<TagToPropertyTag>();

		for (final TagToPropertyTag mapping : this.tagToPropertyTags)
		{
			final PropertyTag myPropertyTag = mapping.getPropertyTag();
			if (myPropertyTag.equals(propertyTag) && mapping.getValue().equals(value))
			{
				removeList.add(mapping);
			}
		}

		for (final TagToPropertyTag mapping : removeList)
		{
			this.tagToPropertyTags.remove(mapping);
			mapping.getPropertyTag().getTagToPropertyTags().remove(mapping);
		}
	}

	public void addPropertyTag(final TagToPropertyTag tagToPropertyTag)
	{
		this.tagToPropertyTags.add(tagToPropertyTag);
		tagToPropertyTag.getPropertyTag().getTagToPropertyTags().add(tagToPropertyTag);
	}

	public void addPropertyTag(final PropertyTag propertyTag, final String value)
	{
		final TagToPropertyTag mapping = new TagToPropertyTag();
		mapping.setTag(this);
		mapping.setPropertyTag(propertyTag);
		mapping.setValue(value);

		this.tagToPropertyTags.add(mapping);
		propertyTag.getTagToPropertyTags().add(mapping);
	}

	@Override
	@Transient
	protected Set<? extends ToPropertyTag<TagToPropertyTag>> getPropertyTags()
	{
		return this.tagToPropertyTags;
	}

	@Transient
	public List<PropertyTag> getPropertyTagsArray()
	{
		final List<PropertyTag> retValue = new ArrayList<PropertyTag>();
		for (final TagToPropertyTag mapping : this.tagToPropertyTags)
		{
			final PropertyTag entity = mapping.getPropertyTag();
			retValue.add(entity);
		}

		return retValue;
	}

	@Transient
	public List<TagToPropertyTag> getTagToPropertyTagsArray()
	{
		return CollectionUtilities.toArrayList(tagToPropertyTags);
	}
}
