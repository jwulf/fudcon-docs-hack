package com.redhat.topicindex.entity;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsArray.array;
import com.j2bugzilla.base.ECSBug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.naming.InitialContext;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import net.htmlparser.jericho.Source;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.j2bugzilla.base.BugzillaConnector;
import com.j2bugzilla.rpc.BugSearch;
import com.j2bugzilla.rpc.GetBug;
import com.j2bugzilla.rpc.LogIn;
import com.redhat.ecs.commonstructures.NameIDSortMap;
import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.commonutils.NotificationUtilities;
import com.redhat.ecs.commonutils.StringUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.ecs.constants.CommonConstants;
import com.redhat.ecs.services.docbookcompiling.DocbookUtils;
import com.redhat.topicindex.entity.base.ParentToPropertyTag;
import com.redhat.topicindex.entity.base.ToPropertyTag;
import com.redhat.topicindex.sort.CategoryNameComparator;
import com.redhat.topicindex.sort.TagIDComparator;
import com.redhat.topicindex.sort.TagNameComparator;
import com.redhat.topicindex.sort.TagToCategorySortingComparator;
import com.redhat.topicindex.sort.TopicIDComparator;
import com.redhat.topicindex.sort.TopicToTagTagIDSort;
import com.redhat.topicindex.sort.TopicToTopicMainTopicIDSort;
import com.redhat.topicindex.sort.TopicToTopicRelatedTopicIDSort;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.topicrenderer.TopicQueueRenderer;
import com.redhat.topicindex.messaging.TopicRendererType;


import net.unto.twitter.Api;



@Entity
@Audited
@Indexed
@Table(name = "Topic")
public class Topic extends ParentToPropertyTag<Topic> implements java.io.Serializable
{
	public static final String SELECT_ALL_QUERY = "SELECT topic FROM com.redhat.topicindex.entity.Topic as Topic";
	/**
	 * The string that identifies the automatically generated comment added to
	 * the end of the XML
	 */
	private static final String DETAILS_COMMENT_NODE_START = " Generated Topic Details";
	/** The string constant that is used as a conceptual overview template */
	private static final Integer CONCEPTUAL_OVERVIEW_TOPIC_STRINGCONSTANTID = 11;
	/** The string constant that is used as a reference template */
	private static final Integer REFERENCE_TOPIC_STRINGCONSTANTID = 12;
	/** The string constant that is used as a task template */
	private static final Integer TASK_TOPIC_STRINGCONSTANTID = 13;
	/** The string constant that is used as a concept template */
	private static final Integer CONCEPT_TOPIC_STRINGCONSTANTID = 14;

	private static final long serialVersionUID = 5580473587657911655L;

	@Logger
	private Log log;

	/**
	 * Returns the headers for the CSV columns
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transient
	public static String getCSVHeaderRow()
	{
		final EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		final List<Category> categories = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
		Collections.sort(categories, new CategoryNameComparator());
		
		String topicColumns = StringUtilities.buildString(new String[] { "Topic ID", "Topic Title", "Topic Text", "Topic URL" }, ",");

		for (final Category category : categories)
			topicColumns += "," + category.getCategoryName();

		return topicColumns;
	}

	/**
	 * If this value is false, when this entity is saved it will not trigger a
	 * rerender of the any related topics
	 */
	private boolean rerenderRelatedTopics = true;

	private Integer topicId;
	private String topicText;
	private Date topicTimeStamp;
	private String topicTitle;
	private Set<TopicToTag> topicToTags = new HashSet<TopicToTag>(0);
	private Set<TopicToTopic> parentTopicToTopics = new HashSet<TopicToTopic>(0);
	private Set<TopicToTopic> childTopicToTopics = new HashSet<TopicToTopic>(0);
	private Set<TopicToTopicSourceUrl> topicToTopicSourceUrls = new HashSet<TopicToTopicSourceUrl>(0);
	private Set<TopicToPropertyTag> topicToPropertyTags = new HashSet<TopicToPropertyTag>(0);
	private Set<TopicToBugzillaBug> topicToBugzillaBugs = new HashSet<TopicToBugzillaBug>(0);
	private String topicXML;
	private TopicSecondOrderData topicSecondOrderData;
	private String topicLocale = Constants.DEFAULT_LOCALE;

	/*
	 * While processing a topic for inclusion in the final output we need some
	 * additional temporary variables. These variables are all prefixed with
	 * 'temp'.
	 */

	/** the role (typically the highest priority lifecycle tag) is saved here */
	private String tempTopicRole;
	/** The parsed XML DOM */
	private Document tempTopicXMLDoc;
	/** the relative priority of this topic */
	private Integer tempRelativePriority;
	/** true if this topic has failed validation */
	private boolean tempInvalidTopic = false;
	/**
	 * The Docbook ListItem that holds an XRef to this topic. This link is used
	 * when building the TOC
	 */
	private String tempNavLinkDocbook;
	/** Used when pushing topics to Zanata */
	private Number tempRevisionNumber;
	
	@Override
	@Transient
	public Integer getId()
	{
		return this.topicId;
	}

	public Topic()
	{
		super(Topic.class);
		
		this.topicLocale = System.getProperty(CommonConstants.DEFAULT_LOCALE_PROPERTY);
	}

