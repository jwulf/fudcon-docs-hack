package com.redhat.topicindex.session;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.ecs.services.docbookcompiling.DocbookBuildingOptions;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Snapshot;
import com.redhat.topicindex.entity.SnapshotRevision;
import com.redhat.topicindex.entity.SnapshotTopic;
import com.redhat.topicindex.entity.SnapshotTopicToSnapshot;
import com.redhat.topicindex.entity.SnapshotTranslatedData;
import com.redhat.topicindex.entity.SnapshotTranslatedString;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedData;
import com.redhat.topicindex.entity.WorkingSnapshotTranslatedString;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.structures.locales.UILocale;
import com.redhat.topicindex.utils.structures.locales.UILocales;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.messaging.TopicRendererType;

/**
 * The backing bean for the Snapshots page
 */
@Name("snapshots")
@Scope(ScopeType.PAGE)
public class Snapshots implements Serializable
{
	private static final long serialVersionUID = 7038462233325730801L;
	/** A list of locales that can be selected */
	private UILocales selectedLocales = new UILocales();
	/**
	 * a mapping of project and category details to tag details with sorting and
	 * selection information
	 */
	private UIProjectsData selectedTags = new UIProjectsData();
	/** The data structure that holds the DocBook building options */
	private DocbookBuildingOptions docbookBuildingOptions = new DocbookBuildingOptions();
	/** The ID of the selected Snapshot entity */
	private Integer selectedSnapshot;
	/** The ID of the selected Snapshot revision */
	private Integer selectedSnapshotRevision;
	/** A list of the current snapshots */
	private List<SelectItem> snapshots = new ArrayList<SelectItem>();
	/** A list of the revisions for the current snapshot */
	private List<SelectItem> snapshotRevisions = new ArrayList<SelectItem>();
	/** The object that holds the topic filter field values */
	private TopicFilter topic = new TopicFilter();
	/** The name given to a new SnapshotRevision */
	private String snapshotRevisionName;

	public UILocales getSelectedLocales()
	{
		return selectedLocales;
	}

	public void setSelectedLocales(final UILocales selectedLocales)
	{
		this.selectedLocales = selectedLocales;
	}
	
	public TopicFilter getTopic()
	{
		return topic;
	}

	public void setTopic(final TopicFilter topic)
	{
		this.topic = topic;
	}

	public List<SelectItem> getSnapshots()
	{
		return snapshots;
	}

	public void setSnapshots(final List<SelectItem> snapshots)
	{
		this.snapshots = snapshots;
	}

	public List<SelectItem> getSnapshotRevisions()
	{
		return snapshotRevisions;
	}

	public void setSnapshotRevisions(final List<SelectItem> snapshotRevisions)
	{
		this.snapshotRevisions = snapshotRevisions;
	}

	public Integer getSelectedSnapshotRevision()
	{
		return selectedSnapshotRevision;
	}

	public void setSelectedSnapshotRevision(final Integer selectedSnapshotRevision)
	{
		this.selectedSnapshotRevision = selectedSnapshotRevision;
	}

	public Integer getSelectedSnapshot()
	{
		return selectedSnapshot;
	}

	public void setSelectedSnapshot(final Integer selectedSnapshot)
	{
		this.selectedSnapshot = selectedSnapshot;
	}

	public DocbookBuildingOptions getDocbookBuildingOptions()
	{
		return docbookBuildingOptions;
	}

	public void setDocbookBuildingOptions(final DocbookBuildingOptions docbookBuildingOptions)
	{
		this.docbookBuildingOptions = docbookBuildingOptions;
	}

	public UIProjectsData getSelectedTags()
	{
		return selectedTags;
	}

	public void setSelectedTags(final UIProjectsData selectedTags)
	{
		this.selectedTags = selectedTags;
	}

