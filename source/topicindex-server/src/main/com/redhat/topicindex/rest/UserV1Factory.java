package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.Role;
import com.redhat.topicindex.entity.User;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.RoleV1;
import com.redhat.topicindex.rest.entities.UserV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class UserV1Factory extends RESTDataObjectFactory<UserV1, User>
{
	UserV1Factory()
	{
		super(User.class);
	}

	@Override
	UserV1 createRESTEntityFromDBEntity(final User entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final UserV1 retValue = new UserV1();

		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add( UserV1.ROLES_NAME);

		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		
		retValue.setId(entity.getUserId());
		retValue.setName(entity.getUserName());
		retValue.setDescription(entity.getDescription());
		
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<UserV1, User>().create(new UserV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		retValue.setRoles(new RESTDataObjectCollectionFactory<RoleV1, Role>().create(new RoleV1Factory(), entity.getRoles(), UserV1.ROLES_NAME, dataType, expand, baseUrl));

		retValue.setLinks(baseUrl, BaseRESTv1.USER_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final User entity, final UserV1 dataObject)
	{
		if (dataObject.isParameterSet(UserV1.DESCRIPTION_NAME))
			entity.setDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(UserV1.NAME_NAME))
			entity.setUserName(dataObject.getName());
		
		entityManager.persist(entity);

		if (dataObject.isParameterSet(UserV1.ROLES_NAME) && dataObject.getRoles() != null && dataObject.getRoles().getItems() != null)
		{
			for (final RoleV1 restEntity : dataObject.getRoles().getItems())
			{
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					final Role dbEntity = entityManager.find(Role.class, restEntity.getId());
					if (dbEntity == null)
						throw new BadRequestException("No entity was found with the primary key " + restEntity.getId());

					if (restEntity.getAddItem())
					{
						entity.addRole(dbEntity);
					}
					else if (restEntity.getRemoveItem())
					{
						entity.removeRole(dbEntity);
					}
				}
			}
		}
	}
}
