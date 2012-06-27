//This script is used when the server book index page is loaded
//It's called from cleantemplate.xhtml
//It sets up a refresh timer that updates the page with information
//about rebuilds happening on the server
/*
 * 
 * Here's the JSON Object that the server returns to the docshack/rebuildstatus.php ajax call 
 * The booksbuildingobject is not guaranteed to be present, as no books may be building
{"buildtimeobject":
	{"booksBuildTime":[
	                   {"buildtime":"Wed Jun 27 19:41:24 EST 2012\n",
	                	"book":"\/opt\/docshack\/server\/standalone\/deployments\/TopicIndex.war\/Books\/Messaging_Programming_Reference"
	                	},
	                	{"buildtime":"Wed Jun 27 21:14:20 EST 2012\n",
	                	"book":"\/opt\/docshack\/server\/standalone\/deployments\/TopicIndex.war\/Books\/Messaging_Installation_and_Configuration_Guide"},
	                	{"buildtime":"Wed Jun 27 19:39:34 EST 2012\n",
	                	"book":"\/opt\/docshack\/server\/standalone\/deployments\/TopicIndex.war\/Books\/Red_Hat_Engineers_Guide_to_Zanata"}
	                	]
	},
{"booksbuildingobject":
	{"booksBuilding":[
	                  {"bookdir":"\/opt\/docshack\/books\/Messaging_Installation_and_Configuration_Guide\n"}
	                  ]
	}
} */

function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}

//This is cookie cutter code to maintain the scroll position when we
//reload the page

//I tried this one using cookies here:
//http://stackoverflow.com/questions/7577897/javascript-page-reload-while-maintaining-current-window-position
//but it needed some debugging;

//This approaches uses URL parameters, and is from here:
//http://www.redips.net/javascript/maintain-scroll-position/

//define functions
var set_scroll,
my_scroll;

window.whoami="unknown";

//if query string in URL contains scroll=nnn, then scroll position will be restored
set_scroll = function () {
	// get query string parameter with "?"
	var search = window.location.search,
	matches;
	// if query string exists
	if (search) {
		// find scroll parameter in query string
		matches = /scroll=(\d+)/.exec(search);
		// jump to the scroll position if scroll parameter exists
		if (matches) {
			window.scrollTo(0, matches[1]);
		}
	}
};

//function appends scroll parameter to the URL or returns scroll value
my_scroll = function (url) {
	var scroll, q;
	// Netscape compliant
	if (typeof(window.pageYOffset) === 'number') {
		scroll = window.pageYOffset;
	}
	// DOM compliant
	else if (document.body && document.body.scrollTop) {
		scroll = document.body.scrollTop;
	}
	// IE6 standards compliant mode
	else if (document.documentElement && document.documentElement.scrollTop) {
		scroll = document.documentElement.scrollTop;
	}
	// needed for IE6 (when vertical scroll bar is on the top)
	else {
		scroll = 0;
	}
	// if input parameter does not exist then return scroll value
	if (url === undefined) {
		return scroll;
	}
	// else append scroll parameter to the URL
	else {
			// set "?" or "&" before scroll parameter
			q = url.indexOf('?') === -1 ? '?' : '&';
			// refresh page with scroll position parameter
			window.location.href = url + q + 'scroll=' + scroll;
		}
};

//add onload event listener
if (window.addEventListener) {
	window.addEventListener('load', set_scroll, false);
}
else if (window.attachEvent) {
	window.attachEvent('onload', set_scroll);
}

//This function is called on page load, and sets up a 3 second timer
//if this is the index page
//and a five second timer if it's a book

function fixlinks() {

	// load the previous scroll position any parameters in the url
	set_scroll;

	// If we're the index page we'll set the appropriate title 
	// and a timer to refresh the index every three seconds
	sekretimg=document.getElementsByClassName("sekretimg");
	if (sekretimg != null && sekretimg.length > 0){
		window.whoami="indexPage";

		document.title="Docs Hack Index Page";

		refreshStatus;
		this.timerID = setInterval(refreshStatus, 3000);
	}

	// if we're a built book, we'll find the book title and set the window
	// title to that
	// and we'll set a five second timer to find out if we've been rebuilt
	lastbuildtime=document.getElementsByClassName("docshack-build-time");
	if (lastbuildtime != null && lastbuildtime.length>0)
	{
		window.whoami="bookPage";

		// Let's do the window title first
		titles = document.getElementsByClassName("title");
		for (var i=0;i<titles.length;i++)
		{
			if (titles[i].tagName=="h1") 
			{
				titlist=titles[i]; 
				if ( titlist.innerHTML != null)
				{
					var mytitle=titlist.innerHTML; 
				}
				break;
			}
		}

		if ( mytitle !=null)
		{
			document.title=mytitle;
		}	

		//computeBuildTime();
		// myBuildTime will be assigned the first time we get a server response that describes this book		
		window.myBuildTime="unknown";

		// Here we get the buildURL of the book from the current URL
		// We'll compare it with the buildtime books from the server
		window.mybuildURL=window.location.pathname;
		topicstrip=mybuildURL.indexOf("/TopicIndex/");
		if ( topicstrip != -1)
		{
			window.mybuildURL=window.mybuildURL.substring(topicstrip+11)
		}
		topicstrip=window.mybuildURL.indexOf("/index.seam");
		if (topicstrip != -1)
		{
			mybuildURL=window.mybuildURL.substring(0,topicstrip);
		}

		this.timerID = setInterval(refreshStatus, 5000);
	}

}


