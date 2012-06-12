package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import com.redhat.topicindex.entity.IntegerConstants;

import java.util.Arrays;

@Name("integerConstantsList")
public class IntegerConstantsList extends EntityQuery<IntegerConstants> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -5704143445934846459L;

	private static final String EJBQL = "select integerConstants from IntegerConstants integerConstants";

	private static final String[] RESTRICTIONS = { "lower(integerConstants.constantName) like lower(concat('%',#{integerConstantsList.integerConstants.constantName},'%'))", };

	private IntegerConstants integerConstants = new IntegerConstants();

	public IntegerConstantsList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public IntegerConstants getIntegerConstants() {
		return integerConstants;
	}
}
