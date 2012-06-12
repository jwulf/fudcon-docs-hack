package com.redhat.topicindex.entity.base;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Transient;

import org.jboss.seam.Component;

import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.TopicToPropertyTag;
import com.redhat.topicindex.exceptions.CustomConstraintViolationException;

/**
 * This class provides consistent access to the details of a property tag
 */
public abstract class ToPropertyTag<T extends AuditedEntity> extends AuditedEntity<T>
{
	protected PropertyTag propertyTag;
	protected String value;

	public ToPropertyTag(final Class<T> classType)
	{
		super(classType);
	}

	@Transient
	public boolean isValid()
	{
		if (this.propertyTag == null)
			return false;
		
		if (this.value == null)
			return this.propertyTag.isPropertyTagCanBeNull();
		
		if (!testUnique())
			return false;
				
		return this.value.matches(this.propertyTag.getPropertyTagRegex());
	}

	protected abstract boolean testUnique();

	public abstract PropertyTag getPropertyTag();

	public abstract void setPropertyTag(final PropertyTag propertyTag);

	public abstract String getValue();

	public abstract void setValue(final String value);
}
