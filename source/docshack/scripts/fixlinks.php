<?php
// Production ready, audited and peer-reviewed high performance script. Standards compliant and DOD approved.
// Author remains anonymous for protection

if (!isset($argv) || $argc < 2)
{
	die('Pass file name as argument, and optionally a CSPHOST URL, outputs to STDOUT');
}

// Declare new link (edit this to change link format)
// If we got an optional argument after the filename, then use it for the link; otherwise: localhost

if ( $argc = 3 )
{
  $startNewLink = '<a href="' + $argv[2] + '/TopicEdit.seam?topicTopicId=';
} 
else
{
  $startNewLink = '<a href="http://127.0.0.1:8080/TopicIndex/TopicEdit.seam?topicTopicId=';
}
$endNewLink = '" target="_new" class="edittopiclink">  Edit  ';

// Read file in
$html = file_get_contents($argv[1]);

// See if we can find a new link to replace
$strpos = stripos($html, '<a href="https://bugzilla.redhat.com/enter_bug.cgi');

// Loop while we can
while ($strpos !== false)
{
	$strendpos = stripos($html, '</a>', $strpos);
	$strtopicidpos = stripos($html, 'cf_build_id=', $strpos);
	$strtopicidendpos = stripos($html, '-', $strtopicidpos + 12);
	$output = substr($html, 0, $strpos) . $startNewLink . substr($html, $strtopicidpos + 12, $strtopicidendpos - $strtopicidpos - 12) . $endNewLink . substr($html, $strendpos);
	$html = $output;
	$strpos = stripos($html, '<a href="https://bugzilla.redhat.com/enter_bug.cgi');
}
echo $output;
?>
