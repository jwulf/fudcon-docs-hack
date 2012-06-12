package com.redhat.topicindex.session;

import java.util.List;

import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.entity.Help;

@Name("customDataLookup")
public class CustomDataLookup 
{
	public String lookupHelpText(final String tableColID)
	{
		final HelpList helpList = new HelpList(-1);
		final List<Help> helps = helpList.getResultList();
		
		String helptext = "";
		for (final Help help : helps)
		{
			if (help.getTableColId().toLowerCase().equals(tableColID.toLowerCase()))
			{
				if (helptext.length() != 0)
					helptext += "\n";
				
				helptext += help.getHelpText();
			}
		}
		
		return helptext;
	}
}
