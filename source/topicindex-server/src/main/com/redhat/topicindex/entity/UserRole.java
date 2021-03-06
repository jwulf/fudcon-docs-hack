package com.redhat.topicindex.entity;

// Generated Aug 8, 2011 9:37:09 AM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.PreRemove;
import javax.persistence.Transient;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

/**
 * UserRole generated by hbm2java
 */
@Entity
@Audited
@Table(name = "UserRole")
public class UserRole extends AuditedEntity<UserRole> implements java.io.Serializable
{

	private static final long serialVersionUID = 5397193862244957553L;
	private Integer userRoleId;
	private User user;
	private Role role;

	public UserRole()
	{
		super(UserRole.class);
	}

	public UserRole(final User user, final Role role)
	{
		super(UserRole.class);
		this.user = user;
		this.role = role;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "UserRoleID", unique = true, nullable = false)
	public Integer getUserRoleId()
	{
		return this.userRoleId;
	}

	public void setUserRoleId(Integer userRoleId)
	{
		this.userRoleId = userRoleId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "UserNameID", nullable = false)
	@NotNull
	public User getUser()
	{
		return this.user;
	}

	public void setUser(final User user)
	{
		this.user = user;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RoleNameID", nullable = false)
	@NotNull
	public Role getRole()
	{
		return this.role;
	}

	public void setRole(final Role role)
	{
		this.role = role;
	}
	
	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		this.user.getUserRoles().remove(this);
		this.role.getUserRoles().remove(this);
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.userRoleId;
	}
}
