package com.redhat.topicindex.session;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.RelationshipTag;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

/** Provides a number of constant'ish values to the web layer */
@Name("constants")
public class Constants
{
	public List<String> getLocales()
	{
		return CommonConstants.LOCALES;
	}
	
	public String getLoginMessage()
	{
		final String retValue = System.getProperty(com.redhat.topicindex.utils.Constants.LOGIN_MESSAGE_SYSTEM_PROPERTY);
		return retValue == null ? "" : retValue;
	}
	
	public String getBugzillaUrl()
	{
		final String retValue = System.getProperty(CommonConstants.BUGZILLA_URL_PROPERTY);
		return retValue == null ? "" : retValue;
	}

	public List<RelationshipTag> getRelationshipTags()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

			@SuppressWarnings("unchecked")
			final List<RelationshipTag> retValue = entityManager.createQuery(RelationshipTag.SELECT_ALL_QUERY).getResultList();

			return retValue;
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an error retrieving a list of RelationshipTags");
		}

		return null;
	}
}
