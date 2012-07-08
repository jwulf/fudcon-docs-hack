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
var topicID;
var skynetURL;


/**
 * AJAX Loader Object
 * uses the revealing module pattern
 * start it by calling ajaxLoader.start();
 * stop it by calling ajaxLoader.stop();
 */
var ajaxLoader = function () {
 
  var start = function() {
    var pageHeight = $(document).height();
 
    // if loader already exists, just show it
    // otherwise add markup to DOM
    if ($('#load-overlay').length) {
      // assign height again as it might have increased due to DOM additions
      $('#load-overlay').css({
        'height' : pageHeight
      });
      $('#load-overlay, #load-indicator').show();
    }
    else {
      $('body').append('<div id="load-overlay"></div><div id="load-indicator"><p>Contacting Server...</p></div>');
      $('#load-overlay').css({
        'height' : pageHeight
      });
    }
 
    // for ie6 only (feature detection used)
    if (typeof document.body.style.maxHeight === 'undefined') {
      // hide visible selects for proper overlay implementation
      $('select').hide();
    }
  };
 
  var stop = function() {
    $('#load-overlay, #load-indicator').hide();
 
    // for ie6 only (feature detection used)
    if (typeof document.body.style.maxHeight === 'undefined') {
      //show hidden selects for proper overlay implementation
      $('select').show();
    }
  };
 
  return {
    // declare which properties and methods are supposed to be public
    start: start,
    stop: stop
  }
}();

$(window).keypress(function(event) {
    if (!(event.which == 115 && event.ctrlKey) && !(event.which == 19)) return true;
    if (pageIsEditor){
      if (! $("#save-button").button("option","disabled")) 
        doSave();
    }
    event.preventDefault();
    return false;
});

$(document).keydown(function(event) {

    //19 for Mac Command+S
    if (!( String.fromCharCode(event.which).toLowerCase() == 's' && event.ctrlKey) && !(event.which == 19)) return true;

    if (pageIsEditor){
      if (! $("#save-button").button("option","disabled")) 
        doSave();
    }
    event.preventDefault();
    return false;
});


function timedRefresh(){ 
  updateXMLPreviewRoute(editor.getValue(), document.getElementById("div-preview"));
  if (window.timerID != 0) {
    clearTimeout(window.timerID);
    window.timerID = 0;
  }
}
        
window.onbeforeunload = function (e) {
  if (! $("#save-button").button("option","disabled")) 
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
  if (! $("#validate-button").button("option","disabled") || callback)
  {
    showStatusMessage("Performing validation check...");
    serversideValidateTopic(editor, callback);
  }
}

function makeValidityAmbiguous(){
  if (validXML)
    $("#div-validation").css("visibility", "hidden");
    $("#validate-button").button("enable");
    enableSaveRevert();
}

function hideSpinner(spinner){
	$(spinner).css("visibility", "hidden");
}

function showSpinner(spinner){
	$(spinner).css("visibility", "visible");
}

// Checks if the topic is valid, and then persists it using a node proxy to do the PUT
function doSave()
{ 
  if (! $("#save-button").button('option', 'disabled'))
  {  
    disableSaveRevert();
    if ( $("validate-button").button('option', 'enabled'))
      // This needs to be a callback, because validation is asynchronous
     { doValidate(doActualSave); }
    else
    { doActualSave(); }
  }
}

function showStatusMessage(message)
{
  $("#div-validation").css("visibility", "visible");	
	$("#div-validation").html(message);
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
       // ajaxStop();
        if (saveAjaxRequest.status == 200 || saveAjaxRequest.status == 304)
        {     
          showStatusMessage("Saved OK");
          $("#save-button").button("disable");
          $("#revert-button").button("disable");
        }
        else
        {
          showStatusMessage("Error saving. Status code: " + saveAjaxRequest.status);
          enableSaveRevert();
        }
      }
    }
    requestURL="/seam/resource/rest/1/topic/put/json";  
    requestString="?topicid="+topicID+"&serverurl="+serverURL+"&requestport="+port+"&requesturl="+urlpath+requestURL;    
     saveAjaxRequest.global=true;
    //alert(restProxy+"restput"+requestString);
    saveAjaxRequest.open("POST", nodeServer + "/restput" + requestString, true);
    saveAjaxRequest.setRequestHeader("Content-Type", "text/xml");
    var textToSave=editor.getValue();
    //ajaxStart();
    saveAjaxRequest.send(textToSave);
}

