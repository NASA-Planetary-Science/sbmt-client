<?php

// Get posted data that we know the keys to.
$rootURL=$_POST['rootURL'];
$userName=$_POST['userName'];
$args=$_POST['args'];
$stdin=$_POST['stdin'];

$SBMTROOT='/disks/d0180/htdocs-sbmt/sbmt/query/sbmt';

putenv("SBMTROOT=$SBMTROOT");

$cmd = "$SBMTROOT/bin/CheckUserAccess.sh $rootURL $userName $args";

$descriptorspec = array(
    0 => array("pipe", "r"),
    1 => array("pipe", "w"),
    2 => array("file", "/dev/null", "a")
);

$content='';

// Open the command in a process.
$process = proc_open($cmd, $descriptorspec, $pipes);

// If proc_open succeeds, process will not be "false". In this
// case, make sure all streams and the process itself get closed.
// Have had trouble with commands hanging, so making sure.
if ($process) {

    // PHP code samples all seem to use this check before
    // actually communicating with the process. My guess
    // is that if the object returned by proc_open is not
    // "false", it is a resource, but can't find
    // definitive documentation of this.
    if (is_resource($process)) {
        fwrite($pipes[0], $stdin);
        fclose($pipes[0]);

        $content = stream_get_contents($pipes[1]);
    } else {
        // Not sure this is possible, but just in case
        // proc_open returned something that was "not
        // a resource", yet also not "false", close the
        // input stream.
        fclose($pipes[0]);
    }

    // Done with output streams now in any case.
    fclose($pipes[1]);
    fclose($pipes[2]);

    // Close the process itself.
    proc_close($process);
}

header('Content-type: text/plain');
echo $content;

?>
