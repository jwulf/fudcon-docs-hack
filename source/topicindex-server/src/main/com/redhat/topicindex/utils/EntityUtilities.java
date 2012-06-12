package com.redhat.topicindex.utils;

import java.net.URLDecoder;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.Role;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;

import com.redhat.contentspec.processor.ContentSpecParser;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.BlobConstants;
import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterCategory;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.FilterLocale;
import com.redhat.topicindex.entity.FilterTag;
import com.redhat.topicindex.entity.IntegerConstants;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.PropertyTagCategory;
import com.redhat.topicindex.entity.PropertyTagToPropertyTagCategory;
import com.redhat.topicindex.entity.TranslatedTopic;
import com.redhat.topicindex.entity.StringConstants;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToProject;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicToBugzillaBug;
import com.redhat.topicindex.entity.TopicToPropertyTag;
import com.redhat.topicindex.entity.TranslatedTopicData;
import com.redhat.topicindex.entity.User;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.filter.TopicFilterQueryBuilder;
import com.redhat.topicindex.sort.RoleNameComparator;
import com.redhat.topicindex.sort.TopicTagCategoryDataNameSorter;
import com.redhat.topicindex.sort.UserNameComparator;
import com.redhat.topicindex.utils.structures.roles.UIRoleUserData;
import com.redhat.topicindex.utils.structures.tags.UIProjectTagData;
import com.redhat.topicindex.utils.structures.tags.UITagProjectData;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;
import com.redhat.topicindex.utils.structures.tags.UIProjectsData;

public class EntityUtilities
{
	public static byte[] loadBlobConstant(final EntityManager entityManager, final Integer id)
	{
		if (id == null)
			return null;

		final BlobConstants constant = entityManager.find(BlobConstants.class, id);

		if (constant == null)
		{
			System.out.println("Expected to find a record in the BlobConstants table with an ID of " + id);
			return null;
		}

		return constant.getConstantValue();
	}

	public static Integer loadIntegerConstant(final EntityManager entityManager, final Integer id)
	{
		if (id == null)
			return null;

		final IntegerConstants constant = entityManager.find(IntegerConstants.class, id);

		if (constant == null)
		{
			System.out.println("Expected to find a record in the IntegerConstants table with an ID of " + id);
			return null;
		}

		return constant.getConstantValue();
	}

	public static String loadStringConstant(final EntityManager entityManager, final Integer id)
	{
		if (id == null)
			return null;

		final StringConstants constant = entityManager.find(StringConstants.class, id);

		if (constant == null)
		{
			System.out.println("Expected to find a record in the StringConstants table with an ID of " + id);
			return null;
		}

		return constant.getConstantValue();
	}

