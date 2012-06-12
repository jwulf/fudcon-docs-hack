package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

import com.redhat.topicindex.entity.SnapshotTranslatedData;

@Name("snapshotTranslatedDataHome")
public class SnapshotTranslatedDataHome extends EntityHome<SnapshotTranslatedData>
{
	private static final long serialVersionUID = 3197641276478790931L;

	public void setSnapshotTranslatedDataHomeId(Integer id)
	{
		setId(id);
	}

	public Integer getSnapshotTranslatedDataHomeId()
	{
		return (Integer) getId();
	}

	@Override
	protected SnapshotTranslatedData createInstance()
	{
		final SnapshotTranslatedData instance = new SnapshotTranslatedData();
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

	public SnapshotTranslatedData getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}
}
