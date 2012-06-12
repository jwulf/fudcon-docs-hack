package com.redhat.topicindex.utils;

import java.text.SimpleDateFormat;
import java.util.List;

import com.redhat.ecs.commonutils.CollectionUtilities;

public class Constants
{
	/**
	 * The Skynet build number, displayed on the top bar of all Skynet pages. Is
	 * in the format yyyymmdd-hhmm
	 */
	public static final String BUILD = "20120329-1646";
	/**
	 * The system property that defines the login message
	 */
	public static final String LOGIN_MESSAGE_SYSTEM_PROPERTY = "topicIndex.loginMessage";
	/**
	 * The system property that defines the XML elements that should be
	 * serialized verbatim
	 */
	public static final String KERBEROS_ENABLED_SYSTEM_PROPERTY = "topicIndex.kerberosEnabled";
	
	public static final String FAS_ENABLED_SYSTEM_PROPERTY = "topicIndex.fasEnabled";
	/**
	 * The system property that determines if topics should be rendered into
	 * HTML
	 */
	public static final String ENABLE_RENDERING_PROPERTY = "topicIndex.rerenderTopic";
	/**
	 * The system property that defines the STOMP message queue that skynet should send topic rendering requests to
	 */
	public static final String STOMP_MESSAGE_SERVER_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY = "topicIndex.stompMessageServerRenderTopicQueue";
	/**
	 * The system property that defines the STOMP message queue that skynet should send topic rendering requests to
	 */
	public static final String STOMP_MESSAGE_SERVER_WORKING_SNAPHOT_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY = "topicIndex.stompMessageServerRenderWorkingSnapshotTopicQueue";
	/**
	 * The system property that defines the STOMP message queue that skynet should send docbook build requests to
	 */
	public static final String STOMP_MESSAGE_SERVER_DOCBOOK_BUILD_QUEUE_SYSTEM_PROPERTY = "topicIndex.stompMessageServerBuildDocbookQueue";

	/**
	 * The system property that identifies the zanata server to send files to
	 * for translation
	 */
	public static final String ZANATA_SERVER_PROPERTY = "topicIndex.zanataServer";

	/**
	 * The system property that identifies the zanata project name
	 */
	public static final String ZANATA_PROJECT_PROPERTY = "topicIndex.zanataProject";

	/**
	 * The system property that identifies the zanata user name
	 */
	public static final String ZANATA_USERNAME_PROPERTY = "topicIndex.zanataUsername";

	/**
	 * The system property that identifies the zanata project version
	 */
	public static final String ZANATA_PROJECT_VERSION_PROPERTY = "topicIndex.zanataProjectVersion";

	/**
	 * The system property that identifies the zanata API token
	 */
	public static final String ZANATA_TOKEN_PROPERTY = "topicIndex.zanataToken";
	
	public static final String PROPERTY_TAG_SELECT_ITEM_VALUE_PREFIX = "PropertyTag";
	public static final String PROPERTY_TAG_SELECT_LABEL_PREFIX = "- ";
	public static final String PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE_PREFIX = "PropertyTagCategory";
	public static final String UNCATEGORISED_PROPERTY_TAG_CATEGORY_SELECT_ITEM_LABEL = "Uncategorised";
	public static final String UNCATEGORISED_PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE = "UncategorisedPropertyTagCategory";

	/**
	 * When bulk adding property tags that are unique, a GUID will be preceeded by this meassage to ensure that the
	 * value of the tag is initially unique.
	 */
	public static final String UNIQUE_PROPERTY_TAG_PREFIX = "Unique Property Tag";
	
	/**
	 * The name given to the tab that shows entities that didn't match any
	 * grouping tags
	 */
	public static final String UNGROUPED_RESULTS_TAB_NAME = "Ungrouped Results";

	/**
	 * The string to be returned by the VersionedEntityHome.persist() function
	 * if a concurrent edit has been detected
	 */
	public static final String CONCURRENT_EDIT = "concurrentEdit";

