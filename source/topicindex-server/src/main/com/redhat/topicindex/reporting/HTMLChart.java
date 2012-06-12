package com.redhat.topicindex.reporting;

import java.util.List;

/**
 * This class provides an abstract way to create the HTML and JavaScript required to draw a chart
 * from an external API. It is built around the idea that each chart requires the following structure:
 * 
 * 	<head>
 * 		for i in getBlockCount()
 *	 		getCommonPreHeaderString(i) 	[called once for each subclass]
 *	 		getHeaderString(i)			[called for each instance of a subclass]
 *	 		getCommonPostHeaderString(i) 	[called once for each subclass]
 * 		end for
 * 	</head>
 *  <body>
 * 		for i in getBlockCount()
 *	 		getCommonPreHeaderString(i) 	[called once for each subclass]
 *	 		getBodyString(i)				[called for each instance of a subclass]
 *	 		getCommonPostHeaderString(i) 	[called once for each subclass]
 * 		end for
 * 	</body>
 */
public abstract class HTMLChart extends ReportData 
{
	public HTMLChart()
	{
		super();
	}
	
	public HTMLChart(final String chartTitle)
	{
		super(chartTitle);
	}
	
	public HTMLChart(final String chartTitle, final List<ReportDataGroup> reportDataGroups)
	{
		super(chartTitle, reportDataGroups);
	}
	
	/** returns the number of blocks that are to be processed */
	public abstract int getBlockCount();
	/** returns the string to be included in the header for a particular block */
	public abstract String getHeaderString(final int block, final int chartCount);
	/** 
	 * Returns the string to preceed all the strings returned by the getHeaderString function for a particular block. 
	 * The String returned by this function is will be prefixed to all the Strings returned by the getHeaderString
	 * function for a particular class.
	*/
	public abstract String getCommonPreHeaderString(final int block);
	/** returns the string to follow all the strings returned by the getHeaderString function for a particular block */
	public abstract String getCommonPostHeaderString(final int block);
	/** returns the string to be included in the body for a particular block */
	public abstract String getBodyString(final int block, final int chartCount);
	/** returns the string to preceed all the strings returned by the getBodyString function for a particular block */
	public abstract String getCommonPreBodyString(final int block);
	/** returns the string to follow all the strings returned by the getBodyString function for a particular block */
	public abstract String getCommonPostBodyString(final int block);
}
