package com.redhat.topicindex.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * Each element in a chart (like a pie slice in a pie chart or bar(s) in a bar
 * chart is represented by a ReportDataGroup object. For charts that display
 * only 1 value per element, like a pie chart, ReportDataGroup objects are
 * effectively just used to hold a single ReportDataElement. For charts that can
 * display multiple values per element, like an area bar chart, a
 * ReportDataElement object represents that element, and the ReportDataElements
 * it holds represents the individual values
 */
public class ReportDataGroup implements Comparable<ReportDataGroup>
{
	private List<ReportDataElement> reportDataElements;
	private String dataGroupTitle;

	public void setReportDataElements(final List<ReportDataElement> reportDataElements)
	{
		this.reportDataElements = reportDataElements;
	}

	public List<ReportDataElement> getReportDataElements()
	{
		return reportDataElements;
	}

	public void setDataGroupTitle(final String dataGroupTitle)
	{
		this.dataGroupTitle = dataGroupTitle;
	}

	public String getDataGroupTitle()
	{
		return dataGroupTitle;
	}

	public ReportDataGroup()
	{
		this.reportDataElements = new ArrayList<ReportDataElement>();
		this.dataGroupTitle = "";
	}

	public ReportDataGroup(final ReportDataElement singleElement)
	{
		this.reportDataElements = new ArrayList<ReportDataElement>();
		this.reportDataElements.add(singleElement);
		this.dataGroupTitle = "";
	}

	public ReportDataGroup(final String dataGroupTitle)
	{
		this.dataGroupTitle = dataGroupTitle;
		this.reportDataElements = new ArrayList<ReportDataElement>();
	}

	public ReportDataGroup(final String dataGroupTitle, final ArrayList<ReportDataElement> reportDataElements)
	{
		this.dataGroupTitle = dataGroupTitle;
		this.reportDataElements = reportDataElements;
	}

	public Integer getTotalCount()
	{
		Integer retValue = 0;
		for (final ReportDataElement element : reportDataElements)
			retValue += element.getCount();
		return retValue;
	}

	@Override
	public int compareTo(final ReportDataGroup o)
	{
		return this.getTotalCount().compareTo(o.getTotalCount());
	}
}