	/**
	 * The ID given to the tab that shows entities that didn't match any
	 * grouping tags
	 */
	public static final Integer UNGROUPED_RESULTS_TAB_ID = -1;

	/**
	 * A Topic ID that no topic should ever match
	 */
	public static final String NULL_TOPIC_ID = "-1";

	/** The message saved by SkynetExceptionUtilities when a precondition fails */
	public static final String PRECONDITION_CHECK_FAILED_MESSAGE = "The method failed a precondition check";

	/**
	 * A collection of the all the locales supported by Java. We replace the '-'
	 * with a '_' to reflect the locales in Zanata.
	 */
	public static final List<String> LOCALES = CollectionUtilities.replaceStrings(CollectionUtilities.sortAndReturn(CollectionUtilities.toStringArrayList((Object[]) SimpleDateFormat.getAvailableLocales())), "-", "_");

	/** The initial name for a snapshot revision */
	public static final String INITIAL_SNAPSHOT_REVISION_NAME = "Initial Untranslated Revision";

	/** The generic error message to display to the user */
	public static final String GENERIC_ERROR_INSTRUCTIONS = "Please log out, log back in and try again. If the problem persists, please log a bug.";

	/** The encoding of the XML, used when converting a DOM object to a string */
	public static final String XML_ENCODING = "UTF-8";

	/** The HTML returned when a Topic's XML could not be transformed */
	public static final String XSL_ERROR_TEMPLATE = "<html><head><title>ERROR</title></head><body>The topic could not be transformed into HTML</body></html>";

	public static final int HOME_LANDING_PAGE_TOPIC_ID = -1;

	/** The base URL from which the REST interface can be accessed */
	public static final String BASE_REST_PATH = "/seam/resource/rest";

	/** The default number of elements to be shown by an EntityQuery object */
	public static final int DEFAULT_PAGING_SIZE = 25;

	/**
	 * The default number of elements to be shown by an EntityQuery object that
	 * has to access Envers data (which is slow)
	 */
	public static final int DEFAULT_ENVERS_PAGING_SIZE = 15;

	/**
	 * The "Common" project includes any tags that are not assigned to any other
	 * project. The Common project has not setup in the database anywhere, but
	 * when processing the data structures that contain the list of projects we
	 * need a name, which is defined in this constant.
	 */
	public static final String COMMON_PROJECT_NAME = "Common";
	/** This is the ID for the Common project */
	public static final Integer COMMON_PROJECT_ID = -1;
	/** This is the description for the Common project */
	public static final String COMMON_PROJECT_DESCRIPTION = "This project holds tags that are not assigned to any other project";
	/**
	 * This is the host name of the live SQL server. This value is used to
	 * provide a label in the top bar when modifying live data.
	 */
	public static final String LIVE_SQL_SERVER = "jboss-eap.bne.redhat.com";
	/** The SQL logic keyword to use when two conditions need to be and'ed */
	public static final String AND_LOGIC = "And";
	/** The SQL logic keyword to use when two conditions need to be or'ed */
	public static final String OR_LOGIC = "Or";
	/** The default logic to be applied to tags within a category */
	public static final String DEFAULT_INTERNAL_LOGIC = OR_LOGIC;
	/** The default logic to be applied between categories */
	public static final String DEFAULT_EXTERNAL_LOGIC = AND_LOGIC;
	/** The url variable prefix to indicate that a tag needs to be matched */
	public static final String MATCH_TAG = "tag";
	/** The url variable prefix to indicate that a tag will be used for grouping */
	public static final String GROUP_TAG = "grouptab";
	/** The url variable prefix to indicate that a tag will be used for grouping */
	public static final String GROUP_LOCALE = "grouplocale";
	/**
	 * The URL variable prefix to indicate the internal logic of a category (and
	 * optionally also specify a project)
	 */
	public static final String CATEORY_INTERNAL_LOGIC = "catint";
	/**
	 * The URL variable prefix to indicate the external logic of a category (and
	 * optionally also specify a project)
	 */
	public static final String CATEORY_EXTERNAL_LOGIC = "catext";
	/** The URL variable the indicates the filter to be used */
	public static final String FILTER_ID = "filterId";
	/**
	 * The value (as used in the FilterTag database TagState field) the
	 * indicates that a tag should be matched
	 */
	public static final int MATCH_TAG_STATE = 1;
	/**
	 * The value (as used in the FilterTag database TagState field) the
	 * indicates that a tag should be excluded
	 */
	public static final int NOT_MATCH_TAG_STATE = 0;
	/**
	 * The value (as used in the FilterTag database TagState field) the
	 * indicates that a tag should be excluded
	 */
	public static final int GROUP_TAG_STATE = 2;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an internal "and" state
	 */
	public static final int CATEGORY_INTERNAL_AND_STATE = 0;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an internal "or" state
	 */
	public static final int CATEGORY_INTERNAL_OR_STATE = 1;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an external "and" state
	 */
	public static final int CATEGORY_EXTERNAL_AND_STATE = 2;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an external "or" state
	 */
	public static final int CATEGORY_EXTERNAL_OR_STATE = 3;
	/** The default internal category logic state */
	public static final int CATEGORY_INTERNAL_DEFAULT_STATE = CATEGORY_INTERNAL_OR_STATE;
	/** The default external category logic state */
	public static final int CATEGORY_EXTERNAL_DEFAULT_STATE = CATEGORY_EXTERNAL_AND_STATE;

