package com.redhat.topicindex.session;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.ImageFile;
import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.PropertyTagCategory;
import com.redhat.topicindex.entity.PropertyTagToPropertyTagCategory;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicSourceUrl;
import com.redhat.topicindex.entity.TopicToPropertyTag;
import com.redhat.topicindex.entity.TopicToTag;
import com.redhat.topicindex.entity.TopicToTopicSourceUrl;
import com.redhat.topicindex.exceptions.CustomConstraintViolationException;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.DroolsEvent;
import com.redhat.topicindex.utils.structures.PropertyTagUISelection;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.messaging.TopicRendererType;

import org.drools.WorkingMemory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.validator.InvalidStateException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;

@Name("topicHome")
public class TopicHome extends VersionedEntityHome<Topic> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 1701692331956663275L;
	private @In
	WorkingMemory businessRulesWorkingMemory;
	private @In
	Identity identity;
	private ImageFile newImageFile = new ImageFile();
	private TopicSourceUrl newTopicSourceUrl = new TopicSourceUrl();
	private ArrayList<Integer> processedTopics = new ArrayList<Integer>();
	private UIProjectsData selectedTags;
	private HashMap<Integer, ArrayList<Integer>> tagExclusions;
	/**
	 * The name of the tab that is to be selected when the tab panel is
	 * displayed
	 */
	private String selectedTab;
	/** The selected PropertyTag ID */
	private String newPropertyTagId;
	/** The value for the new TagToPropertyTag */
	private String newPropertyTagValue;
	/** A list of the available PropertyTags */
	private PropertyTagUISelection properties;
	
	public List<SelectItem> getProperties()
	{
		return properties.getProperties();
	}
	
	public TopicHome()
	{
		setMessages();
	}

	@Override
	protected Topic createInstance()
	{
		Topic topic = new Topic();
		return topic;
	}

	public Topic getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	public String getExclusionArray(final Integer id)
	{
		String values = "";
		if (tagExclusions.containsKey(id))
		{
			for (final Integer exclusion : tagExclusions.get(id))
			{
				if (values.length() != 0)
					values += ", ";

				values += exclusion.toString();
			}
		}

		return "[" + values + "]";
	}

	public String getMultipleUpdateUrl()
	{
		final String retvalue = "/CustomSearchTopicList.seam?topicIds=" + getTopicList();
		return retvalue;
	}

	public ImageFile getNewImageFile()
	{
		return newImageFile;
	}

	public TopicSourceUrl getNewTopicSourceUrl()
	{
		return newTopicSourceUrl;
	}

	public UIProjectsData getSelectedTags()
	{
		return selectedTags;
	}

	public HashMap<Integer, ArrayList<Integer>> getTagExclusions()
	{
		return tagExclusions;
	}

	protected Tag getTagFromId(final Integer tagId)
	{
		final TagHome tagHome = new TagHome();
		tagHome.setId(tagId);
		return tagHome.getInstance();
	}

	protected String getTopicList()
	{
		String retValue = "";
		for (final Integer topicId : processedTopics)
		{
			if (retValue.length() != 0)
				retValue += ",";
			retValue += topicId;
		}
		return retValue;
	}

	public Integer getTopicTopicId()
	{
		return (Integer) getId();
	}

	public boolean isDoingMultipleUpdates()
	{
		return processedTopics.size() != 0;
	}

	public boolean isWired()
	{
		return true;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	@Override
	public String persist()
	{

		updateTags();
		prePersist();

		/*
		 * The initial approach to having the status messages updated with the
		 * topic details was to use:
		 * 
		 * @Override
		 * 
		 * @Factory(value = "topic") public Topic getInstance() { return
		 * super.getInstance(); }
		 * 
		 * The problem with this is that the "topic" object is not refreshed, so
		 * the messages all reprint the details of the first created object. The
		 * next step I took to try and fix this was to refresh the context in an
		 * override of clearInstance() like so:
		 * 
		 * Contexts.getConversationContext().set("topic", null);
		 * 
		 * The problem with this is that the messages generated by the
		 * EntityHome object actually calculate their EL values in a thread. So
		 * by the time the message was calculated, the clearInstance function
		 * had set the "topic" object to null.
		 * 
		 * Manually setting the EL object "lasttopic" before the persist ensures
		 * that the messages have time to calculate their variables before the
		 * object is reset.
		 */

		Contexts.getConversationContext().set("lasttopic", this.getInstance());
		final String retValue = super.persist();
		return retValue;
	}

	public String persistEx(final boolean addMore)
	{
		try
		{
			persist();
			processedTopics.add(this.getInstance().getTopicId());

			this.clearInstance();

			if (addMore)
				return EntityUtilities.buildEditNewTopicUrl(selectedTags);

			return "backToList";
		}
		catch (final InvalidStateException ex)
		{
			this.setDisplayMessage("The Topic violated a constraint");
			SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");
		}
		catch (final PersistenceException ex)
		{
			if (ex.getCause() instanceof ConstraintViolationException)
			{
				this.setDisplayMessage("The Topic violated a constraint");
				SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");
			}
			else
			{
				this.setDisplayMessage("The Topic could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error updating a Tag entity");
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error persisting a ImageFile entity");
			this.setDisplayMessage("The Topic could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
		}

		return null;
	}

	public void populate()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		
		populateTags();
		properties = new PropertyTagUISelection(entityManager);
	}

	public void populateTags()
	{
		selectedTags = new UIProjectsData();
		selectedTags.populateTopicTags(this.getInstance());
		tagExclusions = EntityUtilities.populateExclusionTags();
		EntityUtilities.populateMutuallyExclusiveCategories(selectedTags);
	}

	protected void prePersist()
	{

	}

	protected void preUpdate()
	{
		final Topic topic = this.getInstance();
		businessRulesWorkingMemory.setGlobal("topic", topic);
		EntityUtilities.injectSecurity(businessRulesWorkingMemory, identity);
		businessRulesWorkingMemory.insert(new DroolsEvent("UpdateTopicHome"));
		businessRulesWorkingMemory.fireAllRules();
	}

	public void removeTopicURL(final TopicToTopicSourceUrl url)
	{
		this.getInstance().getTopicToTopicSourceUrls().remove(url);
		if (this.isManaged())
			this.persist();
		else
			this.update();
	}

	public void saveNewTopicSourceUrl()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

			entityManager.persist(newTopicSourceUrl);

			final TopicToTopicSourceUrl topicToTopicSourceUrl = new TopicToTopicSourceUrl(newTopicSourceUrl, this.getInstance());

			entityManager.persist(topicToTopicSourceUrl);

			this.getInstance().getTopicToTopicSourceUrls().add(topicToTopicSourceUrl);
			newTopicSourceUrl = new TopicSourceUrl();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error saving a TopicToTopicSourceUrl entity");
		}
	}

	public void setNewImageFile(ImageFile newImageFile)
	{
		this.newImageFile = newImageFile;
	}

	public void setNewTopicSourceUrl(final TopicSourceUrl newTopicSourceUrl)
	{
		this.newTopicSourceUrl = newTopicSourceUrl;
	}

	public void setSelectedTags(UIProjectsData value)
	{
		selectedTags = value;
	}

	public void setTagExclusions(HashMap<Integer, ArrayList<Integer>> tagExclusions)
	{
		this.tagExclusions = tagExclusions;
	}

	public void setTopicTopicId(Integer id)
	{
		setId(id);
	}

	public void triggerCreateEvent()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Topic topic = this.getInstance();

		if (!this.isManaged())
		{
			// get the tags from the url, and add them by default
			final FacesContext context = FacesContext.getCurrentInstance();
			final Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();

			for (final String key : paramMap.keySet())
			{
				try
				{
					if (key.startsWith(Constants.MATCH_TAG) && Integer.parseInt(paramMap.get(key)) == Constants.MATCH_TAG_STATE)
					{
						final Integer tagID = Integer.parseInt(key.replace(Constants.MATCH_TAG, ""));
						topic.addTag(entityManager, tagID);
					}
				}
				catch (final NumberFormatException ex)
				{
					SkynetExceptionUtilities.handleException(ex, false, "Probably an invalid integer value in the URL query paramaters");
				}
			}
		}

		// now run drools to modify the topic as needed
		businessRulesWorkingMemory.setGlobal("topic", topic);
		EntityUtilities.injectSecurity(businessRulesWorkingMemory, identity);

		if (this.isManaged())
			businessRulesWorkingMemory.insert(new DroolsEvent("LoadTopicHome"));
		else
			businessRulesWorkingMemory.insert(new DroolsEvent("NewTopicHome"));

		businessRulesWorkingMemory.fireAllRules();
	}

	@Override
	public String update()
	{
		updateTags();
		preUpdate();
		return super.update();
	}
	
	public String updateAndStay()
	{
		return update();
	}

	public void updateTags()
	{
		final Topic topic = this.getInstance();

		if (topic != null)
		{
			final ArrayList<Tag> selectedTagObjects = new ArrayList<Tag>();

			for (final UIProjectData project : selectedTags.getProjectCategories())
			{
				for (final UICategoryData cat : project.getCategories())
				{
					// is this a mutually exclusive category?
					if (cat.isMutuallyExclusive())
					{
						/*
						 * if so, the selected tag is stored in the selectedID
						 * field of the category GuiInputData this has the
						 * effect of removing any other tags that might be
						 * already selected in this category
						 */
						if (cat.getSelectedTag() != null)
							selectedTagObjects.add(getTagFromId(cat.getSelectedTag()));

					}
					else
					{
						/*
						 * otherwise we find the selected tags from the tag
						 * GuiInputData objects in the ArrayList
						 */
						for (final UITagData tagId : cat.getTags())
						{
							// if tag is selected
							if (tagId.isSelected())
								selectedTagObjects.add(getTagFromId(tagId.getId()));
						}
					}
				}
			}

			// match up selected tags with existing tags
			final Set<TopicToTag> topicToTags = topic.getTopicToTags();

			// make a note of the tags that were removed
			final ArrayList<Tag> removeTags = new ArrayList<Tag>();
			for (final TopicToTag topicToTag : topicToTags)
			{
				final Tag existingTag = topicToTag.getTag();

				if (!selectedTagObjects.contains(existingTag))
				{
					boolean hasPermission = true;

					/*
					 * check to see if we have authority to modify this flag
					 * thanks to the category(s) it belongs to
					 */
					for (TagToCategory category : existingTag.getTagToCategories())
					{
						try
						{
							Identity.instance().checkRestriction("#{s:hasPermission('" + category.getCategory().getCategoryName() + "', 'Enabled', null)}");
							hasPermission = true;
							break;
						}
						catch (final NotLoggedInException ex)
						{
							SkynetExceptionUtilities.handleException(ex, true, "User is not logged in");
						}
						catch (final AuthorizationException ex)
						{
							SkynetExceptionUtilities.handleException(ex, true, "User does not have permission");
						}
					}

					// otherwise see if we had permission on the tag itself
					if (!hasPermission)
					{
						try
						{
							Identity.instance().checkRestriction("#{s:hasPermission('" + existingTag.getTagName() + "', 'Enabled', null)}");
							hasPermission = true;
							break;
						}
						catch (final NotLoggedInException ex)
						{
							SkynetExceptionUtilities.handleException(ex, true, "User is not logged in");
						}
						catch (final AuthorizationException ex)
						{
							SkynetExceptionUtilities.handleException(ex, true, "User does not have permission");
						}
					}

					if (hasPermission)
					{
						/*
						 * if we get to this point (i.e. no exception was
						 * thrown), we had authority to alter this flag add to
						 * external collection to avoid modifying a collection
						 * while looping over it
						 */
						removeTags.add(existingTag);
					}

				}
			}

			// now make a note of the additions
			final ArrayList<Tag> addTags = new ArrayList<Tag>();
			for (final Tag selectedTag : selectedTagObjects)
			{
				if (filter(having(on(TopicToTag.class).getTag(), equalTo(selectedTag)), topicToTags).size() == 0)
				{
					addTags.add(selectedTag);
				}
			}

			// only proceed if there are some changes to make
			if (removeTags.size() != 0 || addTags.size() != 0)
			{
				// remove the deleted tags
				for (final Tag removeTag : removeTags)
				{
					topic.removeTag(removeTag);
				}

				// add the created tags
				for (final Tag addTag : addTags)
				{
					topic.addTag(addTag);
				}
			}
		}
	}

	private void setMessages()
	{
		this.setCreatedMessage(createValueExpression("Successfully Created Topic With ID: #{lasttopic.topicId} Title: #{lasttopic.topicTitle}"));
		this.setDeletedMessage(createValueExpression("Successfully Created Topic With ID: #{topic.topicId} Title: #{topic.topicTitle}"));
		this.setUpdatedMessage(createValueExpression("Successfully Created Topic With ID: #{topic.topicId} Title: #{topic.topicTitle}"));
	}

	public String getSelectedTab()
	{
		return selectedTab;
	}

	public void setSelectedTab(final String selectedTab)
	{
		this.selectedTab = selectedTab;
	}

	public void reRender()
	{
		this.displayMessage = "This topic has been placed in a queue to be rendered. Please refresh the page in a few minutes.";
		WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(this.getTopicTopicId(), TopicRendererType.TOPIC, false));
	}
	
	public void generateXMLFromTemplate()
	{
		this.updateTags();
		this.instance.initializeFromTemplate();
	}
	
	public String getNewPropertyTagId()
	{
		return newPropertyTagId;
	}

	public void setNewPropertyTagId(final String newPropertyTagId)
	{
		this.newPropertyTagId = newPropertyTagId;
	}

	public String getNewPropertyTagValue()
	{
		return newPropertyTagValue;
	}

	public void setNewPropertyTagValue(final String newPropertyTagValue)
	{
		this.newPropertyTagValue = newPropertyTagValue;
	}

	public void removeProperty(final TopicToPropertyTag tagToPropertyTag)
	{
		this.getInstance().removePropertyTag(tagToPropertyTag);
	}

	public void saveNewProperty()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

			if (this.newPropertyTagId.startsWith(Constants.PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE_PREFIX))
			{
				final String fixedNewPropertyTagId = this.newPropertyTagId.replace(Constants.PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE_PREFIX, "");
				final Integer fixedNewPropertyTagIdInt = Integer.parseInt(fixedNewPropertyTagId);
				final PropertyTagCategory propertyTagCategory = entityManager.find(PropertyTagCategory.class, fixedNewPropertyTagIdInt);
				for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : propertyTagCategory.getPropertyTagToPropertyTagCategories())
				{
					final PropertyTag propertyTag = propertyTagToPropertyTagCategory.getPropertyTag();

					/* don't bulk add the same tags twice */
					if (!this.getInstance().hasProperty(propertyTag))
					{
						final TopicToPropertyTag topicToPropertyTag = new TopicToPropertyTag();
						topicToPropertyTag.setPropertyTag(propertyTag);
						topicToPropertyTag.setTopic(this.getInstance());
						
						/* if we are adding a unique property tag, make sure the initial value is unique */
						if (propertyTag.getPropertyTagIsUnique())
							topicToPropertyTag.setValue(Constants.UNIQUE_PROPERTY_TAG_PREFIX + " " + UUID.randomUUID());
						
						this.getInstance().addPropertyTag(topicToPropertyTag);
					}
				}
			}
			else if (this.newPropertyTagId.startsWith(Constants.PROPERTY_TAG_SELECT_ITEM_VALUE_PREFIX))
			{
				final String fixedNewPropertyTagId = this.newPropertyTagId.replace(Constants.PROPERTY_TAG_SELECT_ITEM_VALUE_PREFIX, "");
				final Integer fixedNewPropertyTagIdInt = Integer.parseInt(fixedNewPropertyTagId);
				final PropertyTag propertyTag = entityManager.find(PropertyTag.class, fixedNewPropertyTagIdInt);
				final TopicToPropertyTag topicToPropertyTag = new TopicToPropertyTag();
				topicToPropertyTag.setPropertyTag(propertyTag);
				topicToPropertyTag.setTopic(this.getInstance());
				topicToPropertyTag.setValue(this.newPropertyTagValue);
				this.getInstance().addPropertyTag(topicToPropertyTag);
			}

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an issue getting an PropertyTag or PropertyTagCategory entity, or maybe a Integer.parse() issue");
			this.displayMessage = "There was an error saving the Property Tag.";
		}
	}

	public void updateBugs()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		this.getInstance().updateBugzillaBugs(entityManager);
	}
}
