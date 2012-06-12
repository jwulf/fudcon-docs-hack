package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.entity.SnapshotTranslatedData;

import com.redhat.topicindex.filter.SnapshotTranslatedDataFilterQueryBuilder;

@Name("snapshotTranslatedDataList")
public class SnapshotTranslatedDataList extends SnapshotTranslatedDataListBase<SnapshotTranslatedData>
{
	private static final long serialVersionUID = -2766610267859468834L;

	public SnapshotTranslatedDataList()
	{
		super(new SnapshotTranslatedDataFilterQueryBuilder(true));
	}

	public SnapshotTranslatedDataList(final int limit)
	{
		super(limit, new SnapshotTranslatedDataFilterQueryBuilder(true));
	}

	public SnapshotTranslatedDataList(final int limit, final String constructedEJBQL)
	{
		super(limit, constructedEJBQL);
	}
}
