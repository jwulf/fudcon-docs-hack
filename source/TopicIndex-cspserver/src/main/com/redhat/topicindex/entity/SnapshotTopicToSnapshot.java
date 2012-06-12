package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "SnapshotTopicToSnapshot")
public class SnapshotTopicToSnapshot extends AuditedEntity<SnapshotTopicToSnapshot> implements java.io.Serializable
{
	private static final long serialVersionUID = 7952025266760852329L;
	private Integer SnapshotTopicToSnapshotID;
	private Snapshot Snapshot;
	private SnapshotTopic SnapshotTopic;
	
	public SnapshotTopicToSnapshot(final SnapshotTopic snapshotTopic, final Snapshot snapshot)
	{
		this();
		this.SnapshotTopic = snapshotTopic;
		this.Snapshot = snapshot;
	}
	
	public SnapshotTopicToSnapshot()
	{
		super(SnapshotTopicToSnapshot.class);
	}
	
	@Transient
	public Integer getId()
	{
		return this.SnapshotTopicToSnapshotID;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "SnapshotTopicToSnapshotID", unique = true, nullable = false)
	public Integer getSnapshotTopicToSnapshotID()
	{
		return SnapshotTopicToSnapshotID;
	}

	public void setSnapshotTopicToSnapshotID(final Integer snapshotTopicToSnapshotID)
	{
		this.SnapshotTopicToSnapshotID = snapshotTopicToSnapshotID;
	}

	@ManyToOne
	@JoinColumn(name = "SnapshotID", nullable = false)
	@NotNull
	public Snapshot getSnapshot()
	{
		return Snapshot;
	}

	public void setSnapshot(final Snapshot snapshot)
	{
		this.Snapshot = snapshot;
	}

	@ManyToOne
	@JoinColumn(name = "SnapshotTopicID", nullable = false)
	@NotNull
	public SnapshotTopic getSnapshotTopic()
	{
		return SnapshotTopic;
	}

	public void setSnapshotTopic(final SnapshotTopic snapshotTopic)
	{
		this.SnapshotTopic = snapshotTopic;
	}
}
