package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.roles.UIRoleUserData;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("roleHome")
public class RoleHome extends EntityHome<Role> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 6808946968809326435L;
	
	private List<UIRoleUserData> users;

	public void setRoleRoleId(Integer id) {
		setId(id);
	}

	public Integer getRoleRoleId() {
		return (Integer) getId();
	}

	@Override
	protected Role createInstance() {
		Role role = new Role();
		return role;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public Role getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<UserRole> getUserRoles() {
		return getInstance() == null ? null : new ArrayList<UserRole>(
				getInstance().getUserRoles());
	}

	public List<UIRoleUserData> getUsers()
	{
		return users;
	}

	public void setUsers(List<UIRoleUserData> users)
	{
		this.users = users;
	}
	
	public void populate()
	{
		this.users = EntityUtilities.getRoleUsers(this.getInstance());
	}
	
	private void updateUsers()
	{
		final Role role = this.getInstance();
		final List<Integer> addedUsers = new ArrayList<Integer>();
		final List<Integer> removedUsers = new ArrayList<Integer>();
		for (final UIRoleUserData user : users)
		{
			if (user.getChecked() && !role.hasUser(user.getId()))
				addedUsers.add(user.getId());
			else if (!user.getChecked() && role.hasUser(user.getId()))
				removedUsers.add(user.getId());
		}
		
		for (final Integer user : removedUsers)
			role.removeUser(user);
		
		for (final Integer user : addedUsers)
			role.addUser(EntityUtilities.getUserFromId(user));
	}
	
	@Override
	public String persist()
	{
		updateUsers();
		return super.persist();
	}
	
	@Override
	public String update()
	{
		updateUsers();
		return super.update();
	}

}
