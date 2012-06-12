package com.redhat.topicindex.entity;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transaction;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.sort.TopicIDComparator;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.zanata.ZanataPullTopicThread;
import com.redhat.topicindex.zanata.ZanataPushTopicThread;

/**
 * A Snapshot is a collection of SnapshotTopics, which represent the state of a
 * topic at a fixed point in time. SnapshotTopics contain a collection of
 * WorkingSnapshotTranslatedDatas, which represent the current translated
 * string pulled out of Zanata.
 * 
 * A snapshot then contains a collection of SnapshotRevisions, which represent
 * the state of the translations at a point in time. SnapshotRevisions hold a
 * collection of SnapshotTranslatedData entities, which are fixed, read only
 * copies of the WorkingSnapshotTranslatedData entities at the time the SnapshotRevision was created.
 * 
 * The Zanata integration requires a number of system properties to be set. The
 * text below shows the settings for the test server:
 * 
 * <system-properties> <property name="topicIndex.zanataServer"
 * value="http://zanata-fortitude.lab.eng.bne.redhat.com:8080"/> <property
 * name="topicIndex.zanataProject" value="skynet"/> <property
 * name="topicIndex.zanataUsername" value="admin"/> <property
 * name="topicIndex.zanataProjectVersion" value="1"/> <property
 * name="topicIndex.zanataToken" value="b6d7044e9ee3b2447c28fb7c50d86d98"/>
 * </system-properties>
 */
@Entity
@Audited
@Table(name = "Snapshot", uniqueConstraints = @UniqueConstraint(columnNames = { "SnapshotName", "SnapshotDate" }))
public class Snapshot extends AuditedEntity<Snapshot> implements java.io.Serializable
{
	public static final String SELECT_ALL_QUERY = "select snapshot from Snapshot snapshot";
	private static final long serialVersionUID = 3885099782121274640L;

	private Integer snapshotId;
	private String snapshotName;
	private Date snapshotDate;
	private Set<SnapshotTopicToSnapshot> snapshotTopicToSnapshots = new HashSet<SnapshotTopicToSnapshot>(0);
	private Set<SnapshotRevision> snapshotRevisions = new HashSet<SnapshotRevision>(0);

	public Snapshot()
	{
		super(Snapshot.class);
	}

	@Transient
	public Integer getId()
	{
		return this.snapshotId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "SnapshotID", unique = true, nullable = false)
	public Integer getSnapshotId()
	{
		return snapshotId;
	}

	public void setSnapshotId(final Integer snapshotId)
	{
		this.snapshotId = snapshotId;
	}

