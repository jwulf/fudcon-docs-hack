package com.redhat.topicindex.session;

import org.drools.WorkingMemory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;

import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.DroolsEvent;
import com.redhat.topicindex.utils.structures.UserPrefsData;

@Name("userPrefs")
public class UserPrefs 
{
	private UserPrefsData userPrefsData = new UserPrefsData();
	@In protected Identity identity;
	@In protected WorkingMemory businessRulesWorkingMemory;


	public void populate()
	{
		businessRulesWorkingMemory.setGlobal("userPrefsData", userPrefsData);
		EntityUtilities.injectSecurity(businessRulesWorkingMemory, identity);
		businessRulesWorkingMemory.insert(new DroolsEvent("PopulateUserPrefs"));
		businessRulesWorkingMemory.fireAllRules();
	}


	public void setUserPrefsData(UserPrefsData userPrefsData) {
		this.userPrefsData = userPrefsData;
	}


	public UserPrefsData getUserPrefsData() {
		return userPrefsData;
	}
}
