package com.redhat.topicindex.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.RelationshipTag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;

@Name("relatedTopicTagsList")
public class RelatedTopicTagsList extends GroupedTopicListBase implements DisplayMessageInterface, Serializable
{
	private static final long serialVersionUID = 1724809677704029918L;
	/** The id of the main topic */
	private Integer topicTopicId;
	/** The actual Topic object found with the topicTopicId */
	private Topic instance;
	/** The object that holds the filter field values */
	private TopicFilter topic = new TopicFilter();
	/** A list of the current snapshots */
	private List<SelectItem> relationshipTags = new ArrayList<SelectItem>();
	/** The selected RelationshipTag ID */
	private Integer selectedRelationshipTagID;
	/** The message to be displayed to the user */
	private String displayMessage;

	@Override
	public String getDisplayMessage()
	{
		return displayMessage;
	}

	@Override
	public String getDisplayMessageAndClear()
	{
		final String retValue = this.displayMessage;
		this.displayMessage = null;
		return retValue;
	}

	public void setDisplayMessage(final String displayMessage)
	{
		this.displayMessage = displayMessage;
	}

	public RelatedTopicTagsList()
	{

	}

	@Override
	@Create
	public void create()
	{
		super.create();

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// build up a Filter object from the URL variables
		final Filter filter = EntityUtilities.populateFilter(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), Constants.FILTER_ID,
				Constants.MATCH_TAG, Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

		/*
		 * preselect the tags on the web page that relate to the tags selected by
		 * the filter
		 */
		selectedTags = new UIProjectsData();
		selectedTags.populateTopicTags(filter, false);

		/* sync up the filter field values */
		for (final FilterField field : filter.getFilterFields())
			this.topic.setFieldValue(field.getField(), field.getValue());

		/* Get the list of RelationshipTags */
		final List<RelationshipTag> tags = entityManager.createQuery(RelationshipTag.SELECT_ALL_QUERY).getResultList();
		for (final RelationshipTag tag : tags)
		{
			this.relationshipTags.add(new SelectItem(tag.getRelationshipTagId(), tag.getRelationshipTagName()));
			if (this.selectedRelationshipTagID == null)
				this.selectedRelationshipTagID = tag.getRelationshipTagId();
		}
	}

