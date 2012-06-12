package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("helpHome")
public class HelpHome extends EntityHome<Help> 
{

	/** Serializable version identifier */
	private static final long serialVersionUID = -7435665803535248599L;

	public void setHelpHelpId(Integer id) {
		setId(id);
	}

	public Integer getHelpHelpId() {
		return (Integer) getId();
	}

	@Override
	protected Help createInstance() {
		Help help = new Help();
		return help;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public Help getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
