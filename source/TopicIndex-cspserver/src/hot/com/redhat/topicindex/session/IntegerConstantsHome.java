package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

import com.redhat.topicindex.entity.IntegerConstants;

@Name("integerConstantsHome")
public class IntegerConstantsHome extends EntityHome<IntegerConstants> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 8149431383454440940L;

	public void setIntegerConstantsIntegerConstantsId(Integer id) {
		setId(id);
	}

	public Integer getIntegerConstantsIntegerConstantsId() {
		return (Integer) getId();
	}

	@Override
	protected IntegerConstants createInstance() {
		IntegerConstants integerConstants = new IntegerConstants();
		return integerConstants;
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

	public IntegerConstants getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
