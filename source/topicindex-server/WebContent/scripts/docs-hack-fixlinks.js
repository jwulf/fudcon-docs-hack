// This script is used when the server book index page is loaded
// It's called from cleantemplate.xhtml
// It sets up a refresh timer that updates the page with information
// about rebuilds happening on the server


// This function is called on page load, and sets up a 5 second 
function fixlinks() {
  sekretimg=document.getElementsByClassName("sekretimg");
  if (sekretimg.length > 0){
    refreshStatus;
    this.timerID = setInterval(refreshStatus, 3000);
  }

}

//
function updatePage(ajaxRequest)
{
  if (ajaxRequest.readyState==4)
  {
	  jsonResponse = eval('(' + ajaxRequest.responseText + ')');
	 //alert(ajaxRequest.responseText);
    
	  // First, enable everything
	  rbButtons=document.getElementsByClassName("rebuild");
	  rbimgs=document.getElementsByClassName("rb-img");
	  for (var i=0;i<rbButtons.length;i++)
	  {
      
	  		rbButtons[i].disabled=false;
	  		rbButtons[i].innerHTML="Rebuild";
	   }

    for (i=0; i<rbimgs.length; i++)
    {
      rbimgs[i].style.visibility="hidden";
	  }  
	    // Now, disable everything the server has reported as rebuilding
     
     for (i=0; i<jsonResponse.books.length; i++)
	   {
       // alert("yo, i'm all up in here!");
       var rbButtonName="rb-"+jsonResponse.books[i].bookdir;
       rbButtonName=rbButtonName.replace(/(\r\n|\n|\r)/gm,"");    
       var imgName="img-"+jsonResponse.books[i].bookdir;
       imgName=imgName.replace(/(\r\n|\n|\r)/gm,"");

       	// change button text to "Rebuild"
       var rbButton = document.getElementById(rbButtonName);
       rbButton.innerHTML="Rebuilding";
	     rbButton.disabled=true;

	     var refreshImg = document.getElementById(imgName);
     
		  // turn the spinner on
	     refreshImg.style.visibility="visible";
	     
	     refreshImg.style.visibility="visible";
	   }
  }
}
// This function queries the server about which books are currently rebuilding
// And updates the status of the buttons on the page accordingly

function refreshStatus()
{
  var serverurl = getMyURL();
  var ajaxRequest = new XMLHttpRequest();
  	
  ajaxRequest.onreadystatechange=function()
  {
   updatePage(ajaxRequest);
  }

  ajaxRequest.open("GET", serverurl + "/docshack/rebuildstatus.php", true);
  ajaxRequest.send(null); 
}

// This function returns the URL of the server
// minus the app server port
// so that we can call php functions on port 80

function getMyURL() {
  var myurl = location.host;
 var n = myurl.indexOf(":");
  if (n==-1) {
    n = myurl.length;
  }
  return "http://" + myurl.substring(0, n);
}

function requestRebuildBook(bookDir){


  var serverurl = getMyURL();
  var ajaxRequest = new XMLHttpRequest();
  	
  ajaxRequest.onreadystatechange=function(){

    if (ajaxRequest.readyState==4){
       //document.getElementById("result").innerHTML=ajaxRequest.responseText;
       refreshStatus();
    }
  }
  argBookDir=encodeURIComponent(bookDir);
  ajaxRequest.open("GET", serverurl + "/docshack/rebuild.php?book="+argBookDir, true);
  ajaxRequest.send(null);  
}