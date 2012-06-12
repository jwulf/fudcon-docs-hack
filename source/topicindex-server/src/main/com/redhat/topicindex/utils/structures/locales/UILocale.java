package com.redhat.topicindex.utils.structures.locales;

public class UILocale
{
	private String name;
	private boolean selected = false;
	/** Whether or not this object has been "not selected" */
	private boolean notSelected = false;

	public UILocale(final String name, final boolean selected, final boolean notSelected)
	{
		this.name = name;
		this.selected = selected;
		this.notSelected = notSelected;
	}
	
	public UILocale(final String name, final boolean selected)
	{
		this.name = name;
		this.selected = selected;
	}
	
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
	
	public boolean isNotSelected() {
		return notSelected;
	}

	public void setNotSelected(boolean notSelected) {
		this.notSelected = notSelected;
	}
}
