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
@Table(name = "SnapshotTranslatedString")
public class SnapshotTranslatedString extends AuditedEntity<SnapshotTranslatedString> implements java.io.Serializable
{
	private static final long serialVersionUID = 3793983622660908661L;

	private Integer snapshotTranslatedStringID;
	private SnapshotTranslatedData snapshotTranslatedData;
	private String shapshotOriginalString;
	private String snapshotTranslatedString;
	
	public SnapshotTranslatedString()
	{
		super(SnapshotTranslatedString.class);
	}
	
	@Transient
	public Integer getId()
	{
		return this.snapshotTranslatedStringID;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "SnapshotTranslatedStringID", unique = true, nullable = false)
	public Integer getSnapshotTranslatedStringID()
	{
		return snapshotTranslatedStringID;
	}

	public void setSnapshotTranslatedStringID(Integer snapshotTranslatedStringID)
	{
		this.snapshotTranslatedStringID = snapshotTranslatedStringID;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SnapshotTranslatedDataID", nullable = false)
	@NotNull
	public SnapshotTranslatedData getSnapshotTranslatedData()
	{
		return snapshotTranslatedData;
	}

	public void setSnapshotTranslatedData(final SnapshotTranslatedData snapshotTranslatedData)
	{
		this.snapshotTranslatedData = snapshotTranslatedData;
	}

	@Column(name = "ShapshotOriginalString", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getShapshotOriginalString()
	{
		return shapshotOriginalString;
	}

	public void setShapshotOriginalString(final String shapshotOriginalString)
	{
		this.shapshotOriginalString = shapshotOriginalString;
	}

	@Column(name = "SnapshotTranslatedString", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getSnapshotTranslatedString()
	{
		return snapshotTranslatedString;
	}

	public void setSnapshotTranslatedString(final String snapshotTranslatedString)
	{
		this.snapshotTranslatedString = snapshotTranslatedString;
	}

}
