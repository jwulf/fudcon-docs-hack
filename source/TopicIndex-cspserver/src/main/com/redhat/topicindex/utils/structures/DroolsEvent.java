package com.redhat.topicindex.utils.structures;

public class DroolsEvent 
{
	protected String eventName;
	
	public DroolsEvent()
	{
		
	}
	
	public DroolsEvent(final String eventName)
	{
		this.setEventName(eventName);
	}

	public void setEventName(String eventName) 
	{
		this.eventName = eventName;
	}

	public String getEventName() 
	{
		return eventName;
	}
}
