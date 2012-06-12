package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.entity.base.AuditedEntity;

/**
 * A SnapshotTopic represents a particular revision of a topic. This revision
 * then holds the translated version of the document for various locales in a
 * collection of WorkingSnapshotTranslatedData entities.
 */
@Entity
@Audited
@Table(name = "SnapshotTopic", uniqueConstraints = @UniqueConstraint(columnNames = { "TopicRevision", "TopicID" }))
public class SnapshotTopic extends AuditedEntity<SnapshotTopic> implements java.io.Serializable
{
	private static final long serialVersionUID = 4190214754023153898L;
	public static final String SELECT_ALL_QUERY = "select snapshotTopic from SnapshotTopic snapshotTopic";
	private Integer snapshotTopicId;
	private Integer topicId;
	private Integer topicRevision;
	private Set<WorkingSnapshotTranslatedData> workingSnapshotTranslatedDataEntities = new HashSet<WorkingSnapshotTranslatedData>(0);

	public SnapshotTopic()
	{
		super(SnapshotTopic.class);
	}

	@Transient
	public Integer getId()
	{
		return this.snapshotTopicId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "SnapshotTopicID", unique = true, nullable = false)
	public Integer getSnapshotTopicId()
	{
		return snapshotTopicId;
	}

	public void setSnapshotTopicId(final Integer snapshotTopicId)
	{
		this.snapshotTopicId = snapshotTopicId;
	}

	@Column(name = "TopicID", nullable = false)
	public Integer getTopicId()
	{
		return topicId;
	}

	public void setTopicId(final Integer topicId)
	{
		this.topicId = topicId;
	}

	@Column(name = "TopicRevision", nullable = false)
	public Integer getTopicRevision()
	{
		return topicRevision;
	}

	public void setTopicRevision(final Integer topicRevision)
	{
		this.topicRevision = topicRevision;
	}

	/**
	 * @return The File ID used to identify this topic and revision in Zanata
	 */
	@Transient
	public String getZanataId()
	{
		return this.topicId + "-" + this.topicRevision;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "snapshotTopic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<WorkingSnapshotTranslatedData> getWorkingSnapshotTranslatedDataEntities()
	{
		return workingSnapshotTranslatedDataEntities;
	}

	public void setWorkingSnapshotTranslatedDataEntities(final Set<WorkingSnapshotTranslatedData> workingSnapshotTranslatedDataEntities)
	{
		this.workingSnapshotTranslatedDataEntities = workingSnapshotTranslatedDataEntities;
	}

	@Transient
	public List<WorkingSnapshotTranslatedData> getWorkingSnapshotTranslatedDataEntitiesArray()
	{
		return CollectionUtilities.toArrayList(this.workingSnapshotTranslatedDataEntities);
	}
}
