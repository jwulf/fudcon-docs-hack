package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.BugzillaBug;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.BugzillaBugV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class BugzillaBugV1Factory extends RESTDataObjectFactory<BugzillaBugV1, BugzillaBug>
{
	BugzillaBugV1Factory()
	{
		super(BugzillaBug.class);
	}
	
	@Override
	BugzillaBugV1 createRESTEntityFromDBEntity(final BugzillaBug entity, final String baseUrl, String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter entity can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final BugzillaBugV1 retValue = new BugzillaBugV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);

		retValue.setId(entity.getBugzillaBugId());
		retValue.setIsOpen(entity.getBugzillaBugOpen());
		retValue.setBugId(entity.getBugzillaBugBugzillaId());
		retValue.setSummary(entity.getBugzillaBugSummary());
		
		retValue.setLinks(baseUrl, BaseRESTv1.BUGZILLABUG_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final BugzillaBug entity, final BugzillaBugV1 dataObject)
	{
		if (dataObject.isParameterSet(BugzillaBugV1.BUG_ID))
			entity.setBugzillaBugBugzillaId(dataObject.getBugId());
		if (dataObject.isParameterSet(BugzillaBugV1.BUG_ISOPEN))
			entity.setBugzillaBugOpen(dataObject.getIsOpen());
		if (dataObject.isParameterSet(BugzillaBugV1.BUG_SUMMARY))
			entity.setBugzillaBugSummary(dataObject.getSummary());
		
		entityManager.persist(entity);
	}

}

