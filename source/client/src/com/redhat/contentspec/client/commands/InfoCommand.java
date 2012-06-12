package com.redhat.contentspec.client.commands;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.redhat.contentspec.client.config.ContentSpecConfiguration;
import com.redhat.contentspec.client.constants.Constants;
import com.redhat.contentspec.processor.ContentSpecParser;
import com.redhat.contentspec.rest.RESTManager;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.entities.UserV1;

@Parameters(commandDescription = "Get some basic information and metrics about a project.")
public class InfoCommand extends BaseCommandImpl {

	@Parameter(metaVar = "[ID]")
	private List<Integer> ids = new ArrayList<Integer>();
	
	public InfoCommand(JCommander parser) {
		super(parser);
	}

	public List<Integer> getIds() {
		return ids;
	}

	public void setIds(List<Integer> ids) {
		this.ids = ids;
	}

	@Override
	public void printHelp() {
		printHelp(Constants.INFO_COMMAND_NAME);
	}

	@Override
	public void printError(String errorMsg, boolean displayHelp) {
		printError(errorMsg, displayHelp, Constants.INFO_COMMAND_NAME);
	}

	@Override
	public UserV1 authenticate(RESTReader reader) {
		return null;
	}

	@Override
	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
		// Add the details for the csprocessor.cfg if no ids are specified
		if (ids.size() == 0 && cspConfig.getContentSpecId() != null) {
			setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
		}
		
		// Check that an id was entered
		if (ids.size() == 0) {
			printError(Constants.ERROR_NO_ID_MSG, false);
			shutdown(Constants.EXIT_ARGUMENT_ERROR);
		} else if (ids.size() > 1) {
			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
			shutdown(Constants.EXIT_ARGUMENT_ERROR);
		}
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Get the Content Specification from the server.
		final TopicV1 contentSpec = restManager.getReader().getContentSpecById(ids.get(0), null);
		if (contentSpec == null || contentSpec.getXml() == null) {
			printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
			shutdown(Constants.EXIT_FAILURE);
		}
		
		// Print the initial CSP ID & Title message
		JCommander.getConsole().println(String.format(Constants.CSP_ID_MSG, ids.get(0)));
		JCommander.getConsole().println(String.format(Constants.CSP_REVISION_MSG, contentSpec.getRevision()));
		JCommander.getConsole().println(String.format(Constants.CSP_TITLE_MSG, contentSpec.getTitle()));
		JCommander.getConsole().println("");
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Parse the spec to get the ids
		ContentSpecParser csp = new ContentSpecParser(elm, restManager);
		try {
			csp.parse(contentSpec.getXml());
		} catch (Exception e) {
			JCommander.getConsole().println(elm.generateLogs());
			shutdown(Constants.EXIT_FAILURE);
		}
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Calculate the percentage complete
		final int numTopics = csp.getReferencedTopicIds().size();
		int numTopicsComplete = 0;
		BaseRestCollectionV1<TopicV1> topics = restManager.getReader().getTopicsByIds(csp.getReferencedTopicIds());
		if (topics != null && topics.getItems() != null) {
			for (TopicV1 topic: topics.getItems()) {
				if (topic.getXml() != null && !topic.getXml().isEmpty()) {
					numTopicsComplete++;
				}
				
				// Good point to check for a shutdown
				if (isAppShuttingDown()) {
					shutdown.set(true);
					return;
				}
			}
		}
		
		// Print the completion status
		JCommander.getConsole().println(String.format(Constants.CSP_COMPLETION_MSG, numTopics, numTopicsComplete, ((float)numTopicsComplete/(float)numTopics*100.0f)));
	}

}
