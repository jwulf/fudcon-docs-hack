package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;

/**
 * A class to use in conjunction with a4j:actionparam in order to combine
 * variables used to build up URLs
 */
@Name("ajaxHelper")
public class AjaxHelper
{
	private Integer topicId;
	private String baseUrl;
	private String otherVars;

	public void setTopicId(final Integer topicId)
	{
		this.topicId = topicId;
	}

	public Integer getTopicId()
	{
		return topicId;
	}

	public void setBaseUrl(final String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	public void setOtherVars(final String otherVars)
	{
		this.otherVars = otherVars;
	}

	public String getOtherVars()
	{
		return otherVars;
	}

	public String getTopicUrl()
	{
		String retValue = "";

		if (baseUrl != null && topicId != null)
			retValue = baseUrl + "?topicTopicId=" + topicId;
		if (otherVars != null && otherVars.length() != 0)
			retValue += "&" + otherVars;

		topicId = null;
		baseUrl = null;
		otherVars = null;

		return retValue;
	}

	public String getTopicUrlAndEndConverstaion()
	{
		if (otherVars == null)
			otherVars = "";
		else if (otherVars.length() != 0)
			otherVars += "&";

		otherVars += "conversationPropagation=none";

		return getTopicUrl();
	}
}
