package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;

@Name("workingSnapshotTranslatedDataHome")
public class WorkingSnapshotTranslatedDataHome extends EntityHome<WorkingSnapshotTranslatedData>
{
	private static final long serialVersionUID = -3872824824385606193L;

	public void setWorkingSnapshotTranslatedDataId(Integer id)
	{
		setId(id);
	}

	public Integer getWorkingSnapshotTranslatedDataId()
	{
		return (Integer) getId();
	}

	@Override
	protected WorkingSnapshotTranslatedData createInstance()
	{
		final WorkingSnapshotTranslatedData instance = new WorkingSnapshotTranslatedData();
		return instance;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public void wire()
	{
		getInstance();
	}

	public boolean isWired()
	{
		return true;
	}

	public WorkingSnapshotTranslatedData getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}
}