	@SuppressWarnings("unchecked")
	static public void populateProjectTags(final Project project, final List<UIProjectTagData> tags)
	{
		try
		{
			tags.clear();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<Tag> tagList = entityManager.createQuery(Tag.SELECT_ALL_QUERY).getResultList();

			for (final Tag tag : tagList)
			{
				boolean found = false;

				for (final TagToProject tagToProject : project.getTagToProjects())
				{
					if (tagToProject.getProject().equals(project))
					{
						found = true;
						break;
					}
				}

				tags.add(new UIProjectTagData(tag, found));
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving a Tag entity");
		}
	}

	@SuppressWarnings("unchecked")
	static public void populateTagProjects(final Tag mainTag, final List<UITagProjectData> projects)
	{
		try
		{
			projects.clear();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<Project> projectList = entityManager.createQuery(Project.SELECT_ALL_QUERY).getResultList();

			for (final Project project : projectList)
			{
				boolean found = false;

				for (final TagToProject tagToProject : mainTag.getTagToProjects())
				{
					if (tagToProject.getProject().equals(project))
					{
						found = true;
						break;
					}
				}

				projects.add(new UITagProjectData(project, found));
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving a Project entity");
		}
	}

	static public void populateProjectTags(final Project project, final UIProjectsData selectedTags)
	{
		selectedTags.populateTags(project.getTags(), null, true);
	}

	/**
	 * When assigning tags to a category, we need to know the sorting order of
	 * the tags as it related to a specific category. This is different to the
	 * sorting order used to show the tags, because those values are specific to
	 * the categories that the tags appear in.
	 */
	static public void populateTagTagsSortingForCategory(final Category category, final UIProjectsData selectedTags)
	{
		for (final UIProjectData projectData : selectedTags.getProjectCategories())
		{
			for (final UICategoryData categoryData : projectData.getCategories())
			{
				if (categoryData.getId().equals(category.getCategoryId()))
				{
					for (final UITagData tagData : categoryData.getTags())
					{
						/*
						 * match the sorting order for the tags in the category
						 * with the newSort values for the UI tags
						 */

						for (final TagToCategory tagToCategory : category.getTagToCategories())
						{
							if (tagData.getId().equals(tagToCategory.getTag().getTagId()))
							{
								tagData.setNewSort(tagToCategory.getSorting());
								break;
							}
						}
					}
				}
			}
		}
	}

	public static Tag getTagFromId(final Integer tagId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.find(Tag.class, tagId);
		return tag;
	}

	public static PropertyTag getPropertyTagFromId(final Integer tagId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final PropertyTag tag = entityManager.find(PropertyTag.class, tagId);
		return tag;
	}

	public static com.redhat.topicindex.entity.Role getRoleFromId(final Integer roleId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final com.redhat.topicindex.entity.Role role = entityManager.find(com.redhat.topicindex.entity.Role.class, roleId);
		return role;
	}

	public static User getUserFromId(final Integer userId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final User user = entityManager.find(User.class, userId);
		return user;
	}

	static public List<UIRoleUserData> getUserRoles(final User user)
	{
		final List<UIRoleUserData> retValue = new ArrayList<UIRoleUserData>();

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		@SuppressWarnings("unchecked")
		final List<com.redhat.topicindex.entity.Role> roleList = entityManager.createQuery(com.redhat.topicindex.entity.Role.SELECT_ALL_QUERY).getResultList();

		Collections.sort(roleList, new RoleNameComparator());

		for (final com.redhat.topicindex.entity.Role role : roleList)
		{
			final boolean selected = user.isInRole(role);
			final UIRoleUserData roleUserData = new UIRoleUserData(role.getRoleId(), role.getRoleName(), selected);
			retValue.add(roleUserData);
		}

		return retValue;
	}

	static public List<UIRoleUserData> getRoleUsers(final com.redhat.topicindex.entity.Role role)
	{
		final List<UIRoleUserData> retValue = new ArrayList<UIRoleUserData>();

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		@SuppressWarnings("unchecked")
		final List<User> userList = entityManager.createQuery(User.SELECT_ALL_QUERY).getResultList();

		Collections.sort(userList, new UserNameComparator());

		for (final User user : userList)
		{
			final boolean selected = role.hasUser(user);
			final UIRoleUserData roleUserData = new UIRoleUserData(user.getUserId(), user.getUserName(), selected);
			retValue.add(roleUserData);
		}

		return retValue;
	}

	/**
	 * This function is used to populate the data structures that display the
	 * categories that a tag can and does belong to.
	 * 
	 * @param tag
	 *            The Tag being displayed
	 * @param categories
	 *            A collection of data structures representing the categories
	 * @param selectedCategories
	 *            A collection of selected categories
	 * @param tagSortValues
	 *            A collection of data structures representing the tags sorting
	 *            order within a category
	 */
	@SuppressWarnings("unchecked")
	static public void populateTagCategories(final Tag tag, final List<UICategoryData> categories)
	{
		categories.clear();

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<Category> categoryList = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
		final List<TagToCategory> tagToCategoryList = entityManager.createQuery(TagToCategory.SELECT_ALL_QUERY).getResultList();

		// then loop through the categories
		for (final Category category : categoryList)
		{
			final String catName = category.getCategoryName();
			final Integer catID = category.getCategoryId();
			final String catDesc = category.getCategoryDescription();
			final Integer tagId = tag.getTagId();

			// find out if the tag is already in the category
			final boolean selected = tag.isInCategory(category);

			// get the sorting value.
			Integer sorting = null;
			for (final TagToCategory tagToCategory : tagToCategoryList)
			{
				if (tagToCategory.getCategory().getCategoryId() == catID && tagToCategory.getTag().getTagId() == tagId)
				{
					sorting = tagToCategory.getSorting();
					break;
				}
			}

			/*
			 * in this case the sort value in the TopicTagCategoryData
			 * represents the tags sorting position within the category, not the
			 * category's sorting position amongst other categories
			 */
			categories.add(new UICategoryData(catName, catDesc, catID, sorting == null ? 0 : sorting, selected, false, false, false));
		}

		// sort the categories by name
		Collections.sort(categories, new TopicTagCategoryDataNameSorter());
	}

	@SuppressWarnings("unchecked")
	static public void populatePropertyTagCategories(final PropertyTag tag, final List<UICategoryData> categories)
	{
		categories.clear();

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<PropertyTagCategory> categoryList = entityManager.createQuery(PropertyTagCategory.SELECT_ALL_QUERY).getResultList();
		final List<PropertyTagToPropertyTagCategory> tagToCategoryList = entityManager.createQuery(PropertyTagToPropertyTagCategory.SELECT_ALL_QUERY).getResultList();

		// then loop through the categories
		for (final PropertyTagCategory category : categoryList)
		{
			final String catName = category.getPropertyTagCategoryName();
			final Integer catID = category.getPropertyTagCategoryId();
			final String catDesc = category.getPropertyTagCategoryDescription();
			final Integer tagId = tag.getPropertyTagId();

			// find out if the tag is already in the category
			final boolean selected = tag.isInCategory(category);

			// get the sorting value.
			Integer sorting = null;
			for (final PropertyTagToPropertyTagCategory tagToCategory : tagToCategoryList)
			{
				if (tagToCategory.getPropertyTagCategory().getPropertyTagCategoryId() == catID && tagToCategory.getPropertyTag().getPropertyTagId() == tagId)
				{
					sorting = tagToCategory.getSorting();
					break;
				}
			}

			/*
			 * in this case the sort value in the TopicTagCategoryData
			 * represents the tags sorting position within the category, not the
			 * category's sorting position amongst other categories
			 */
			categories.add(new UICategoryData(catName, catDesc, catID, sorting == null ? 0 : sorting, selected, false, false, false));
		}

		// sort the categories by name
		Collections.sort(categories, new TopicTagCategoryDataNameSorter());
	}

	/**
	 * Used to add the current user roles into Drools working memory. This
	 * function has been copied from
	 * RuleBasedPermissionResolver.synchronizeContext()
	 */
	@SuppressWarnings("unchecked")
	public static void injectSecurity(WorkingMemory workingMemory, Identity identity)
	{
		for (final Group sg : identity.getSubject().getPrincipals(Group.class))
		{
			if (Identity.ROLES_GROUP.equals(sg.getName()))
			{
				Enumeration<Principal> e = (Enumeration<Principal>) sg.members();
				while (e.hasMoreElements())
				{
					Principal role = e.nextElement();

					boolean found = false;
					Iterator<Role> iter = (Iterator<Role>) workingMemory.iterateObjects(new ClassObjectFilter(Role.class));
					while (iter.hasNext())
					{
						Role r = iter.next();
						if (r.getName().equals(role.getName()))
						{
							found = true;
							break;
						}
					}

					if (!found)
					{
						workingMemory.insert(new Role(role.getName()));
					}

				}
			}
		}
	}

	public static Filter populateFilter(final MultivaluedMap<String, String> paramMap, final String filterName, final String tagPrefix, final String groupTagPrefix, final String categoryInternalPrefix, final String categoryExternalPrefix, final String localePrefix)
	{
		final Map<String, String> newParamMap = new HashMap<String, String>();
		for (final String key : paramMap.keySet())
		{
			try
			{
				newParamMap.put(key, URLDecoder.decode(paramMap.getFirst(key), "UTF-8"));
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, true, "The URL query parameter " + key + " with value " + paramMap.getFirst(key) + " could not be URLDecoded");
			}
		}
		return populateFilter(newParamMap, filterName, tagPrefix, groupTagPrefix, categoryInternalPrefix, categoryExternalPrefix, localePrefix);

	}

	/**
	 * This function takes the url parameters and uses them to populate a Filter
	 * object
	 */
	public static Filter populateFilter(final Map<String, String> paramMap, final String filterName, final String tagPrefix, final String groupTagPrefix, final String categoryInternalPrefix, final String categoryExternalPrefix, final String localePrefix)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// attempt to get the filter id from the url
		Integer filterId = null;
		if (paramMap.containsKey(filterName))
		{
			final String filterQueryParam = paramMap.get(filterName);

			try
			{
				filterId = Integer.parseInt(filterQueryParam);
			}
			catch (final Exception ex)
			{
				// filter value was not an integer
				filterId = null;

				SkynetExceptionUtilities.handleException(ex, true, "The filter ID URL query parameter was not an integer. Got " + filterQueryParam + ". Probably a malformed URL.");
			}
		}

		Filter filter = null;

		/* First attempt to populate the filter from a filterID variable */
		if (filterId != null)
		{
			filter = entityManager.find(Filter.class, filterId);
		}

		/* If that fails, use the other URL params */
		if (filter == null)
		{
			filter = new Filter();

			for (final String key : paramMap.keySet())
			{
				final boolean tagVar = key.startsWith(tagPrefix);
				final boolean groupTagVar = key.startsWith(groupTagPrefix);
				final boolean catIntVar = key.startsWith(categoryInternalPrefix);
				final boolean catExtVar = key.startsWith(categoryExternalPrefix);
				final boolean localeVar = key.matches("^" + localePrefix + "\\d*$");
				final String state = paramMap.get(key);

				// add the filter category states
				if (catIntVar || catExtVar)
				{
					/*
					 * get the category and project id data from the variable
					 * name
					 */
					final String catProjDetails = catIntVar ? key.replaceFirst(categoryInternalPrefix, "") : key.replaceFirst(categoryExternalPrefix, "");
					// split the category and project id out of the data
					final String[] catProjID = catProjDetails.split("-");

					/*
					 * some validity checks. make sure we have one or two
					 * strings after the split.
					 */
					if (catProjID.length != 1 && catProjID.length != 2)
						continue;

					// try to get the category and project ids
					Integer catID = null;
					Integer projID = null;
					try
					{
						catID = Integer.parseInt(catProjID[0]);

						/*
						 * if the array has just one element, we have only
						 * specified the category. in this case the project is
						 * the common project
						 */
						if (catProjID.length == 2)
							projID = Integer.parseInt(catProjID[1]);
					}
					catch (final Exception ex)
					{
						SkynetExceptionUtilities.handleException(ex, true, "Was expecting an integer. Got " + catProjID[0] + ". Probably a malformed URL.");
						continue;
					}

					// at this point we have found a url variable that
					// contains a catgeory and project id

					final Category category = entityManager.find(Category.class, catID);
					final Project project = projID != null ? entityManager.find(Project.class, projID) : null;

					Integer dbState;

					if (catIntVar)
					{
						if (state.equals(Constants.AND_LOGIC))
							dbState = Constants.CATEGORY_INTERNAL_AND_STATE;
						else
							dbState = Constants.CATEGORY_INTERNAL_OR_STATE;
					}
					else
					{
						if (state.equals(Constants.AND_LOGIC))
							dbState = Constants.CATEGORY_EXTERNAL_AND_STATE;
						else
							dbState = Constants.CATEGORY_EXTERNAL_OR_STATE;
					}

					final FilterCategory filterCategory = new FilterCategory();
					filterCategory.setFilter(filter);
					filterCategory.setProject(project);
					filterCategory.setCategory(category);
					filterCategory.setCategoryState(dbState);

					filter.getFilterCategories().add(filterCategory);
				}

				// add the filter tag states
				else if (tagVar)
				{
					try
					{
						final Integer tagId = Integer.parseInt(key.replaceFirst(tagPrefix, ""));
						final Integer intState = Integer.parseInt(state);

						// get the Tag object that the tag id represents
						final Tag tag = entityManager.getReference(Tag.class, tagId);

						if (tag != null)
						{
							final FilterTag filterTag = new FilterTag();
							filterTag.setTag(tag);
							filterTag.setTagState(intState);
							filterTag.setFilter(filter);
							filter.getFilterTags().add(filterTag);
						}
					}
					catch (final Exception ex)
					{
						SkynetExceptionUtilities.handleException(ex, true, "Probably an invalid tag query pramater. Parameter: " + key + " Value: " + state);
					}
				}

				else if (groupTagVar)
				{
					final Integer tagId = Integer.parseInt(key.replaceFirst(groupTagPrefix, ""));
					// final Integer intState = Integer.parseInt(state);

					// get the Tag object that the tag id represents
					final Tag tag = entityManager.getReference(Tag.class, tagId);

					if (tag != null)
					{
						final FilterTag filterTag = new FilterTag();
						filterTag.setTag(tag);
						filterTag.setTagState(Constants.GROUP_TAG_STATE);
						filterTag.setFilter(filter);
						filter.getFilterTags().add(filterTag);
					}
				}
				else if (localeVar)
				{
					try
					{
						final String localeName = state.replaceAll("\\d", "");
						final Integer intState = Integer.parseInt(state.replaceAll("[^\\d]", ""));

						final FilterLocale filterLocale = new FilterLocale();
						filterLocale.setLocaleName(localeName);
						filterLocale.setLocaleState(intState);
						filterLocale.setFilter(filter);
						filter.getFilterLocales().add(filterLocale);
					}
					catch (final Exception ex)
					{
						SkynetExceptionUtilities.handleException(ex, true, "Probably an invalid locale query pramater. Parameter: " + key + " Value: " + state);
					}
				}

				// add the filter field states
				else
				{
					if (TopicFilter.hasFilterName(key))
					{
						final FilterField filterField = new FilterField();
						filterField.setFilter(filter);
						filterField.setField(key);
						filterField.setValue(state);
						filterField.setDescription(TopicFilter.getFilterDesc(key));
						filter.getFilterFields().add(filterField);
					}
				}

			}

		}

		return filter;
	}

