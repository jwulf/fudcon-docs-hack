package com.redhat.topicindex.utils.structures.tags;

import com.redhat.topicindex.entity.Tag;

public class UIProjectTagData
{
	private boolean selected;
	private Tag tag;

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public Tag getTag()
	{
		return tag;
	}

	public void setTag(final Tag tag)
	{
		this.tag = tag;
	}

	public UIProjectTagData()
	{
		this.tag = null;
		this.selected = false;
	}

	public UIProjectTagData(final Tag tag, final boolean selected)
	{
		this.tag = tag;
		this.selected = selected;
	}
}
