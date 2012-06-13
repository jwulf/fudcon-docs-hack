<?php
// Step 2: Find <div class="note"> and wrap in link if found wrap in href from child edittopiclink
// This doesn't work when the DOCTYPE declaration is present, so strip that line out of the html file before handing it over to this script

if (!isset($argv) || $argc < 3)
{
	die('Pass input and output file names as arguments');
}

$doc = new DOMDocument;
$doc->preserveWhiteSpace = true;
$doc->Load($argv[1]);
$xpath = new DOMXPath($doc);
$xpath->registerNamespace("x", "http://www.w3.org/1999/xhtml");

// The Sections that SKynet produces (Invalid or Unwritten topics) have the class "clicknode"
$classname="clicknode";
$query = "//*[contains(concat(' ', normalize-space(@class), ' '), ' $classname ')]";
$clicknodes = $xpath->query($query);

// debug counters
$clickablecount = 0;
$linkcount = 0;

foreach ($clicknodes as $clicknode)
{
 
//  $editTopicLinkQuery = ".//x:a";
//  $editTopicLinkQuery = ".//*[contains(concat(' ', normalize-space(@class), ' '), ' edittopiclink ')]";
  $editTopicLinkQuery = ".//x:a[@class='edittopiclink']";
  $editLinks = $xpath->query($editTopicLinkQuery, $clicknode);

	$clickableQuery = ".//*[contains(concat(' ', normalize-space(@class), ' '), ' clickable ')]";
	$clickables = $xpath->query($clickableQuery, $clicknode);
  
  $clickablecount=$clickablecount+$clickables->length;

  $linkcount=$linkcount+$editLinks->length;

  if ( $clickables->length == 1 ) {
 
    if ( $editLinks->length == 1 ) {

/*
// This code clones the lower link when there is a clickable
     $newEditLink = $editLinks->item(0)->cloneNode(false);
     $clickwrapper = $clicknode->insertBefore($newEditLink, $clickables->item(0));
     $clickwrapper->appendChild($clickables->item(0));
*/
// This code moves the lower link when there is a clickable
     $clickwrapper = $clicknode->insertBefore($editLinks->item(0), $clickables->item(0));
     
	//Remove the existing text from the link
     $clickwrapper->nodeValue="";
	echo $clickwrapper->nodeValue;

     $clickwrapper->appendChild($clickables->item(0));


    }
  }

}


echo "clicknodes:", $clicknodes->length, "\n";
echo "clickables:", $clickablecount, "\n";
echo "links:", $linkcount;

$doc->saveHTMLFile($argv[2]);
?>

