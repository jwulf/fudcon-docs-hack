package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

import com.redhat.topicindex.entity.StringConstants;

@Name("stringConstantsHome")
public class StringConstantsHome extends EntityHome<StringConstants> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 3460475961377023811L;

	public void setStringConstantsStringConstantsId(Integer id) {
		setId(id);
	}

	public Integer getStringConstantsStringConstantsId() {
		return (Integer) getId();
	}

	@Override
	protected StringConstants createInstance() {
		StringConstants stringConstants = new StringConstants();
		return stringConstants;
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

	public StringConstants getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
