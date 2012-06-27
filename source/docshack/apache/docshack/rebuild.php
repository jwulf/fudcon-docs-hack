<?php 
  $ini_array = parse_ini_file('docshack.ini');
  $builder=$ini_array['docshackdir'].'/bin/builder';
  exec($builder . ' ' . $_GET["book"]);
?>