	/**
	 * The file name for the RocBook DTD schema. This is used when matching and
	 * providing XML resources
	 */
	public static final String ROCBOOK_DTD = "rocbook.dtd";
	/**
	 * The file name for the DocBook DTD schema. This is used when matching and
	 * providing XML resources
	 */
	public static final String DOCBOOK_DTD = "docbook.dtd";
	/** The URL variable that defines the topic text search field */
	public static final String TOPIC_TEXT_SEARCH_FILTER_VAR = "topicTextSearch";
	/** The description of the topic text search field */
	public static final String TOPIC_TEXT_SEARCH_FILTER_VAR_DESC = "Topic Text Search";
	/** The URL variable that defines the topic IDs search field */
	public static final String TOPIC_IDS_FILTER_VAR = "topicIds";
	/** The description of the topic IDs search field */
	public static final String TOPIC_IDS_FILTER_VAR_DESC = "Topic IDs";
	/** The URL variable that defines the topic title search field */
	public static final String TOPIC_TITLE_FILTER_VAR = "topicTitle";
	/** The description of the topic title search field */
	public static final String TOPIC_TITLE_FILTER_VAR_DESC = "Title";
	/** The URL variable that defines the topic description search field */
	public static final String TOPIC_DESCRIPTION_FILTER_VAR = "topicText";
	/** The description of the topic description search field */
	public static final String TOPIC_DESCRIPTION_FILTER_VAR_DESC = "Text";
	/** The URL variable that defines the topic xml search field */
	public static final String TOPIC_XML_FILTER_VAR = "topicXml";
	/** The description of the topic xml search field */
	public static final String TOPIC_XML_FILTER_VAR_DESC = "XML";
	/**
	 * The URL variable that defines the start range for the topic create date
	 * search field
	 */
	public static final String TOPIC_STARTDATE_FILTER_VAR = "startDate";
	/**
	 * The description of the start range for the topic create date search field
	 */
	public static final String TOPIC_STARTDATE_FILTER_VAR_DESC = "Min Creation Date";
	/**
	 * The URL variable that defines the end range for the topic create date
	 * search field
	 */
	public static final String TOPIC_ENDDATE_FILTER_VAR = "endDate";
	/** The description of the end range for the topic create date search field */
	public static final String TOPIC_ENDDATE_FILTER_VAR_DESC = "Max Creation Date";
	/**
	 * The URL variable that defines the start edit range for the topic create
	 * date search field
	 */
	public static final String TOPIC_STARTEDITDATE_FILTER_VAR = "startEditDate";
	/**
	 * The description of the start edit range for the topic create date search
	 * field
	 */
	public static final String TOPIC_STARTEDITDATE_FILTER_VAR_DESC = "Min Edited Date";
	/**
	 * The URL variable that defines the end edit range for the topic create
	 * date search field
	 */
	public static final String TOPIC_ENDEDITDATE_FILTER_VAR = "endEditDate";
	/**
	 * The description of the end edit range for the topic create date search
	 * field
	 */
	public static final String TOPIC_ENDEDITDATE_FILTER_VAR_DESC = "Max Edited Date";
	/**
	 * The URL variable that defines the logic to be applied to the search
	 * fields
	 */
	public static final String TOPIC_LOGIC_FILTER_VAR = "logic";
	/** The description the logic to be applied to the search fields */
	public static final String TOPIC_LOGIC_FILTER_VAR_DESC = "Field Logic";
	/** The URL variable that defines the has relationships search field */
	public static final String TOPIC_HAS_OPEN_BUGZILLA_BUGS = "topicHasOpenBugzillaBugs";
	/** The description of the has relationships search field */
	public static final String TOPIC_HAS_OPEN_BUGZILLA_BUGS_DESC = "Has Open Bugzilla Bugs";
	/** The URL variable that defines the has relationships search field */
	public static final String TOPIC_HAS_BUGZILLA_BUGS = "topicHasBugzillaBugs";
	/** The description of the has relationships search field */
	public static final String TOPIC_HAS_BUGZILLA_BUGS_DESC = "Has Bugzilla Bugs";
	/** The URL variable that defines the has relationships search field */
	public static final String TOPIC_HAS_RELATIONSHIPS = "topicHasRelationships";
	/** The description of the has relationships search field */
	public static final String TOPIC_HAS_RELATIONSHIPS_DESC = "Has Relationships";
	/**
	 * The URL variable that defines the has incoming relationships search field
	 */
	public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS = "topicHasIncomingRelationships";
	/** The description of the has incoming relationships search field */
	public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS_DESC = "Has Incoming Relationships";
	/** The URL variable that defines the has related to search field */
	public static final String TOPIC_RELATED_TO = "topicRelatedTo";
	/** The description of the has related to search field */
	public static final String TOPIC_RELATED_TO_DESC = "Related To";
	/** The URL variable that defines the has related from search field */
	public static final String TOPIC_RELATED_FROM = "topicRelatedFrom";
	/** The description of the has related from search field */
	public static final String TOPIC_RELATED_FROM_DESC = "Related From";
	/** The URL variable that defines the has related from search field */
	public static final String TOPIC_HAS_XML_ERRORS = "topicHasXMLErrors";
	/** The description of the has related from search field */
	public static final String TOPIC_HAS_XML_ERRORS_DESC = "Topic Has XML Errors";
	/** The URL variable that defines the has related from search field */
	public static final String TOPIC_EDITED_IN_LAST_DAYS = "topicEditedInLastDays";
	/** The description of the has related from search field */
	public static final String TOPIC_EDITED_IN_LAST_DAYS_DESC = "Topic Edited In Last Days";
	/** The URL variable that defines the parent snapshot revision ID */
	public static final String SNAPSHOT_REVISION_ID = "snapshotRev";
	/** The description of the has related from search field */
	public static final String SNAPSHOT_REVISION_ID_DESC = "Snapshot Revision ID";
	/** The URL variable that defines the parent snapshot revision ID */
	public static final String SNAPSHOT_ID = "snapshot";
	/** The description of the has related from search field */
	public static final String SNAPSHOT_ID_DESC = "Snapshot ID";
	/** The URL variable that defines the topic locale */
	public static final String LOCALE = "locale";
	/** The description of the has related from search field */
	public static final String LOCALE_DESC = "Locale";
	/** The URL variable that defines the topic property tag */
	public static final String TOPIC_PROPERTY_TAG = "propertyTag";
	/** The description of the property tag search field */
	public static final String TOPIC_PROPERTY_TAG_DESC = "Property Tag";
	/** The URL variable that defines the has relationships search field */
	public static final String TOPIC_IS_INCLUDED_IN_SPEC = "topicIncludedInSpec";
	/** The description of the has relationships search field */
	public static final String TOPIC_IS_INCLUDED_IN_SPEC_DESC = "Topics Included In Spec";
	/** The default logic to be applied to the search fields */
	public static final String TOPIC_LOGIC_FILTER_VAR_DEFAULT_VALUE = "and";

