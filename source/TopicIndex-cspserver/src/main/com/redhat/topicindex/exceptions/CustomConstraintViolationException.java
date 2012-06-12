package com.redhat.topicindex.exceptions;

public class CustomConstraintViolationException extends Exception
{
	private static final long serialVersionUID = -6156576595811734635L;
	
	public CustomConstraintViolationException(final String message)
	{
		super(message);
	}
}
