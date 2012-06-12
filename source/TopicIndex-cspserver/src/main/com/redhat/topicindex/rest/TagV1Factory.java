package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.commonutils.NotificationUtilities;
import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.Role;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToPropertyTag;
import com.redhat.topicindex.entity.TopicToPropertyTag;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.entities.ProjectV1;
import com.redhat.topicindex.rest.entities.PropertyTagV1;
import com.redhat.topicindex.rest.entities.SnapshotV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class TagV1Factory extends RESTDataObjectFactory<TagV1, Tag>
{
	TagV1Factory()
	{
		super(Tag.class);
	}

	@Override
	TagV1 createRESTEntityFromDBEntity(final Tag entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final TagV1 retValue = new TagV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(TagV1.CATEGORIES_NAME);
		expandOptions.add(TagV1.PARENT_TAGS_NAME);
		expandOptions.add(TagV1.CHILD_TAGS_NAME);
		expandOptions.add(TagV1.PROJECTS_NAME);
		expandOptions.add(BaseRESTv1.PROPERTIES_EXPANSION_NAME);

		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);	
		
		/* Set simple properties */
		retValue.setId(entity.getTagId());

		retValue.setName(entity.getTagName());
		retValue.setDescription(entity.getTagDescription());
		
		/* Set collections */
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		retValue.setCategories(new RESTDataObjectCollectionFactory<CategoryV1, TagToCategory>().create(new TagCategoryV1Factory(), CollectionUtilities.toArrayList(entity.getTagToCategories()), TagV1.CATEGORIES_NAME, dataType, expand, baseUrl));
		retValue.setParentTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getParentTags(), TagV1.PARENT_TAGS_NAME, dataType, expand, baseUrl));
		retValue.setChildTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getChildTags(), TagV1.CHILD_TAGS_NAME, dataType, expand, baseUrl));
		retValue.setProperties(new RESTDataObjectCollectionFactory<PropertyTagV1, TagToPropertyTag>().create(new TagPropertyTagV1Factory(), entity.getTagToPropertyTagsArray(), BaseRESTv1.PROPERTIES_EXPANSION_NAME, dataType, expand, baseUrl));
		retValue.setProjects(new RESTDataObjectCollectionFactory<ProjectV1, Project>().create(new ProjectV1Factory(), entity.getProjects(), TagV1.PROJECTS_NAME, dataType, expand, baseUrl));

		retValue.setLinks(baseUrl, BaseRESTv1.TAG_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Tag entity, final TagV1 dataObject)
	{
		if (dataObject.isParameterSet(TagV1.DESCRIPTION_NAME))
			entity.setTagDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(TagV1.NAME_NAME))
			entity.setTagName(dataObject.getName());
		
		entityManager.persist(entity);

		if (dataObject.isParameterSet(TagV1.CATEGORIES_NAME) && dataObject.getCategories() != null && dataObject.getCategories().getItems() != null)
		{
			for (final CategoryV1 restEntity : dataObject.getCategories().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					final Category dbEntity = entityManager.find(Category.class, restEntity.getId());
					if (dbEntity == null)
						throw new BadRequestException("No Category entity was found with the primary key " + restEntity.getId());

					if (restEntity.getAddItem())
					{
						if (restEntity.isParameterSet(CategoryV1.SORT_NAME))
							dbEntity.addTagRelationship(entity, restEntity.getSort());
						else
							dbEntity.addTagRelationship(entity);
					}
					else if (restEntity.getRemoveItem())
					{
						dbEntity.removeTagRelationship(entity);
					}
				}
			}

		}

		if (dataObject.isParameterSet(TagV1.CHILD_TAGS_NAME) && dataObject.getChildTags() != null && dataObject.getChildTags().getItems() != null)
		{
			for (final TagV1 restEntity : dataObject.getChildTags().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
					if (dbEntity == null)
						throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());

					if (restEntity.getAddItem())
					{
						entity.addTagRelationship(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						entity.removeTagRelationship(dbEntity);
					}
				}
			}
		}

		if (dataObject.isParameterSet(TagV1.PARENT_TAGS_NAME) && dataObject.getParentTags() != null  && dataObject.getParentTags().getItems() != null)
		{
			for (final TagV1 restEntity : dataObject.getParentTags().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
					if (dbEntity == null)
						throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());

					if (restEntity.getAddItem())
					{
						dbEntity.addTagRelationship(entity);
					}
					else if (restEntity.getRemoveItem())
					{
						dbEntity.removeTagRelationship(entity);
					}
				}
			}
		}

		if (dataObject.isParameterSet(TagV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null)
		{
			for (final PropertyTagV1 restEntity : dataObject.getProperties().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
					if (dbEntity == null)
						throw new BadRequestException("No PropertyTag entity was found with the primary key " + restEntity.getId());

					if (restEntity.getAddItem())
					{
						entity.addPropertyTag(dbEntity, restEntity.getValue());
					}
					else if (restEntity.getRemoveItem())
					{
						entity.removePropertyTag(dbEntity, restEntity.getValue());
					}
				}
			}
		}
	}

}
