package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.Role;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.ProjectV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class ProjectV1Factory extends RESTDataObjectFactory<ProjectV1, Project>
{
	ProjectV1Factory()
	{
		super(Project.class);
	}

	@Override
	ProjectV1 createRESTEntityFromDBEntity(final Project entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final ProjectV1 retValue = new ProjectV1();

		retValue.setId(entity.getProjectId());
		retValue.setDescription(entity.getProjectDescription());
		retValue.setName(entity.getProjectName());
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(BaseRESTv1.TAGS_EXPANSION_NAME);
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);	

		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<ProjectV1, Project>().create(new ProjectV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl));

		retValue.setLinks(baseUrl, BaseRESTv1.PROJECT_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Project entity, final ProjectV1 dataObject)
	{
		if (dataObject.isParameterSet(ProjectV1.DESCRIPTION_NAME))
			entity.setProjectDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(ProjectV1.NAME_NAME))
			entity.setProjectName(dataObject.getName());
		
		entityManager.persist(entity);

		/* Many To Many - Add will create a mapping */
		if (dataObject.isParameterSet(ProjectV1.TAGS_NAME) && dataObject.getTags() != null && dataObject.getTags().getItems() != null)
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
						entity.addRelationshipTo(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						entity.removeRelationshipTo(dbEntity.getTagId());
					}
				}
			}
		}
	}
}
