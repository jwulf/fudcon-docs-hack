package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import org.jboss.seam.Component;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.RelationshipTag;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilterQueryBuilder;
import com.redhat.topicindex.sort.GroupedTopicListNameComparator;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.structures.GroupedList;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;

public class GroupedTopicListBase extends GroupedListBase<ExtendedTopicList>
{
	/**
	 * a mapping of category details to tag details with sorting and selection
	 * information
	 */
	protected UIProjectsData selectedTags;

	/** Used by the action links */
	protected Integer actionTopicId;

	/** Used by the remove relationship link */
	protected Integer otherTopicId;

	/** Used by the remove relationship link */
	protected Integer relationshipTagId;

	/** Used by the remove relationship link */
	private Integer newRelationshipTagId;
	
	/** URL used by the .page.xml file to redirect the user */
	private String externalURL;
	
	public String getExternalURL()
	{
		return externalURL;
	}

	public void setExternalURL(final String externalURL)
	{
		this.externalURL = externalURL;
	}

	public Integer getNewRelationshipTagId()
	{
		return newRelationshipTagId;
	}

	public void setNewRelationshipTagId(Integer newRelationshipTagId)
	{
		this.newRelationshipTagId = newRelationshipTagId;
	}

	public void setOtherTopicId(final Integer otherTopicId)
	{
		this.otherTopicId = otherTopicId;
	}

	public Integer getOtherTopicId()
	{
		return otherTopicId;
	}

	public void setActionTopicId(final Integer actionTopicID)
	{
		this.actionTopicId = actionTopicID;
	}

	public Integer getActionTopicId()
	{
		return actionTopicId;
	}

	public Integer getRelationshipTagId()
	{
		return relationshipTagId;
	}

	public void setRelationshipTagId(final Integer relationshipTag)
	{
		this.relationshipTagId = relationshipTag;
	}

	public void setSelectedTags(final UIProjectsData selectedTags)
	{
		this.selectedTags = selectedTags;
	}

	public UIProjectsData getSelectedTags()
	{
		return selectedTags;
	}

	public void create()
	{
		final Map<String, String> urlParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		final Filter filter = EntityUtilities.populateFilter(urlParameters, Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);
		getAllQuery = filter.buildQuery(new TopicFilterQueryBuilder());

		// get a map of variable names to variable values
		filterVars = filter.getUrlVariables();

		filterURLVars = filter.buildFilterUrlVars();

		// get the heading to display over the list of topics
		searchTagHeading = filter.getFilterTitle();

		/*
		 * get a string that can be appended to a url that contains the url
		 * variables
		 */
		urlVars = filter.buildFilterUrlVars();

		/* The tags included in the URL query parameters */
		final List<Tag> urlGroupedTags = getGroupedTags();
		/* The tag IDs that have some Topics tagged against them */
		final List<Integer> groupedTags = new ArrayList<Integer>();

		for (final Tag tag : urlGroupedTags)
		{
			/*
			 * build the query, and add a new restriction that forces the group
			 * tag to be present
			 */
			final String query = getAllQuery + " AND EXISTS (SELECT 1 FROM TopicToTag topicToTag WHERE topicToTag.topic = topic and topicToTag.tag.tagId = " + tag.getTagId() + ") ";
			final ExtendedTopicList topics = new ExtendedTopicList(Constants.DEFAULT_PAGING_SIZE, query);
			if (topics.getResultCount() != 0)
			{
				final GroupedList<ExtendedTopicList> groupedTopicList = new GroupedList<ExtendedTopicList>();
				groupedTopicList.setGroup(tag.getTagName());
				groupedTopicList.setEntityList(topics);

				groupedLists.add(groupedTopicList);

				if (pagingEntityQuery == null || pagingEntityQuery.getResultCount() < topics.getResultCount())
					pagingEntityQuery = topics;

				groupedTags.add(tag.getTagId());
			}
		}

		/* sort by tag name, and then add the unsorted topics on the end */
		Collections.sort(groupedLists, new GroupedTopicListNameComparator<ExtendedTopicList>());

		/*
		 * we didn't have any groups, so just find all the matching topics and
		 * dump them in the default group
		 */
		if (groupedTags.size() == 0)
		{
			final ExtendedTopicList topics = new ExtendedTopicList(Constants.DEFAULT_PAGING_SIZE, getAllQuery);

			final GroupedList<ExtendedTopicList> groupedTopicList = new GroupedList<ExtendedTopicList>();
			groupedTopicList.setGroup(Constants.UNGROUPED_RESULTS_TAB_NAME);
			groupedTopicList.setEntityList(topics);

			groupedLists.add(groupedTopicList);
			pagingEntityQuery = topics;
		}
		/*
		 * Find that topics that are part of the query, but couldn't be matched
		 * in any group
		 */
		else
		{
			String notQuery = "";
			for (final Integer tagID : groupedTags)
			{
				notQuery += " AND NOT EXISTS (SELECT 1 FROM TopicToTag topicToTag WHERE topicToTag.topic = topic AND topicToTag.tag.tagId = " + tagID + ")";
			}

			final ExtendedTopicList topics = new ExtendedTopicList(Constants.DEFAULT_PAGING_SIZE, getAllQuery + notQuery);

			if (topics.getResultCount() != 0)
			{
				final GroupedList<ExtendedTopicList> groupedTopicList = new GroupedList<ExtendedTopicList>();
				groupedTopicList.setGroup(Constants.UNGROUPED_RESULTS_TAB_NAME);
				groupedTopicList.setEntityList(topics);

				groupedLists.add(groupedTopicList);

				if (pagingEntityQuery == null || pagingEntityQuery.getMaxResults() < topics.getMaxResults())
					pagingEntityQuery = topics;
			}
		}

		if (groupedLists.size() != 0)
			this.tab = groupedLists.get(0).getGroup();
	}