// Sends the editor content to a node server for validation
function serversideValidateTopic(editor, callback){
  ajaxRequest = new XMLHttpRequest();
  //ajaxStart();
  ajaxRequest.onreadystatechange=function()
  {
     if (ajaxRequest.readyState==4)
     {
        //ajaxStop();
        if (ajaxRequest.status == 200 || ajaxRequest.status == 304)
        {
          validationServerResponse=1;
          if (ajaxRequest.responseText == "0")
          { 
            showStatusMessage("Topic XML is valid Docbook 4.5");
            validXML=true;
            $("#validate-button").button("disable"); 
            if (callback && typeof(callback)=="function") callback(); 
            } 
          else {
            showStatusMessage(ajaxRequest.responseText);
            validXML=false;
            if (callback && typeof(callback)=="function") callback();
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
    var xml = (new DOMParser()).parseFromString(cm, "text/xml");
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
  topicID = url_query('topicid');
  skynetURL = url_query('skyneturl');
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

/*function onUnload()
{
  if ( document.getElementbyId("button-save") && ! document.getElementById("button-save").disabled)
  {
    var r=confirm("You have unsaved changes. Do you want to discard them?");
  }
}*/

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

function setPageTitle(topicTitle)
{
  $("#page-title").html(topicID + ": ");
  if (topicTitle)
    $("#page-title").html($("#page-title").html()+topicTitle);
}

// Creates a link to the read-only rendered view, useful for passing to people for preview
function injectPreviewLink()
{
  $("#preview-link").html('<a href="preview.html?skyneturl=http://'+skynetURL+'&topicid='+topicID+'">Preview Link</a>');
}

// This function loads the topic xml via JSONP, without a proxy
function loadSkynetTopicJsonP(topicID, skynetURL)
{
  if (topicID && skynetURL) 
  {
    requestURL="/seam/resource/rest/1/topic/get/jsonp/"+topicID+"?callback=?";  
    requeststring=skynetURL+requestURL;
    $.getJSON("http://"+requeststring, function(json) {
    if (json.xml == "") json.xml="<section>\n\t<title>"+json.title+"</title>\n\n\t<para>Editor initialized empty topic content</para>\n\n</section>";      
    if (pageIsEditor) {
      window.editor.setValue(json.xml);
      disableSaveRevert();
      doValidate();
      injectPreviewLink();
    }
    setPageTitle(json.title);
    updateXMLPreviewRoute(json.xml, document.getElementById("div-preview"));
    window.title=json.title;
  });
  }
}

function onPreviewPageLoad()
{
  generateRESTParameters();
  loadSkynetTopicJsonP(topicID, skynetURL)
}

function serverTopicLoadCallback(topicAjaxRequest)
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
      editorTitle=document.getElementById("page-title");
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
// This function loads the topic xml using a node.js proxy server
// Currently unused, as we're loading via JSONP
function loadSkynetTopicNodeProxy(topicID,skynetURL)
{ 
 if (topicID && skynetURL) 
  {
  // Load codemirror contents from Skynet URL
    topicAjaxRequest= new XMLHttpRequest();
    topicAjaxRequest.onreadystatechange=serverTopicLoadCallback(topicAjaxRequest)
    

    requestURL="/seam/resource/rest/1/topic/get/xml/"+topicID+"/xml";  
    requestString="?serverurl="+serverURL+"&requestport="+port+"&requesturl="+urlpath+requestURL;    
    // alert(restProxy+requestString);
    topicAjaxRequest.open("GET", nodeServer + "/restget" + requestString, true);
    topicAjaxRequest.send(null);
    }
}
      
function enableSaveRevert()
{
  $("#save-button").button("enable");
  $("#revert-button").button("enable");
}

function disableSaveRevert()
{

  $("#save-button").button("disable");
  $("#revert-button").button("disable");
}

function doRevert(){
    loadSkynetTopicJsonP(topicID,skynetURL);
}


function ajaxStart()
{
  //$('#loadingDiv').show();
  ajaxLoader.start();
}

function ajaxStop() {
//  $('#loadingDiv').hide();
  ajaxLoader.stop();
}

// This is the onload function for the editor page
function initializeTopicEditPage(){

// Killer idea for a loading div, 
// from here: http://stackoverflow.com/questions/68485/how-to-show-loading-spinner-in-jquery
    $('#loadingDiv')
    .hide()  // hide it initially
;
// Gots to get a gif from here: http://www.ajaxload.info/
//The ajaxStart and ajaxStop events are not called in JQuery post-1.4.4 when using JSONP
// This was suggested as a workaround, but didn't work for me. So we manually call those functions
// see: http://bugs.jquery.com/ticket/8338
/*  jQuery.ajaxPrefilter(function( options ) {
      options.global = true;
  }); */

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
	  lineWrapping: true,
    lineNumbers: true
	});

    $("#validate-button, #save-button, #revert-button, #skynet-button").button ();
    $("#validate-button").bind("click", doValidate);
    $("#save-button").bind("click", doSave);
    $("#revert-button").bind("click", doRevert);
    $("#skynet-button").bind("click", openTopicInSkynet);



 // topicid and skyneturl need to be written into the url by fixlinks.php / fixlinks in docs-hack-fixlinks.js
    generateRESTParameters();
    //loadSkynetTopicNodeProxy(topicID,skynetURL);
    loadSkynetTopicJsonP(topicID,skynetURL);
    skynetButtonURL="http://"+skynetURL+"/TopicEdit.seam?topicTopicId="+topicID;
}

function openTopicInSkynet()
{
  window.open(skynetButtonURL,'_blank');
}
