package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.roles.UIRoleUserData;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("userHome")
public class UserHome extends EntityHome<User>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 4678394145285598496L;
	
	private List<UIRoleUserData> roles;

	public void setUserUserId(Integer id)
	{
		setId(id);
	}

	public Integer getUserUserId()
	{
		return (Integer) getId();
	}

	@Override
	protected User createInstance()
	{
		User user = new User();
		return user;
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

	public User getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	public List<UserRole> getUserRoles()
	{
		return getInstance() == null ? null : new ArrayList<UserRole>(getInstance().getUserRoles());
	}

	public List<UIRoleUserData> getRoles()
	{
		return roles;
	}

	public void setRoles(List<UIRoleUserData> roles)
	{
		this.roles = roles;
	}
	
	public void populate()
	{
		this.roles = EntityUtilities.getUserRoles(this.getInstance());
	}
	
	private void updateRoles()
	{
		final User user = this.getInstance();
		final List<Integer> addedRoles = new ArrayList<Integer>();
		final List<Integer> removedRoles = new ArrayList<Integer>();
		for (final UIRoleUserData role : roles)
		{
			if (role.getChecked() && !user.isInRole(role.getId()))
				addedRoles.add(role.getId());
			else if (!role.getChecked() && user.isInRole(role.getId()))
				removedRoles.add(role.getId());
		}
		
		for (final Integer role : removedRoles)
			user.removeRole(role);
		
		for (final Integer role : addedRoles)
			user.addRole(EntityUtilities.getRoleFromId(role));
	}
	
	@Override
	public String persist()
	{
		updateRoles();
		return super.persist();
	}
	
	@Override
	public String update()
	{
		updateRoles();
		return super.update();
	}

}
