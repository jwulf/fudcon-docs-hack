package com.redhat.topicindex.reporting.googlecharts;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;

import com.redhat.topicindex.reporting.ReportDataElement;
import com.redhat.topicindex.reporting.ReportDataGroup;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class is used to build a Google bar chart.
 */
public class GoogleBarChart extends GoogleChart 
{
	protected boolean isStacked = false;
	protected List<Integer> lineSeries = new ArrayList<Integer>();
	
	public List<Integer> getLineSeries() 
	{
		return lineSeries;
	}

	public void setLineSeries(final List<Integer> lineSeries) 
	{
		this.lineSeries = lineSeries;
	}

	public GoogleBarChart()
	{
		super();
	}
	
	public GoogleBarChart(final String chartTitle)
	{
		super(chartTitle);
	}
	
	public GoogleBarChart(final String chartTitle, final List<ReportDataGroup> reportDataGroups)
	{
		super(chartTitle, reportDataGroups);
	}
	
	public GoogleBarChart(final boolean isStacked)
	{
		super();
		this.isStacked = isStacked;
	}
	
	public GoogleBarChart(final String chartTitle, final boolean isStacked)
	{
		super(chartTitle);
		this.isStacked = isStacked;
	}
	
	public GoogleBarChart(final String chartTitle, final List<ReportDataGroup> reportDataGroups, final boolean isStacked)
	{
		super(chartTitle, reportDataGroups);
		this.isStacked = isStacked;
	}
	
	@Override
	public int getBlockCount() 
	{
		return 2;
	}

	@Override
	public String getBodyString(final int block, final int chartCount) 
	{
		if (!this.validate())
			return "";
		
		// the first block is the javascript code
		if (block == 0)
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			
			// generate the data table javascript variable name
			final String dataTableVariable =  "data" + "_" + EntityUtilities.cleanStringForJavaScriptVariableName(this.getChartTitle());
			final String chartVariable =  "chart" + "_" + EntityUtilities.cleanStringForJavaScriptVariableName(this.getChartTitle());							
			
			/*			 
			  The end result of this code should look something like:
			  
			  var data = new google.visualization.DataTable();
			  data.addColumn('string', 'Year');
			  data.addColumn('number', 'Sales');
			  data.addColumn('number', 'Expenses');
			  data.addRows(4);
			  data.setValue(0, 0, '2004');
			  data.setValue(0, 1, 1000);
			  data.setValue(0, 2, 400);
			  data.setValue(1, 0, '2005');
			  data.setValue(1, 1, 1170);
			  data.setValue(1, 2, 460);
			  data.setValue(2, 0, '2006');
			  data.setValue(2, 1, 660);
			  data.setValue(2, 2, 1120);
			  data.setValue(3, 0, '2007');
			  data.setValue(3, 1, 1030);
			  data.setValue(3, 2, 540);			 
			*/
			
			// start by calculating the topic counts
			for (final ReportDataGroup reportDataGroup : this.getReportDataGroups())
				for (final ReportDataElement reportElement : reportDataGroup.getReportDataElements())
					reportElement.setCount(entityManager.createQuery(reportElement.getQuery()).getResultList().size());
			
			// now sort by total value
			this.sortChildren(true);
			
			String thisDataString = 	"		var " + dataTableVariable + " = new google.visualization.DataTable();\n";
			thisDataString +=			"		" + dataTableVariable + ".addColumn('string', 'Count');\n";
			
			// build up the data rows of the table using the title of the elements from the first data group
			for (final ReportDataElement reportElement : this.getReportDataGroups().get(0).getReportDataElements())
			{
				final String rowTitle = reportElement.getTitle();
				thisDataString +=		"		" + dataTableVariable + ".addColumn('number', '" + rowTitle + "');\n";
			}
			
			thisDataString +=			"		" + dataTableVariable + ".addRows(" + this.getReportDataGroups().size() + ");\n";
			
			// add the data
			int i = 0;
			for (final ReportDataGroup reportDataGroup : this.getReportDataGroups())
			{
				thisDataString += 	"		" + dataTableVariable + ".setValue(" + i + ", 0, '" + reportDataGroup.getDataGroupTitle() + "');\n";				
				
				int j = 1;
				for (final ReportDataElement reportElement : reportDataGroup.getReportDataElements())
				{
					thisDataString += 	"		" + dataTableVariable + ".setValue(" + i + ", " + j + ", " + reportElement.getCount() + ");\n";
					++j;
				}
				++i;
			}
			
			thisDataString += "		var " + chartVariable + " = new google.visualization.ComboChart(document.getElementById('chart_div" + chartCount + "'));\n";
			thisDataString += 
				"		" + chartVariable + ".draw(\n" + 
				"			" + dataTableVariable + ",\n" + 
				"			{\n";
			
			if (this.lineSeries.size() != 0)
			{
				String seriesEnties = "";
				for (final Integer line : this.lineSeries)
				{
					if (seriesEnties.length() != 0)
						seriesEnties += ", ";
					seriesEnties += line + ": {type: 'line'}";
				}
						
				thisDataString += "				series: {" + seriesEnties + "},\n";			
			}
			
			thisDataString +=
				"				width: 1500,\n" +
				"				height: 600,\n" +
				"				title: '" + this.getChartTitle() + "',\n" +
				"				seriesType: 'bars'\n," +
				"				isStacked: " + this.isStacked + ",\n" +
	            "				chartArea: {height: '50%'},\n" +
	            "				hAxis: {showTextEvery: 1, slantedText: true, slantedTextAngle: 45}\n" +
				"			}\n" + 
				"		);\n";
			
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
				"	function drawChart()\n" +
				"	{\n";
		return "";
	}

	@Override
	public String getCommonPostBodyString(final int block) 
	{
		if (!this.validate())
			return "";
		
		if (block == 0)
			return		
				"	}\n"+
				"</script>";
		
		return "";
	}

	@Override
	public boolean validate() 
	{
		// make sure we have some data
		if (this.getReportDataGroups().size() == 0)
			return false;
		
		// make sure each element has the same number of values to display
		int initialCount = -1;
		for (final ReportDataGroup reportDataGroup : this.getReportDataGroups())
		{
			final int getReportDataElementCount = reportDataGroup.getReportDataElements().size();
			
			if (initialCount == -1)
				initialCount = getReportDataElementCount;
			else if (getReportDataElementCount != initialCount)
				return false;
		}
				
		return true;
	}

}
