package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("categoryList")
public class CategoryList extends EntityQuery<Category> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -1296202451139604532L;

	private static final String[] RESTRICTIONS = {
			"lower(category.categoryName) like lower(concat('%',#{categoryList.category.categoryName},'%'))",
			"lower(category.categoryDescription) like lower(concat('%',#{categoryList.category.categoryDescription},'%'))", };

	private Category category = new Category();

	public CategoryList()
	{
		this(com.redhat.topicindex.utils.Constants.DEFAULT_PAGING_SIZE);
	}
	
	public CategoryList(int limit) 
	{
		setEjbql(Category.SELECT_ALL_QUERY);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		if (limit != -1)
			setMaxResults(limit);
	}

	public Category getCategory() {
		return category;
	}
}
