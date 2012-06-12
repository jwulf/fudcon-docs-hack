package com.redhat.topicindex.entity;

// Generated Aug 8, 2011 9:22:31 AM by Hibernate Tools 3.4.0.CR1

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.PreRemove;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "Role", uniqueConstraints = @UniqueConstraint(columnNames = { "RoleName" }))
public class Role extends AuditedEntity<Role> implements java.io.Serializable
{
	public static final String SELECT_ALL_QUERY = "select role from Role role";
	private static final long serialVersionUID = 894929331710959265L;
	private Integer roleId;
	private String roleName;
	private String description;
	private Set<UserRole> userRoles = new HashSet<UserRole>(0);
	private Set<RoleToRole> childrenRoleToRole = new HashSet<RoleToRole>(0);
	private Set<RoleToRole> parentRoleToRole = new HashSet<RoleToRole>(0);

	public Role()
	{
		super(Role.class);
	}

	public Role(final String roleName)
	{
		super(Role.class);
		this.roleName = roleName;
	}

	public Role(final String roleName, final String description, final Set<UserRole> userRoles)
	{
		super(Role.class);
		this.roleName = roleName;
		this.description = description;
		this.userRoles = userRoles;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "RoleID", unique = true, nullable = false)
	public Integer getRoleId()
	{
		return this.roleId;
	}

	public void setRoleId(Integer roleId)
	{
		this.roleId = roleId;
	}

	@Column(name = "RoleName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getRoleName()
	{
		return this.roleName;
	}

	public void setRoleName(String roleName)
	{
		this.roleName = roleName;
	}

	// @Column(name = "Description", length = 512)
	@Column(name = "Description", columnDefinition = "TEXT")
	@Length(max = 512)
	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "role", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, orphanRemoval = true)
	public Set<UserRole> getUserRoles()
	{
		return this.userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles)
	{
		this.userRoles = userRoles;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "secondaryRole", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, orphanRemoval = true)
	public Set<RoleToRole> getChildrenRoleToRole()
	{
		return childrenRoleToRole;
	}

	public void setChildrenRoleToRole(final Set<RoleToRole> childrenRoleToRole)
	{
		this.childrenRoleToRole = childrenRoleToRole;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "primaryRole", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, orphanRemoval = true)
	public Set<RoleToRole> getParentRoleToRole()
	{
		return parentRoleToRole;
	}

	public void setParentRoleToRole(final Set<RoleToRole> parentRoleToRole)
	{
		this.parentRoleToRole = parentRoleToRole;
	}

	public boolean hasUser(final User user)
	{
		return hasUser(user.getUserId());
	}

	public boolean hasUser(final Integer user)
	{
		for (final UserRole userRole : this.userRoles)
		{
			if (userRole.getUser().getUserId().equals(user))
				return true;
		}

		return false;
	}

	public void addUser(final User user)
	{
		if (!hasUser(user))
		{
			final UserRole userRole = new UserRole(user, this);
			this.getUserRoles().add(userRole);
			user.getUserRoles().add(userRole);
		}
	}

	public void removeUser(final User user)
	{
		removeUser(user.getUserId());
	}

	public void removeUser(final Integer userId)
	{
		for (final UserRole userRole : this.userRoles)
		{
			if (userRole.getUser().getUserId().equals(userId))
			{
				this.getUserRoles().remove(userRole);
				userRole.getUser().getUserRoles().remove(userRole);
				break;
			}
		}
	}

	@Transient
	public String getUsersCommaSeperatedList()
	{
		String retValue = "";
		for (final UserRole role : this.userRoles)
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += role.getUser().getUserName();
		}
		return retValue;
	}

	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		this.userRoles.clear();
		this.childrenRoleToRole.clear();
		this.parentRoleToRole.clear();
	}

	@Transient
	public List<User> getUsers()
	{
		final List<User> retValue = new ArrayList<User>();

		for (final UserRole userRole : userRoles)
		{
			retValue.add(userRole.getUser());
		}

		return retValue;
	}

	@Transient
	public List<Role> getParentRoles()
	{
		final List<Role> retValue = new ArrayList<Role>();

		for (final RoleToRole userRole : parentRoleToRole)
		{
			retValue.add(userRole.getSecondaryRole());
		}

		return retValue;
	}

	@Transient
	public List<Role> getChildRoles()
	{
		final List<Role> retValue = new ArrayList<Role>();

		for (final RoleToRole userRole : parentRoleToRole)
		{
			retValue.add(userRole.getPrimaryRole());
		}

		return retValue;
	}
	
	public RoleToRole getParentRole(final Integer role, final Integer relationshipID)
	{
		for (final RoleToRole roleToRole : this.parentRoleToRole)
		{
			if (roleToRole.getSecondaryRole().getRoleId().equals(role) && roleToRole.getRoleToRoleRelationship().getRoleToRoleRelationshipId().equals(relationshipID))
				return roleToRole;
		}

		return null;
	}

	public boolean hasParentRole(final Integer role, final Integer relationshipID)
	{
		return getParentRole(role, relationshipID) != null;
	}
	
	public RoleToRole getChildRole(final Integer role, final Integer relationshipID)
	{
		for (final RoleToRole roleToRole : this.getChildrenRoleToRole())
		{
			if (roleToRole.getPrimaryRole().getRoleId().equals(role)  && roleToRole.getRoleToRoleRelationship().getRoleToRoleRelationshipId().equals(relationshipID))
				return roleToRole;
		}

		return null;
	}
	
	public boolean hasChildRole(final Integer role, final Integer relationshipID)
	{
		return getChildRole(role, relationshipID) != null;
	}

	public void addParentRole(final Role role, final RoleToRoleRelationship roleToRoleRelationship)
	{
		if (!hasParentRole(role.getRoleId(), roleToRoleRelationship.getRoleToRoleRelationshipId()))
		{			
			final RoleToRole roleToRole = new RoleToRole(roleToRoleRelationship, this, role);
			this.getParentRoleToRole().add(roleToRole);
			role.getChildrenRoleToRole().add(roleToRole);
		}
	}
	
	public void addChildRole(final Role role, final RoleToRoleRelationship roleToRoleRelationship)
	{
		if (!hasChildRole(role.getRoleId(), roleToRoleRelationship.getRoleToRoleRelationshipId()))
		{			
			final RoleToRole roleToRole = new RoleToRole(roleToRoleRelationship, role, this);
			this.getChildrenRoleToRole().add(roleToRole);
			role.getParentRoleToRole().add(roleToRole);
		}
	}
	
	public void removeParentRole(final Role role, final RoleToRoleRelationship roleToRoleRelationship)
	{
		final RoleToRole roleToRole = getParentRole(role.getRoleId(), roleToRoleRelationship.getRoleToRoleRelationshipId());
		if (roleToRole != null)
		{			
			this.getParentRoleToRole().remove(roleToRole);
			role.getChildrenRoleToRole().remove(roleToRole);
		}
	}
	
	public void removeChildRole(final Role role, final RoleToRoleRelationship roleToRoleRelationship)
	{
		final RoleToRole roleToRole = getChildRole(role.getRoleId(), roleToRoleRelationship.getRoleToRoleRelationshipId());
		if (roleToRole != null)
		{			
			this.getChildrenRoleToRole().remove(roleToRole);
			role.getParentRoleToRole().remove(roleToRole);
		}
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.roleId;
	}

}
