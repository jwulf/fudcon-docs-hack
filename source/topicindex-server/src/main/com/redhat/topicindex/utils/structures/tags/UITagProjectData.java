package com.redhat.topicindex.utils.structures.tags;

import com.redhat.topicindex.entity.Project;

public class UITagProjectData 
{
	private Project project;
	private boolean selected;

	public boolean isSelected() 
	{
		return selected;
	}

	public void setSelected(final boolean selected) 
	{
		this.selected = selected;
	}

	public Project getProject() 
	{
		return project;
	}

	public void setProject(final Project project) 
	{
		this.project = project;
	}
	
	public UITagProjectData()
	{
		this.project = null;
		this.selected = false;
	}
	
	public UITagProjectData(final Project project, final boolean selected)
	{
		this.project = project;
		this.selected = selected;
	}
}
