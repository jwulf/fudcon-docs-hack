package com.redhat.contentspec.client.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import com.beust.jcommander.internal.Console;
import com.redhat.contentspec.ContentSpec;
import com.redhat.contentspec.client.config.ContentSpecConfiguration;
import com.redhat.contentspec.client.constants.Constants;
import com.redhat.contentspec.client.entities.Spec;
import com.redhat.contentspec.client.entities.SpecList;
import com.redhat.contentspec.constants.CSConstants;
import com.redhat.contentspec.processor.ContentSpecParser;
import com.redhat.contentspec.rest.RESTManager;
import com.redhat.contentspec.rest.RESTReader;
import com.redhat.contentspec.utils.StringUtilities;
import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.entities.UserV1;

public class ClientUtilities {
	
	/**
	 * Checks if the location for a config location is in the correct format then corrects it if not.
	 * 
	 * @param location The path location to be checked
	 * @return The fixed path location string
	 */
	public static String validateConfigLocation(String location) {
		String fixedLocation = location;
		if (location.startsWith("~")) {
			fixedLocation = Constants.HOME_LOCATION + location.substring(1);
		} else if (location.startsWith("./") || location.startsWith("../")) {
			try {
				fixedLocation = new File(location).getCanonicalPath();
			} catch (IOException e) {
				// Do nothing
			}
		}
		File file = new File(fixedLocation);
		if (file.exists() && file.isFile()) {
			return fixedLocation;
		}
		if (!location.endsWith(File.separator) && !location.endsWith(".ini")) {
			fixedLocation += File.separator;
		}
		if (!location.endsWith(".ini")) {
			fixedLocation += Constants.CONFIG_FILENAME;
		}
		return fixedLocation;
	}
	
	/**
	 * Checks if the location is in the correct format then corrects it if not.
	 * 
	 * @param location The path location to be checked
	 * @return The fixed path location string
	 */
	public static String validateLocation(String location) {
		String fixedLocation = location;
		if (!location.endsWith(File.separator)) {
			fixedLocation += File.separator;
		}
		if (location.startsWith("~")) {
			fixedLocation = Constants.HOME_LOCATION + fixedLocation.substring(1);
		} else if (location.startsWith("./") || location.startsWith("../")) {
			try {
				fixedLocation = location.substring(location.indexOf("/")) + new File(location.substring(0, location.indexOf("/"))).getCanonicalPath();
			} catch (IOException e) {
				// Do nothing
			}
		}
		return fixedLocation;
	}
	
	/**
	 * Checks if the host address is in the correct format then corrects it if not.
	 * 
	 * @param host The host address of the server to be checked
	 * @return The fixed host address string
	 */
	public static String validateHost(String host) {
		String fixedHost = host;
		if (!host.endsWith("/")) {
			fixedHost += "/";
		}
		if (!host.startsWith("http://") && !host.startsWith("https://")) {
			fixedHost = "http://" + fixedHost;
		}
		return fixedHost;
	}
	
