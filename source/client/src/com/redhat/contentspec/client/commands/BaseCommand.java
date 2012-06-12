package com.redhat.contentspec.client.commands;

import com.redhat.contentspec.client.config.ContentSpecConfiguration;
import com.redhat.contentspec.interfaces.ShutdownAbleApp;
import com.redhat.contentspec.rest.RESTManager;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
import com.redhat.topicindex.rest.entities.UserV1;

public interface BaseCommand extends ShutdownAbleApp {

	String getUsername();
	void setUsername(String username);
	String getServerUrl();
	void setServerUrl(String serverUrl);
	Boolean isShowHelp();
	void setShowHelp(Boolean showHelp);
	String getConfigLocation();
	void setConfigLocation(String configLocation);
	boolean isAppShuttingDown();
	void setAppShuttingDown(boolean shuttingDown);
	void setShutdown(boolean shutdown);
	
	void printHelp();
	void printError(String errorMsg, boolean displayHelp);
	UserV1 authenticate(RESTReader reader);
	void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user);
}
