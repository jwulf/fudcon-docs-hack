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

// Find all sections in the document
$classname="section";
$query = "//*[contains(concat(' ', normalize-space(@class), ' '), ' $classname ')]";
$sections = $xpath->query($query);

// debug counters
$sectioncount = $sections->length;
$linkcount = 0;

echo "I found $sectioncount sections";

foreach ($sections as $section)
{
  // In each section find an edittopiclink, if one exists
  $editTopicLinkQuery = ".//x:a[@class='edittopiclink']";
  $editLinks = $xpath->query($editTopicLinkQuery, $section);

    if ( $editLinks->length == 1 ) {
    // if it does exist, give the section the 'sectionTopic' class 
      $domAttribute = $doc->createAttribute('class');
      $domAttribute->value = 'sectionTopic';
      $section->appendChild($domAttribute);
    }

}

$doc->saveHTMLFile($argv[2]);
?>