	/*
	 * TODO: These tag and category ids should probably come from a
	 * configuration file instead of being hard coded. Any changes to the tags
	 * will break the docbook compilation, and require this source code to be
	 * modified to reflect the new tag ids.
	 * 
	 * Generally speaking, tags referenced here should eventually become fields
	 * on a topic.
	 */
	public static final Integer TYPE_CATEGORY_ID = 4;
	public static final Integer TECHNOLOGY_CATEGORY_ID = 3;
	public static final Integer RELEASE_CATEGORY_ID = 15;
	public static final Integer WRITER_CATEGORY_ID = 12;
	public static final Integer COMMON_NAME_CATEGORY_ID = 17;
	public static final String TECHNOLOGY_CATEGORY_NAME = "Technologies";
	public static final Integer CONCERN_CATEGORY_ID = 2;
	public static final String CONCERN_CATEGORY_NAME = "Concerns";
	public static final Integer LIFECYCLE_CATEGORY_ID = 5;
	


	/** The Concept tag ID */
	public static final Integer CONCEPT_TAG_ID = 5;
	/** The Concept tag name */
	public static final String CONCEPT_TAG_NAME = "Concept";
	/** The Conceptual Overview tag ID */
	public static final Integer CONCEPTUALOVERVIEW_TAG_ID = 93;
	/** The Conceptual Overview tag name */
	public static final String CONCEPTUALOVERVIEW_TAG_NAME = "Overview";
	/** The Reference tag ID */
	public static final Integer REFERENCE_TAG_ID = 6;
	/** The Reference tag name */
	public static final String REFERENCE_TAG_NAME = "Reference";
	/** The Task tag ID */
	public static final Integer TASK_TAG_ID = 4;
	/** The Task tag name */
	public static final String TASK_TAG_NAME = "Task";
	/** The Written tag ID */
	public static final Integer WRITTEN_TAG_ID = 19;
	/** The Tag Description tag ID */
	public static final Integer TAG_DESCRIPTION_TAG_ID = 215;
	/** The Home tag ID */
	public static final Integer HOME_TAG_ID = 216;
	/** The Content Specification tag ID */
	public static final Integer CONTENT_SPEC_TAG_ID = 268;
	/** The Content Specification tag name */
	public static final String CONTENT_SPEC_TAG_NAME = "Content Specfication";


