package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Entity
@Audited
@Table(name = "WorkingSnapshotTranslatedString")
public class WorkingSnapshotTranslatedString extends AuditedEntity<WorkingSnapshotTranslatedString> implements java.io.Serializable
{
	private static final long serialVersionUID = 5185674451816385008L;
	private Integer workingSnapshotTranslatedStringID;
	private WorkingSnapshotTranslatedData workingSnapshotTranslatedData;
	private String workingShapshotOriginalString;
	private String workingSnapshotTranslatedString;
	
	public WorkingSnapshotTranslatedString()
	{
		super(WorkingSnapshotTranslatedString.class);
	}
	
	@Transient
	public Integer getId()
	{
		return this.workingSnapshotTranslatedStringID;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "WorkingSnapshotTranslatedStringID", unique = true, nullable = false)
	public Integer getWorkingSnapshotTranslatedStringID()
	{
		return workingSnapshotTranslatedStringID;
	}

	public void setWorkingSnapshotTranslatedStringID(final Integer workingSnapshotTranslatedStringID)
	{
		this.workingSnapshotTranslatedStringID = workingSnapshotTranslatedStringID;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WorkingSnapshotTranslatedDataID", nullable = false)
	@NotNull
	public WorkingSnapshotTranslatedData getWorkingSnapshotTranslatedData()
	{
		return workingSnapshotTranslatedData;
	}

	public void setWorkingSnapshotTranslatedData(final WorkingSnapshotTranslatedData workingSnapshotTranslatedData)
	{
		this.workingSnapshotTranslatedData = workingSnapshotTranslatedData;
	}

	@Column(name = "WorkingShapshotOriginalString", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getWorkingShapshotOriginalString()
	{
		return workingShapshotOriginalString;
	}

	public void setWorkingShapshotOriginalString(final String workingShapshotOriginalString)
	{
		this.workingShapshotOriginalString = workingShapshotOriginalString;
	}

	@Column(name = "WorkingSnapshotTranslatedString", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getWorkingSnapshotTranslatedString()
	{
		return workingSnapshotTranslatedString;
	}

	public void setWorkingSnapshotTranslatedString(final String workingSnapshotTranslatedString)
	{
		this.workingSnapshotTranslatedString = workingSnapshotTranslatedString;
	}

}
