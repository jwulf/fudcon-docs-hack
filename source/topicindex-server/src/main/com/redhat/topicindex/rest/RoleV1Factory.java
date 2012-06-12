package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.Role;
import com.redhat.topicindex.entity.RoleToRoleRelationship;
import com.redhat.topicindex.entity.User;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.RoleV1;
import com.redhat.topicindex.rest.entities.UserV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class RoleV1Factory extends RESTDataObjectFactory<RoleV1, Role>
{
	RoleV1Factory()
	{
		super(Role.class);
	}

	@Override
	RoleV1 createRESTEntityFromDBEntity(final Role entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final RoleV1 retValue = new RoleV1();

		retValue.setId(entity.getRoleId());
		retValue.setName(entity.getRoleName());
		retValue.setDescription(entity.getDescription());
		
		final List<String> expandOptions = new ArrayList<String>();
		expandOptions.add(RoleV1.USERS_NAME);
		expandOptions.add(RoleV1.CHILDROLES_NAME);
		expandOptions.add(RoleV1.PARENTROLES_NAME);
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		
		retValue.setExpand(expandOptions);	

		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<RoleV1, Role>().create(new RoleV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		retValue.setUsers(new RESTDataObjectCollectionFactory<UserV1, User>().create(new UserV1Factory(), entity.getUsers(), RoleV1.USERS_NAME, dataType, expand, baseUrl));
		retValue.setParentRoles(new RESTDataObjectCollectionFactory<RoleV1, Role>().create(new RoleV1Factory(), entity.getParentRoles(), RoleV1.PARENTROLES_NAME, dataType, expand, baseUrl));
		retValue.setChildRoles(new RESTDataObjectCollectionFactory<RoleV1, Role>().create(new RoleV1Factory(), entity.getChildRoles(), RoleV1.CHILDROLES_NAME, dataType, expand, baseUrl));

		retValue.setLinks(baseUrl, BaseRESTv1.ROLE_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Role entity, final RoleV1 dataObject)
	{
		if (dataObject.isParameterSet(UserV1.DESCRIPTION_NAME))
			entity.setDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(UserV1.NAME_NAME))
			entity.setRoleName(dataObject.getName());
		
		entityManager.persist(entity);

		/* Many To Many - Add will create a mapping */
		if (dataObject.isParameterSet(RoleV1.USERS_NAME) && dataObject.getUsers() != null && dataObject.getUsers().getItems() != null)
		{
			for (final UserV1 restEntity : dataObject.getUsers().getItems())
			{
				final User dbEntity = entityManager.find(User.class, restEntity.getId());

				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (dbEntity == null)
						throw new BadRequestException("No User entity was found with the primary key " + restEntity.getId());
				}
				
				if (restEntity.getAddItem())
				{
					entity.addUser(dbEntity);
				}
				else if (restEntity.getRemoveItem())
				{
					entity.removeUser(dbEntity);
				}
			}
		}
		
		/* Many To Many - Add will create a mapping */
		if (dataObject.isParameterSet(RoleV1.PARENTROLES_NAME) && dataObject.getParentRoles() != null && dataObject.getParentRoles().getItems() != null)
		{
			for (final RoleV1 restEntity : dataObject.getParentRoles().getItems())
			{
				final Role dbEntity = entityManager.find(Role.class, restEntity.getId());
				final RoleToRoleRelationship dbRelationshipEntity = entityManager.find(RoleToRoleRelationship.class, restEntity.getRelationshipId());
				
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (dbEntity == null)
						throw new BadRequestException("No Role entity was found with the primary key " + restEntity.getId());
					else if (dbRelationshipEntity == null)
						throw new BadRequestException("No RoleToRoleRelationship entity was found with the primary key " + restEntity.getRelationshipId());
				}

				if (restEntity.getAddItem())
				{					
					entity.addParentRole(dbEntity, dbRelationshipEntity);
				}
				else if (restEntity.getRemoveItem())
				{
					entity.removeParentRole(dbEntity, dbRelationshipEntity);
				}
			}
		}
		
		/* Many To Many - Add will create a mapping */
		if (dataObject.isParameterSet(RoleV1.CHILDROLES_NAME) && dataObject.getChildRoles() != null && dataObject.getChildRoles().getItems() != null)
		{
			for (final RoleV1 restEntity : dataObject.getChildRoles().getItems())
			{
				final Role dbEntity = entityManager.find(Role.class, restEntity.getId());
				final RoleToRoleRelationship dbRelationshipEntity = entityManager.find(RoleToRoleRelationship.class, restEntity.getRelationshipId());
				
				if (restEntity.getAddItem() || restEntity.getRemoveItem())
				{
					if (dbEntity == null)
						throw new BadRequestException("No Role entity was found with the primary key " + restEntity.getId());
					else if (dbRelationshipEntity == null)
						throw new BadRequestException("No RoleToRoleRelationship entity was found with the primary key " + restEntity.getRelationshipId());
				}

				if (restEntity.getAddItem())
				{					
					entity.addChildRole(dbEntity, dbRelationshipEntity);
				}
				else if (restEntity.getRemoveItem())
				{
					entity.removeChildRole(dbEntity, dbRelationshipEntity);
				}
			}
		}
	}
}
