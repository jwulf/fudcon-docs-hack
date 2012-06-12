package com.redhat.topicindex.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import net.ser1.stomp.Client;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.ecs.services.docbookcompiling.BuildDocbookMessage;
import com.redhat.ecs.services.docbookcompiling.DocbookBuildingOptions;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.messaging.TopicRendererType;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.structures.locales.UILocales;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;

/**
 * The backing bean for the Translations Search page
 */
@Name("translatedTopicSearch")
@Scope(ScopeType.PAGE)
public class TranslatedTopicSearch  implements DisplayMessageInterface, Serializable
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
	/** The object that holds the topic filter field values */
	private TopicFilter topic = new TopicFilter();
	/** The id of the Filter object that has been loaded */
	private Integer selectedFilter;
	/** The name of the Filter object that has been loaded */
	private String selectedFilterName;
	/** A list of Filters from the database, used to populate the drop down list */
	private List<Filter> filters = new ArrayList<Filter>();
	/** The message to be displayed to the user */
	private String displayMessage;

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
	
	public void setSelectedFilter(final Integer selectedFilter)
	{
		this.selectedFilter = selectedFilter;
	}

	public Integer getSelectedFilter()
	{
		return selectedFilter;
	}
	
	public void setFilters(final List<Filter> filters)
	{
		this.filters = filters;
	}

	public List<Filter> getFilters()
	{
		return filters;
	}

	public String getSelectedFilterName() {
		return selectedFilterName;
	}

	public void setSelectedFilterName(String selectedFilterName) {
		this.selectedFilterName = selectedFilterName;
	}

	@Create
	public void populate()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		
		Filter filter;

		// If we don't have a filter selected, populate the filter from the url
		// variables
		if (this.selectedFilter == null)
		{
			// build up a Filter object from the URL variables
			final FacesContext context = FacesContext.getCurrentInstance();
			filter = EntityUtilities.populateFilter(context.getExternalContext().getRequestParameterMap(),
					Constants.FILTER_ID,
					Constants.MATCH_TAG,
					Constants.GROUP_TAG,
					Constants.CATEORY_INTERNAL_LOGIC,
					Constants.CATEORY_EXTERNAL_LOGIC,
					Constants.MATCH_LOCALE);
		}
		// otherwise load the filter using the filter id
		else
		{
			filter = entityManager.find(Filter.class, selectedFilter);
		}
		
		this.selectedTags.populateTopicTags(filter, false);
		this.selectedLocales.loadLocaleCheckboxes(filter);
		this.topic.syncWithFilter(filter);
		filter.updateDocbookOptionsFilter(this.docbookBuildingOptions);
		
		getFilterList();
	}

	@SuppressWarnings("unchecked")
	private void getFilterList()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// get a list of the existing filters in the database
		filters.clear();
		filters.addAll(entityManager.createQuery(Filter.SELECT_ALL_QUERY).getResultList());
	}

	public String doSearch()
	{
		final Filter filter = new Filter();
		syncFilterWithUI(filter);
		
		String params = filter.buildFilterUrlVars();

		return "/GroupedTranslatedTopicDataList.seam?" + params;
	}
	
	public void buildDocbook()
	{
		/* when building only use the latest translations */
		if (!this.topic.getLatestCompletedTranslations())
			this.topic.setLatestTranslations(true);
		else
			this.topic.setLatestTranslations(false);
			
		final Filter filter = new Filter();
		this.syncFilterWithUI(filter);

		Client client = null;
		try
		{
			/*
			 * Send the message to the STOMP server once the transaction has
			 * completed
			 */
			final String messageServer = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_SYSTEM_PROPERTY);
			final String messageServerPort = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PORT_SYSTEM_PROPERTY);
			final String messageServerUser = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_USER_SYSTEM_PROPERTY);
			final String messageServerPass = System.getProperty(CommonConstants.STOMP_MESSAGE_SERVER_PASS_SYSTEM_PROPERTY);
			final String messageServerQueue = System.getProperty(Constants.STOMP_MESSAGE_SERVER_DOCBOOK_BUILD_QUEUE_SYSTEM_PROPERTY);

			if (messageServer != null && messageServerPort != null && messageServerUser != null && messageServerPass != null && messageServerQueue != null)
			{
				final ObjectMapper mapper = new ObjectMapper();
								
				final BuildDocbookMessage buildDocbookMessage = new BuildDocbookMessage();
				buildDocbookMessage.setQuery(filter.buildRESTQueryString());
				buildDocbookMessage.setDocbookOptions(this.docbookBuildingOptions);
				buildDocbookMessage.setEntityType(TopicRendererType.TRANSLATEDTOPIC);
				
				client = new Client(messageServer, Integer.parseInt(messageServerPort), messageServerUser, messageServerPass);
				client.send(messageServerQueue, mapper.writeValueAsString(buildDocbookMessage));
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably a STOMP message queue error");
		}
		finally
		{
			if (client != null)
				client.disconnect();
			client = null;
		}
	}

	/**
	 * This function takes the tag selections and uses these to populate a
	 * Filter
	 * 
	 * @param filter
	 *            The filter to sync with the tag selection
	 * @param persist
	 *            true if this filter is being saved to the db (i.e. the user is
	 *            actually saving the filter), and false if it is not (like when
	 *            the user is building the docbook zip file)
	 */
	protected void syncFilterWithUI(final Filter filter)
	{
		filter.syncFilterWithTags(this.selectedTags);
		filter.syncFilterWithCategories(this.selectedTags);
		filter.syncFilterWithFieldUIElements(this.topic);
		filter.syncFilterWithLocales(selectedLocales);
		filter.syncWithDocbookOptions(this.docbookBuildingOptions);
	}
	
	/**
	 * Loads a Filter object from the database given a FilterID, and selects the
	 * appropriate tags in the ui
	 */
	public void loadFilter()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// load the filter from the database using the filter id
		Filter filter = null;
		if (selectedFilter != null)
			filter = entityManager.find(Filter.class, selectedFilter);
		else
			filter = new Filter();

		this.setSelectedFilterName(filter.getFilterName());

		this.selectedTags.loadTagCheckboxes(filter);
		this.selectedTags.loadCategoryLogic(filter);
		this.selectedLocales.loadLocaleCheckboxes(filter);
		this.topic.loadFilterFields(filter);
		filter.updateDocbookOptionsFilter(this.docbookBuildingOptions);
	}
	
	public String loadFilterAndSearch()
	{
		loadFilter();
		return doSearch();
	}

	public void loadFilterAndDocbook()
	{
		this.loadFilter();
		this.buildDocbook();
	}
	
	/**
	 * This function synchronizes the tags selected in the gui with the
	 * FilterTags in the Filter object, and saves the changes to the database.
	 * This persists the currently selected filter.
	 */
	public void saveFilter()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			Filter filter;

			// load the filter object if it exists
			if (this.selectedFilter != null)
				filter = entityManager.find(Filter.class, selectedFilter);
			else
				// get a reference to the Filter object
				filter = new Filter();

			// set the name
			filter.setFilterName(this.selectedFilterName);

			// populate the filter with the options that are selected in the ui
			syncFilterWithUI(filter);

			// save the changes
			entityManager.persist(filter);
			this.selectedFilter = filter.getFilterId();

			getFilterList();
		}
		catch (final PersistenceException ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, true, "Probably a constraint violation");
			if (ex.getCause() instanceof ConstraintViolationException)
				this.setDisplayMessage("The filter requires a unique name");
			else
				this.setDisplayMessage("The filter could not be saved");
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an error saving the Filter");
			this.setDisplayMessage("The filter could not be saved");
		}
	}

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

	public void setDisplayMessage(String displayMessage)
	{
		this.displayMessage = displayMessage;
	}
}