 	public void tweet()
	{
 		if ( System.getProperty("topicIndex.twitterOn") == "true" )
 		{
			try
			{
				NotificationUtilities.dumpMessageToStdOut("Tweeting");
				Credentials credentials = (Credentials) Component.getInstance(Credentials.class);
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setDebugEnabled(true)
				  .setOAuthConsumerKey(System.getProperty("topicIndex.twitterConsumerKey"))
				  .setOAuthConsumerSecret(System.getProperty("topicIndex.twitterConsumerSecret"))
				  .setOAuthAccessToken(System.getProperty("topicIndex.twitterAccessToken"))
				  .setOAuthAccessTokenSecret(System.getProperty("topicIndex.twitterAccessTokenSecret"));
				TwitterFactory tf = new TwitterFactory(cb.build());
				Twitter twitter = tf.getInstance();
				
				Status status = twitter.updateStatus(credentials.getUsername() + " edited \"" + this.topicTitle + "\"" + System.getProperty("topicIndex.myURL") + "/Topic.seam?topicTopicId="  + this.topicId + " " + System.getProperty("topicIndex.twitterHashTag"));
				NotificationUtilities.dumpMessageToStdOut("Successfully updated the status to [" + status.getText() + "].");
				    
			}
			catch (final Exception ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "A problem with the twitter integration");
			}
 		}
	}

	
 	
	public void addDetailsCommentToXML()
	{
		final Document doc = XMLUtilities.convertStringToDocument(this.topicXML);
		if (doc != null)
		{
			String detailsCommentContent = DETAILS_COMMENT_NODE_START + "\n\n" + "GENERAL DETAILS\n" + "\n" + "Topic ID: " + this.topicId + "\n" + "Topic Title: " + this.topicTitle + "\nTopic Description: " + this.topicText + "\n\n" + "TOPIC TAGS\n" + "\n";

			final ArrayList<TopicToTag> sortedTags = new ArrayList<TopicToTag>(this.getTopicToTags());
			Collections.sort(sortedTags, new TopicToTagTagIDSort());

			for (final TopicToTag topicToTag : sortedTags)
			{
				final Tag tag = topicToTag.getTag();
				detailsCommentContent += tag.getTagId() + ": " + tag.getTagName() + "\n";
			}

			detailsCommentContent += "\nSOURCE URLS\n\n";
			for (final TopicToTopicSourceUrl topicToSourceUrl : this.getTopicToTopicSourceUrls())
			{
				final TopicSourceUrl url = topicToSourceUrl.getTopicSourceUrl();
				detailsCommentContent += (url.getTitle() == null || url.getTitle().length() == 0 ? "Source URL: " : url.getTitle() + ": ");
				detailsCommentContent += url.getSourceUrl() + "\n";
			}

			final ArrayList<TopicToTopic> sortedTopics = new ArrayList<TopicToTopic>(this.getParentTopicToTopics());
			Collections.sort(sortedTopics, new TopicToTopicRelatedTopicIDSort());

			detailsCommentContent += "\nRELATED TOPICS\n\n";
			for (final TopicToTopic topicToTopic : sortedTopics)
			{
				final Topic topic = topicToTopic.getRelatedTopic();
				detailsCommentContent += topic.getTopicId() + ": " + topic.getTopicTitle() + "\n";
			}

			detailsCommentContent += "\n";

			final Node detailsComment = doc.createComment(detailsCommentContent);

			boolean foundComment = false;
			for (final Node comment : XMLUtilities.getComments(doc))
			{
				final String commentContent = comment.getTextContent();
				if (commentContent.startsWith(DETAILS_COMMENT_NODE_START))
				{
					foundComment = true;
					comment.getParentNode().replaceChild(detailsComment, comment);
					break;
				}
			}

			if (!foundComment)
			{
				doc.getDocumentElement().appendChild(detailsComment);
			}

			this.topicXML = XMLUtilities.convertDocumentToString(doc, CommonConstants.XML_ENCODING);
		}
	}
	
	public boolean addRelationshipFrom(final EntityManager entityManager, final Integer topicId, final Integer relationshipTagId)
	{
		final Topic topic = entityManager.getReference(Topic.class, topicId);
		final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
		return addRelationshipFrom(topic, relationshipTag);
	}

	public boolean addRelationshipFrom(final EntityManager entityManager, final Topic topic, final Integer relationshipTagId)
	{
		final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
		return addRelationshipFrom(topic, relationshipTag);
	}

	public boolean addRelationshipFrom(final Topic relatedTopic, final RelationshipTag relationshipTag)
	{
		if (!this.isRelatedTo(relatedTopic, relationshipTag))
		{
			final TopicToTopic topicToTopic = new TopicToTopic(relatedTopic, this, relationshipTag);
			this.getChildTopicToTopics().add(topicToTopic);
			relatedTopic.getParentTopicToTopics().add(topicToTopic);
			return true;
		}

		return false;
	}

	public boolean addRelationshipTo(final EntityManager entityManager, final Integer topicId, final Integer relationshipTagId)
	{
		final Topic topic = entityManager.getReference(Topic.class, topicId);
		final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
		return addRelationshipTo(topic, relationshipTag);
	}

	public boolean addRelationshipTo(final EntityManager entityManager, final Topic topic, final Integer relationshipTagId)
	{
		final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
		return addRelationshipTo(topic, relationshipTag);
	}

	public boolean addRelationshipTo(final Topic relatedTopic, final RelationshipTag relationshipTag)
	{
		if (!this.isRelatedTo(relatedTopic, relationshipTag))
		{
			final TopicToTopic topicToTopic = new TopicToTopic(this, relatedTopic, relationshipTag);
			this.getParentTopicToTopics().add(topicToTopic);
			relatedTopic.getChildTopicToTopics().add(topicToTopic);
			return true;
		}

		return false;
	}

	public void addTag(final EntityManager entityManager, final int tagID)
	{
		final Tag tag = entityManager.getReference(Tag.class, tagID);
		addTag(tag);
	}

	public void addTag(final int tagID)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.getReference(Tag.class, tagID);
		addTag(tag);
	}

	public void addTag(final Tag tag)
	{
		if (filter(having(on(TopicToTag.class).getTag(), equalTo(tag)), this.getTopicToTags()).size() == 0)
		{
			this.topicToTags.add(new TopicToTag(this, tag));
		}
	}

	public void addTopicSourceUrl(final TopicSourceUrl topicSourceUrl)
	{
		if (filter(having(on(TopicToTopicSourceUrl.class).getTopicSourceUrl(), equalTo(topicSourceUrl)), this.getTopicToTopicSourceUrls()).size() == 0)
		{
			this.topicToTopicSourceUrls.add(new TopicToTopicSourceUrl(topicSourceUrl, this));
		}
	}
	
	public void addBugzillaBug(final BugzillaBug entity)
	{
		if (filter(having(on(TopicToBugzillaBug.class).getBugzillaBug(), equalTo(entity)), this.topicToBugzillaBugs).size() == 0)
		{
			this.topicToBugzillaBugs.add(new TopicToBugzillaBug(entity, this));
		}
	}

	@Transient
	private TreeMap<NameIDSortMap, ArrayList<Tag>> getCategoriesMappedToTags()
	{
		final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = new TreeMap<NameIDSortMap, ArrayList<Tag>>();

		final Set<TopicToTag> topicToTags = this.topicToTags;
		for (final TopicToTag topicToTag : topicToTags)
		{
			final Tag tag = topicToTag.getTag();
			final Set<TagToCategory> tagToCategories = tag.getTagToCategories();

			if (tagToCategories.size() == 0)
			{
				final NameIDSortMap categoryDetails = new NameIDSortMap("Uncatagorised", -1, 0);

				if (!tags.containsKey(categoryDetails))
					tags.put(categoryDetails, new ArrayList<Tag>());

				tags.get(categoryDetails).add(tag);
			}
			else
			{
				for (final TagToCategory category : tagToCategories)
				{
					final NameIDSortMap categoryDetails = new NameIDSortMap(category.getCategory().getCategoryName(), category.getCategory().getCategoryId(), category.getCategory().getCategorySort() == null ? 0 : category.getCategory().getCategorySort());

					if (!tags.containsKey(categoryDetails))
						tags.put(categoryDetails, new ArrayList<Tag>());

					tags.get(categoryDetails).add(tag);
				}
			}
		}

		return tags;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "relatedTopic")
	public Set<TopicToTopic> getChildTopicToTopics()
	{
		return childTopicToTopics;
	}

	@Transient
	public String getCommaSeparatedTagList()
	{
		final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = getCategoriesMappedToTags();

		String tagsList = "";
		for (final NameIDSortMap key : tags.keySet())
		{
			// sort alphabetically
			Collections.sort(tags.get(key), new TagNameComparator());

			if (tagsList.length() != 0)
				tagsList += " ";

			tagsList += key.getName() + ": ";

			String thisTagList = "";

			for (final Tag tag : tags.get(key))
			{
				if (thisTagList.length() != 0)
					thisTagList += ", ";

				thisTagList += tag.getTagName();
			}

			tagsList += thisTagList + " ";
		}

		return tagsList;
	}

	@SuppressWarnings("unchecked")
	@Transient
	public String getCSVRow()
	{
		// get a space separated list of source URLs
		String sourceUrls = "";
		for (final TopicToTopicSourceUrl url : this.getTopicToTopicSourceUrls())
		{
			if (sourceUrls.length() != 0)
				sourceUrls += " ";
			sourceUrls += url.getTopicSourceUrl().getSourceUrl();
		}

		String topicColumns = StringUtilities.cleanTextForCSV(this.topicId.toString()) + "," + StringUtilities.cleanTextForCSV(this.topicTitle) + "," + StringUtilities.cleanTextForCSV(this.topicText) + "," + StringUtilities.cleanTextForCSV(sourceUrls);

		final EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		final List<Category> categories = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
		Collections.sort(categories, new CategoryNameComparator());

		for (final Category category : categories)
		{
			final List<TopicToTag> matchingTags = filter(having(on(TopicToTag.class).getTag().getTagToCategories().toArray(), array(org.hamcrest.Matchers.hasProperty("category", equalTo(category)))), this.getTopicToTags());

			String tags = "";
			for (final TopicToTag topicToTag : matchingTags)
			{
				final Tag tag = topicToTag.getTag();
				if (tags.length() != 0)
					tags += ", ";
				tags += tag.getTagId() + ": " + tag.getTagName();
			}

			topicColumns += "," +  StringUtilities.cleanTextForCSV(tags);
		}

		return topicColumns;
	}

	@Transient
	public List<Integer> getIncomingRelatedTopicIDs()
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToTopic topicToTopic : this.getChildTopicToTopics())
			retValue.add(topicToTopic.getMainTopic().getTopicId());
		return retValue;
	}



	@Transient
	public List<Topic> getOutgoingRelatedTopicsArray()
	{
		final ArrayList<Topic> retValue = new ArrayList<Topic>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
		{
			retValue.add(topicToTopic.getRelatedTopic());
		}
		return retValue;
	}

	@Transient
	public List<Topic> getIncomingRelatedTopicsArray()
	{
		final ArrayList<Topic> retValue = new ArrayList<Topic>();
		for (final TopicToTopic topicToTopic : this.getChildTopicToTopics())
			retValue.add(topicToTopic.getMainTopic());

		Collections.sort(retValue, new TopicIDComparator());

		return retValue;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mainTopic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToTopic> getParentTopicToTopics()
	{
		return parentTopicToTopics;
	}

	@Transient
	public Topic getRelatedTopicByID(final Integer id)
	{
		for (final Topic topic : this.getOutgoingRelatedTopicsArray())
			if (topic.getTopicId().equals(id))
				return topic;
		return null;
	}

	@Transient
	public List<Integer> getRelatedTopicIDs()
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			retValue.add(topicToTopic.getRelatedTopic().getTopicId());
		return retValue;
	}

	@Transient
	public String getRelatedTopicsList()
	{
		String topicList = "";
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
		{
			final Topic relatedTopic = topicToTopic.getRelatedTopic();

			if (topicList.length() != 0)
				topicList += ", ";

			topicList += "<span title=\"" + relatedTopic.getTopicTitle() + " \n" + relatedTopic.getCommaSeparatedTagList() + "\">" + topicToTopic.getRelatedTopic().getTopicId() + "</span>";
		}
		return topicList;
	}

	@Transient
	public List<Integer> getTagIDs()
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToTag topicToTag : this.topicToTags)
		{
			final Integer tagId = topicToTag.getTag().getTagId();
			retValue.add(tagId);
		}

		return retValue;
	}

	@Transient
	public List<Tag> getTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TopicToTag topicToTag : this.topicToTags)
		{
			final Tag tag = topicToTag.getTag();
			retValue.add(tag);
		}

		Collections.sort(retValue, new TagIDComparator());

		return retValue;
	}

	/**
	 * This is necessary because a4j:repeat does not work with a Set
	 */
	@Transient
	public ArrayList<Tag> getTagsArray()
	{
		final ArrayList<Tag> retValue = new ArrayList<Tag>();
		for (final TopicToTag topicToTag : this.topicToTags)
			retValue.add(topicToTag.getTag());
		return retValue;
	}

	@Transient
	public List<Tag> getTagsInCategories(final List<Category> categories)
	{
		final List<Integer> catgeoriesByID = new ArrayList<Integer>();
		for (final Category category : categories)
			catgeoriesByID.add(category.getCategoryId());
		return getTagsInCategoriesByID(catgeoriesByID);
	}

	@Transient
	public List<Tag> getTagsInCategoriesByID(final List<Integer> categories)
	{
		final List<Tag> retValue = new ArrayList<Tag>();

		for (final Integer categoryId : categories)
		{
			for (final TopicToTag topicToTag : this.topicToTags)
			{
				final Tag tag = topicToTag.getTag();

				if (topicToTag.getTag().isInCategory(categoryId))
				{
					if (!retValue.contains(tag))
						retValue.add(tag);
				}
			}
		}

		return retValue;
	}

	@Transient
	public String getTagsList()
	{
		return getTagsList(true);
	}

	/**
	 * Generates a HTML formatted and categorized list of the tags that are
	 * associated with this topic
	 * 
	 * @return A HTML String to display in a table
	 */
	@Transient
	public String getTagsList(final boolean brLineBreak)
	{
		// define the line breaks for html and for tooltips
		final String lineBreak = brLineBreak ? "<br/>" : " \n";
		final String boldStart = brLineBreak ? "<b>" : "";
		final String boldEnd = brLineBreak ? "</b>" : "";

		final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = getCategoriesMappedToTags();

		String tagsList = "";
		for (final NameIDSortMap key : tags.keySet())
		{
			// sort alphabetically
			Collections.sort(tags.get(key), new TagNameComparator());

			if (tagsList.length() != 0)
				tagsList += lineBreak;

			tagsList += boldStart + key.getName() + boldEnd + ": ";

			String thisTagList = "";

			for (final Tag tag : tags.get(key))
			{
				if (thisTagList.length() != 0)
					thisTagList += ", ";

				thisTagList += "<span title=\"Tag ID: " + tag.getTagId() + " &#13;Tag Description: " + tag.getTagDescription() + "\">" + tag.getTagName() + "</span>";
			}

			tagsList += thisTagList + " ";
		}

		return tagsList;
	}

	@Transient
	public String getTempNavLinkDocbook()
	{
		return tempNavLinkDocbook;
	}

	@Transient
	public Integer getTempRelativePriority()
	{
		return tempRelativePriority;
	}

	@Transient
	public Number getTempRevisionNumber()
	{
		return tempRevisionNumber;
	}

	@Transient
	public String getTempTopicRole()
	{
		return tempTopicRole;
	}

	@Transient
	public Document getTempTopicXMLDoc()
	{
		return tempTopicXMLDoc;
	}

	/**
	 * Convert an in memory XML document to a string. Also remove any XML
	 * preamble, meaning the XML returned by this function can be easily
	 * appended to existing XML.
	 */
	@Transient
	public String getTempTopicXMLDocString()
	{
		if (tempTopicXMLDoc != null)
		{
			String retValue = XMLUtilities.convertDocumentToString(this.tempTopicXMLDoc, CommonConstants.XML_ENCODING);

			retValue = "<!-- Topic ID: " + this.topicId + " -->\n" + retValue.replaceAll("<\\?xml.*?\\?>", "").trim();

			return retValue;
		}

		return "";
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicID", unique = true, nullable = false)
	public Integer getTopicId()
	{
		return this.topicId;
	}

	@Column(name = "TopicLocale", length = 45)
	@NotNull
	@Length(max = 512)
	public String getTopicLocale()
	{
		return this.topicLocale == null ? Constants.DEFAULT_LOCALE : this.topicLocale;
	}

	@Transient
	public String getTopicRendered()
	{
		if (this.topicSecondOrderData == null)
			return null;

		return topicSecondOrderData.getTopicHTMLView();
	}

	/**
	 * This function will take the XML in the topicXML String and use it to
	 * generate a text only view that will be used by Hibernate Search. The text
	 * extraction uses Jericho - http://jericho.htmlparser.net/
	 */
	@Transient
	@Field(name = "TopicSearchText", index = Index.TOKENIZED, store = Store.YES)
	public String getTopicSearchText()
	{
		if (this.topicXML == null)
			return "";
		
		final Source source = new Source(this.topicXML);
		source.fullSequentialParse();
		return source.getTextExtractor().toString();
	}

	@OneToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(name = "TopicToTopicSecondOrderData", joinColumns = { @JoinColumn(name = "TopicID", unique = true) }, inverseJoinColumns = { @JoinColumn(name = "TopicSecondOrderDataID") })
	@NotAudited
	public TopicSecondOrderData getTopicSecondOrderData()
	{
		return topicSecondOrderData;
	}

	// @Column(name = "TopicText", length = 65535)
	@Column(name = "TopicText", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTopicText()
	{
		return this.topicText;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TopicTimeStamp", nullable = false, length = 0)
	@NotNull
	public Date getTopicTimeStamp()
	{
		return this.topicTimeStamp;
	}

	@Column(name = "TopicTitle", length = 512)
	@Length(max = 512)
	public String getTopicTitle()
	{
		return this.topicTitle;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToTag> getTopicToTags()
	{
		return this.topicToTags;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToTopicSourceUrl> getTopicToTopicSourceUrls()
	{
		return this.topicToTopicSourceUrls;
	}

	// @Column(name = "TopicXML", length = 65535)
	@Column(name = "TopicXML", columnDefinition = "MEDIUMTEXT")
	@Length(max = 16777215)
	public String getTopicXML()
	{
		return this.topicXML;
	}

	@Transient
	public String getTopicXMLErrors()
	{
		if (this.topicSecondOrderData == null)
			return null;

		return topicSecondOrderData.getTopicXMLErrors();
	}

	/**
	 * @return The File ID used to identify this topic and revision in Zanata
	 */
	@Transient
	public String getZanataId()
	{
		return this.topicId + "-" + this.tempRevisionNumber;
	}

	/**
	 * @return The URL used to list the bugs in Bugzilla assigned against this
	 *         topic. The buildIDRegexParam is tied into the value returned by
	 *         the getBugzillaBuildId function.
	 */
	@Transient
	public String getBugzillaSearchURL()
	{
		final String baseURL = "https://bugzilla.redhat.com/buglist.cgi";
		final String componentParam = "component=topics-EAP6";
		final String productParam = "product=Topic%20Tool";
		final String buildIDRegexParam = "field0-0-0=cf_build_id&type0-0-0=regexp&value0-0-0=" + this.topicId + "-%5B0-9%5D%2B%20%5B0-9%5D%7B2%7D%20%5BA-Za-z%5D%7B3%7D%20%5B0-9%5D%7B4%7D%20%5B0-9%5D%7B2%7D%3A%5B0-9%5D%7B2%7D";

		return baseURL + "?" + buildIDRegexParam + "&" + componentParam + "&" + productParam;
	}

	public void initializeFromTemplate()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

			if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.CONCEPT_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, CONCEPT_TOPIC_STRINGCONSTANTID).getConstantValue();
			}
			else if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.TASK_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, TASK_TOPIC_STRINGCONSTANTID).getConstantValue();
			}
			else if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.REFERENCE_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, REFERENCE_TOPIC_STRINGCONSTANTID).getConstantValue();
			}
			else if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.CONCEPTUALOVERVIEW_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, CONCEPTUAL_OVERVIEW_TOPIC_STRINGCONSTANTID).getConstantValue();
			}

			processXML();

			// addDetailsCommentToXML();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably countn't find one of the string constants");
		}
	}

	public void initializeTempTopicXMLDoc()
	{
		this.tempTopicXMLDoc = XMLUtilities.convertStringToDocument(this.topicXML);
	}

	@Transient
	public boolean isRelatedTo(final Integer relatedTopicId)
	{
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().topicId.equals(relatedTopicId))
				return true;

		return false;
	}

	@Transient
	public boolean isRelatedTo(final Topic relatedTopic, final RelationshipTag relationshipTag)
	{
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().equals(relatedTopic) && topicToTopic.getRelationshipTag().equals(relationshipTag))
				return true;

		return false;
	}

	@Transient
	public boolean isRelatedTo(final Topic relatedTopic)
	{
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().equals(relatedTopic))
				return true;

		return false;
	}

	@Transient
	public boolean isRerenderRelatedTopics()
	{
		return rerenderRelatedTopics;
	}

	@Transient
	public boolean isTaggedWith(final Integer tagId)
	{
		for (final TopicToTag topicToTag : this.getTopicToTags())
			if (topicToTag.getTag().getTagId().equals(tagId))
				return true;

		return false;
	}

	@Transient
	public boolean isTaggedWith(final Tag tag)
	{
		for (final TopicToTag topicToTag : this.getTopicToTags())
			if (topicToTag.getTag().equals(tag))
				return true;

		return false;
	}

	@Transient
	public boolean isTempInvalidTopic()
	{
		return tempInvalidTopic;
	}

	@SuppressWarnings("unused")
	@PostPersist
	private void onPostPersist()
	{
		// renderTopics();
		tweet();
	}

	@SuppressWarnings("unused")
	@PostUpdate
	private void onPostUpdate()
	{
		//renderTopics();
		tweet();
	}

	@SuppressWarnings("unused")
	@PrePersist
	private void onPrePresist()
	{
		this.topicTimeStamp = new Date();
		validate();
	}

	@SuppressWarnings("unused")
	@PreUpdate
	private void onPreUpdate()
	{
		validate();
	}

	public boolean removeRelationshipTo(final Topic topic, final RelationshipTag relationshipTag)
	{
		return removeRelationshipTo(topic.getTopicId(), relationshipTag.getRelationshipTagId());
	}

	public boolean removeRelationshipTo(final Integer relatedTopicId, final Integer relationshipTagId)
	{
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
		{
			final Topic relatedTopic = topicToTopic.getRelatedTopic();
			final RelationshipTag relationshipTag = topicToTopic.getRelationshipTag();

			if (relatedTopic.getTopicId().equals(relatedTopicId) && relationshipTag.getRelationshipTagId().equals(relationshipTagId))
			{
				/* remove the relationship from this topic */
				this.getParentTopicToTopics().remove(topicToTopic);

				/* now remove the relationship from the other topic */
				for (final TopicToTopic childTopicToTopic : relatedTopic.getChildTopicToTopics())
				{
					if (childTopicToTopic.getMainTopic().equals(this))
					{
						relatedTopic.getChildTopicToTopics().remove(childTopicToTopic);
						break;
					}
				}

				return true;
			}
		}

		return false;
	}

	public void removeTag(final int tagID)
	{
		final List<TopicToTag> mappingEntities = filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(tagID)), this.getTopicToTags());
		if (mappingEntities.size() != 0)
		{
			this.topicToTags.remove(mappingEntities.get(0));
		}
	}
	
	public void removeTag(final Tag tag)
	{
		removeTag(tag.getTagId());
	}

	public void removeTopicSourceUrl(final int id)
	{
		final List<TopicToTopicSourceUrl> mappingEntities = filter(having(on(TopicToTopicSourceUrl.class).getTopicSourceUrl().getTopicSourceUrlid(), equalTo(id)), this.getTopicToTopicSourceUrls());
		if (mappingEntities.size() != 0)
		{
			this.topicToTopicSourceUrls.remove(mappingEntities.get(0));
		}
	}
	
	public void removeBugzillaBug(final int id)
	{
		final List<TopicToBugzillaBug> mappingEntities = filter(having(on(TopicToBugzillaBug.class).getBugzillaBug().getBugzillaBugId(), equalTo(id)), this.topicToBugzillaBugs);
		if (mappingEntities.size() != 0)
		{
			this.topicToBugzillaBugs.remove(mappingEntities.get(0));
		}
	}

	private void renderTopics()
	{
		if (!isRerenderRelatedTopics())
			return;
		
		if (isTaggedWith(Constants.CONTENT_SPEC_TAG_ID))
			return;

		/*
		 * Send a message to the queue that this topic, and all those that have
		 * inbound relationships, need to be rerendered. We use the
		 * TopicQueueRenderer to send the topic id once the transaction has
		 * finished.
		 */
		try
		{
			final InitialContext initCtx = new InitialContext();
			final TransactionManager transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			final Transaction transaction = transactionManager.getTransaction();
			WorkQueue.getInstance().execute(TopicQueueRenderer.createNewInstance(this.getTopicId(), TopicRendererType.TOPIC, transaction));
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a STOMP messaging problem");
		}

	}

	public void setChildTopicToTopics(final Set<TopicToTopic> childTopicToTopics)
	{
		this.childTopicToTopics = childTopicToTopics;
	}

	public void setParentTopicToTopics(final Set<TopicToTopic> parentTopicToTopics)
	{
		this.parentTopicToTopics = parentTopicToTopics;
	}

	public void setRerenderRelatedTopics(boolean rerenderRelatedTopics)
	{
		this.rerenderRelatedTopics = rerenderRelatedTopics;
	}

	public void setTempInvalidTopic(final boolean tempInvalidTopic)
	{
		this.tempInvalidTopic = tempInvalidTopic;
	}

	public void setTempNavLinkDocbook(final String tempNavLinkDocbook)
	{
		this.tempNavLinkDocbook = tempNavLinkDocbook;
	}

	public void setTempRelativePriority(final Integer tempRelativePriority)
	{
		this.tempRelativePriority = tempRelativePriority;
	}

	public void setTempRevisionNumber(final Number tempRevisionNumber)
	{
		this.tempRevisionNumber = tempRevisionNumber;
	}

	public void setTempTopicRole(final String tempTopicRole)
	{
		this.tempTopicRole = tempTopicRole;
	}

	public void setTempTopicXMLDoc(final Document tempTopicXMLDoc)
	{
		this.tempTopicXMLDoc = tempTopicXMLDoc;
	}

	public void setTopicId(final Integer topicId)
	{
		this.topicId = topicId;
	}

	public void setTopicLocale(final String topicLocale)
	{
		this.topicLocale = topicLocale;
	}

	public void setTopicRendered(final String value)
	{
		if (this.topicSecondOrderData == null)
			this.topicSecondOrderData = new TopicSecondOrderData();

		this.topicSecondOrderData.setTopicHTMLView(value);
	}

	public void setTopicSecondOrderData(TopicSecondOrderData topicSecondOrderData)
	{
		this.topicSecondOrderData = topicSecondOrderData;
	}

	public void setTopicText(final String topicText)
	{
		this.topicText = topicText;
	}

	public void setTopicTimeStamp(final Date topicTimeStamp)
	{
		this.topicTimeStamp = topicTimeStamp;
	}

	/*
	 * Envers has issues with this: https://hibernate.onjira.com/browse/HHH-3853
	 * Hibernate also has issues here, so we use a join table instead
	 */
	/*
	 * @OneToOne(cascade = CascadeType.ALL, optional = true, fetch =
	 * FetchType.LAZY, orphanRemoval = true)
	 * 
	 * @PrimaryKeyJoinColumn
	 */

	public void setTopicTitle(final String topicTitle)
	{
		this.topicTitle = topicTitle;
	}

	public void setTopicToTags(final Set<TopicToTag> topicToTags)
	{
		this.topicToTags = topicToTags;
	}

	public void setTopicToTopicSourceUrls(final Set<TopicToTopicSourceUrl> topicToTopicSourceUrls)
	{
		this.topicToTopicSourceUrls = topicToTopicSourceUrls;
	}

	public void setTopicXML(final String topicXML)
	{
		this.topicXML = topicXML;
	}

	public void setTopicXMLErrors(final String value)
	{
		if (this.topicSecondOrderData == null)
		{
			this.topicSecondOrderData = new TopicSecondOrderData();
		}

		this.topicSecondOrderData.setTopicXMLErrors(value);
	}

	private void processXML()
	{
		if (isTaggedWith(Constants.CONTENT_SPEC_TAG_ID))
			return;
		
		final Document doc = XMLUtilities.convertStringToDocument(this.topicXML);
		if (doc != null)
		{
			DocbookUtils.setSectionTitle(this.topicTitle, doc);

			// this.topicXML = XMLUtilities.convertDocumentToString(doc);

			/* Get the XML elements that require special processing */
			final String verbatimElementsString = System.getProperty(CommonConstants.VERBATIM_XML_ELEMENTS_SYSTEM_PROPERTY);
			final String inlineElementsString = System.getProperty(CommonConstants.INLINE_XML_ELEMENTS_SYSTEM_PROPERTY);
			final String contentsInlineElementsString = System.getProperty(CommonConstants.CONTENTS_INLINE_XML_ELEMENTS_SYSTEM_PROPERTY);

			final ArrayList<String> verbatimElements = verbatimElementsString == null ? new ArrayList<String>() : CollectionUtilities.toArrayList(verbatimElementsString.split(","));

			final ArrayList<String> inlineElements = inlineElementsString == null ? new ArrayList<String>() : CollectionUtilities.toArrayList(inlineElementsString.split(","));

			final ArrayList<String> contentsInlineElements = contentsInlineElementsString == null ? new ArrayList<String>() : CollectionUtilities.toArrayList(contentsInlineElementsString.split(","));

			this.topicXML = XMLUtilities.convertNodeToString(doc, verbatimElements, inlineElements, contentsInlineElements, true);
		}
	}

	/**
	 * Syncs the XML with the topic details, such as setting the topic title as
	 * the title of the XML.
	 */
	public void syncXML()
	{
		/* remove line breaks from the title */
		this.topicTitle = this.topicTitle.replaceAll("\n", " ").trim();
		processXML();

	}

	public void validate()
	{
		syncXML();
		validateTags();
		validateRelationships();
	}

	private void validateRelationships()
	{
		/* remove relationships to this topic in the parent collection*/
		final ArrayList<TopicToTopic> removeList = new ArrayList<TopicToTopic>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().getTopicId().equals(this.getTopicId()))
				removeList.add(topicToTopic);

		for (final TopicToTopic topicToTopic : removeList)
			this.getParentTopicToTopics().remove(topicToTopic);
		
		/* remove relationships to this topic in the child collection*/
		final ArrayList<TopicToTopic> removeChildList = new ArrayList<TopicToTopic>();
		for (final TopicToTopic topicToTopic : this.getChildTopicToTopics())
			if (topicToTopic.getMainTopic().getTopicId().equals(this.getTopicId()))
				removeChildList.add(topicToTopic);

		for (final TopicToTopic topicToTopic : removeChildList)
			this.getChildTopicToTopics().remove(topicToTopic);
	}

	private void validateTags()
	{
		/*
		 * validate the tags that are applied to this topic. generally the gui
		 * should enforce these rules, with the exception of the bulk tag apply
		 * function
		 */

		/*
		 * create a collection of Categories mapped to TagToCategories, sorted
		 * by the Category sorting order
		 */
		final TreeMap<Category, ArrayList<TagToCategory>> tagDB = new TreeMap<Category, ArrayList<TagToCategory>>(Collections.reverseOrder());

		for (final TopicToTag topicToTag : this.topicToTags)
		{
			final Tag tag = topicToTag.getTag();
			for (final TagToCategory tagToCategory : tag.getTagToCategories())
			{
				final Category category = tagToCategory.getCategory();

				if (!tagDB.containsKey(category))
					tagDB.put(category, new ArrayList<TagToCategory>());

				tagDB.get(category).add(tagToCategory);
			}
		}

		/* now remove conflicting tags */
		for (final Category category : tagDB.keySet())
		{
			/* sort by the tags position in the category */
			Collections.sort(tagDB.get(category), new TagToCategorySortingComparator(false));

			/*
			 * because of the way we have ordered the tagDB collections, and the
			 * ArrayLists it contains, this process will remove those tags that
			 * belong to lower priority categories, and lower priority tags in
			 * those categories
			 */

			final ArrayList<TagToCategory> tagToCategories = tagDB.get(category);

			// remove tags in the same mutually exclusive categories
			if (category.isMutuallyExclusive() && tagToCategories.size() > 1)
			{
				while (tagToCategories.size() > 1)
				{
					final TagToCategory tagToCategory = tagToCategories.get(1);
					/* get the lower priority tag */
					final Tag removeTag = tagToCategory.getTag();
					/* remove it from the tagDB collection */
					tagToCategories.remove(tagToCategory);

					/* and remove it from the tag collection */
					final ArrayList<TopicToTag> removeTopicToTagList = new ArrayList<TopicToTag>();
					for (final TopicToTag topicToTag : this.topicToTags)
					{
						if (topicToTag.getTag().equals(removeTag))
							removeTopicToTagList.add(topicToTag);
					}

					for (final TopicToTag removeTopicToTag : removeTopicToTagList)
					{
						this.topicToTags.remove(removeTopicToTag);
					}
				}
			}

			/* remove tags that are explicitly defined as mutually exclusive */
			for (final TagToCategory tagToCategory : tagToCategories)
			{
				final Tag tag = tagToCategory.getTag();
				for (final Tag exclusionTag : tag.getExcludedTags())
				{
					if (filter(having(on(TopicToTag.class).getTag(), equalTo(tagToCategory.getTag())), this.topicToTags).size() != 0 && // make
							/*
							 * sure that we have not removed this tag already
							 */
							filter(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)), this.topicToTags).size() != 0 && // make
							/*
							 * sure the exclusion tag exists
							 */
							!exclusionTag.equals(tagToCategory.getTag())) // make
					/*
					 * sure we are not trying to remove ourselves
					 */
					{
						with(this.topicToTags).remove(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)));
					}
				}
			}
		}
	}

	@Transient
	public List<TopicToTopic> getParentTopicToTopicsArray()
	{
		final List<TopicToTopic> retValue = CollectionUtilities.toArrayList(parentTopicToTopics);
		Collections.sort(retValue, new TopicToTopicRelatedTopicIDSort());
		return retValue;
	}

	@Transient
	public List<TopicToTopic> getChildTopicToTopicsArray()
	{
		final List<TopicToTopic> retValue = CollectionUtilities.toArrayList(childTopicToTopics);
		Collections.sort(retValue, new TopicToTopicMainTopicIDSort());
		return retValue;
	}

	public void changeTopicToTopicRelationshipTag(final RelationshipTag relationshipTag, final Topic existingTopic, final RelationshipTag existingRelationshipTag)
	{
		for (final TopicToTopic topicToTopic : this.parentTopicToTopics)
		{
			if (topicToTopic.getRelatedTopic().equals(existingTopic) && topicToTopic.getRelationshipTag().equals(existingRelationshipTag))
			{
				topicToTopic.setRelationshipTag(relationshipTag);
				break;
			}
		}
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToPropertyTag> getTopicToPropertyTags()
	{
		return topicToPropertyTags;
	}

	public void setTopicToPropertyTags(Set<TopicToPropertyTag> topicToPropertyTags)
	{
		this.topicToPropertyTags = topicToPropertyTags;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToBugzillaBug> getTopicToBugzillaBugs()
	{
		return this.topicToBugzillaBugs;
	}

	public void setTopicToBugzillaBugs(final Set<TopicToBugzillaBug> topicToBugzillaBugs)
	{
		this.topicToBugzillaBugs = topicToBugzillaBugs;
	}

	@Transient
	public List<TopicToPropertyTag> getTopicToPropertyTagsArray()
	{
		return CollectionUtilities.toArrayList(topicToPropertyTags);
	}

	@Transient
	public List<PropertyTag> getPropertyTagsArray()
	{
		final List<PropertyTag> retValue = new ArrayList<PropertyTag>();
		for (final TopicToPropertyTag mapping : this.topicToPropertyTags)
		{
			final PropertyTag entity = mapping.getPropertyTag();
			retValue.add(entity);
		}

		return retValue;
	}

	@Transient
	public List<TopicSourceUrl> getTopicSourceUrls()
	{
		final List<TopicSourceUrl> retValue = new ArrayList<TopicSourceUrl>();
		for (final TopicToTopicSourceUrl mapping : this.topicToTopicSourceUrls)
		{
			final TopicSourceUrl entity = mapping.getTopicSourceUrl();
			retValue.add(entity);
		}

		return retValue;
	}
	
	@Transient
	public List<BugzillaBug> getBugzillaBugs()
	{
		final List<BugzillaBug> retValue = new ArrayList<BugzillaBug>();
		for (final TopicToBugzillaBug mapping : this.topicToBugzillaBugs)
		{
			final BugzillaBug entity = mapping.getBugzillaBug();
			retValue.add(entity);
		}

		return retValue;
	}

	@Override
	@Transient
	protected Set<? extends ToPropertyTag<?>> getPropertyTags()
	{
		return this.topicToPropertyTags;
	}

	public void removePropertyTag(final TopicToPropertyTag topicToPropertyTag)
	{
		this.topicToPropertyTags.remove(topicToPropertyTag);
		topicToPropertyTag.getPropertyTag().getTopicToPropertyTags().remove(topicToPropertyTag);
	}

	public void addPropertyTag(final TopicToPropertyTag topicToPropertyTag)
	{
		this.topicToPropertyTags.add(topicToPropertyTag);
		topicToPropertyTag.getPropertyTag().getTopicToPropertyTags().add(topicToPropertyTag);
	}

	@SuppressWarnings("unused")
	@PreRemove
	private void preRemove()
	{
		for (final TopicToTag mapping : topicToTags)
			mapping.getTag().getTopicToTags().remove(mapping);
		
		for (final TopicToTopic mapping : childTopicToTopics)
			mapping.getMainTopic().getParentTopicToTopics().remove(mapping);
		
		for (final TopicToTopic mapping : parentTopicToTopics)
			mapping.getRelatedTopic().getChildTopicToTopics().remove(mapping);
		
		for (final TopicToPropertyTag mapping : topicToPropertyTags)
			mapping.getPropertyTag().getTopicToPropertyTags().remove(mapping);
		
		for (final TopicToTopicSourceUrl mapping : topicToTopicSourceUrls)
			mapping.getTopicSourceUrl().getTopicToTopicSourceUrls().remove(mapping);
		
		for (final TopicToBugzillaBug mapping : this.topicToBugzillaBugs)
			mapping.getBugzillaBug().getTopicToBugzillaBugs().remove(mapping);
		
		this.topicToTags.clear();
		this.childTopicToTopics.clear();
		this.parentTopicToTopics.clear();
		this.topicToPropertyTags.clear();
		this.topicToTopicSourceUrls.clear();
		this.topicToBugzillaBugs.clear();
	}

	public void addPropertyTag(final PropertyTag propertyTag, final String value)
	{
		final TopicToPropertyTag mapping = new TopicToPropertyTag();
		mapping.setTopic(this);
		mapping.setPropertyTag(propertyTag);
		mapping.setValue(value);

		this.topicToPropertyTags.add(mapping);
		propertyTag.getTopicToPropertyTags().add(mapping);
	}

	public void removePropertyTag(final PropertyTag propertyTag, final String value)
	{
		final List<TopicToPropertyTag> removeList = new ArrayList<TopicToPropertyTag>();

		for (final TopicToPropertyTag mapping : this.topicToPropertyTags)
		{
			final PropertyTag myPropertyTag = mapping.getPropertyTag();
			if (myPropertyTag.equals(propertyTag) && mapping.getValue().equals(value))
			{
				removeList.add(mapping);
			}
		}

		for (final TopicToPropertyTag mapping : removeList)
		{
			this.topicToPropertyTags.remove(mapping);
			mapping.getPropertyTag().getTopicToPropertyTags().remove(mapping);
		}
	}
	
	public void updateBugzillaBugs(final EntityManager entityManager)
	{
		final String username = System.getProperty(CommonConstants.BUGZILLA_USERNAME_PROPERTY);
		final String password = System.getProperty(CommonConstants.BUGZILLA_PASSWORD_PROPERTY);
		final String url = System.getProperty(CommonConstants.BUGZILLA_URL_PROPERTY);
		
		/* return if the system properties have not been setup properly */
		if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() || url == null || url.trim().isEmpty())
			return;
		
		final LogIn login = new LogIn(username, password);
		final BugzillaConnector connector = new BugzillaConnector();
		final BugSearch<ECSBug> search = new BugSearch<ECSBug>(ECSBug.class);
		
		/* Create a query that will return all bugs whose Build ID matches what is prepopulated by Skynet */
		search.addQueryParam("field0-0-0", "cf_build_id");
		search.addQueryParam("type0-0-0", "regexp");
		search.addQueryParam("value0-0-0", this.topicId + CommonConstants.BUGZILLA_BUILD_ID_RE);
		
		try
		{
			connector.connectTo("https://" + url + "/xmlrpc.cgi");
			connector.executeMethod(login);
			connector.executeMethod(search);
			
			/* clear the mappings */
			while (this.topicToBugzillaBugs.size() != 0)
			{
				final TopicToBugzillaBug map = this.topicToBugzillaBugs.iterator().next();
				map.getBugzillaBug().getTopicToBugzillaBugs().remove(map);
				this.topicToBugzillaBugs.remove(map);
			}

			/* For each bug, get the bug details and comments */
			for (ECSBug bug : search.getSearchResults())
			{
				int id = bug.getID();
				
				final GetBug<ECSBug> getBug = new GetBug<ECSBug>(ECSBug.class, id);
				connector.executeMethod(getBug);
				final ECSBug bugDetails = getBug.getBug();

				final BugzillaBug bugzillaBug = new BugzillaBug();
				bugzillaBug.setBugzillaBugBugzillaId(bugDetails.getID());
				bugzillaBug.setBugzillaBugOpen(bugDetails.getIsOpen());
				bugzillaBug.setBugzillaBugSummary(bugDetails.getSummary());
				
				entityManager.persist(bugzillaBug);
				
				final TopicToBugzillaBug mapping = new TopicToBugzillaBug();
				mapping.setBugzillaBug(bugzillaBug);
				mapping.setTopic(this);
				
				this.topicToBugzillaBugs.add(mapping);
			}

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleSeamException(ex, false, "Probably an error searching Bugzilla");
		}
	}
	
	@Transient
	public List<TranslatedTopicData> getTranslatedTopics() {
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		
		return getTranslatedTopics(entityManager);
	}
	
	@SuppressWarnings("unchecked")
	@Transient
	public List<TranslatedTopicData> getTranslatedTopics(final EntityManager entityManager) {
		final List<TranslatedTopic> topicRevisions;
		
		final String query = TranslatedTopic.SELECT_ALL_QUERY + " WHERE translatedTopic.topicId = " + this.topicId;
		topicRevisions = entityManager.createQuery(query).getResultList();
		
		final List<TranslatedTopicData> translatedTopics = new ArrayList<TranslatedTopicData>();
		
		/* Add all of the translated topic data entities to the list */
		for (final TranslatedTopic translatedTopic: topicRevisions)
		{
			translatedTopics.addAll(translatedTopic.getTranslatedTopicDataEntitiesArray());
		}
		
		return translatedTopics;
	}
	
	@Transient
	public void createTranslatedTopic()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		
		final Number revision = getLatestRevision();

		/* Search to see if a translated topic already exists for the current revision */
		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<TranslatedTopic> criteriaQuery = criteriaBuilder.createQuery(TranslatedTopic.class);
		final Root<TranslatedTopic> root = criteriaQuery.from(TranslatedTopic.class);
		final Predicate whereTopicId = criteriaBuilder.equal(root.get("topicId"), topicId);
		final Predicate whereTopicRevision = criteriaBuilder.equal(root.get("topicRevision"), revision);
		criteriaQuery.where(criteriaBuilder.and(whereTopicId, whereTopicRevision));

		final TypedQuery<TranslatedTopic> query = entityManager.createQuery(criteriaQuery);
		final List<TranslatedTopic> result = query.getResultList();

		if (result.size() == 0)
		{
			final TranslatedTopic translatedTopic = new TranslatedTopic();
			translatedTopic.setTopicId(topicId);
			translatedTopic.setTopicRevision((Integer) revision);

			/*
			 * Create a TranslationTopicData entity that
			 * represents the original locale
			 */
			final Topic revisionTopic = entityManager.find(Topic.class, topicId);
			if (revisionTopic != null)
			{
				final String locale = revisionTopic.getTopicLocale();

				final TranslatedTopicData translatedTopicData = new TranslatedTopicData();
				translatedTopicData.setTranslatedTopic(translatedTopic);
				translatedTopicData.setTranslatedXml(revisionTopic.getTopicXML());
				translatedTopicData.setTranslationLocale(locale == null ? System.getProperty(CommonConstants.DEFAULT_LOCALE_PROPERTY) : locale);
				translatedTopicData.setTranslationPercentage(100);
				translatedTopic.getTranslatedTopicDataEntities().add(translatedTopicData);
			}

			/* Persist the new Translated Topic */
			entityManager.persist(translatedTopic);
		}
	}
}
