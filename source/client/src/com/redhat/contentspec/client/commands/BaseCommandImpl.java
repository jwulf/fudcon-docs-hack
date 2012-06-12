package com.redhat.contentspec.client.commands;

import java.util.concurrent.atomic.AtomicBoolean;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.redhat.contentspec.client.constants.Constants;
import com.redhat.contentspec.client.utils.ClientUtilities;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.topicindex.rest.entities.UserV1;

public abstract class BaseCommandImpl implements BaseCommand {
	
	protected final JCommander parser;
	
	@Parameter(names = {Constants.SERVER_LONG_PARAM, Constants.SERVER_SHORT_PARAM}, hidden = true)
	private String serverUrl;
	
	@Parameter(names = {Constants.USERNAME_LONG_PARAM, Constants.USERANME_SHORT_PARAM}, hidden = true)
	private String username;
	
	@Parameter(names = Constants.HELP_LONG_PARAM, hidden = true)
	private Boolean showHelp = false;
	
	@Parameter(names = Constants.CONFIG_LONG_PARAM, hidden = true)
	private String configLocation = Constants.DEFAULT_CONFIG_LOCATION;
	
	@Parameter(names = Constants.DEBUG_LONG_PARAM, hidden = true)
	private Boolean debug = false;
	
	protected final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
	protected final AtomicBoolean shutdown = new AtomicBoolean(false);
	
	public BaseCommandImpl(JCommander parser) {
		this.parser = parser;
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getServerUrl() {
		return serverUrl;
	}

	@Override
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Override
	public Boolean isShowHelp() {
		return showHelp;
	}

	@Override
	public void setShowHelp(Boolean showHelp) {
		this.showHelp = showHelp;
	}

	@Override
	public String getConfigLocation() {
		return configLocation;
	}

	@Override
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}
	
	public Boolean isDebug() {
		return debug;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	@Override
	public boolean isAppShuttingDown() {
		return isShuttingDown.get();
	}
	
	@Override
	public void setAppShuttingDown(boolean shuttingDown) {
		this.isShuttingDown.set(shuttingDown);
	}
	
	public boolean isShutdown() {
		return shutdown.get();
	}
	
	public void setShutdown(boolean shutdown) {
		this.shutdown.set(shutdown);
	}

	/**
	 * Prints an error message and then displays the main help screen
	 * 
	 * @param errorMsg The error message to be displayed.
	 * @param displayHelp Whether help should be displayed for the error.
	 */
	protected void printError(String errorMsg, boolean displayHelp, String commandName) {
		JCommander.getConsole().println("ERROR: " + errorMsg);
		if (displayHelp) {
			JCommander.getConsole().println("");
			printHelp();
		} else {
			JCommander.getConsole().println("");
		}
	}
	
	/**
	 * Prints the Help output for a specific command
	 * 
	 * @param commandName The name of the command
	 */
	protected void printHelp(String commandName) {
		if (commandName == null) {
			parser.usage(false);
		} else {
			parser.usage(true, new String[]{commandName});
		}
	}
	
	/**
	 * Authenticate a user to ensure that they exist on the server.
	 * 
	 * @param username The username of the user.
	 * @param reader The RESTReader that is used to connect via REST to the server.
	 * @return The user object if they existed otherwise false.
	 */
	public UserV1 authenticate(String username, RESTReader reader) {
		if (username == null || username.equals("")) {
			printError(Constants.ERROR_NO_USERNAME, false);
			shutdown(Constants.EXIT_UNAUTHORISED);
		}
		UserV1 user = ClientUtilities.authenticateUser(username, reader);
		if (user == null) {
			printError(Constants.ERROR_UNAUTHORISED, false);
			shutdown(Constants.EXIT_UNAUTHORISED);
		}
		return user;
	}
	
	@Override
	public void shutdown() {
		setAppShuttingDown(true);
	}
	
	public void shutdown(int exitStatus) {
		shutdown.set(true);
		System.exit(exitStatus);
	}
}
