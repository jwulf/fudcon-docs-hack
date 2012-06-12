package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("propertyTagList")
public class PropertyTagList extends EntityQuery<PropertyTag> 
{
	private static final long serialVersionUID = 257514207135829077L;

	private static final String SELECT_ALL_QUERY = "select propertyTag from PropertyTag propertyTag";

	private static final String[] RESTRICTIONS = {
			"lower(propertyTag.propertyTagName) like lower(concat('%',#{propertyTagList.propertyTag.propertyTagName},'%'))",
			"lower(propertyTag.propertyTagDescription) like lower(concat('%',#{propertyTagList.propertyTag.propertyTagDescription},'%'))", };

	private PropertyTag propertyTag = new PropertyTag();

	public PropertyTagList() 
	{
		this(com.redhat.topicindex.utils.Constants.DEFAULT_PAGING_SIZE);
	}
	
	public PropertyTagList(int limit) 
	{
		setEjbql(SELECT_ALL_QUERY);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		if (limit != -1)
			setMaxResults(limit);
	}

	public PropertyTag getPropertyTag() 
	{
		return propertyTag;
	}
}
