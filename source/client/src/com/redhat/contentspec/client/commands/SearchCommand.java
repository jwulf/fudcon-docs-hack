package com.redhat.contentspec.client.commands;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.redhat.contentspec.client.config.ContentSpecConfiguration;
import com.redhat.contentspec.client.constants.Constants;
import com.redhat.contentspec.client.utils.ClientUtilities;
import com.redhat.contentspec.constants.CSConstants;
import com.redhat.contentspec.processor.ContentSpecParser;
import com.redhat.contentspec.rest.RESTManager;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.contentspec.utils.StringUtilities;
import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.entities.UserV1;

@Parameters(commandDescription = "Search for a Content Specification")
public class SearchCommand extends BaseCommandImpl {

	@Parameter(metaVar = "[QUERY]")
	private List<String> queries = new ArrayList<String>();
	
	@Parameter(names = {Constants.CONTENT_SPEC_LONG_PARAM, Constants.CONTENT_SPEC_SHORT_PARAM})
	private Boolean useContentSpec;
	
	@Parameter(names = {Constants.SNAPSHOT_LONG_PARAM, Constants.SNAPSHOT_SHORT_PARAM}, hidden = true)
	private Boolean useSnapshot = false;

	public SearchCommand(JCommander parser) {
		super(parser);
	}
	
	public List<String> getQueries() {
		return queries;
	}

	public void setQueries(List<String> queries) {
		this.queries = queries;
	}

	public Boolean isUseContentSpec() {
		return useContentSpec;
	}

	public void setUseContentSpec(Boolean useContentSpec) {
		this.useContentSpec = useContentSpec;
	}

	public Boolean isUseSnapshot() {
		return useSnapshot;
	}

	public void setUseSnapshot(Boolean useSnapshot) {
		this.useSnapshot = useSnapshot;
	}

	@Override
	public void printError(String errorMsg, boolean displayHelp) {
		printError(errorMsg, displayHelp, Constants.SEARCH_COMMAND_NAME);
	}

	@Override
	public void printHelp() {
		printHelp(Constants.SEARCH_COMMAND_NAME);
	}
	
	@Override
	public UserV1 authenticate(RESTReader reader) {
		return null;
	}

	@Override
	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
		List<TopicV1> csList = new ArrayList<TopicV1>();
		String searchText = StringUtilities.join(queries.toArray(new String[queries.size()]), " ");
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Search the database for content specs that match the query parameters
		List<TopicV1> contentSpecs = restManager.getReader().getContentSpecs(null, null);
		if (contentSpecs != null) {
			for (TopicV1 contentSpec: contentSpecs) {
				
				// Good point to check for a shutdown
				if (isAppShuttingDown()) {
					shutdown.set(true);
					return;
				}
				
				ContentSpecParser csp = new ContentSpecParser(elm, restManager);
				try {
					csp.parse(contentSpec.getXml());
				} catch (Exception e) {
					printError(Constants.ERROR_INTERNAL_ERROR, false);
					shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
				}
				
				// Search on title
				if (contentSpec.getTitle().matches(".*" + searchText + ".*")) {
					csList.add(contentSpec);
				// Search on Product title
				} else if (csp.getContentSpec().getProduct().matches(".*" + searchText + ".*")) {
					csList.add(contentSpec);
				// Search on Version
				} else if (csp.getContentSpec().getVersion().matches(".*" + searchText + ".*")) {
					csList.add(contentSpec);
				// Search on created by
				} else if (contentSpec.getProperty(CSConstants.ADDED_BY_PROPERTY_TAG_ID) != null && contentSpec.getProperty(CSConstants.ADDED_BY_PROPERTY_TAG_ID).getValue() != null) {
					if (contentSpec.getProperty(CSConstants.ADDED_BY_PROPERTY_TAG_ID).getValue().matches(".*" + searchText + ".*")) {
						csList.add(contentSpec);
					}
				}
			}
		}
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Display the search results
		if (csList.isEmpty()) {
			JCommander.getConsole().println(Constants.NO_CS_FOUND_MSG);
		} else {
			try {
				JCommander.getConsole().println(ClientUtilities.generateContentSpecListResponse(ClientUtilities.buildSpecList(csList, restManager, elm)));
			} catch (Exception e) {
				printError(Constants.ERROR_INTERNAL_ERROR, false);
				shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
			}
		}
	}
}
