function propertyTagComboBoxItemSelected(comboBox, textboxId, saveId) 
{
	var textboxElement = document.getElementById(textboxId);
	var comboBoxSelectedIndex = comboBox.selectedIndex;
	var comboBoxSelectedValue = comboBox.options[comboBoxSelectedIndex].value;
	textboxElement.disabled = comboBoxSelectedValue.startsWith("PropertyTagCategory") || comboBoxSelectedValue.startsWith("UncategorisedPropertyTagCategory");

	var save = document.getElementById(saveId);
	
	save.disabled = comboBoxSelectedValue.startsWith("UncategorisedPropertyTagCategory");
	
	if (comboBoxSelectedValue.startsWith("PropertyTagCategory")) 
	{
		save.value = "Add All";
	} 
	else if (comboBoxSelectedValue.startsWith("UncategorisedPropertyTagCategory"))
	{
		save.value = " - ";
	}
	else
	{
		save.value = "Add";
	}
	
	
}
