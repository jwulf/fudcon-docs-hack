package com.redhat.contentspec.client.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.redhat.contentspec.client.converter.FileConverter;
import com.redhat.contentspec.client.config.ContentSpecConfiguration;
import com.redhat.contentspec.client.constants.Constants;
import com.redhat.contentspec.constants.TemplateConstants;
import com.redhat.contentspec.rest.RESTManager;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
import com.redhat.topicindex.rest.entities.UserV1;

@Parameters(commandDescription = "Get a basic Content Specification template.")
public class TemplateCommand extends BaseCommandImpl {

	@Parameter(names = Constants.COMMENTED_LONG_PARAM, description = "Get the fully commented template")
	private Boolean commented = false;
	
	@Parameter(names = {Constants.OUTPUT_LONG_PARAM, Constants.OUTPUT_SHORT_PARAM}, description = "Save the output to the specified file/directory.", metaVar = "<FILE>",
			converter = FileConverter.class)
	private File output;
	
	public TemplateCommand(JCommander parser) {
		super(parser);
	}

	public Boolean getCommented() {
		return commented;
	}

	public void setCommented(Boolean commented) {
		this.commented = commented;
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	@Override
	public void printHelp() {
		printHelp(Constants.TEMPLATE_COMMAND_NAME);
	}

	@Override
	public void printError(String errorMsg, boolean displayHelp) {
		printError(errorMsg, displayHelp, Constants.TEMPLATE_COMMAND_NAME);
	}

	@Override
	public UserV1 authenticate(RESTReader reader) {
		return null;
	}

	@Override
	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
		String template = commented ? TemplateConstants.FULLY_COMMENTED_TEMPLATE : TemplateConstants.EMPTY_TEMPLATE;
		
		// Save or print the data
		if (output == null || output.equals("")) {
			JCommander.getConsole().println(template);
		} else {
			
			// Make sure the directories exist
			if (output.isDirectory()) {
				output.mkdirs();
				output = new File(output.getAbsolutePath() + File.separator + "template." + Constants.FILENAME_EXTENSION);
			} else {
				if (output.getParentFile() != null)
					output.getParentFile().mkdirs();
			}
			
			// Good point to check for a shutdown
			if (isAppShuttingDown()) {
				shutdown.set(true);
				return;
			}
			
			// Create and write to the file
			try {
				FileOutputStream fos = new FileOutputStream(output);
				fos.write(template.getBytes());
				fos.flush();
				fos.close();
				JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, output.getAbsolutePath()));
			} catch (IOException e) {
				printError(Constants.ERROR_FAILED_SAVING, false);
				shutdown(Constants.EXIT_FAILURE);
			}
		}
	}

}