	/** The Added By Property Tag ID */
	public static final Integer ADDED_BY_PROPERTY_TAG_ID = 14;
	/** The First Name Property Tag ID */
	public static final Integer FIRST_NAME_PROPERTY_TAG_ID = 1;
	/** The Last Name Property Tag ID */
	public static final Integer LAST_NAME_PROPERTY_TAG_ID = 2;
	/** The Email Address Property Tag ID */
	public static final Integer EMAIL_PROPERTY_TAG_ID = 3;
	/** The Organization Property Tag ID */
	public static final Integer ORGANIZATION_PROPERTY_TAG_ID = 18;
	/** The Organization Division Property Tag ID */
	public static final Integer ORG_DIVISION_PROPERTY_TAG_ID = 19;
	/** The DTD Property Tag ID */
	public static final Integer DTD_PROPERTY_TAG_ID = 16;
	/** The Content Specification Type Property Tag ID */
	public static final Integer CSP_TYPE_PROPERTY_TAG_ID = 17;

	/**
	 * This identifies the the tag that is assigned to a topics when it is in
	 * its final state
	 */
	public static final Integer TOPIC_FINAL_LIFECYCLE = 19;

	/**
	 * The ID for the inherit relationship type, as defined in the
	 * RoleToRoleRelationship table
	 */
	public static final Integer INHERIT_ROLE_RELATIONSHIP_TYPE = 1;
}
