function initializeCodeMirror(id, readOnly)
{
	var xmlTextArea = document.getElementById(id);
	myCodeMirror = CodeMirror.fromTextArea(
		xmlTextArea, 
		{
      mode: {name: "xmlpure"}, 
     // mode: {name: "xml", alignCDATA: true},			
      lineNumbers: true,
			readOnly: readOnly,
			lineWrapping: true,
     extraKeys: {
				"'>'": function(cm) { cm.closeTag(cm, '>'); },
				"'/'": function(cm) { cm.closeTag(cm, '/'); }
			},
    }
  );
}
