/*
	 * Copyright 2010, Red Hat, Inc. and individual contributors
	 * as indicated by the @author tags. See the copyright.txt file in the
	 * distribution for a full listing of individual contributors.
	 *
	 * This is free software; you can redistribute it and/or modify it
	 * under the terms of the GNU Lesser General Public License as
	 * published by the Free Software Foundation; either version 2.1 of
	 * the License, or (at your option) any later version.
	 *
	 * This software is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	 * Lesser General Public License for more details.
	 *
	 * You should have received a copy of the GNU Lesser General Public
	 * License along with this software; if not, write to the Free
	 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
	 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
	 */
	package com.redhat.topicindex.security;
	
	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.LinkedHashMap;
	import java.util.LinkedList;
	import java.util.List;
	import java.util.Map;
	
	import javax.security.auth.*;
	import javax.security.auth.callback.*;
	import javax.security.auth.login.*;
	import javax.security.auth.spi.*;
	
	import org.jboss.seam.annotations.Logger;
	import org.jboss.seam.annotations.Name;
	import org.jboss.seam.log.Log;
	import org.json.simple.parser.ContainerFactory;
	import org.json.simple.parser.JSONParser;
	import org.apache.http.HttpEntity;
	import org.apache.http.HttpResponse;
	import org.apache.http.NameValuePair;
	import org.apache.http.client.HttpClient;
	import org.apache.http.client.entity.UrlEncodedFormEntity;
	import org.apache.http.client.methods.HttpPost;
	import org.apache.http.impl.client.DefaultHttpClient;
	import org.apache.http.message.BasicNameValuePair;
	import org.apache.http.util.EntityUtils;
	
	
	@Name("FASLoginModule")
	@SuppressWarnings({"unused", "rawtypes"})
	public class FedoraAccountSystem implements LoginModule
	{
	   private static final String FEDORA_JSON_URL = "https://admin.fedoraproject.org/accounts/json/person_by_username?tg_format=json";
	   @Logger private Log log;
	
	   // Initial state
	   private Subject subject;
	   private CallbackHandler callbackHandler;
	   private Map sharedState;
	   private Map options;
	
	   // Authentication status
	   private boolean loginSucceeded = false;
	   private boolean commitSucceeded = false;
	
	   // Username and password
	   private String username;
	   private char[] password;
	
	   /*
	    * Custom implementation of the initialize method of LoginModule for the Fedora Account System
	    * 
	    * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
	    */
	   public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
	   {
	  	   this.subject = subject;
	  	   this.callbackHandler = callbackHandler;
	  	   this.sharedState = sharedState;
	  	   this.options = options;
	   }
	   
	   /*
	    * Custom login method for logging in a user to the Fedora Account System
	    * @see javax.security.auth.spi.LoginModule#login()
	    * 
	    * @exception LoginException A custom error message about why the login failed
	    * @return Returns true if the login is successful
	    */
	   public boolean login() throws LoginException {
		   if (callbackHandler == null) throw new LoginException("No CallbackHandler available");
		   
		   NameCallback nameCallback = new NameCallback("Username");
		   PasswordCallback passwordCallback = new PasswordCallback("Password", false);
	
		   Callback[] callbacks = new Callback[]{nameCallback, passwordCallback};
		   
		   try {
			   callbackHandler.handle(callbacks);
			   
			   username = nameCallback.getName();
			   password = passwordCallback.getPassword();
			   passwordCallback.clearPassword();
		   } catch (IOException e) {
			   throw new LoginException(e.toString());
		   } catch (UnsupportedCallbackException e) {
			   throw new LoginException("Error: " + e.getCallback().toString() + "not available");
		   }
		   
		   if (authenticate()) {
			   loginSucceeded = true;
		   } else {
			   return false;
		   }
		   
		   return true;
	   }
	   
	   /*
	    * Authenticates the user against the Fedora Account System. it also checks to ensure that the user 
	    * has accepted the Contributor License Agreement (CLA).
	    * 
	    * @return Returns true if the username and password is valid and the user has accepted the CLA.
	    */
	   private boolean authenticate() throws LoginException {   
		   if (password == null || username == null) throw new LoginException("No Username/Password found");
		   if (password.equals("") || username.equals("")) throw new LoginException("No Username/Password found");
		   
		   HttpClient client = new DefaultHttpClient();
	   
		   try {
			   // Generate the data to send
			   List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			   formparams.add(new BasicNameValuePair("username", username));
			   formparams.add(new BasicNameValuePair("user_name", username));
			   formparams.add(new BasicNameValuePair("password", String.valueOf(password)));
			   formparams.add(new BasicNameValuePair("login", "Login"));
			   UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
	   
			   HttpPost method = new HttpPost(FEDORA_JSON_URL);
			   method.setEntity(entity);
	   
			   // Send the data and get the response
			   HttpResponse response = client.execute(method);
			   HttpEntity resEntity = response.getEntity();
	   
			   // Handle the response
			   BufferedReader br = new BufferedReader(new InputStreamReader(resEntity.getContent(), "UTF-8"));
	    
			   JSONParser parser = new JSONParser();
			   ContainerFactory containerFactory = new ContainerFactory(){
				   public List creatArrayContainer() {
					   return new LinkedList();
				   }
	
				   public Map createObjectContainer() {
					   return new LinkedHashMap();
				   }                
			   };
			   
			   // Parse the response to check authentication success and valid groups
			   String line;
			   while ((line = br.readLine()) != null) {
				   Map json = (Map)parser.parse(line, containerFactory);
				   if (json.containsKey("success") && json.containsKey("person")) {
					   if (json.get("person") instanceof LinkedHashMap) {
						   LinkedHashMap person = (LinkedHashMap)json.get("person");
						   if (person.get("status").equals("active")) {
							   if (person.containsKey("approved_memberships")) {
								   if (person.get("approved_memberships") instanceof LinkedList) {
									   if (!checkCLAAgreement(((LinkedList)person.get("approved_memberships")))) {
										   throw new LoginException("FAS authentication failed for " + username + ". Contributor License Agreement not yet signed");
									   }
								   } else if (person.get("approved_memberships") instanceof LinkedHashMap) {
									   if (!checkCLAAgreement(((LinkedHashMap)person.get("approved_memberships")))) {
										   throw new LoginException("FAS authentication failed for " + username + ". Contributor License Agreement not yet signed");
									   }
								   }
							   } else {
								   throw new LoginException("FAS authentication failed for " + username + ". Contributor License Agreement not yet signed");
							   }
						   } else {
							   throw new LoginException("FAS authentication failed for " + username + ". Account is not active");
						   }
					   }
				   } else {
					   throw new LoginException("Error: FAS authentication failed for " + username);
				   }
			   }
			   
			   // Consume anything else in the response
			   EntityUtils.consume(resEntity);
		   } catch (LoginException e) {
			   throw e;
	   	   } catch (Exception e) {
			   log.error(e.getMessage());
			   // e.printStackTrace();
		   } finally {
			   client.getConnectionManager().shutdown();
		   }
		   return true;
	   }
	   
	   /*
	    * Checks to see if a user has accepted the CLA for fedora.
	    * 
	    * @param groups A LinkedList that contains a list of groups for a user.
	    * @return Returns true if the user has an entity in groups that matches the name "cla_done"
	    */
	   private boolean checkCLAAgreement(LinkedList<LinkedHashMap> groups) {
		   for (LinkedHashMap group: groups) {
			   if (group.containsKey("name")) {
				   if (group.get("name").equals("cla_done")) {
					   return true;
				   }
			   }
		   }
		   return false;
	   }
	   
	   /*
	    * Checks to see if a user has accepted the CLA for fedora.
	    * 
	    * @param groups A LinkedHashMap that contains the groups for a user.
	    * @return Returns true if the user has an entity in groups that matches the name "cla_done"
	    */
	   private boolean checkCLAAgreement(LinkedHashMap groups) {
		   for (Object key: groups.keySet()) {
			   LinkedHashMap group = (LinkedHashMap)groups.get(key);
			   if (group.containsKey("name")) {
				   if (group.get("name").equals("cla_done")) {
					   return true;
				   }
			   }
		   }
		   return false;
	   }
	   
	   /*
	    * Custom commit method  for logging the user into the Fedora Account System.
	    * TODO If necessary add principles here
	    * 
	    * @see javax.security.auth.spi.LoginModule#commit()
	    * @return Returns true if the user data was successfully committed. Returns false if the login procedure failed or the commit failed.
	    */
	   public boolean commit() throws LoginException {
		   if (loginSucceeded == false) {
			   return false;
		   } else {
			   username = null;
			   for (int i = 0; i < password.length; i++)
				   password[i] = ' ';
			   password = null;
	
			   commitSucceeded = true;
			   return true;
		   }
	   }
	   
	   /*
	    * Custom abort method for logging the user into the Fedora Account System.
	    * 
	    * @see javax.security.auth.spi.LoginModule#abort()
	    * @return Returns true if the login was successfully aborted. Returns false if the login never succeeded or the aborting fails.
	    */
	   public boolean abort() throws LoginException {
		   if (loginSucceeded == false) {
			   return false;
		   } else {
			   username = null;
			   for (int i = 0; i < password.length; i++)
				   password[i] = ' ';
			   password = null;
			   
			   return true;
		   }
	   }
	   
	   /*
	    * Custom logout method for logging out a user from the Fedora Account System.
	    * 
	    * @see javax.security.auth.spi.LoginModule#logout()
	    * @return Returns true if all the user data was cleaned up and logged out, otherwise returns false.
	    */
	   public boolean logout() throws LoginException {
		   loginSucceeded = false;
		   commitSucceeded = false;
		   
		   username = null;
		   for (int i = 0; i < password.length; i++)
			   password[i] = ' ';
		   password = null;
		   return true;
	   }
	
	}