package com.redhat.contentspec.client.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import com.redhat.contentspec.client.config.ContentSpecConfiguration;
import com.redhat.contentspec.client.config.ServerConfiguration;
import com.redhat.contentspec.client.constants.Constants;
import com.redhat.contentspec.client.utils.ClientUtilities;
import com.redhat.contentspec.rest.RESTManager;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
import com.redhat.topicindex.rest.entities.UserV1;

@Parameters(commandDescription = "Setup the Content Specification Processor configuration files")
public class SetupCommand extends BaseCommandImpl {

	public SetupCommand(JCommander parser) {
		super(parser);
	}

	@Override
	public void printHelp() {
		printHelp(Constants.SETUP_COMMAND_NAME);
	}

	@Override
	public void printError(String errorMsg, boolean displayHelp) {
		printError(errorMsg, displayHelp, Constants.SEARCH_COMMAND_NAME);
	}

	@Override
	public UserV1 authenticate(RESTReader reader) {
		return null;
	}

	@Override
	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
		JCommander.getConsole().println("Use the default server configuration? (Yes/No)");
		String answer = JCommander.getConsole().readLine();
		
		String username = "";
		String defaultServerName = "";
		String rootDir = "";
		HashMap<String, ServerConfiguration> servers = new HashMap<String, ServerConfiguration>();
		
		/* We are using the default setup so we only need to get the default server and a username */
		if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
			
			// Get which server they want to connect to by default
			while (!defaultServerName.equalsIgnoreCase("test") && !defaultServerName.equalsIgnoreCase("production")) {
				JCommander.getConsole().println("Which server do you want to connect to by default? (test/production)");
				defaultServerName = JCommander.getConsole().readLine().toLowerCase();
				
				// Good point to check for a shutdown
				if (isAppShuttingDown()) {
					shutdown.set(true);
					return;
				}
			}
			
			// Get the users username
			JCommander.getConsole().println("Please enter a username to connect to the servers: ");
			username = JCommander.getConsole().readLine();
			
			// Create the default settings
			servers.put(Constants.DEFAULT_SERVER_NAME, new ServerConfiguration(Constants.DEFAULT_SERVER_NAME, defaultServerName, username));
			
			// Setup each servers settings
			servers.put("test", new ServerConfiguration("test", "http://skynet.usersys.redhat.com:8080/TopicIndex/"));
			servers.put("production", new ServerConfiguration("test", "http://skynet.usersys.redhat.com:8080/TopicIndex/"));
			
		/* We need to read in a list of servers and then get the default server */
		} else if (answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) {
			while (!answer.matches("^[0-9]+$")) {
				JCommander.getConsole().print("How many servers are to be configured? ");
				answer = JCommander.getConsole().readLine();
				
				// Good point to check for a shutdown
				if (isAppShuttingDown()) {
					shutdown.set(true);
					return;
				}
			}
			
			// Get the server setup details from the user
			Integer numServers = Integer.parseInt(answer);
			String serverNames = "";
			for (int i = 1; i <= numServers; i++) {
				ServerConfiguration config = new ServerConfiguration();
				
				// Get the name of the server
				JCommander.getConsole().println("Please enter the name of server no. " + i + ": ");
				config.setName(JCommander.getConsole().readLine().toLowerCase());
				
				// Get the url of the server
				JCommander.getConsole().println("Please enter the URL of server no. " + i + ": ");
				config.setUrl(ClientUtilities.validateHost(JCommander.getConsole().readLine()));
				
				// Get the username for the server
				JCommander.getConsole().println("Please enter the username of server no. " + i + ": ");
				config.setUsername(JCommander.getConsole().readLine());
				
				// Add the server configuration and add the name to the list of displayable strings
				servers.put(config.getName(), config);
				serverNames += config.getName() + "/";
				
				// Good point to check for a shutdown
				if (isAppShuttingDown()) {
					shutdown.set(true);
					return;
				}
			}
			
			// Get which server they want to connect to
			while (!servers.containsKey(defaultServerName)) {
				JCommander.getConsole().println("Which server do you want to connect to by default? (" + serverNames.substring(0, serverNames.length() - 1) + ")");
				defaultServerName = JCommander.getConsole().readLine().toLowerCase();
				
				// Good point to check for a shutdown
				if (isAppShuttingDown()) {
					shutdown.set(true);
					return;
				}
			}
			
			// Create the default settings
			servers.put(Constants.DEFAULT_SERVER_NAME, new ServerConfiguration(Constants.DEFAULT_SERVER_NAME, defaultServerName));
		} else {
			printError(Constants.INVALID_ARG_MSG, false);
			shutdown(Constants.EXIT_ARGUMENT_ERROR);
		}
		
		// Get the root directory to store content specifications
		JCommander.getConsole().println("Enter a root directory to store Content Specifications. (Press enter for no root directory)");
		rootDir = JCommander.getConsole().readLine();
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Create the configuration file
		String configFile = "[servers]\n";
		for (String serverName: servers.keySet()) {
			ServerConfiguration config = servers.get(serverName);
			
			// Setup the url for the server
			if (serverName.equals(Constants.DEFAULT_SERVER_NAME)) {
				configFile += serverName + "=" + config.getUrl() + "\n";
			} else {
				configFile += serverName + ".url=" + config.getUrl() + "\n";
			}
			
			// Setup the username for the server
			if (config.getUsername() != null && !config.getUsername().equals(""))
				configFile += serverName + ".username=" + config.getUsername() + "\n";
			else if (!serverName.equals(Constants.DEFAULT_SERVER_NAME))
				configFile += serverName + ".username=\n";
			
			// Add a blank line to separate servers
			configFile += "\n";
			
			// Good point to check for a shutdown
			if (isAppShuttingDown()) {
				shutdown.set(true);
				return;
			}
		}
		
		// Create the Root Directory
		configFile += "[directory]\n";
		configFile += "root=" + rootDir + "\n\n";
		
		// Create the publican options
		configFile += "[publican]\n";
		configFile += "build.parameters=" + Constants.DEFAULT_PUBLICAN_OPTIONS + "\n";
		configFile += "preview.format=" + Constants.DEFAULT_PUBLICAN_FORMAT + "\n";
		
		// Good point to check for a shutdown
		if (isAppShuttingDown()) {
			shutdown.set(true);
			return;
		}
		
		// Save the configuration file
		File file = new File(System.getProperty("user.home") + "/.config/csprocessor.ini");
		try {
			// Make sure the directory exists
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}
			
			// Make a backup of any existing csprocessor.ini
			if (file.exists()) {
				file.renameTo(new File(file.getAbsolutePath() + ".backup"));
			}
			
			// Save the config
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(configFile.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			printError(Constants.ERROR_FAILED_CREATING_CONFIG_MSG, false);
			shutdown(Constants.EXIT_CONFIG_ERROR);
		}
		
		JCommander.getConsole().println(Constants.SUCCESSFUL_SETUP_MSG);
	}

}
