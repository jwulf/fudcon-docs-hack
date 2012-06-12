package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class CategoryV1Factory extends RESTDataObjectFactory<CategoryV1, Category>
{
	CategoryV1Factory()
	{
		super(Category.class);
	}

	@Override
	CategoryV1 createRESTEntityFromDBEntity(final Category entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final CategoryV1 retValue = new CategoryV1();

		retValue.setId(entity.getCategoryId());
		retValue.setName(entity.getCategoryName());
		retValue.setDescription(entity.getCategoryDescription());
		retValue.setMutuallyExclusive(entity.isMutuallyExclusive());
		retValue.setSort(entity.getCategorySort());
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(BaseRESTv1.TAGS_EXPANSION_NAME);
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);	

		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<CategoryV1, Category>().create(new CategoryV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl));

		retValue.setLinks(baseUrl, BaseRESTv1.CATEGORY_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Category entity, final CategoryV1 dataObject)
	{
		if (dataObject.isParameterSet(CategoryV1.DESCRIPTION_NAME))
			entity.setCategoryDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(CategoryV1.MUTUALLYEXCLUSIVE_NAME))
			entity.setMutuallyExclusive(dataObject.isMutuallyExclusive());
		if (dataObject.isParameterSet(CategoryV1.NAME_NAME))
			entity.setCategoryName(dataObject.getName());
		if (dataObject.isParameterSet(CategoryV1.SORT_NAME))
			entity.setCategorySort(dataObject.getSort());
		
		entityManager.persist(entity);

		/* Many To Many - Add will create a mapping */
		if (dataObject.isParameterSet(CategoryV1.TAGS_NAME) && dataObject.getTags() != null  && dataObject.getTags().getItems() != null)
		{
			for (final TagV1 restEntity : dataObject.getTags().getItems())
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
	}
}