	public void populate()
	{
		final FacesContext context = FacesContext.getCurrentInstance();
		final Filter filter = EntityUtilities.populateFilter(context.getExternalContext().getRequestParameterMap(), Constants.FILTER_ID, Constants.MATCH_TAG,
				Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

		this.selectedTags.populateTopicTags(filter, false);
		this.topic.syncWithFilter(filter);
		filter.updateDocbookOptionsFilter(this.docbookBuildingOptions);

		loadSnapshots();
		loadSnapshotRevisions();
	}

	public void pushToZanata()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Snapshot snapshot = entityManager.find(Snapshot.class, this.selectedSnapshot);
			if (snapshot != null)
				snapshot.pushSnapshotTopicsToZanata(entityManager);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an error retrieving the EntityManager");
		}
	}

	public void pullFromZanata()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Snapshot snapshot = entityManager.find(Snapshot.class, this.selectedSnapshot);
			if (snapshot != null)
				snapshot.pullSnapshotTopicsFromZanata();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Catch all exception handler");
		}
	}

	public void createRevision()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Snapshot snapshot = entityManager.find(Snapshot.class, this.selectedSnapshot);
			if (snapshot != null)
			{
				/* Create a new SnapshotRevision */
				final SnapshotRevision snapshotRevision = new SnapshotRevision();
				snapshotRevision.setSnapshot(snapshot);
				snapshotRevision.setSnapshotRevisionDate(new Date());
				snapshotRevision.setSnapshotRevisionName(getSnapshotRevisionName());

				/* Make sure the SnapshotRevision has an ID */
				entityManager.persist(snapshotRevision);

				/* Copy the working translations into the SnapshotRevision */
				final List<Integer> snapshotTranslatedDataIds = new ArrayList<Integer>();
				for (final SnapshotTopicToSnapshot snapshotTopicToSnapshot : snapshot.getSnapshotTopicToSnapshot())
				{
					final SnapshotTopic snapshotTopic = snapshotTopicToSnapshot.getSnapshotTopic();

					for (final WorkingSnapshotTranslatedData working : snapshotTopic.getWorkingSnapshotTranslatedDataEntities())
					{
						final SnapshotTranslatedData snapshotTranslatedData = new SnapshotTranslatedData();
						snapshotTranslatedData.setSnapshotTopic(snapshotTopic);
						snapshotTranslatedData.setSnapshotRevision(snapshotRevision);
						snapshotTranslatedData.setTranslationLocale(working.getTranslationLocale());
						snapshotTranslatedData.setTranslatedXml(working.getTranslatedXml());

						/* Make sure the SnapshotTranslatedData has an ID */
						entityManager.persist(snapshotTranslatedData);

						/*
						 * save the id so we can spawn a thread to render the
						 * SnapshotTranslatedData
						 */
						snapshotTranslatedDataIds.add(snapshotTranslatedData.getSnapshotTranslatedDataId());

						/* Copy the translated strings */
						for (final WorkingSnapshotTranslatedString translatedString : working.getWorkingSnapshotTranslatedStrings())
						{
							final SnapshotTranslatedString snapshotTranslatedString = new SnapshotTranslatedString();
							snapshotTranslatedString.setShapshotOriginalString(translatedString.getWorkingShapshotOriginalString());
							snapshotTranslatedString.setSnapshotTranslatedString(translatedString.getWorkingSnapshotTranslatedString());
							snapshotTranslatedString.setSnapshotTranslatedData(snapshotTranslatedData);	
						}
						
						/* rerender the snapshottranslatedstring entities */
						WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(snapshotTranslatedData.getSnapshotTranslatedDataId(), TopicRendererType.SNAPSHOTTOPIC));
					}
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Catch all exception handler");
		}
	}

	public void loadSnapshot()
	{
		loadSnapshotRevisions();
	}

	@SuppressWarnings("unchecked")
	private void loadSnapshots()
	{
		try
		{
			this.snapshots.clear();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<Snapshot> snapshots = entityManager.createQuery(Snapshot.SELECT_ALL_QUERY).getResultList();

			this.snapshots.add(new SelectItem(null, ""));

			for (final Snapshot snapshot : snapshots)
			{
				final SimpleDateFormat formatter = new SimpleDateFormat(CommonConstants.FILTER_DISPLAY_DATE_FORMAT);
				final String formattedDate = formatter.format(snapshot.getSnapshotDate());
				final SelectItem selectItem = new SelectItem(snapshot.getSnapshotId(), snapshot.getSnapshotName() + " [Created: " + formattedDate + "] [Topics: "
						+ snapshot.getSnapshotTopicToSnapshot().size() + "]");
				this.snapshots.add(selectItem);
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an error retrieving Snapshot entities");
		}
	}

	private void loadSnapshotRevisions()
	{
		try
		{
			this.snapshotRevisions.clear();

			if (this.selectedSnapshot != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Snapshot snapshot = entityManager.find(Snapshot.class, this.selectedSnapshot);

				if (snapshot != null)
				{
					this.snapshotRevisions.add(new SelectItem(null, "Working Translations"));

					for (final SnapshotRevision snapshotRevision : snapshot.getSnapshotRevisions())
					{
						final SimpleDateFormat formatter = new SimpleDateFormat(CommonConstants.FILTER_DISPLAY_DATE_FORMAT);
						final String formattedDate = formatter.format(snapshotRevision.getSnapshotRevisionDate());
						final SelectItem selectItem = new SelectItem(snapshotRevision.getSnapshotRevisionID(), snapshotRevision.getSnapshotRevisionName() + " [Created: "
								+ formattedDate + "]");
						this.snapshotRevisions.add(selectItem);
					}
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Catch all exception handler");
		}
	}

	public String getSnapshotRevisionName()
	{
		return snapshotRevisionName;
	}

	public void setSnapshotRevisionName(final String snapshotRevisionName)
	{
		this.snapshotRevisionName = snapshotRevisionName;
	}

	public String doSearch()
	{
		final Filter filter = new Filter();
		filter.syncFilterWithCategories(selectedTags);
		filter.syncFilterWithFieldUIElements(topic);
		filter.syncFilterWithTags(selectedTags);

		String params = filter.buildFilterUrlVars();

		if (params.length() != 0)
			params += "&";

		if (this.selectedSnapshotRevision != null)
			params += Constants.SNAPSHOT_REVISION_ID + "=" + this.selectedSnapshotRevision;
		else
			params += Constants.SNAPSHOT_ID + "=" + this.selectedSnapshot;
		
		
		String groupedLocales = "";
		for (final UILocale uiLocale :  this.selectedLocales.getLocales())
		{
			if (uiLocale.isSelected())
			{
				if (groupedLocales.length() != 0)
					groupedLocales += ",";
				
				groupedLocales += uiLocale.getName();
			}
		}
		
		if (groupedLocales.length() != 0)
		{
			if (params.length() != 0)
				params += "&";
			params += Constants.GROUP_LOCALE + "=" + groupedLocales;
		}

		return "/GroupedSnapshotTranslatedDataList.seam?" + params;
	}
	
	public void buildDocbook()
	{
		
	}

	public void downloadXML()
	{
		
	}
	
	public void downloadCSV()
	{
		
	}

}
