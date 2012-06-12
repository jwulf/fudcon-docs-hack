package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("exceptionList")
public class ExceptionList extends EntityQuery<Exception>
{

	private static final String EJBQL = "select skynetException from SkynetException skynetException";

	private static final String[] RESTRICTIONS =
	{ "lower(skynetException.details) like lower(concat(#{exceptionList.skynetException.details},'%'))", "lower(skynetException.description) like lower(concat(#{exceptionList.skynetException.description},'%'))", };

	private SkynetException skynetException = new SkynetException();

	public ExceptionList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public SkynetException getSkynetException()
	{
		return skynetException;
	}
}
