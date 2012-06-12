package com.redhat.topicindex.utils.structures.locales;

public class UILocale
{
	private String name;
	private boolean selected = false;

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(final boolean selected)
	{
		this.selected = selected;
	}
	
	public UILocale(final String name, final boolean selected)
	{
		this.name = name;
		this.selected = selected;
	}
}