	@Column(name = "SnapshotName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getSnapshotName()
	{
		return snapshotName;
	}

	public void setSnapshotName(final String snapshotName)
	{
		this.snapshotName = snapshotName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SnapshotDate", nullable = false, length = 0)
	@NotNull
	public Date getSnapshotDate()
	{
		return snapshotDate;
	}

	public void setSnapshotDate(final Date snapshotDate)
	{
		this.snapshotDate = snapshotDate;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "snapshot", orphanRemoval = false, cascade = CascadeType.ALL)
	public Set<SnapshotTopicToSnapshot> getSnapshotTopicToSnapshot()
	{
		return snapshotTopicToSnapshots;
	}

	public void setSnapshotTopicToSnapshot(final Set<SnapshotTopicToSnapshot> snapshotTopicToSnapshots)
	{
		this.snapshotTopicToSnapshots = snapshotTopicToSnapshots;
	}

	/**
	 * Using a list of topics and a date, find the historical versions of those
	 * topics and use that data to create a snapshot
	 */
	public void initailize(final EntityManager entityManager, final Transaction transaction, final String name, final Date date, final List<Topic> topics)
	{
		this.snapshotName = name;
		this.snapshotDate = date;
		this.snapshotTopicToSnapshots.clear();

		final AuditReader reader = AuditReaderFactory.get(entityManager);

		final List<Integer> processedTopics = new ArrayList<Integer>();
		int i = -1;

		/*
		 * A map of the original topic to the SnapshotTopic that will be
		 * referenced by this Snapshot. This will be used later on to retrieve
		 * the details of the topic for a given SnapshotTopic.
		 */
		final Map<SnapshotTopic, Topic> snapshotTopicToTopic = new HashMap<SnapshotTopic, Topic>();

		for (final Topic topic : topics)
		{
			++i;

			final int topicId = topic.getTopicId();

			/* see https://hibernate.onjira.com/browse/HHH-6824 */
			if (processedTopics.contains(topicId))
			{
				System.out.println("Duplicate topic at index " + i + " with TopicID of " + topicId);
				continue;
			}

			processedTopics.add(topicId);

			/*
			 * get the maximum revision number of the topic revisions that exist
			 * before the supplied date
			 */
			final Number revision = (Number) reader.createQuery().forRevisionsOfEntity(Topic.class, false, true).addProjection(AuditEntity.revisionNumber().max()).add(AuditEntity.id().eq(topicId)).add(AuditEntity.revisionProperty("timestamp").le(this.snapshotDate.getTime())).getSingleResult();

			if (revision != null)
			{

				final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

				final CriteriaQuery<SnapshotTopic> criteriaQuery = criteriaBuilder.createQuery(SnapshotTopic.class);
				final Root<SnapshotTopic> root = criteriaQuery.from(SnapshotTopic.class);
				final Predicate whereTopicId = criteriaBuilder.equal(root.get("topicId"), topicId);
				final Predicate whereTopicRevision = criteriaBuilder.equal(root.get("topicRevision"), revision);
				criteriaQuery.where(criteriaBuilder.and(whereTopicId, whereTopicRevision));

				final TypedQuery<SnapshotTopic> query = entityManager.createQuery(criteriaQuery);
				final List<SnapshotTopic> result = query.getResultList();

				if (result.size() != 0)
				{
					final SnapshotTopic snapshotTopic = result.get(0);

					final SnapshotTopicToSnapshot snapshotTopicToSnapshot = new SnapshotTopicToSnapshot();
					snapshotTopicToSnapshot.setSnapshot(this);
					snapshotTopicToSnapshot.setSnapshotTopic(snapshotTopic);

					this.snapshotTopicToSnapshots.add(snapshotTopicToSnapshot);
					snapshotTopicToTopic.put(snapshotTopic, topic);
				}
				else
				{
					final SnapshotTopic snapshotTopic = new SnapshotTopic();
					snapshotTopic.setTopicId(topicId);
					snapshotTopic.setTopicRevision((Integer) revision);

					/*
					 * Create a WorkingSnapshotTranslationData entity that
					 * represents the original locale
					 */
					final Topic revisionTopic = reader.find(Topic.class, topicId, revision);
					if (revisionTopic != null)
					{
						final String locale = revisionTopic.getTopicLocale();

						final WorkingSnapshotTranslatedData workingSnapshotTranslatedData = new WorkingSnapshotTranslatedData();
						workingSnapshotTranslatedData.setSnapshotTopic(snapshotTopic);
						workingSnapshotTranslatedData.setTranslatedXml(revisionTopic.getTopicXML());
						workingSnapshotTranslatedData.setTranslationLocale(locale == null ? System.getProperty(CommonConstants.DEFAULT_LOCALE_PROPERTY) : locale);
						snapshotTopic.getWorkingSnapshotTranslatedDataEntities().add(workingSnapshotTranslatedData);
					}

					/* Persist the new Snapshot Topic */
					entityManager.persist(snapshotTopic);

					final SnapshotTopicToSnapshot snapshotTopicToSnapshot = new SnapshotTopicToSnapshot();
					snapshotTopicToSnapshot.setSnapshot(this);
					snapshotTopicToSnapshot.setSnapshotTopic(snapshotTopic);

					this.snapshotTopicToSnapshots.add(snapshotTopicToSnapshot);

					snapshotTopicToTopic.put(snapshotTopic, topic);
				}
			}
		}

		/*
		 * The first time a snapshot is created, a default revision is also
		 * created. This revision has no translations.
		 */
		final SnapshotRevision snapshotRevision = new SnapshotRevision();
		snapshotRevision.setSnapshot(this);
		snapshotRevision.setSnapshotRevisionDate(new Date());
		snapshotRevision.setSnapshotRevisionName(Constants.INITIAL_SNAPSHOT_REVISION_NAME);
		this.snapshotRevisions.add(snapshotRevision);

		/*
		 * For each SnapshotTopic, we need to create an associated
		 * SnapshotTranslatedData object to represent the state of the
		 * translation at a particular point in time
		 */
		for (final SnapshotTopicToSnapshot mapping : this.snapshotTopicToSnapshots)
		{
			final SnapshotTopic snapshotTopic = mapping.getSnapshotTopic();

			if (snapshotTopicToTopic.containsKey(snapshotTopic))
			{
				final Topic topic = snapshotTopicToTopic.get(snapshotTopic);
				final String locale = topic.getTopicLocale();

				final SnapshotTranslatedData snapshotTranslatedData = new SnapshotTranslatedData();
				snapshotTranslatedData.setSnapshotRevision(snapshotRevision);
				snapshotTranslatedData.setSnapshotTopic(snapshotTopic);
				snapshotTranslatedData.setTranslatedXml(topic.getTopicXML());
				snapshotTranslatedData.setTranslationLocale(locale == null ? System.getProperty(CommonConstants.DEFAULT_LOCALE_PROPERTY) : locale);

				snapshotRevision.getSnapshotTranslatedDataEntities().add(snapshotTranslatedData);
			}
		}
	}

	@Transient
	private List<Integer> getSnapshotTopicIds()
	{
		final List<Integer> retValue = new ArrayList<Integer>();

		for (final SnapshotTopicToSnapshot mapping : this.snapshotTopicToSnapshots)
			retValue.add(mapping.getSnapshotTopic().getSnapshotTopicId());

		return retValue;
	}

	@Transient
	public List<String> getSnapshotTopicZanataIds()
	{
		final List<String> retValue = new ArrayList<String>();

		for (final SnapshotTopicToSnapshot mapping : this.snapshotTopicToSnapshots)
			retValue.add(mapping.getSnapshotTopic().getZanataId());

		return retValue;
	}

	/**
	 * Create a thread to push up the translation strings for the SnapshotTopics
	 * into Zanata
	 */
	public void pushSnapshotTopicsToZanata(final EntityManager entityManager)
	{
		final List<Pair<Integer, Integer>> topics = new ArrayList<Pair<Integer, Integer>>();
		for (final SnapshotTopicToSnapshot mapping : this.snapshotTopicToSnapshots)
		{
			final SnapshotTopic topic = mapping.getSnapshotTopic();
			topics.add(new Pair<Integer, Integer>(topic.getTopicId(), topic.getTopicRevision()));
		}

		final ZanataPushTopicThread zanataPushTopicThread = new ZanataPushTopicThread(topics, true);
		final Thread thread = new Thread(zanataPushTopicThread);
		thread.start();
	}

	/**
	 * Create a thread to pull down the translation strings from Zanata into
	 * SnapshotTranslationData entities
	 */
	public void pullSnapshotTopicsFromZanata()
	{
		final ZanataPullTopicThread zanataPullTopicThread = new ZanataPullTopicThread(getSnapshotTopicIds());
		final Thread thread = new Thread(zanataPullTopicThread);
		thread.start();
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "snapshot", orphanRemoval = false, cascade = CascadeType.ALL)
	public Set<SnapshotRevision> getSnapshotRevisions()
	{
		return snapshotRevisions;
	}

	public void setSnapshotRevisions(Set<SnapshotRevision> snapshotRevisions)
	{
		this.snapshotRevisions = snapshotRevisions;
	}

	@Transient
	public List<SnapshotRevision> getSnapshotRevisionsArray()
	{
		return CollectionUtilities.toArrayList(this.snapshotRevisions);
	}

	@Transient
	public List<SnapshotTopic> getSnapshotTopicsArray()
	{
		final List<SnapshotTopic> retValue = new ArrayList<SnapshotTopic>();
		for (final SnapshotTopicToSnapshot mapping : this.snapshotTopicToSnapshots)
			retValue.add(mapping.getSnapshotTopic());
		return retValue;
	}

	public void addSnapshotTopic(final EntityManager entityManager, final int id)
	{
		final SnapshotTopic entity = entityManager.getReference(SnapshotTopic.class, id);
		addSnapshotTopic(entity);
	}

	public void addSnapshotTopic(final int id)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final SnapshotTopic entity = entityManager.getReference(SnapshotTopic.class, id);
		addSnapshotTopic(entity);
	}

	public void addSnapshotTopic(final SnapshotTopic entity)
	{
		if (filter(having(on(SnapshotTopicToSnapshot.class).getSnapshotTopic(), equalTo(entity)), this.snapshotTopicToSnapshots).size() == 0)
		{
			this.snapshotTopicToSnapshots.add(new SnapshotTopicToSnapshot(entity, this));
		}
	}

	public void removeSnapshotTopic(final int id)
	{
		final List<SnapshotTopicToSnapshot> mappingEntities = filter(having(on(SnapshotTopicToSnapshot.class).getSnapshotTopic().getSnapshotTopicId(), equalTo(id)), this.snapshotTopicToSnapshots);
		if (mappingEntities.size() != 0)
		{
			this.snapshotTopicToSnapshots.remove(mappingEntities.get(0));
		}
	}

	public void removeSnapshotTopic(final SnapshotTopic entity)
	{
		removeSnapshotTopic(entity.getSnapshotTopicId());
	}

}
