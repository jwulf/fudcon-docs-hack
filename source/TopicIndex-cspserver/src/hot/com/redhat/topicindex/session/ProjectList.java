package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("projectList")
public class ProjectList extends EntityQuery<Project> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 1508750835147007740L;

	private static final String EJBQL = "select project from Project project";

	private static final String[] RESTRICTIONS = {
			"lower(project.projectName) like lower(concat(#{projectList.project.projectName},'%'))",
			"lower(project.projectDescription) like lower(concat(#{projectList.project.projectDescription},'%'))", };

	private Project project = new Project();

	public ProjectList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public Project getProject() {
		return project;
	}
}
