package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

/**
 * This factory is used when creating a collection of Categories for a Tag. It
 * will populate the sort property, which is left null by the CategoryV1Factory.
 */
public class TagCategoryV1Factory extends RESTDataObjectFactory<CategoryV1, TagToCategory>
{
	TagCategoryV1Factory()
	{
		super(TagToCategory.class);
	}

	@Override
	CategoryV1 createRESTEntityFromDBEntity(final TagToCategory entity, final String baseUrl, String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final CategoryV1 retValue = new CategoryV1();

		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);

		retValue.setId(entity.getCategory().getCategoryId());
		retValue.setDescription(entity.getCategory().getCategoryDescription());
		retValue.setName(entity.getCategory().getCategoryName());
		retValue.setMutuallyExclusive(entity.getCategory().isMutuallyExclusive());
		retValue.setSort(entity.getSorting());

		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<CategoryV1, TagToCategory>().create(new TagCategoryV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final TagToCategory entity, final CategoryV1 dataObject)
	{
		if (dataObject.isParameterSet(CategoryV1.SORT_NAME))
			entity.setSorting(dataObject.getSort());
		
		entityManager.persist(entity);
	}
}
