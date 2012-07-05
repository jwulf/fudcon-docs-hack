// TODO:
// 1. Add the ability to click on the title and edit it
// 2. Add buttons to insert elements
// 3. General usability enhancements like coloured state messages and action button locations

nodeServer="http://deathstar1.usersys.redhat.com:8888"
// window.previewserverurl="http://127.0.0.1:8888";
window.refreshTime=1000;
window.timerID=0;
window.clientXSLFile="xsl/docbook2html.xsl";
// window.restProxy="http://127.0.0.1:8888/";
window.mutex=0;
validXML=false;
var validationServerResponse;
var port;
var urlpath;
var serverURL;

function timedRefresh(){ 
  updateXMLPreviewRoute(window.editor, document.getElementById("div-preview"));
  if (window.timerID != 0) {
    clearTimeout(window.timerID);
    window.timerID = 0;
  }
}
        
window.onbeforeunload = function (e) {
  if (! document.getElementById("button-save").disabled) 
    return 'You have unsaved changes.';
};

// callback function for use when a node server is generating the live HTML preview
function handleHTMLPreviewResponse(ajaxRequest, serverFunction){
  if (ajaxRequest.readyState==4)
  {
    window.mutex = 0;
    if (ajaxRequest.status == 200 || ajaxRequest.status == 304)
    {

      if (serverFunction == "preview")
      {

        divpreview=document.getElementById("div-preview");
            if (divpreview.hasChildNodes)
              while(divpreview.hasChildNodes())
                divpreview.removeChild(divpreview.lastChild);
                            
       
        if (ajaxRequest.responseXML !== null)
        {
          section=ajaxRequest.responseXML.getElementsByClassName("section");
          if (section !== null)
          {
            divpreview.appendChild(section[0]);
          }
        }
      }

    }

  }          
}

function doValidate(callback)
{
  showStatusMessage("Performing validation check...");
  showSpinner("spinner-validate");
  serversideValidateTopic(editor, callback);
}

function makeValidityAmbiguous(){
  divvalidation=document.getElementById("div-validation");
  if (divvalidation && validXML)
    divvalidation.style.visibility="hidden";
  document.getElementById("button-validate").disabled=false;
}

function hideSpinner(spinner){
	document.getElementById(spinner).style.visibility="hidden";
}

function showSpinner(spinner){
	document.getElementById(spinner).style.visibility="visible";
}


// Checks if the topic is valid, and then persists it using a node proxy to do the PUT
function doSave()
{   
  disableSaveRevert();
  showSpinner("spinner-save");
  if ( document.getElementById("button-validate").disabled="false" )
   { doValidate(doActualSave); }
  else
  { doActualSave(); }
  // This needs to be a callback, because it's asynchronous
}

function showStatusMessage(message)
{
	divvalidation=document.getElementById("div-validation");
	divvalidation.style.visibility="visible";
	divvalidation.innerHTML=message;
}

function doActualSave()  
{
	if (! validXML && validationServerResponse == 1)
	  {
	    alert("This is not valid Docbook XML. If you are using Skynet injections I cannot help you.");
	  }
    if (validationServerResponse == 0)
    	{
    	alert("Unable to perform validation. Saving without validation.");
    	}
    showStatusMessage("Performing Save...")
	saveAjaxRequest= new XMLHttpRequest();
    saveAjaxRequest.onreadystatechange=function()
    {
      if (saveAjaxRequest.readyState==4)
      {
        
        if (saveAjaxRequest.status == 200 || saveAjaxRequest.status == 304)
        {     
          showStatusMessage("Saved OK");
          hideSpinner("spinner-save");
        }
        else
        {
          showStatusMessage("Error saving. Status code: " + saveAjaxRequest.status);
          hideSpinner("spinner-save");
          enableSaveRevert;
        }
      }
    }
    requestURL="/seam/resource/rest/1/topic/put/json";  
    requestString="?topicid="+topicID+"&serverurl="+serverURL+"&requestport="+port+"&requesturl="+urlpath+requestURL;    
    //alert(restProxy+"restput"+requestString);
    saveAjaxRequest.open("POST", nodeServer + "/restput" + requestString, true);
    saveAjaxRequest.setRequestHeader("Content-Type", "text/xml");
    var textToSave=editor.getValue();
    saveAjaxRequest.send(textToSave);
}

