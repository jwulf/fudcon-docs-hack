package com.redhat.topicindex.session;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.Subject;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;

/**
 * This class provides information about the user that is currently signed in. This is used to
 * display tooltips on the user name in the top bar.
 */
@Name("identityDetails")
public class IdentityDetails 
{
	/** The Seam identity object */
	@In private Identity identity;
	
	/**
	 * @return A collection of the roles that current users belongs to
	 */
	@SuppressWarnings("unchecked")
	public List<String> getIdentityRoles() 
	{
      final List<String> roles = new ArrayList<String>();
      final Subject subject = identity.getSubject();

      for (final Group sg : subject.getPrincipals(Group.class)) 
      {
         if (Identity.ROLES_GROUP.equals(sg.getName())) 
         {
            final Enumeration<Principal> e = (Enumeration<Principal>) sg.members();
            while (e.hasMoreElements()) 
            {
               final Principal member = e.nextElement();
               roles.add(member.getName());
            }
         }
      }
      return roles;
   }
	
	/** 
	 * @return A common separated list of the roles the current user belongs to 
	 */
	public String getRolesString()
	{
		String retValue = "";
		for (final String role : getIdentityRoles())
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += role;
		}
		return "Roles: " + (retValue.length() == 0 ? "EMPTY" : retValue);
	}
}
