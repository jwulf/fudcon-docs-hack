package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("propertyTagCategoryList")
public class PropertyTagCategoryList extends EntityQuery<PropertyTagCategory>
{
	private static final long serialVersionUID = -8089488742149823433L;

	private static final String EJBQL = "select propertyTagCategory from PropertyTagCategory propertyTagCategory";

	private static final String[] RESTRICTIONS = { "lower(propertyTagCategory.propertyTagCategoryName) like lower(concat(#{propertyTagCategoryList.propertyTagCategory.propertyTagCategoryName},'%'))",
			"lower(propertyTagCategory.propertyTagCategoryDescription) like lower(concat(#{propertyTagCategoryList.propertyTagCategory.propertyTagCategoryDescription},'%'))", };

	private PropertyTagCategory propertytagcategory = new PropertyTagCategory();

	public PropertyTagCategoryList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public PropertyTagCategory getPropertyTagCategory()
	{
		return propertytagcategory;
	}
}