// Sends the editor content to a node server for validation
function serversideValidateTopic(editor, callback){
  ajaxRequest = new XMLHttpRequest();
  ajaxRequest.onreadystatechange=function()
  {
     if (ajaxRequest.readyState==4)
     {
    	hideSpinner("spinner-validate");
        if (ajaxRequest.status == 200 || ajaxRequest.status == 304)
        {
          validationServerResponse=1;
          if (ajaxRequest.responseText == "0")
          { 
            showStatusMessage("Topic XML is valid Docbook 4.5");
            validXML=true;
            document.getElementById("button-validate").disabled=true; 
            if (callback) callback(); 
            } 
          else {
            showStatusMessage(ajaxRequest.responseText);
            validXML=false;
            if (callback) callback();
          }
        }
        else
        {
        	showStatusMessage("Error performing validation: " + ajaxRequest.status);
        }
     }
  }
  validationServerReponse=0;
  ajaxRequest.open("POST", nodeServer + "/topicvalidate", true);
  ajaxRequest.setRequestHeader("Content-Type", "text/xml");
  ajaxRequest.send(editor.getValue());
}

function updateXMLPreviewRoute(cm,preview){
  // serverFunction = "validate";
  // serverFunction = "preview";
  //serversideUpdateXMLPreview(cm,preview, serverFunction);
  clientsideUpdateXMLPreview(cm,preview);
}

