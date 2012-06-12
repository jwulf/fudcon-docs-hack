package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("topicSourceUrlHome")
public class TopicSourceUrlHome extends EntityHome<TopicSourceUrl>
{

	public void setTopicSourceUrlTopicSourceUrlid(Integer id)
	{
		setId(id);
	}

	public Integer getTopicSourceUrlTopicSourceUrlid()
	{
		return (Integer) getId();
	}

	@Override
	protected TopicSourceUrl createInstance()
	{
		TopicSourceUrl topicSourceUrl = new TopicSourceUrl();
		return topicSourceUrl;
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

	public TopicSourceUrl getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	public List<TopicToTopicSourceUrl> getTopicToTopicSourceUrls()
	{
		return getInstance() == null ? null : new ArrayList<TopicToTopicSourceUrl>(getInstance().getTopicToTopicSourceUrls());
	}

}