	public void oneWayToAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, this.selectedRelationshipTagID);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				final boolean isChild = mainTopic.isRelatedTo(topic, relationshipTag);

				if (!isChild && !mainTopic.equals(topic))
					mainTopic.addRelationshipTo(topic, relationshipTag);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void oneWayFromAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, this.selectedRelationshipTagID);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				final boolean isChild = topic.isRelatedTo(mainTopic, relationshipTag);

				if (!isChild && !mainTopic.equals(topic))
				{
					topic.addRelationshipTo(mainTopic, relationshipTag);
					entityManager.persist(topic);
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void twoWayWithAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, this.selectedRelationshipTagID);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				final boolean isMainTopicChild = topic.isRelatedTo(mainTopic, relationshipTag);

				if (!isMainTopicChild && !mainTopic.equals(topic))
				{
					topic.addRelationshipTo(mainTopic, relationshipTag);
					entityManager.persist(topic);
				}

				final boolean isTopicChild = mainTopic.isRelatedTo(topic, relationshipTag);

				if (!isTopicChild && !mainTopic.equals(topic))
					mainTopic.addRelationshipTo(topic, relationshipTag);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void removeToAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final RelationshipTag relationshipTag = entityManager.find(RelationshipTag.class, this.selectedRelationshipTagID);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				mainTopic.removeRelationshipTo(topic, relationshipTag);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void removeFromAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final RelationshipTag relationshipTag = entityManager.find(RelationshipTag.class, this.selectedRelationshipTagID);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				if (topic.removeRelationshipTo(mainTopic, relationshipTag))
					entityManager.persist(topic);
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void removeBetweenAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final RelationshipTag relationshipTag = entityManager.find(RelationshipTag.class, this.selectedRelationshipTagID);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				if (topic.removeRelationshipTo(mainTopic, relationshipTag))
				{
					entityManager.persist(topic);
				}

				mainTopic.removeRelationshipTo(topic, relationshipTag);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public String doSearch()
	{
		return "/CustomRelatedTopicList.seam?" + getSearchUrlVars();
	}

	protected String getSearchUrlVars()
	{
		return getSearchUrlVars(null);
	}

	protected String getSearchUrlVars(final String startRecord)
	{
		final Filter filter = new Filter();
		filter.syncFilterWithCategories(selectedTags);
		filter.syncFilterWithFieldUIElements(topic);
		filter.syncFilterWithTags(selectedTags);

		final String params = filter.buildFilterUrlVars();
		return "topicTopicId=" + topicTopicId + "&" + params;
	}

	public String removeRelationship(final Integer otherTopicId, final boolean to, final boolean from, final boolean returnToSearch)
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic thisTopic = entityManager.find(Topic.class, topicTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);
			final RelationshipTag relationshipTag = entityManager.find(RelationshipTag.class, this.selectedRelationshipTagID);

			if (from)
			{
				if (thisTopic.removeRelationshipTo(otherTopic, relationshipTag))
				{
					entityManager.persist(thisTopic);
					instance = thisTopic;
				}
			}

			if (to)
			{
				if (otherTopic.removeRelationshipTo(thisTopic, relationshipTag))
				{
					entityManager.persist(otherTopic);
				}
			}

			entityManager.flush();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving or persiting Topics in the database");
		}

		final String retValue = returnToSearch
				? "/CustomSearchTopicList.xhtml"
				: null;

		return retValue;
	}

	public String createRelationship(final Integer otherTopicId, final boolean to, final boolean from, final boolean returnToSearch)
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic thisTopic = entityManager.find(Topic.class, topicTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);
			final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, this.selectedRelationshipTagID);

			if (from)
			{
				if (!thisTopic.isRelatedTo(otherTopic, relationshipTag) && !thisTopic.equals(otherTopic))
				{
					if (thisTopic.addRelationshipTo(otherTopic, relationshipTag))
					{
						entityManager.persist(thisTopic);
					}
				}
			}

			if (to)
			{
				if (!otherTopic.isRelatedTo(thisTopic, relationshipTag) && !thisTopic.equals(otherTopic))
				{
					if (otherTopic.addRelationshipTo(thisTopic, relationshipTag))
					{
						entityManager.persist(otherTopic);
						entityManager.refresh(thisTopic);
					}
				}
			}

			instance = thisTopic;
			entityManager.flush();

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving or perssting Topics in the database");
		}

		final String retValue = returnToSearch
				? "/CustomSearchTopicList.xhtml"
				: null;

		return retValue;
	}

	public void setTopicTopicId(final Integer topicTopicId)
	{
		this.topicTopicId = topicTopicId;

		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			instance = entityManager.find(Topic.class, topicTopicId);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving a Topic from the database");
		}
	}

	public Integer getTopicTopicId()
	{
		return topicTopicId;
	}

	public void setInstance(final Topic instance)
	{
		this.instance = instance;
	}

	public Topic getInstance()
	{
		return instance;
	}

	public TopicFilter getTopic()
	{
		return topic;
	}

	public void setTopic(final TopicFilter topic)
	{
		this.topic = topic;
	}

	public Integer getSelectedRelationshipTagID()
	{
		return selectedRelationshipTagID;
	}

	public void setSelectedRelationshipTagID(final Integer selectedRelationshipTagID)
	{
		this.selectedRelationshipTagID = selectedRelationshipTagID;
	}

	public List<SelectItem> getRelationshipTags()
	{
		return relationshipTags;
	}

	public void setRelationshipTags(List<SelectItem> relationshipTags)
	{
		this.relationshipTags = relationshipTags;
	}
}
