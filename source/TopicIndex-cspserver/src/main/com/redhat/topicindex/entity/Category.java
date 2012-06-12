package com.redhat.topicindex.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hamcrest.Matchers.equalTo;

import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.sort.TagIDComparator;
import com.redhat.topicindex.sort.TagToCategorySortingComparator;

@Entity
@Audited
@Table(name = "Category", uniqueConstraints = @UniqueConstraint(columnNames =
{ "CategoryName" }))
public class Category extends AuditedEntity<Category> implements java.io.Serializable, Comparable<Category>
{
	public static final String SELECT_ALL_QUERY = "select category from Category category";

	private static final long serialVersionUID = -8650833773254246211L;
	private Integer categoryId;
	private String categoryName;
	private String categoryDescription;
	private Set<TagToCategory> tagToCategories = new HashSet<TagToCategory>(0);
	private Integer categorySort;
	private boolean mutuallyExclusive;

	public Category()
	{
		super(Category.class);
	}

	public Category(String categoryName)
	{
		super(Category.class);
		this.categoryName = categoryName;
	}

	public Category(final String categoryName, final String categoryDescription, final Set<TagToCategory> tagToCategories)
	{
		super(Category.class);
		this.categoryName = categoryName;
		this.categoryDescription = categoryDescription;
		this.tagToCategories = tagToCategories;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToCategory> getTagToCategories()
	{
		return this.tagToCategories;
	}

	public void setTagToCategories(Set<TagToCategory> tagToCategories)
	{
		this.tagToCategories = tagToCategories;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "CategoryID", unique = true, nullable = false)
	public Integer getCategoryId()
	{
		return this.categoryId;
	}

	public void setCategoryId(Integer categoryId)
	{
		this.categoryId = categoryId;
	}

	// @Column(name = "CategoryName", nullable = false, length = 65535)
	@Column(name = "CategoryName", nullable = false, columnDefinition = "TEXT")
	@NotNull
	@Length(max = 65535)
	public String getCategoryName()
	{
		return this.categoryName;
	}

	public void setCategoryName(String categoryName)
	{
		this.categoryName = categoryName;
	}

	@Column(name = "CategoryDescription", length = 2048)
	@Length(max = 2048)
	public String getCategoryDescription()
	{
		return this.categoryDescription;
	}

	public void setCategoryDescription(String categoryDescription)
	{
		this.categoryDescription = categoryDescription;
	}

	@Column(name = "CategorySort")
	public Integer getCategorySort()
	{
		return this.categorySort;
	}

	public void setCategorySort(Integer categorySort)
	{
		this.categorySort = categorySort;
	}

	@Column(name = "MutuallyExclusive", nullable = false)
	@NotNull
	public boolean isMutuallyExclusive()
	{
		return this.mutuallyExclusive;
	}

	public void setMutuallyExclusive(boolean mutuallyExclusive)
	{
		this.mutuallyExclusive = mutuallyExclusive;
	}

	@Override
	public int compareTo(Category o)
	{
		if (o == null)
			return 1;

		if (o.getCategorySort() == null && this.getCategorySort() == null)
			return 0;

		if (o.getCategorySort() == null)
			return 1;

		if (this.getCategorySort() == null)
			return -1;

		return this.getCategorySort().compareTo(o.getCategorySort());
	}

	@Transient
	public List<Tag> getTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TagToCategory tag : this.tagToCategories)
			retValue.add(tag.getTag());
		
		Collections.sort(retValue, new TagIDComparator());

		return retValue;
	}

	@Transient
	public List<Tag> getTagsInProject(final Project project)
	{
		final List<Tag> retValue = new ArrayList<Tag>();

		for (final TagToCategory tagToCategory : this.tagToCategories)
		{
			final Tag tag = tagToCategory.getTag();
			if (tag.isInProject(project))
				retValue.add(tag);
		}

		return retValue;
	}

	@Transient
	public String getTagsList()
	{
		String tagsList = "";
		
		final List<TagToCategory> tags = CollectionUtilities.toArrayList(this.getTagToCategories());
		Collections.sort(tags, new TagToCategorySortingComparator());
		
		for (final TagToCategory tagToCatgeory : tags)
		{
			if (tagsList.length() != 0)
				tagsList += ", ";
			tagsList += tagToCatgeory.getTag().getTagName();
		}
		return tagsList;
	}

	public boolean removeTagRelationship(final Tag childTag)
	{
		final List<TagToCategory> children = filter(having(on(TagToCategory.class).getTag(), equalTo(childTag)), this.getTagToCategories());
		for (final TagToCategory child : children)
		{
			this.getTagToCategories().remove(child);
			childTag.getTagToCategories().remove(child);
		}

		return children.size() != 0;
	}

	public boolean addTagRelationship(final Tag childTag, final Integer sort)
	{
		final List<TagToCategory> children = filter(having(on(TagToCategory.class).getTag(), equalTo(childTag)), this.getTagToCategories());
		
		/* If this tag is not mapped at all, add it */
		if (children.size() == 0)
		{
			final TagToCategory tagToCategory = new TagToCategory(childTag, this);
			tagToCategory.setSorting(sort);
			this.getTagToCategories().add(tagToCategory);
			childTag.getTagToCategories().add(tagToCategory);
			return true;
		}
		/* otherwise update the sorting order */
		else
		{
			for (final TagToCategory child : children)
			{
				child.setSorting(sort);
			}
		}

		return false;
	}
	
	public boolean addTagRelationship(final Tag childTag)
	{
		return addTagRelationship(childTag, null);
	}
	
	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		for (final TagToCategory tagToCategory : tagToCategories)
			tagToCategory.getTag().getTagToCategories().remove(tagToCategory);
		
		this.tagToCategories.clear();
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.categoryId;
	}

}