function loadXMLDoc(dname)
{
if (window.XMLHttpRequest)
  {
  xhttp=new XMLHttpRequest();
  }
else
  {
  xhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
xhttp.open("GET",dname,false);
xhttp.send("");
return xhttp.responseXML;
}
 

// This function generates the live HTML preview in the browser using XSLT
function clientsideUpdateXMLPreview(cm, preview){
  xsl=loadXMLDoc(clientXSLFile);
  try{  
    var xml = (new DOMParser()).parseFromString(cm.getValue(), "text/xml");
    xsltProcessor=new XSLTProcessor();
    xsltProcessor.importStylesheet(xsl);
    resultDocument = xsltProcessor.transformToFragment(xml,document);
      divpreview=document.getElementById("div-preview");
      if (divpreview.hasChildNodes)
        while(divpreview.hasChildNodes())
          divpreview.removeChild(divpreview.lastChild);
       
      divpreview.appendChild(resultDocument);

     
  }
  catch(err)
  {
    donothing=1;
  }
}

// Decompose skynetURL to server URL, server port, and path
// for REST GET and POST of the topic XML
function generateRESTParameters()
{
  // Take off the leading "http://", if it exists
  if (skynetURL.indexOf("http://") !== -1)    
    skynetURL=skynetURL.substring(7);

  portstart=skynetURL.indexOf(":");
  portend=skynetURL.indexOf("/");

  if (portstart !== -1)
  {
    // Deals with the case of a URL with a port number, eg: skynet.whatever.com:8080
    port=skynetURL.substring(portstart+1,portend);
    urlpath=skynetURL.substring(portend);
    serverURL=skynetURL.substring(0, portstart);
  }
  else
  {
    if (portend !== -1)
    {
      // Deals with the case of a URL with no port number, but a path after the server URL
      port="80";
      urlpath=skynetURL.substring(portend);
      serverURL=skynetURL.substring(0, portend);
    }
    else
    {
      // Deals with the case of no port number and no path
      port="80";
      urlpath="/"
      serverURL=skynetURL;
    }
  }
}

// This function sends the editor content to a node server to get back a rendered HTML view
function serversideUpdateXMLPreview(cm, serverFunction){      
            
  // If we weren't called from the 2 second timer, we must have been called by the 
  // Enter key event. In that case we'll clear the timer
  //   

   //preview.innerHTML=cm.getValue();
  if (window.mutex == 0)
  {
   
    ajaxRequest = new XMLHttpRequest();
    ajaxRequest.onreadystatechange=function()
    {
        handleHTMLPreviewResponse(ajaxRequest, serverFunction);
    }

    ajaxRequest.open("POST", nodeServer + "/xmlpreview", true);
    ajaxRequest.setRequestHeader("Content-Type", "text/xml");
    ajaxRequest.send(cm.getValue());
    window.mutex = 1;
    
  }
  
}

onUnload()
{
  if (! document.getElementById("button-save").disabled)
  {
    var r=confirm("You have unsaved changes. Do you want to discard them?");

  }
}

// Parse URL Queries
// from http://www.kevinleary.net/get-url-parameters-javascript-jquery/
function url_query( query ) {
	query = query.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var expr = "[\\?&]"+query+"=([^&#]*)";
	var regex = new RegExp( expr );
	var results = regex.exec( window.location.href );
	if( results !== null ) {
		return results[1];
		return decodeURIComponent(results[1].replace(/\+/g, " "));
	} else {
		return false;
	}
}

function setEditorTitle(topicTitle)
{
  editorTitle=document.getElementById("editor-title");
  if (editorTitle)
  {
    editorTitle.innerHTML= topicID+ ": ";
    if (topicTitle)
      editorTitle.innerHTML=editorTitle.innerHTML+topicTitle;
  }
}

// This function loads the topic xml via JSONP, without a proxy
function loadSkynetTopicJsonP(topicID, skynetURL)
{
  if (topicID && skynetURL) 
  {
    requestURL="/seam/resource/rest/1/topic/get/jsonp/"+topicID+"?callback=?";  
    requeststring=skynetURL+requestURL;
   // alert(requeststring);
    $.getJSON("http://"+requeststring, function(json) {
    if (json.xml == "") json.xml="<section>\n\t<title>"+json.title+"</title>\n\n\t<para>Editor initialized empty topic content</para>\n\n</section>";   
    window.editor.setValue(json.xml);
    setEditorTitle(json.title);    
    updateXMLPreviewRoute(editor, document.getElementById("div-preview"));
    disableSaveRevert();
    doValidate();
  });
  }
}

// This function loads the topic xml using a node.js proxy server
function loadSkynetTopicNodeProxy(topicID,skynetURL)
{
  // Here is where we should decode the page URL to get the TopicID
  // and pull it from skynet via an ajax call
  // The updateXMLPreview call below should be moved into the ajax request handler
  // that sets the editor with the topic xml

   // for the URL we're expecting to get something like
  // http://skynet.usersys.redhat.com:8080/TopicIndex
  // We'll decompose that into url, port, and path

  // we'll add the seam/resource/rest/1/topic/get/xml/7069/xml

  if (topicID && skynetURL) 
  {
  // Load codemirror contents from Skynet URL
    topicAjaxRequest= new XMLHttpRequest();
    topicAjaxRequest.onreadystatechange=function()
    {
      if (topicAjaxRequest.readyState==4)
      {
        if (topicAjaxRequest.status == 200 || topicAjaxRequest.status == 304)
        {  
          // Load the server response into the editor
          window.editor.setValue(topicAjaxRequest.response);
          disableSaveRevert();
          doValidate();
          updateXMLPreviewRoute(editor, document.getElementById("div-preview"));
          editorTitle=document.getElementById("editor-title");
          setEditorTitle(topicTitle[0].firstChild.nodeValue);
          
/*          if (editorTitle)
          {
            editorTitle.innerHTML=topicID+": ";
            topicTitle=topicAjaxRequest.responseXML.getElementsByTagName("title");
            if (topicTitle)
              editorTitle.innerHTML=editorTitle.innerHTML+topicTitle[0].firstChild.nodeValue;
          }
*/
        }
      }
    }

    requestURL="/seam/resource/rest/1/topic/get/xml/"+topicID+"/xml";  
    requestString="?serverurl="+serverURL+"&requestport="+port+"&requesturl="+urlpath+requestURL;    
    // alert(restProxy+requestString);
    topicAjaxRequest.open("GET", nodeServer + "/restget" + requestString, true);
    topicAjaxRequest.send(null);
    }
}
      

function enableSaveRevert()
{
  disableSaveRevert("false");
}

function disableSaveRevert(arg)
{
  if (!arg) arg="true";
  save=document.getElementById("button-save");
  if (save)
    save.disabled=(arg == "true");
  revert=document.getElementById("button-revert");
  if (revert)
    revert.disabled=(arg == "true");
}

function doRevert(){
    loadSkynetTopicJsonP(topicID,skynetURL);
}

// This is the onload function for the page
function initializeTopicEditPage(){
  window.mutex = 0;

  // Create our Codemirror text editor
  window.editor = CodeMirror.fromTextArea(document.getElementById("code"), {
    mode: 'text/html',
		extraKeys: {
			"'>'": function(cm) { cm.closeTag(cm, '>'); },
			"'/'": function(cm) { cm.closeTag(cm, '/'); }
  	},	   
		onKeyEvent: function(cm, e) {
    // with this function we set a timer so that the preview is updated every two seconds while the user is 
    // typing. When the user stops typing, the refreshes stop.
			  if (window.timerID == 0) 
          window.timerID = setTimeout("timedRefresh()", window.refreshTime);
        
        // Since the user hit a key, we will enable the Save and Revert Buttons
        // As long as the key isn't a cursor key or similar
        // key code reference: http://www.cambiaresearch.com/articles/15/javascript-char-codes-key-codes
        k=e.keyCode;
        if (k != 16 && k != 17 && k != 18 && k != 20 && k != 19 && k != 27 && k != 36 && k != 37 && k != 38 && k != 39 && k !=40 && k != 45)
        {
          enableSaveRevert();
          makeValidityAmbiguous();
        }
        return false; // return false tells Codemirror to also process the key;
		},
	  wordWrap: true,
	  lineWrapping: true
	});

 // topicid and skyneturl need to be written into the url by fixlinks.php / fixlinks in docs-hack-fixlinks.js
    topicID = url_query('topicid');
    skynetURL = url_query('skyneturl');
    generateRESTParameters();
    //loadSkynetTopicNodeProxy(topicID,skynetURL);
    loadSkynetTopicJsonP(topicID,skynetURL);
    skynetButtonURL="http://"+skynetURL+"/TopicEdit.seam?topicTopicId="+topicID;
}

function openTopicInSkynet()
{
  window.open(skynetButtonURL,'_blank');
}
