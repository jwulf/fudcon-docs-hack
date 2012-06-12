package com.redhat.topicindex.utils.structures.roles;

public class UIRoleUserData
{
	private Integer id;
	private String name;
	private Boolean checked;

	public Integer getId()
	{
		return id;
	}

	public void setId(final Integer id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public Boolean getChecked()
	{
		return checked;
	}

	public void setChecked(final Boolean checked)
	{
		this.checked = checked;
	}
	
	public UIRoleUserData(final Integer id, final String name, final Boolean checked)
	{
		this.id = id;
		this.name = name;
		this.checked = checked;
	}
}