	/**
	 * @return A list of topic ids that have open bugzilla bugs assigned to them
	 */
	@SuppressWarnings("unchecked")
	public static List<Integer> getTopicsWithOpenBugs()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<TopicToBugzillaBug> results = entityManager.createQuery(TopicToBugzillaBug.SELECT_ALL_QUERY + " WHERE topicToBugzillaBug.bugzillaBug.bugzillaBugOpen = true").getResultList();
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToBugzillaBug map : results)
			retValue.add(map.getTopic().getTopicId());
		return retValue;
	}

	/**
	 * @return A comma separated list of topic ids that have been included in a
	 *         content spec
	 * @throws Exception
	 */
	public static String getTopicsInContentSpec(final Integer contentSpecTopicID) throws Exception
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic contentSpec = entityManager.find(Topic.class, contentSpecTopicID);

			if (contentSpec == null)
				return Constants.NULL_TOPIC_ID;

			final ContentSpecParser csp = new ContentSpecParser("http://localhost:8080/TopicIndex/");
			if (csp.parse(contentSpec.getTopicXML()))
			{
				final List<Integer> topicIds = csp.getReferencedTopicIds();
				if (topicIds.size() == 0)
					return Constants.NULL_TOPIC_ID;

				return CollectionUtilities.toSeperatedString(topicIds);
			}
			else
			{
				return Constants.NULL_TOPIC_ID;
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, true, "An invalid Topic ID was stored for a Content Spec in the database, or the topic was not a valid content spec");
			return Constants.NULL_TOPIC_ID;
		}
	}

	/**
	 * @return A comma separated list of topic ids that have open bugzilla bugs
	 *         assigned to them
	 */
	public static String getTopicsWithOpenBugsString()
	{
		final List<Integer> topics = getTopicsWithOpenBugs();
		if (topics.size() == 0)
			return Constants.NULL_TOPIC_ID;

		return CollectionUtilities.toSeperatedString(topics);
	}

	/**
	 * @return A list of topic ids that have bugzilla bugs assigned to them
	 */
	@SuppressWarnings("unchecked")
	public static List<Integer> getTopicsWithBugs()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<TopicToBugzillaBug> results = entityManager.createQuery(TopicToBugzillaBug.SELECT_ALL_QUERY).getResultList();
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToBugzillaBug map : results)
			if (!retValue.contains(map.getTopic().getTopicId()))
				retValue.add(map.getTopic().getTopicId());
		return retValue;
	}

	@SuppressWarnings("unchecked")
	public static String getTopicsWithPropertyTag(final Integer propertyTagIdInt, final String propertyTagValue)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<TopicToPropertyTag> mappings = entityManager.createQuery(TopicToPropertyTag.SELECT_ALL_QUERY + " WHERE topicToPropertyTag.propertyTag.propertyTagId = " + propertyTagIdInt + " AND topicToPropertyTag.value = '" + propertyTagValue + "'").getResultList();
		if (mappings.size() == 0)
			return Constants.NULL_TOPIC_ID;

		final StringBuilder retValue = new StringBuilder();
		for (final TopicToPropertyTag mapping : mappings)
		{
			if (retValue.length() != 0)
				retValue.append(",");
			retValue.append(mapping.getTopic().getTopicId());
		}

		return retValue.toString();
	}

	/**
	 * @return A comma separated list of topic ids that have bugzilla bugs
	 *         assigned to them
	 */
	public static String getTopicsWithBugsString()
	{
		final List<Integer> topics = getTopicsWithBugs();
		if (topics.size() == 0)
			return Constants.NULL_TOPIC_ID;

		return CollectionUtilities.toSeperatedString(topics);
	}

	@SuppressWarnings("unchecked")
	public static <E> List<Integer> getEditedEntities(final Class<E> type, final String pkColumnName, final DateTime startDate, final DateTime endDate)
	{
		if (startDate == null && endDate == null)
			return null;

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);

		final AuditQuery query = reader.createQuery().forRevisionsOfEntity(type, true, false).addOrder(AuditEntity.revisionProperty("timestamp").asc()).addProjection(AuditEntity.property("originalId." + pkColumnName).distinct());

		if (startDate != null)
			query.add(AuditEntity.revisionProperty("timestamp").ge(startDate.toDate().getTime()));

		if (endDate != null)
			query.add(AuditEntity.revisionProperty("timestamp").le(endDate.toDate().getTime()));

		final List<Integer> entityyIds = query.getResultList();

		return entityyIds;
	}

	public static <E> String getEditedEntitiesString(final Class<E> type, final String pkColumnName, final DateTime startDate, final DateTime endDate)
	{
		final List<Integer> ids = getEditedEntities(type, pkColumnName, startDate, endDate);
		if (ids != null && ids.size() != 0)
			return CollectionUtilities.toSeperatedString(ids);
		return Constants.NULL_TOPIC_ID;
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> getTextSearchTopicMatch(final String phrase)
	{
		/* Escape all values that are interpreted as key words/values by lucene */
		String escapedPhrase = phrase.replaceAll("\\{", "\\\\{");
		escapedPhrase = escapedPhrase.replaceAll("\\}", "\\\\}");
		escapedPhrase = escapedPhrase.replaceAll("\\[", "\\\\[");
		escapedPhrase = escapedPhrase.replaceAll("\\]", "\\\\]");
		escapedPhrase = escapedPhrase.replaceAll(":", "\\:");
		
		final List<Integer> retValue = new ArrayList<Integer>();

		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			// get the Hibernate session from the EntityManager
			final Session session = (Session) entityManager.getDelegate();
			// get a Hibernate full text session. we use the Hibernate version,
			// instead of the JPA version,
			// because we can use the Hibernate versions to do projections
			final FullTextSession fullTextSession = Search.getFullTextSession(session);
			// create a query parser
			final QueryParser parser = new QueryParser(Version.LUCENE_31, "TopicSearchText", fullTextSession.getSearchFactory().getAnalyzer(Topic.class));
			// parse the query string
			final org.apache.lucene.search.Query query = parser.parse(escapedPhrase);

			// build a lucene query
			/*
			 * final org.apache.lucene.search.Query query = qb .keyword()
			 * .onFields("TopicSearchText") .matching(phrase) .createQuery();
			 */

			// build a hibernate query
			final org.hibernate.search.FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Topic.class);
			// set the projection to return the id's of any topic's that match
			// the query
			hibQuery.setProjection("topicId");
			// get the results. because we setup a projection, there is no trip
			// to the database
			final List<Object[]> results = hibQuery.list();
			// extract the data into the List<Integer>
			for (final Object[] projection : results)
			{
				final Integer id = (Integer) projection[0];
				retValue.add(id);
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error using Lucene");
		}
		
		/*
		 * an empty list will be interpreted as no restriction as opposed to
		 * return none. so add a non existent topic id so no matches are
		 * made
		 */
		if (retValue.size() == 0)
			retValue.add(-1);

		return retValue;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<Integer, ArrayList<Integer>> populateExclusionTags()
	{
		final HashMap<Integer, ArrayList<Integer>> retValue = new HashMap<Integer, ArrayList<Integer>>();
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		List<Tag> tagList = entityManager.createQuery(Tag.SELECT_ALL_QUERY).getResultList();
		for (final Tag tag : tagList)
			retValue.put(tag.getTagId(), tag.getExclusionTagIDs());
		return retValue;
	}

	@SuppressWarnings("unchecked")
	public static void populateMutuallyExclusiveCategories(final UIProjectsData guiData)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<Category> categoryList = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
		for (final Category category : categoryList)
		{
			for (final UIProjectData project : guiData.getProjectCategories())
			{
				for (final UICategoryData guiInputData : project.getCategories())
				{
					if (guiInputData.getId().equals(category.getCategoryId()) && category.isMutuallyExclusive())
					{
						guiInputData.setMutuallyExclusive(true);
						break;
					}
				}
			}
		}
	}

	/**
	 * Clean up generic text so it can be included in an XML tag
	 */
	public static String cleanTextForXML(final String input)
	{
		if (input == null)
			return "";

		final String retValue = Jsoup.parse(input).text().replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;").replaceAll("", "");
		return retValue;
	}

	/**
	 * This function is used to get a list of the currently selected tags. This
	 * is used to prepopulate the selected tags when creating a new topic
	 */
	public static String urlTagParameters(final FacesContext context)
	{
		final Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();

		String variables = "";

		for (final String key : paramMap.keySet())
		{
			if (key.startsWith(Constants.MATCH_TAG))
			{
				if (variables.length() == 0)
					variables += "?";
				else
					variables += "&";

				variables += key + "=" + paramMap.get(key);
			}
		}

		return variables;
	}

	/**
	 * A utility function that is used to append url parameters to a collection
	 * of url parameters
	 * 
	 * @param params
	 *            The existing url parameter string
	 * @param name
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 * @return The url parameters that were passed in via params, with the new
	 *         parameter appended to it
	 */
	public static String addParameter(final String params, final String name, final String value)
	{
		// Just use an empty string as the default value
		return addParameter(params, name, value, "");
	}

	/**
	 * A utility function that is used to append url parameters to a collection
	 * of url parameters
	 * 
	 * @param params
	 *            The existing url parameter string
	 * @param name
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 * @param defaultValue
	 *            Used in place of value if it is null
	 * @return The url parameters that were passed in via params, with the new
	 *         parameter appended to it
	 */
	public static String addParameter(final String params, final String name, final String value, final String defaultValue)
	{
		String newParams = params;

		if (newParams.length() == 0)
			newParams += "?";
		else
			newParams += "&";

		newParams += name + "=" + (value == null ? defaultValue : value);

		return newParams;
	}

	public static String cleanStringForJavaScriptVariableName(final String input)
	{
		return input.replaceAll("[^a-zA-Z]", "");
	}

	public static List<Integer> getOutgoingRelatedTopicIDs(final Integer topicRelatedTo)
	{
		try
		{
			if (topicRelatedTo != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic topic = entityManager.find(Topic.class, topicRelatedTo);
				return topic.getRelatedTopicIDs();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, topicRelatedTo + " is probably not a valid Topic ID");
		}

		return null;
	}

	public static List<Integer> getIncomingRelatedTopicIDs(final Integer topicRelatedFrom)
	{
		try
		{
			if (topicRelatedFrom != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic topic = entityManager.find(Topic.class, topicRelatedFrom);
				return topic.getIncomingRelatedTopicIDs();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, topicRelatedFrom + " is probably not a valid Topic ID");
		}

		return null;
	}

	public static String getIncomingRelationshipsTo(final Integer topicId)
	{
		final List<Integer> ids = getIncomingRelatedTopicIDs(topicId);
		if (ids != null && ids.size() != 0)
			return CollectionUtilities.toSeperatedString(ids);
		return Constants.NULL_TOPIC_ID;
	}

	public static String getOutgoingRelationshipsFrom(final Integer topicId)
	{
		final List<Integer> ids = getOutgoingRelatedTopicIDs(topicId);
		if (ids != null && ids.size() != 0)
			return CollectionUtilities.toSeperatedString(ids);
		return Constants.NULL_TOPIC_ID;
	}

	public static String buildEditNewTopicUrl(final UIProjectsData selectedTags)
	{
		String tags = "";
		for (final UIProjectData project : selectedTags.getProjectCategories())
		{
			for (final UICategoryData cat : project.getCategories())
			{
				if (cat.isMutuallyExclusive())
				{
					if (cat.getSelectedTag() != null)
					{
						if (tags.length() != 0)
							tags += "&";
						tags += "tag" + cat.getSelectedTag() + "=1";
					}

				}
				else
				{
					for (final UITagData tag : cat.getTags())
					{
						if (tag.isSelected())
						{
							if (tags.length() != 0)
								tags += "&";
							tags += "tag" + tag.getId() + "=1";
						}
					}
				}
			}
		}
		final String retValue = "/TopicEdit.seam?" + tags;
		return retValue;
	}

	@SuppressWarnings("unchecked")
	public static List<Topic> getTopicsFromFilter(final Filter filter)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// build the query that will be used to get the topics
		final String query = filter.buildQuery(new TopicFilterQueryBuilder());

		// get the base topic list
		final List<Topic> topicList = entityManager.createQuery(query).getResultList();

		return topicList;
	}

	public static Map<TranslatedTopic, Topic> getHistoricalTopicsFromTranslatedTopics()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		/* get the current TranslatedTopics */
		@SuppressWarnings("unchecked")
		final List<TranslatedTopic> translatedTopics = entityManager.createQuery(TranslatedTopic.SELECT_ALL_QUERY).getResultList();

		final Map<TranslatedTopic, Topic> retValue = new HashMap<TranslatedTopic, Topic>();

		/*
		 * Loop over all the TranslatedTopics, get the associated Envers object,
		 * and check to see is that topics contains the tag we are looking for
		 */
		for (final TranslatedTopic translatedTopic : translatedTopics)
		{
			retValue.put(translatedTopic, translatedTopic.getEnversTopic(entityManager));
		}

		return retValue;

	}
	
	@SuppressWarnings("unchecked")
	public static List<Integer> getLatestTranslatedTopics()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		String query = TranslatedTopicData.SELECT_ALL_QUERY;
		query += " where translatedTopicData.translatedTopic.topicRevision = (Select MAX(B.translatedTopic.topicRevision) FROM TranslatedTopicData B WHERE translatedTopicData.translatedTopic.topicId = B.translatedTopic.topicId AND B.translationLocale = translatedTopicData.translationLocale GROUP BY B.translatedTopic.topicId)";
		final List<TranslatedTopicData> results = entityManager.createQuery(query).getResultList();
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TranslatedTopicData topic : results)
			if (!retValue.contains(topic.getTranslatedTopicDataId()))
				retValue.add(topic.getTranslatedTopicDataId());
		return retValue;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Integer> getLatestCompletedTranslatedTopics()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		String query = TranslatedTopicData.SELECT_ALL_QUERY;
		query += " where translatedTopicData.translatedTopic.topicRevision = (Select MAX(B.translatedTopic.topicRevision) FROM TranslatedTopicData B WHERE translatedTopicData.translatedTopic.topicId = B.translatedTopic.topicId AND B.translationLocale = translatedTopicData.translationLocale AND B.translationPercentage >= 100 GROUP BY B.translatedTopic.topicId)";
		final List<TranslatedTopicData> results = entityManager.createQuery(query).getResultList();
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TranslatedTopicData topic : results)
			if (!retValue.contains(topic.getTranslatedTopicDataId()))
				retValue.add(topic.getTranslatedTopicDataId());
		return retValue;
	}
	
	public static String getLatestTranslatedTopicsString()
	{
		final List<Integer> topics = getLatestTranslatedTopics();
		if (topics.size() == 0)
			return Constants.NULL_TOPIC_ID;

		return CollectionUtilities.toSeperatedString(topics);
	}
	
	public static String getLatestCompletedTranslatedTopicsString()
	{
		final List<Integer> topics = getLatestCompletedTranslatedTopics();
		if (topics.size() == 0)
			return Constants.NULL_TOPIC_ID;

		return CollectionUtilities.toSeperatedString(topics);
	}
}
