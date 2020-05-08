<?php

// Get posted data that we know the keys to
$rootURL=$_POST['rootURL'];
$userName=$_POST['userName'];
$args=$_POST['args'];
$stdin=$_POST['stdin'];

$SBMTROOT='/disks/d0180/htdocs-sbmt/sbmt/query/sbmt';

$cmd = "export SBMTROOT=$SBMTROOT; $SBMTROOT/bin/CheckUserAccess.sh $rootURL $userName $args";

$descriptorspec = array(
    0 => array("pipe", "r"),
    1 => array("pipe", "w")
);

$process = proc_open($cmd, $descriptorspec, $pipes);

if (is_resource($process)) {
    
    fwrite($pipes[0], $stdin);
    fclose($pipes[0]);
    
    $content = stream_get_contents($pipes[1]);
    fclose($pipes[1]);
    
    $return_value = proc_close($process);
    
    header('Content-type: text/plain');
    echo $content;
}

?>
