package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("helpList")
public class HelpList extends EntityQuery<Help> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -6973219687174541782L;

	private static final String EJBQL = "select help from Help help";

	private static final String[] RESTRICTIONS = {
			"lower(help.tableColId) like lower(concat('%',#{helpList.help.tableColId},'%'))",
			"lower(help.helpText) like lower(concat('%',#{helpList.help.helpText},'%'))", };

	private Help help = new Help();

	public HelpList() 
	{
		this(com.redhat.topicindex.utils.Constants.DEFAULT_PAGING_SIZE);
	}
	
	public HelpList(int limit) 
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		if (limit != -1)
			setMaxResults(limit);
	}
	
	public Help getHelp() 
	{
		return help;
	}
}
