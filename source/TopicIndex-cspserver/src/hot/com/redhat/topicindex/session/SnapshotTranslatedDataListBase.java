package com.redhat.topicindex.session;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.jboss.seam.Component;
import org.jboss.seam.framework.EntityQuery;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.SnapshotTranslatedDataBase;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.FilterQueryBuilder;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

public abstract class SnapshotTranslatedDataListBase<T extends SnapshotTranslatedDataBase> extends EntityQuery<T>
{
	/**
	 * A predefined query that overrides the URL parameters
	 */
	private String constructedEJBQL;

	public SnapshotTranslatedDataListBase(final FilterQueryBuilder filterQueryBuilder)
	{
		construct(Constants.DEFAULT_ENVERS_PAGING_SIZE, null, filterQueryBuilder);
	}

	public SnapshotTranslatedDataListBase(int limit, final FilterQueryBuilder filterQueryBuilder)
	{
		construct(limit, null, filterQueryBuilder);
	}

	public SnapshotTranslatedDataListBase(int limit, final String constructedEJBQL)
	{
		construct(limit, constructedEJBQL, null);
	}

	
	public void populate()
	{
		/* prepopulate the enversTopic field on the returned entities */
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);

		final List<T> results = this.getResultList();

		for (final T snapshotTranslatedData : results)
		{
			final AuditQuery query = reader.createQuery().forEntitiesAtRevision(Topic.class, snapshotTranslatedData.getSnapshotTopic().getTopicRevision()).add(AuditEntity.id().eq(snapshotTranslatedData.getSnapshotTopic().getTopicId()));
			snapshotTranslatedData.setEnversTopic((Topic) query.getSingleResult());
		}
	}
	
	protected void construct(int limit, final String constructedEJBQL, final FilterQueryBuilder filterQueryBuilder)
	{
		if (constructedEJBQL == null)
		{
			// initialize filter home
			final Filter filter = EntityUtilities.populateFilter(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

			/*
			 * the filter may be null if an invalid variable was passed in the
			 * URL
			 */
			if (filter != null)
			{
				// add the and and or categories clause to the default statement
				this.constructedEJBQL = filter.buildQuery(filterQueryBuilder);
			}
			else
			{
				this.constructedEJBQL = SnapshotTranslatedData.SELECT_ALL_QUERY;
			}
		}
		else
		{
			this.constructedEJBQL = constructedEJBQL;
		}

		if (limit != -1)
			setMaxResults(limit);

		setEjbql(this.constructedEJBQL);
	}
}
