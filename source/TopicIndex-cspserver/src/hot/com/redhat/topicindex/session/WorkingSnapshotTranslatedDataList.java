package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;

import com.redhat.topicindex.filter.SnapshotTranslatedDataFilterQueryBuilder;

@Name("workingSnapshotTranslatedDataList")
public class WorkingSnapshotTranslatedDataList extends SnapshotTranslatedDataListBase<WorkingSnapshotTranslatedData>
{
	private static final long serialVersionUID = 4265050133041173177L;

	public WorkingSnapshotTranslatedDataList()
	{
		super(new SnapshotTranslatedDataFilterQueryBuilder(false));
	}

	public WorkingSnapshotTranslatedDataList(final int limit)
	{
		super(limit, new SnapshotTranslatedDataFilterQueryBuilder(false));
	}

	public WorkingSnapshotTranslatedDataList(final int limit, final String constructedEJBQL)
	{
		super(limit, constructedEJBQL);
	}
}
