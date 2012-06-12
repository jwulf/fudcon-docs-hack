package com.redhat.topicindex.utils.structures.locales;

import java.util.ArrayList;
import java.util.List;

import com.redhat.topicindex.utils.Constants;

public class UILocales
{
	private List<UILocale> locales = new ArrayList<UILocale>();
	
	public UILocales()
	{
		for (final String locale : Constants.LOCALES)
		{
			getLocales().add(new UILocale(locale, false));
		}
	}

	public List<UILocale> getLocales()
	{
		return locales;
	}

	public void setLocales(final List<UILocale> locales)
	{
		this.locales = locales;
	}
	
	
}