//Unused Function - for reference purposes
//This function scans the book for an inserted authorgroup with a specific class
//this holds the build time of the document
//But we don't actually use it - the build.time on the server is the time of deployment, not
//the time of publican build that is encoded in the book.
function computeBuildTime()
{
	lastbuildtime=document.getElementsByClassName("docshack-build-time");
	if (lastbuildtime.length > 0)
	{
		// Here we try to get our build time
		if (lastbuildtime[0].hasChildNodes())
		{
			if (lastbuildtime[0].childNodes[0].childNodes.length=3)
			{
				if (lastbuildtime[0].childNodes[0].childNodes[2].innerHTML != null)
				{	
					window.myBuildTime=trim(lastbuildtime[0].childNodes[0].childNodes[2].innerHTML);

				}
			}
		}
	}
}

//This function is used by a book page to check if the book has been rebuilt on the server
//If it has, then the book page will reload itself from the server
function updateBookPage(ajaxRequest)
{
	if (ajaxRequest.readyState==4)
	{
		//alert(ajaxRequest.responseText);
		jsonResponse = eval('(' + ajaxRequest.responseText + ')');

		if (jsonResponse.buildtimeobject != null)
		{
			for (i=0; i<jsonResponse.buildtimeobject.booksBuildTime.length; i++)
			{
				book = jsonResponse.buildtimeobject.booksBuildTime[i].book.toString();
				strLocation=book.indexOf("/Books/");
				if (strLocation != -1)
				{
					book=book.substring(strLocation);
				}
				buildtime = jsonResponse.buildtimeobject.booksBuildTime[i].buildtime.toString();
				buildtime=buildtime.replace(/(\r\n|\n|\r)/gm,"");
				if ( book == window.mybuildURL )
				{
					// If myBuildTime is not set, it means this is the first response from the server
					if ( window.myBuildTime == "unknown")
					{
						window.myBuildTime = buildtime;
					}
					if (buildtime != window.myBuildTime)
					{
						//alert("The book has changed on the server");
						window.myBuildTime = "unknown";
						// no warning, we'll just refresh the page
						// we'll save the scroll location so that it will come back to where it was
						// using this parameter passing function;
						my_scroll(location.href);
					}
				}
			}
		}

	}

}

function updatePage(ajaxRequest)
{
	if (window.whoami != null)
	{
		if (window.whoami == "indexPage")
		{
			updateIndexPage(ajaxRequest);
		}
		if (window.whoami == "bookPage")
		{
			updateBookPage(ajaxRequest);
		}
	}

}

//This function is used by the index page to update the state of rebuild buttons
//and the value of last build times on tool tips
function updateIndexPage(ajaxRequest)
{
	if (ajaxRequest.readyState==4)
	{
		//alert(ajaxRequest.responseText);
		jsonResponse = eval('(' + ajaxRequest.responseText + ')');


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

		if (jsonResponse.booksbuildingobject != null)
		{
			for (i=0; i<jsonResponse.booksbuildingobject.booksBuilding.length; i++)
			{
				// alert("yo, i'm all up in here!");
				var rbButtonName="rb-"+jsonResponse.booksbuildingobject.booksBuilding[i].bookdir;
				rbButtonName=rbButtonName.replace(/(\r\n|\n|\r)/gm,"");    
				var imgName="img-"+jsonResponse.booksbuildingobject.booksBuilding[i].bookdir;
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
//		alert("yo, i'm all up in here!");

		// Now update the last built times
		// alert(jsonResponse.booksBuildTime.length);
		if (jsonResponse.buildtimeobject != null)
		{
			for (i=0; i<jsonResponse.buildtimeobject.booksBuildTime.length; i++)
			{
				var labelName="lbl-"+jsonResponse.buildtimeobject.booksBuildTime[i].book;
				labelName=labelName.replace(/(\r\n|\n|\r)/gm,""); 
				label=document.getElementById(labelName);
				var title="Last Build: " + jsonResponse.buildtimeobject.booksBuildTime[i].buildtime;
				title=title.replace(/(\r\n|\n|\r)/gm,"");
				nodes=label.parentNode.childNodes;
				for (j=0; j<nodes.length;j++)
				{
					if (nodes[j].nodeType == 1)
					{
						nodes[j].title = title;
					}
				}
				//     label.title=title;
			}
		}
	}
}
//This function queries the server about which books are currently rebuilding
//And updates the status of the buttons on the page accordingly

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

//This function returns the URL of the server
//minus the app server port
//so that we can call php functions on port 80

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
