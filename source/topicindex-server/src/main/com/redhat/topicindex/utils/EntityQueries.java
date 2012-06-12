package com.redhat.topicindex.utils;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;

import com.redhat.topicindex.entity.Category;

/**
 * A class that contains a bunch of static functions to return common collections of
 * entities
 */
public class EntityQueries 
{
	public static EntityManager getEntityManager()
	{
		return (EntityManager)Component.getInstance("entityManager");
	}
	
	@SuppressWarnings("unchecked")
	public static List<Category> getAllCategories()
	{
		final EntityManager entityManager = getEntityManager();
		return entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> getAllPropertiesFromEntity(final String entity, final String property)
	{
		final String query = "select entity." + property + " from " + entity + " entity";
		final EntityManager entityManager = getEntityManager();
		return entityManager.createQuery(query).getResultList();
	}
}
