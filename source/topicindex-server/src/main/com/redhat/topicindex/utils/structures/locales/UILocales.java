package com.redhat.topicindex.utils.structures.locales;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ecs.constants.CommonConstants;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.utils.Constants;

public class UILocales
{
	private List<UILocale> locales = new ArrayList<UILocale>();
	
	public UILocales()
	{
		for (final String locale : CommonConstants.LOCALES)
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
	
	public void loadLocaleCheckboxes(final Filter filter)
	{
		if (filter == null) return;
		
		for (final UILocale uiLocale : locales)
		{
			final List<Integer> localeStates = filter.hasLocale(uiLocale.getName());
			
			/*
			 * find out if this locale is already selected
			 */
			boolean selected = false;
			boolean selectedNot = false;

			for (final Integer localeState : localeStates)
			{
				if (localeState == Constants.NOT_MATCH_TAG_STATE)
					selected = selectedNot = true;
				else if (localeState == Constants.MATCH_TAG_STATE)
					selected = true;
			}
			
			uiLocale.setNotSelected(selectedNot);
			uiLocale.setSelected(selected);
		}
	}
}
