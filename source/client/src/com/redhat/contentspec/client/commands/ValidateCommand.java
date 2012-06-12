package com.redhat.contentspec.client.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.redhat.contentspec.client.config.ContentSpecConfiguration;
import com.redhat.contentspec.client.constants.Constants;
import com.redhat.contentspec.client.converter.FileConverter;
import com.redhat.contentspec.processor.ContentSpecParser;
import com.redhat.contentspec.processor.ContentSpecProcessor;
import com.redhat.contentspec.rest.RESTManager;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.contentspec.utils.StringUtilities;
import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
import com.redhat.ecs.commonutils.FileUtilities;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.entities.UserV1;

@Parameters(commandDescription = "Validate a Content Specification")
public class ValidateCommand extends BaseCommandImpl {

	@Parameter(converter = FileConverter.class, metaVar = "[FILE]")
	private List<File> files = new ArrayList<File>();
	
	@Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
	private Boolean permissive = false;
	
	private ContentSpecProcessor csp = null;
	
	public ValidateCommand(JCommander parser) {
		super(parser);
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public Boolean getPermissive() {
		return permissive;
	}

	public void setPermissive(Boolean permissive) {
		this.permissive = permissive;
	}

	@Override
	public void printError(String errorMsg, boolean displayHelp) {
		printError(errorMsg, displayHelp, Constants.VALIDATE_COMMAND_NAME);
	}

	@Override
	public void printHelp() {
		printHelp(Constants.VALIDATE_COMMAND_NAME);
	}
	
	@Override
	public UserV1 authenticate(RESTReader reader) {
		return authenticate(getUsername(), reader);
	}
	
	public boolean isValid() {
		// We should have only one file
		if (files.size() != 1) return false;
		
		// Check that the file exists
		File file = files.get(0);
		if (file.isDirectory()) return false;
		if (!file.exists()) return false;
		if (!file.isFile()) return false;
		
		return true;
	}

	@Override
	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
		// If files is empty then we must be using a csprocessor.cfg file
		if (files.size() == 0 && cspConfig.getContentSpecId() != null) {
			TopicV1 contentSpec = restManager.getReader().getContentSpecById(cspConfig.getContentSpecId(), null);
			String fileName = StringUtilities.escapeTitle(contentSpec.getTitle()) + "-post." + Constants.FILENAME_EXTENSION;
			File file = new File(fileName);
			if (!file.exists()) {
				// Backwards compatibility check for files ending with .txt
				file = new File(StringUtilities.escapeTitle(contentSpec.getTitle()) + "-post.txt");
				if (!file.exists()) {
					printError(String.format(Constants.NO_FILE_FOUND_FOR_CONFIG, fileName), false);
					shutdown(Constants.EXIT_FAILURE);
				}
			}
			files.add(file);
		}
		
		// Check that the parameters are valid
		if (!isValid()) {
			printError(Constants.ERROR_NO_FILE_MSG, true);
			shutdown(Constants.EXIT_FAILURE);
		}
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		boolean success = false;
		
		// Read in the file contents
		String contentSpec = FileUtilities.readFileContents(files.get(0));
		
		if (contentSpec == null  || contentSpec.equals("")) {
			printError(Constants.ERROR_EMPTY_FILE_MSG, false);
			shutdown(Constants.EXIT_FAILURE);
		}
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Process the content spec to see if its valid
		csp = new ContentSpecProcessor(restManager, elm, permissive, true);
		try {
			success = csp.processContentSpec(contentSpec, user, ContentSpecParser.ParsingMode.EITHER);
		} catch (Exception e) {
			printError(Constants.ERROR_INTERNAL_ERROR, false);
			shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
		}
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Print the logs
		if (success) {
			JCommander.getConsole().println("VALID");
		} else {
			JCommander.getConsole().println(elm.generateLogs());
			JCommander.getConsole().println("INVALID");
			JCommander.getConsole().println("");
			shutdown(Constants.EXIT_TOPIC_INVALID);
		}
	}
	
	@Override
	public void shutdown() {
		super.shutdown();
		if (csp != null) {
			csp.shutdown();
		}
	}
}
