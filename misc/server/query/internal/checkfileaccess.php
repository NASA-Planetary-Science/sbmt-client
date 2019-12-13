<?php

// Get posted data that we know the keys to
$rootURL=$_POST['rootURL'];
$userName=$_POST['userName'];
$password=$_POST['password'];
$args=$_POST['args'];

$cmd = "QueryFileAccessibility.sh $rootURL $userName $password $args";

$descriptorspec = array(
    0 => array("pipe", "r"),
    1 => array("pipe", "w")
);

$process = proc_open($cmd, $descriptorspec, $pipes);

if (is_resource($process)) {
    
    fwrite($pipes[0], $fileList);
    fclose($pipes[0]);
    
    $content = stream_get_contents($pipes[1]);
    fclose($pipes[1]);
    
    $return_value = proc_close($process);
    
    header('Content-type: text/plain');
    echo $content;
}

?>
