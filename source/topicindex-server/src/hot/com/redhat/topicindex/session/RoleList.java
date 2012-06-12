package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("roleList")
public class RoleList extends EntityQuery<Role> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 8874402340457479545L;

	

	private static final String[] RESTRICTIONS = {
			"lower(role.roleName) like lower(concat(#{roleList.role.roleName},'%'))",
			"lower(role.description) like lower(concat(#{roleList.role.description},'%'))", };

	private Role role = new Role();

	public RoleList() {
		setEjbql(Role.SELECT_ALL_QUERY);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public Role getRole() {
		return role;
	}
}
