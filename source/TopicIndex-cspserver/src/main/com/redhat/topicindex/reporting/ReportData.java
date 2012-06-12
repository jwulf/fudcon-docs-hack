package com.redhat.topicindex.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class represents the data required to draw a chart.
 */
public abstract class ReportData 
{
	private String chartTitle;
	private List<ReportDataGroup> reportDataGroups;
	
	public void setChartTitle(final String chartTitle) 
	{
		this.chartTitle = chartTitle;
	}
	
	public String getChartTitle() 
	{
		return chartTitle;
	}
	
	public void setReportDataGroups(final List<ReportDataGroup> reportDataGroups) 
	{
		this.reportDataGroups = reportDataGroups;
	}
	
	public List<ReportDataGroup> getReportDataGroups() 
	{
		return reportDataGroups;
	}
	
	public ReportData()
	{
		this.chartTitle = "";
		this.reportDataGroups = new ArrayList<ReportDataGroup>();
	}
	
	public ReportData(final String chartTitle)
	{
		this.chartTitle = chartTitle;
		this.reportDataGroups = new ArrayList<ReportDataGroup>();
	}
	
	public ReportData(final String chartTitle, final List<ReportDataGroup> reportDataGroups)
	{
		this.chartTitle = chartTitle;
		this.reportDataGroups = reportDataGroups;
	}
	
	public void sortChildren(final boolean acending)
	{
		Collections.sort(reportDataGroups);
		if (!acending)
			Collections.reverse(reportDataGroups);
	}
	
	abstract public boolean validate();
}
