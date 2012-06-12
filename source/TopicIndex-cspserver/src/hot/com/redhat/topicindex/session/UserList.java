package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("userList")
public class UserList extends EntityQuery<User>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -3474205178769352514L;

	private static final String EJBQL = "select user from User user";

	private static final String[] RESTRICTIONS =
	{ "lower(user.userName) like lower(concat(#{userList.user.userName},'%'))", "lower(user.description) like lower(concat(#{userList.user.description},'%'))", };

	private User user = new User();

	public UserList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public User getUser()
	{
		return user;
	}
}
