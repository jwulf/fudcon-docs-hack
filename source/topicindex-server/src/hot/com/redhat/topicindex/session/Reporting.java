package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.reporting.HTMLChart;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.DroolsEvent;
import com.redhat.ecs.commonstructures.Pair;

@Name("reporting")
public class Reporting 
{
	@In protected Identity identity;
	@In protected WorkingMemory businessRulesWorkingMemory;
	/** A category mapped to a chart title mapped to a number of queries */
	private TreeMap<String, ArrayList<HTMLChart>> reportDatabase = new TreeMap<String, ArrayList<HTMLChart>>();
	/** The report we are looking at */
	private String reportName;
	/** The generated string that should appear in the head tag */
	private String body;
	/** The generated string that should appear in the body tag */
	private String head;

	/**
	 * This function takes a collection of queries and titles, counts the results, and then creates a javascript
	 * function to display those topic counts on a chart. Basically it feeds in the variables into the bolierplate
	 * code you can find at http://code.google.com/apis/chart/interactive/docs/gallery.html
	 */
	public void populate()
	{
		final Map<Integer, String> technologyTagIds = new HashMap<Integer, String>();
		final Map<Integer, String> concernTagIds = new HashMap<Integer, String>();
		final Map<Integer, String> releases = new HashMap<Integer, String>();
		final Map<Integer, String> writers = new HashMap<Integer, String>();
		
		populateMap(Constants.TECHNOLOGY_CATEGORY_ID, technologyTagIds);
		populateMap(Constants.COMMON_NAME_CATEGORY_ID, technologyTagIds);		
		populateMap(Constants.CONCERN_CATEGORY_ID, concernTagIds);
		populateMap(Constants.RELEASE_CATEGORY_ID, releases);
		populateMap(Constants.WRITER_CATEGORY_ID, writers);
		
		businessRulesWorkingMemory.setGlobal("reportDatabase", reportDatabase);
		businessRulesWorkingMemory.setGlobal("technologyTagIds", technologyTagIds);
		businessRulesWorkingMemory.setGlobal("concernTagIds", concernTagIds);
		businessRulesWorkingMemory.setGlobal("releases", releases);
		businessRulesWorkingMemory.setGlobal("writers", writers);
		
		EntityUtilities.injectSecurity(businessRulesWorkingMemory, identity);
		businessRulesWorkingMemory.insert(new DroolsEvent("SetReportingData"));
		businessRulesWorkingMemory.fireAllRules();
		
		generateReport(reportName);
	}
	
	private void populateMap(final Integer categoryID, final Map<Integer, String> map)
	{
		final EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		final Category category = entityManager.find(Category.class, categoryID);
		if (category != null)
		{
			for (final TagToCategory tagToCategory : category.getTagToCategories())
			{
				final Tag tag = tagToCategory.getTag();
				map.put(tag.getTagId(), tag.getTagName());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void generateReport(final String reportCategory)
	{		
		body = "";
		head = "";
		ArrayList<HTMLChart> reportDataList = null;
		
		// make sure we have some reports to show
		if (reportDatabase.size() != 0)
		{
			// is the reportCategory valid. of so, use it to get the report
			if (reportCategory != null && reportDatabase.containsKey(reportCategory))
				reportDataList  = reportDatabase.get(reportCategory);
			// if not, show the first report
			else
				reportDataList = reportDatabase.get(reportDatabase.keySet().toArray()[0]);
			
			// loop through each chart
			int chartCount = 0;
			Map<Pair<Class, Integer>, String> preBodyStrings = new HashMap<Pair<Class, Integer>, String>();
			Map<Pair<Class, Integer>, String> postBodyStrings = new HashMap<Pair<Class, Integer>, String>();
			Map<Pair<Class, Integer>, String> bodyStrings = new HashMap<Pair<Class, Integer>, String>();
			Map<Pair<Class, Integer>, String> preHeaderStrings = new HashMap<Pair<Class, Integer>, String>();
			Map<Pair<Class, Integer>, String> postHeaderStrings = new HashMap<Pair<Class, Integer>, String>();
			Map<Pair<Class, Integer>, String> headerStrings = new HashMap<Pair<Class, Integer>, String>();
			
			for (final HTMLChart reportData : reportDataList)
			{
				if (!reportData.validate())
					continue;
	
				for (int block = 0; block < reportData.getBlockCount(); ++block)
				{
					final Pair<Class, Integer> key = new Pair<Class, Integer>(reportData.getClass(), block);
					
					if (!bodyStrings.containsKey(key))
					{
						// we create only one getCommonPreBodyString per class per block
						preBodyStrings.put(key, reportData.getCommonPreBodyString(block));
						// we create only one getCommonPostBodyString per class per block
						postBodyStrings.put(key, reportData.getCommonPostBodyString(block));
						bodyStrings.put(key, "");
						
						// we create only one getCommonPreHeaderString per class per block
						preHeaderStrings.put(key, reportData.getCommonPreHeaderString(block));
						// we create only one getCommonPostHeaderString per class per block
						postHeaderStrings.put(key, reportData.getCommonPostHeaderString(block));
						headerStrings.put(key, "");
					}
						
					String bodyString = bodyStrings.get(key);					
					bodyString += reportData.getBodyString(block, chartCount);
					bodyStrings.put(key, bodyString);
					
					String headerString = headerStrings.get(key);		
					headerString += reportData.getHeaderString(block, chartCount);
					headerStrings.put(key, headerString);
				}
				
				for (final Pair<Class, Integer> classType : bodyStrings.keySet())
				{
					final String headerString = preHeaderStrings.get(classType) + headerStrings.get(classType) + postHeaderStrings.get(classType);
					final String bodyString = preBodyStrings.get(classType) + bodyStrings.get(classType) + postBodyStrings.get(classType);
	
					body += bodyString;
					head += headerString;
				}
								
				++chartCount;
			}
		}
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getReportName() {
		return reportName;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getHead() {
		return head;
	}
	
	public Set<String> getReportCategories()
	{
		return this.reportDatabase.keySet();
	}
}
