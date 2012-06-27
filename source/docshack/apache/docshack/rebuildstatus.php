<?php

// This function traverses the built book hierarchy and grabs the build.time information
function traverse_hierarchy($path)
{
//  print($path);
  $return_array=array(); 
  $dir=opendir($path);
  while(($file = readdir($dir)) !== false)
  {
    if($file[0] == '.') continue;
    $fullpath = $path . '/' . $file;
    if(is_dir($fullpath))
    $return_array = array_merge($return_array, traverse_hierarchy($fullpath));
      else if ($file == "build.time")
	{
          $handle = @fopen($fullpath, "r");
         if ($handle) 
	  {
            while (($buffer = fgets($handle, 4096)) !== false) 
	    {
		$return_array[]=array('buildtime' => $buffer, 'book' => $path);
            }
           fclose($handle);
          } 
	}
   }
   return $return_array;
}

  $booksbuilding=array();
  $rowarray=array();
  $ini_array = parse_ini_file('docshack.ini');
  $docshackdir=$ini_array['docshackdir'];
  $buildlist=$docshackdir.'/books/booksbuilding';

// Here's where we build an array of the books that are building
  $handle = @fopen($buildlist, "r");
  if ($handle) {
    while (($buffer = fgets($handle, 4096)) !== false) {
        if ($buffer !== "\n")
          {
             $linearray= array('bookdir' => $buffer);
            $rowarray[]=$linearray;
          }
    }
    if (!feof($handle)) {
        echo "Error: unexpected fgets() fail\n";
    }
    fclose($handle);
  }
  if (count($rowarray) > 0)
    $booksbuilding=array('booksBuilding' => $rowarray);

  // Here we build a list of all books with their build time
  $buildtimelines = array();

  $bookdir = $docshackdir.'/server/standalone/deployments/TopicIndex.war/Books';
//  print ($bookdir);
  $buildtimes=array('booksBuildTime' => traverse_hierarchy($bookdir));

  if (count($booksbuilding) == 0)
    $returnobject=array("buildtimeobject" => $buildtimes);
  else
    $returnobject=array("buildtimeobject" => $buildtimes, "booksbuildingobject" => $booksbuilding);

  $jsonobject = json_encode($returnobject);
  print($jsonobject);
?>
