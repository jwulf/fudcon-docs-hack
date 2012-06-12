package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("exceptionHome")
public class ExceptionHome extends EntityHome<SkynetException>
{
	private static final long serialVersionUID = -4565730598338795691L;

	public void setSkynetExceptionExceptionId(Integer id)
	{
		setId(id);
	}

	public Integer getSkynetExceptionExceptionId()
	{
		return (Integer) getId();
	}

	@Override
	protected SkynetException createInstance()
	{
		SkynetException exception = new SkynetException();
		return exception;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public void wire()
	{
		getInstance();
	}

	public boolean isWired()
	{
		return true;
	}

	public SkynetException getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
