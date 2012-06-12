package com.redhat.topicindex.utils.structures;

import com.redhat.ecs.commonutils.CollectionUtilities;

/**
 * Used to work around the fact that variables accessed in an anonymous function
 * have to be final. A final instance of the ObjectWrapper class can hold a 
 * non-final instance of X.
 */
public class ObjectWrapper<X>
{
	private X myObject = null;

	public X getMyObject()
	{
		return myObject;
	}

	public void setMyObject(final X arg)
	{
		this.myObject = arg;
	}

	public ObjectWrapper(final X first)
	{
		this.myObject = first;
	}

	public ObjectWrapper()
	{

	}

	@Override
	public boolean equals(final Object other)
	{
		if (other == null)
			return false;
		
		if (!(other instanceof ObjectWrapper<?>))
			return false;

		final ObjectWrapper<?> otherObjectWrapper = (ObjectWrapper<?>) other;

		return CollectionUtilities.isEqual(this.myObject, otherObjectWrapper.myObject);
	}
}