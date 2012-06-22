function initializeCodeMirror(id, readOnly)
{
	var xmlTextArea = document.getElementById(id);
  alert("yo");
	myCodeMirror = CodeMirror.fromTextArea(
		xmlTextArea, {
    mode: {name: "xml", alignCDATA: true},
        lineNumbers: true, 
			extraKeys: {
				"'>'": function(cm) { cm.closeTag(cm, '>'); },
				"'/'": function(cm) { cm.closeTag(cm, '/'); }
			}
	);
}
