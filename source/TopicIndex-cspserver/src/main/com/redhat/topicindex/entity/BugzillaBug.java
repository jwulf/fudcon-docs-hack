package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "BugzillaBug")
public class BugzillaBug extends AuditedEntity<BugzillaBug> implements java.io.Serializable
{
	private static final long serialVersionUID = -2421128855519132960L;

	public static final String SELECT_ALL_QUERY = "select bugzillaBug from BugzillaBug bugzillaBug";

	private Integer bugzillaBugId;
	private Integer bugzillaBugBugzillaId;
	private String bugzillaBugSummary;
	private Boolean bugzillaBugOpen;
	private Set<TopicToBugzillaBug> topicToBugzillaBugs = new HashSet<TopicToBugzillaBug>(0);
	
	public BugzillaBug()
	{
		super(BugzillaBug.class);
	}
	
	@Override
	@Transient
	public Integer getId()
	{
		return this.bugzillaBugId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "BugzillaBugID", unique = true, nullable = false)
	public Integer getBugzillaBugId()
	{
		return bugzillaBugId;
	}

	public void setBugzillaBugId(final Integer bugzillaBugId)
	{
		this.bugzillaBugId = bugzillaBugId;
	}

	// @Column(name = "TranslatedXML", length = 65535)
	@Column(name = "BugzillaBugSummary", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getBugzillaBugSummary()
	{
		return bugzillaBugSummary;
	}

	public void setBugzillaBugSummary(final String bugzillaBugSummary)
	{
		this.bugzillaBugSummary = bugzillaBugSummary;
	}

	@Column(name = "BugzillaBugBugzillaID", nullable = false)
	@NotNull
	public Integer getBugzillaBugBugzillaId()
	{
		return bugzillaBugBugzillaId;
	}

	public void setBugzillaBugBugzillaId(final Integer bugzillaBugBugzillaId)
	{
		this.bugzillaBugBugzillaId = bugzillaBugBugzillaId;
	}
	
	@Column(name = "BugzillaBugOpen")
	public Boolean getBugzillaBugOpen()
	{
		return bugzillaBugOpen;
	}

	public void setBugzillaBugOpen(final Boolean bugzillaBugOpen)
	{
		this.bugzillaBugOpen = bugzillaBugOpen;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "bugzillaBug", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToBugzillaBug> getTopicToBugzillaBugs()
	{
		return this.topicToBugzillaBugs;
	}

	public void setTopicToBugzillaBugs(final Set<TopicToBugzillaBug> topicToBugzillaBugs)
	{
		this.topicToBugzillaBugs = topicToBugzillaBugs;
	}
	
	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		for (final TopicToBugzillaBug mapping : this.topicToBugzillaBugs)
			mapping.getTopic().getTopicToBugzillaBugs().remove(mapping);
		this.topicToBugzillaBugs.clear();
	}
}
