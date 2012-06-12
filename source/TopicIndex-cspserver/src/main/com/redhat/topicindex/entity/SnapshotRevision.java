package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "SnapshotRevision")
public class SnapshotRevision extends AuditedEntity<SnapshotRevision> implements Serializable
{
	private static final long serialVersionUID = 2883473069724598551L;

	private int snapshotRevisionID;
	private Snapshot snapshot;
	private String snapshotRevisionName;
	private Date snapshotRevisionDate;
	private Set<SnapshotTranslatedData> snapshotTranslatedDataEntities = new HashSet<SnapshotTranslatedData>(0);
	
	public SnapshotRevision()
	{
		super(SnapshotRevision.class);
	}
	
	@Transient
	public Integer getId()
	{
		return this.snapshotRevisionID;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "SnapshotRevisionID", unique = true, nullable = false)
	public int getSnapshotRevisionID()
	{
		return snapshotRevisionID;
	}

	public void setSnapshotRevisionID(final int snapshotRevisionID)
	{
		this.snapshotRevisionID = snapshotRevisionID;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SnapshotID", nullable = false)
	@NotNull
	public Snapshot getSnapshot()
	{
		return snapshot;
	}

	public void setSnapshot(final Snapshot snapshot)
	{
		this.snapshot = snapshot;
	}

	@Column(name = "SnapshotRevisionName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getSnapshotRevisionName()
	{
		return snapshotRevisionName;
	}

	public void setSnapshotRevisionName(final String snapshotRevisionName)
	{
		this.snapshotRevisionName = snapshotRevisionName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SnapshotRevisionDate", nullable = false, length = 0)
	@NotNull
	public Date getSnapshotRevisionDate()
	{
		return snapshotRevisionDate;
	}

	public void setSnapshotRevisionDate(final Date snapshotRevisionDate)
	{
		this.snapshotRevisionDate = snapshotRevisionDate;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "snapshotRevision", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<SnapshotTranslatedData> getSnapshotTranslatedDataEntities()
	{
		return snapshotTranslatedDataEntities;
	}

	public void setSnapshotTranslatedDataEntities(Set<SnapshotTranslatedData> snapshotTranslatedDataEntities)
	{
		this.snapshotTranslatedDataEntities = snapshotTranslatedDataEntities;
	}
	
	@Transient
	public List<SnapshotTranslatedData> getSnapshotTranslatedDataEntitiesArray()
	{
		return CollectionUtilities.toArrayList(snapshotTranslatedDataEntities);
	}

}