	public void removeRelationship()
	{
		try
		{
			if (!(actionTopicId != null && otherTopicId != null && relationshipTagId != null))
				throw new IllegalArgumentException();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic topic = entityManager.find(Topic.class, actionTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);
			final RelationshipTag relationshipTag = entityManager.find(RelationshipTag.class, relationshipTagId);

			topic.removeRelationshipTo(otherTopic, relationshipTag);
			entityManager.persist(topic);

			entityManager.flush();

		}
		catch (final IllegalArgumentException ex)
		{
			SkynetExceptionUtilities.handleSeamPreconditionFailedException(ex);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an issue removing a relationship");
		}
		finally
		{
			resetAjaxVars();
		}
	}

	public String viewRelatedTopic()
	{
		try
		{
			if (otherTopicId != null)
				return "/Topic.xhtml?topicTopicId=" + otherTopicId;

			return null;
		}
		finally
		{
			this.resetAjaxVars();
		}
	}

	public void convertRelationshipType()
	{
		try
		{
			if (!(actionTopicId != null && otherTopicId != null && relationshipTagId != null && newRelationshipTagId != null))
				throw new IllegalArgumentException();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic topic = entityManager.find(Topic.class, actionTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);
			final RelationshipTag relationshipTag = entityManager.find(RelationshipTag.class, relationshipTagId);
			final RelationshipTag newRelationshipTag = entityManager.find(RelationshipTag.class, newRelationshipTagId);

			topic.changeTopicToTopicRelationshipTag(newRelationshipTag, otherTopic, relationshipTag);
			entityManager.persist(topic);

			entityManager.flush();

		}
		catch (final IllegalArgumentException ex)
		{
			SkynetExceptionUtilities.handleSeamPreconditionFailedException(ex);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an issue removing a relationship");
		}
		finally
		{
			resetAjaxVars();
		}
	}

	protected void resetAjaxVars()
	{
		actionTopicId = null;
		otherTopicId = null;
		relationshipTagId = null;
		newRelationshipTagId = null;
	}
	
	public String searchForBugs()
	{
		try
		{
			if (!(this.actionTopicId != null))
				throw new IllegalArgumentException();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic topic = entityManager.find(Topic.class, this.actionTopicId);

			this.externalURL =  topic.getBugzillaSearchURL();
			return "redirect";
		}
		catch (final IllegalArgumentException ex)
		{
			SkynetExceptionUtilities.handleSeamPreconditionFailedException(ex);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an issue retrieving a Topic entity");
		}
		finally
		{
			resetAjaxVars();
		}
		
		return null;
	}



}
