package com.redhat.topicindex.reporting;

/**
 * This class represents the individual values displayed in a chart.
 */
public class ReportDataElement 
{
	private String query = "";
	private String title = "";
	private Integer count = null;
	
	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCount() {
		return count;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public ReportDataElement()
	{
		
	}
	
	public ReportDataElement(final String query, final String title, final Integer count)
	{
		this.query = query;
		this.title = title;
		this.count = count;
	}

	public ReportDataElement(final String query, final String title)
	{
		this.query = query;
		this.title = title;
	}
}