	/**
	 * Checks that a server exists at the specified URL by sending a request to get the headers from the URL.
	 * 
	 * @param serverUrl The URL of the server.
	 * @return True if the server exists and got a succesful response otherwise false.
	 */
	public static boolean validateServerExists(String serverUrl) {
		try {
			URL url = new URL(serverUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Checks if the file path is in the correct format then corrects it if not.
	 * 
	 * @param host The path location to be checked
	 * @return The fixed path location string
	 */
	public static String validateFilePath(String filePath) {
		if (filePath == null) return null;
		String fixedPath = filePath;
		if (filePath.startsWith("~")) {
			fixedPath = Constants.HOME_LOCATION + fixedPath.substring(1);
		} else if (filePath.startsWith("./") || filePath.startsWith("../")) {
			try {
				fixedPath = filePath.substring(filePath.indexOf("/")) + new File(filePath.substring(0, filePath.indexOf("/"))).getCanonicalPath();
			} catch (IOException e) {
				// Do nothing
			}
		}
		return fixedPath;
	}
	
	/**
	 * Authenticates a user against the database specified by their username
	 * 
	 * @param username The key used to search the database for a user
	 * @return The database User object for the specified API Key or null if none was found
	 */
	public static UserV1 authenticateUser(String username, RESTReader reader) {
		// Check that the username is valid and get the user for that username
		if (username == null) return null;
		if (!StringUtilities.isAlphanumeric(username)) {
			return null;
		}
		List<UserV1> users = reader.getUsersByName(username);
		return users != null && users.size() == 1 ? users.get(0) : null;
	}
	
	/**
	 * Read from a csprocessor.cfg file and intitialise the variables into a configuration object.
	 * 
	 * @param csprocessorcfg The csprocessor.cfg file.
	 * @return The initialised configuration object from the csprocessor.cfg file.
	 * @throws FileNotFoundException The csprocessor.cfg couldn't be found
	 * @throws IOException
	 */
	public static ContentSpecConfiguration readFromCsprocessorCfg(File csprocessorcfg) throws FileNotFoundException, IOException {
		ContentSpecConfiguration cspCfg = new ContentSpecConfiguration();
		Properties prop = new Properties();
		prop.load(new FileInputStream(csprocessorcfg));
		cspCfg.setContentSpecId(Integer.parseInt(prop.getProperty("SPEC_ID")));
		cspCfg.setServerUrl(prop.getProperty("SERVER_URL"));
		return cspCfg;
	}
	
	/**
	 * Generates the contents of a csprocessor.cfg file from the passed arguments.
	 * 
	 * @param contentSpec The content specification object the csprocessor.cfg will be used for.
	 * @param serverUrl The server URL that the content specification exists on.
	 * @return The generated contents of the csprocessor.cfg file.
	 */
	public static String generateCsprocessorCfg(TopicV1 contentSpec, String serverUrl) {
		String output = "";
		output += "# SPEC_TITLE=" + StringUtilities.escapeTitle(contentSpec.getTitle()) + "\n";
		output += "SPEC_ID=" + contentSpec.getId() + "\n";
		output += "SERVER_URL=" + serverUrl + "\n";
		return output;
	}
	
	/**
	 * Runs a command from a specified directory
	 * 
	 * @param command The command to be run.
	 * @param dir The directory to run the command from.
	 * @param console The console to print the output to.
	 * @param displayOutput Whether the output should be displayed or not.
	 * @return The exit value of the command
	 * @throws IOException
	 */
	public static Integer runCommand(String command, File dir, final Console console, boolean displayOutput) throws IOException {
		if (!dir.isDirectory()) throw new IOException();
		
		try {
			final Process p = Runtime.getRuntime().exec(command, null, dir);
			// Get the output of the command
			if (displayOutput) {
				
				// Create a separate thread to read the stderr stream
				Thread t = new Thread(new Runnable() {

		            @Override
		            public void run() {
		            	BufferedReader br = new BufferedReader( new InputStreamReader(p.getErrorStream()));
						String line = null;
						try {
							while ((line = br.readLine()) != null) {
								synchronized(console) {
									console.println(line);
								}
							}
						} catch (IOException e) {
							// Do nothing
						}

		            }
		        } );
		        t.start();
				
		        BufferedReader br = new BufferedReader( new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					synchronized(console) {
						console.println(line);
					}
				}
			}
			p.waitFor();
			return p.exitValue();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Opens a file using the Java Desktop API.
	 * 
	 * @param file The file to be opened.
	 * @throws Exception
	 */
	public static void openFile(File file) throws Exception {
		// Check that the file is a file
		if (!file.isFile()) throw new Exception("Passed file is not a file.");
		
		// Check that the Desktop API is supported
		if(!Desktop.isDesktopSupported()) {
            throw new Exception("Desktop is not supported");
        }

        Desktop desktop = Desktop.getDesktop();

        // Check that the open functionality is supported
        if(!desktop.isSupported(Desktop.Action.OPEN)) {
        	throw new Exception("Desktop doesn't support the open action");
        }
        
        // Open the file
        desktop.open(file);
	}
	
	/**
	 * Builds a Content Specification list for a list of content specifications.
	 */
	public static SpecList buildSpecList(List<TopicV1> specList, RESTManager restManager, ErrorLoggerManager elm) throws Exception {
		List<Spec> specs = new ArrayList<Spec>();
		for (TopicV1 cs: specList) {
			UserV1 creator = null;
			if (cs.getProperty(CSConstants.ADDED_BY_PROPERTY_TAG_ID) != null) {
				List<UserV1> users = restManager.getReader().getUsersByName(cs.getProperty(CSConstants.ADDED_BY_PROPERTY_TAG_ID).getValue());
				if (users.size() == 1) {
					creator = users.get(0);
				}
			}
			ContentSpecParser csp = new ContentSpecParser(elm, restManager);
			csp.parse(cs.getXml());
			ContentSpec contentSpec = csp.getContentSpec();
			specs.add(new Spec(cs.getId(), cs.getTitle(), contentSpec.getProduct(), contentSpec.getVersion(), creator != null ? creator.getName() : null));
		}
		return new SpecList(specs, specs.size());
	}
	
	/**
	 * Generates the response output for a list of content specifications
	 * 
	 * @param contentSpecs The SpecList that contains the processed Content Specifications
	 * @return The generated response ouput.
	 */
	public static String generateContentSpecListResponse(SpecList contentSpecs) {
		LinkedHashMap<String, Integer> sizes = new LinkedHashMap<String, Integer>();
		// Create the initial sizes incase they never increase
		sizes.put("ID", 2);
		sizes.put("SPEC ID", 7);
		sizes.put("TITLE", 5);
		sizes.put("SPEC TITLE", 10);
		sizes.put("PRODUCT", 7);
		sizes.put("VERSION", 7);
		sizes.put("CREATED BY", 10);
		if (contentSpecs != null && contentSpecs.getSpecs() != null && !contentSpecs.getSpecs().isEmpty()) {
			for (Spec spec: contentSpecs.getSpecs()) {
				if (spec.getId().toString().length() > sizes.get("ID")) {
					sizes.put("ID", spec.getId().toString().length());
				}
				if (spec.getProduct() != null && spec.getProduct().length() > sizes.get("PRODUCT")) {
					sizes.put("PRODUCT", spec.getProduct().length());
				}
				if (spec.getTitle().length() > sizes.get("TITLE")) {
					sizes.put("TITLE", spec.getTitle().length());
				}
				if (spec.getVersion() != null && spec.getVersion().length() > sizes.get("VERSION")) {
					sizes.put("VERSION", spec.getVersion().length());
				}
				if (spec.getCreator() != null && spec.getCreator().length() > sizes.get("CREATED BY")) {
					sizes.put("CREATED BY", spec.getCreator().length());
				}
			}
			String format = "%" + (sizes.get("ID") + 2) + "s" +
							"%" + (sizes.get("TITLE") + 2) + "s" +
							"%" + (sizes.get("PRODUCT") + 2) + "s" +
							"%" + (sizes.get("VERSION") + 2) + "s" +
							"%" + (sizes.get("CREATED BY") + 2) + "s";
			
			String output = String.format(format, "ID", "TITLE", "PRODUCT", "VERSION", "CREATED BY") + "\n";
			for (Spec spec: contentSpecs.getSpecs()) {
				output += String.format(format, spec.getId().toString(), spec.getTitle(), spec.getProduct(), spec.getVersion(), spec.getCreator()) + "\n";
			}
			return output;
		}
		return "";
	}
	
	/**
	 * Delete a directory and all of its sub directories/files
	 * 
	 * @param dir The directory to be deleted.
	 * @return True if the directory was deleted otherwise false if an error occurred.
	 */
	public static boolean deleteDir(File dir) {
		// Delete the contents of the directory first
	    if (!deleteDirContents(dir)) return false;
	    
	    // The directory is now empty so delete it
	    return dir.delete();
	}
	
	/**
	 * Delete the contents of a directory and all of its sub directories/files
	 * 
	 * @param dir The directory whose content is to be deleted.
	 * @return True if the directories contents were deleted otherwise false if an error occurred.
	 */
	public static boolean deleteDirContents(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            if (!deleteDir(new File(dir, children[i]))) {
	                return false;
	            }
	        }
	    }
	    return true;
	}
}
