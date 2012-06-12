package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.entity.TranslatedTopicData;

@Name("translatedTopicDataHome")
public class TranslatedTopicDataHome extends VersionedEntityHome<TranslatedTopicData>
{
	private static final long serialVersionUID = -3872824824385606193L;

	public void setTranslatedTopicDataId(Integer id)
	{
		setId(id);
	}

	public Integer getTranslatedTopicDataId()
	{
		return (Integer) getId();
	}

	@Override
	protected TranslatedTopicData createInstance()
	{
		final TranslatedTopicData instance = new TranslatedTopicData();
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

	public TranslatedTopicData getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}
}
