package com.redhat.topicindex.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Role;
import com.redhat.topicindex.entity.RoleToRole;
import com.redhat.topicindex.entity.User;
import com.redhat.topicindex.entity.UserRole;
import com.redhat.topicindex.utils.Constants;

/** This class is used to authenticate Skynet users */
@Name("authenticator")
public class Authenticator
{
	@Logger
	private Log log;

	@In
	private Identity identity;
	@In
	private Credentials credentials;

	/**
	 * Checks the supplied username and password against a Kerberos database
	 * 
	 * @return true if the supplied credentials match a Kerberos user, false
	 *         otherwise
	 */
	@SuppressWarnings("unchecked")
	protected boolean kerberosLogin()
	{
		/*
		 * In AS7, add the following XML elements to the standalone.xml file:
		 * 
		 * Under the <server> element (and somewhere near the top):
		 * 
		 * <system-properties> <property name="java.security.krb5.kdc"
		 * value="kerberos.bne.redhat.com:88"/> <property
		 * name="java.security.krb5.realm" value="REDHAT.COM"/>
		 * </system-properties>
		 * 
		 * Under the <subsystem xmlns="urn:jboss:domain:security:1.0">
		 * <security-domains> element:
		 * 
		 * <security-domain name="jaas"> <authentication> <login-module
		 * code="Kerberos" flag="required"/> </authentication>
		 * </security-domain>
		 * 
		 * See http://community.jboss.org/wiki/DRAFTUsingJBossNegotiationOnAS7
		 * for more details
		 * 
		 * This code also relies on the org.picketbox and
		 * org.jboss.security.negotiation AS7 modules.
		 */

		LoginContext lc = null;
		try
		{
			lc = new LoginContext("jaas", new CallbackHandler()
			{
				@Override
				public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
				{
					for (int i = 0; i < callbacks.length; i++)
					{
						if (callbacks[i] instanceof PasswordCallback)
						{
							final PasswordCallback pc = (PasswordCallback) callbacks[i];
							final char[] password = credentials.getPassword().toCharArray();
							pc.setPassword(password);
						}
						else if (callbacks[i] instanceof NameCallback)
						{
							final NameCallback nc = (NameCallback) callbacks[i];
							nc.setName(credentials.getUsername());
						}
					}
				}
			});
		}
		catch (final LoginException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "The user could not be authenticated against a Kerberos database");
			return false;
		}
		catch (final SecurityException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "The user could not be authenticated against a Kerberos database");
			return false;
		}

		try
		{
			lc.login();

			// loop through the rolls in the database and assign them to the
			// user
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<User> users = entityManager.createQuery(User.SELECT_ALL_QUERY + " where user.userName = '" + credentials.getUsername() + "'").getResultList();
			if (users.size() == 1)
			{
				// keep a list of the roles we have processed, so we don't get
				// caught in a loop
				final List<Role> processedRoles = new ArrayList<Role>();
				final User user = users.get(0);
				for (final UserRole userRole : user.getUserRoles())
				{
					final Role role = userRole.getRole();
					processRole(role, processedRoles);
				}
			}

			return true;
		}
		catch (final LoginException ex)
		{
			// Authentication failed
			SkynetExceptionUtilities.handleException(ex, true, "The user could not be authenticated");
			return false;
		}
	}
	
	public boolean fasLogin()
	{
		LoginContext lc = null;
		try
		{
			lc = new LoginContext("fas", new CallbackHandler()
			{
				@Override
				public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
				{
					for (int i = 0; i < callbacks.length; i++)
					{
						if (callbacks[i] instanceof PasswordCallback)
						{
							final PasswordCallback pc = (PasswordCallback) callbacks[i];
							final char[] password = credentials.getPassword().toCharArray();
							pc.setPassword(password);
						}
						else if (callbacks[i] instanceof NameCallback)
						{
							final NameCallback nc = (NameCallback) callbacks[i];
							nc.setName(credentials.getUsername());
						}
					}
				}
			});
		}
		catch (final LoginException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "The user could not be authenticated against a FAS database");
			return false;
		}
		catch (final SecurityException ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "The user could not be authenticated against a FAS database");
			return false;
		}

		try
		{
			lc.login();

			// loop through the rolls in the database and assign them to the
			// user
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<User> users = entityManager.createQuery(User.SELECT_ALL_QUERY + " where user.userName = '" + credentials.getUsername() + "'").getResultList();
			if (users.size() == 1)
			{
				// keep a list of the roles we have processed, so we don't get
				// caught in a loop
				final List<Role> processedRoles = new ArrayList<Role>();
				final User user = users.get(0);
				for (final UserRole userRole : user.getUserRoles())
				{
					final Role role = userRole.getRole();
					processRole(role, processedRoles);
				}
			}
			// No user found so give them writer and basic role auth
			else
			{
				final Role writerRole = entityManager.find(Role.class, 2);
				processRole(writerRole, new ArrayList<Role>());
			}

			return true;
		}
		catch (final LoginException ex)
		{
			// Authentication failed
			SkynetExceptionUtilities.handleException(ex, true, "The user could not be authenticated");
			return false;
		}
	}

	/**
	 * Adds a role to a user account, recursively calling this function for any
	 * inherited roles.
	 * 
	 * @param role The role to add to the user account
	 * @param processedRoles A collection of previously processed roles, which
	 *            is used to prevent circular dependencies from causing an
	 *            infinite loop
	 */
	private void processRole(final Role role, final List<Role> processedRoles)
	{
		// watch out for inheritance loops
		if (!processedRoles.contains(role))
		{
			processedRoles.add(role);
			identity.addRole(role.getRoleName());

			// add all inherited roles
			for (final RoleToRole roleToRole : role.getParentRoleToRole())
			{
				if (roleToRole.getRoleToRoleRelationship().getRoleToRoleRelationshipId() == Constants.INHERIT_ROLE_RELATIONSHIP_TYPE)
					processRole(roleToRole.getSecondaryRole(), processedRoles);
			}
		}
	}

	/**
	 * Used to authenticate generic users.
	 * @return true if the supplied credentials match a generic user, and false otherwise
	 */
	protected boolean genericLogin()
	{
		if ("admin".equals(credentials.getUsername()) && "nofate".equals(credentials.getPassword()))
		{
			identity.addRole("basicRole");
			identity.addRole("adminRole");
			return true;
		}

		if ("writer".equals(credentials.getUsername()))
		{
			identity.addRole("basicRole");
			identity.addRole("writerRole");
			return true;
		}

		if ("ia".equals(credentials.getUsername()))
		{
			identity.addRole("basicRole");
			identity.addRole("iaRole");
			return true;
		}

		if ("sme".equals(credentials.getUsername()))
		{
			identity.addRole("basicRole");
			identity.addRole("smeRole");
			return true;
		}

		if ("pm".equals(credentials.getUsername()))
		{
			identity.addRole("basicRole");
			identity.addRole("pmRole");
			identity.addRole("smeRole");
			return true;
		}

		if ("qe".equals(credentials.getUsername()))
		{
			identity.addRole("basicRole");
			identity.addRole("qeRole");
			return true;
		}

		if ("editor".equals(credentials.getUsername()))
		{
			identity.addRole("basicRole");
			identity.addRole("editorRole");
			identity.addRole("writerRole");
			return true;
		}

		if ("eng".equals(credentials.getUsername()))
		{
			identity.addRole("engRole");
			return true;
		}

		return false;
	}

	/**
	 * Authenticates a user
	 * @return true if the supplied credentials are valid, false otherwise
	 */
	public boolean authenticate()
	{
		log.info("authenticating {0}", credentials.getUsername());

		final String kerberosEnabled = System.getProperty(Constants.KERBEROS_ENABLED_SYSTEM_PROPERTY);
		final String fasEnabled = System.getProperty(Constants.FAS_ENABLED_SYSTEM_PROPERTY);
		
		// first attempt a kerberos login
		if ("true".equalsIgnoreCase(kerberosEnabled) && kerberosLogin())
			return true;
		
		if ("true".equalsIgnoreCase(fasEnabled) && fasLogin())
			return true;
		
		
		// if that fails, fall back to the generic user names
		return genericLogin();
	}

}
