package com.redhat.topicindex.reporting.googlecharts;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;

import com.redhat.ecs.commonutils.StringUtilities;
import com.redhat.topicindex.reporting.ReportDataElement;
import com.redhat.topicindex.reporting.ReportDataGroup;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class is used to build a Google pie chart.
 */
public class GooglePieChart extends GoogleChart
{
	public GooglePieChart()
	{
		super();
	}
	
	public GooglePieChart(final String chartTitle)
	{
		super(chartTitle);
	}
	
	public GooglePieChart(final String chartTitle, final List<ReportDataGroup> reportDataGroups)
	{
		super(chartTitle, reportDataGroups);
	}
	
	@Override
	public int getBlockCount()
	{
		return 2;
	}
	
	@Override
	public String getBodyString(final int block, final int chartCount) 
	{
		// the first block is the javascript code
		if (block == 0)
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			
			// generate the data table javascript variable name
			final String dataTableVariable =  "data" + "_" + EntityUtilities.cleanStringForJavaScriptVariableName(this.getChartTitle());
			final String chartVariable =  "chart" + "_" + EntityUtilities.cleanStringForJavaScriptVariableName(this.getChartTitle());
							
			// generate the boilerplate datatable initialization code
			String thisDataString = StringUtilities.buildString(new String[] {
				"       var " + dataTableVariable + " = new google.visualization.DataTable();", 
				"       " + dataTableVariable + ".addColumn('string', 'Lifecycle');",
				"       " + dataTableVariable + ".addColumn('number', 'Number Of Topics');",
				"       " + dataTableVariable + ".addRows(" + this.getReportDataGroups().size() + ");\n"
			});
			
			// the ReportDataGroups are redundant for pie charts. all we do is get the first 
			// ReportDataElement and use that as the pie slice
			int i = 0;
			int total = 0;
			for (final ReportDataGroup reportDataGroup : this.getReportDataGroups())
			{
				final ReportDataElement reportDataItem = reportDataGroup.getReportDataElements().get(0);
				reportDataItem.setCount(entityManager.createQuery(reportDataItem.getQuery()).getResultList().size());
				thisDataString += "       " + dataTableVariable + ".setValue(" + i + ", 0, '" + reportDataItem.getTitle() + "');\n";
				thisDataString += "       " + dataTableVariable + ".setValue(" + i + ", 1, " + reportDataItem.getCount() + ");\n";
				++i;
				total += reportDataItem.getCount();
			}
			
			thisDataString += "       var " + chartVariable + " = new google.visualization.PieChart(document.getElementById('chart_div" + chartCount + "'));\n";
			thisDataString += "       " + chartVariable + ".draw(" + dataTableVariable + ", {width: 800, height: 400, title: '[Total: " + total + "] " + this.getChartTitle() + "', is3D: true, chartArea:{left:20,top:20,width:'100%',height:'100%'}});";
			
			return thisDataString;
		}
		// the second block is the divs that the charts will be rendered into
		else if (block == 1)
		{
			return "\n<div id=\"chart_div" + chartCount + "\"></div>";
		}
		
		return "";
	}
	
	@Override
	public String getCommonPreBodyString(final int block)
	{
		if (block == 0)
			return 
				super.getCommonPreBodyString(block) +
				"\n<script type=\"text/javascript\">\n" +
				"	google.load('visualization', '1', {packages:['corechart']});\n" +
				"	google.setOnLoadCallback(drawChart);\n\n" +
				"	function drawChart() {\n";
		return "";
	}
	
	@Override
	public String getCommonPostBodyString(final int block)
	{
		if (block == 0)
			return		
				"	}\n"+
				"</script>";
		return "";
	}
	
	@Override
	public boolean validate()
	{
		// for a pie chart, each ReportDataGroup is expected to have only 1 ReportDataElement
		// if this is not the case, this data is invalid and will not be processed
		for (final ReportDataGroup reportDataGroup : this.getReportDataGroups())
		{
			if (reportDataGroup.getReportDataElements().size() != 1)
				return false;
		}
		
		return true;
	}
	
}
