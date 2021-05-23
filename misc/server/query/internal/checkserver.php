<?php

// Hard-wired for now. Get this from Apache configuration somehow?
$maximumQuerySize = 10000;

// Under no circumstance tell a client to check again sooner than this.
$minimumWaitTime = 5000.;

// Under no circumstance tell a client to wait longer than this to check again.
$maximumWaitTime = 60000.;

// Reference load average when server is not very active.
$idleLoadAverage = 0.13; // Empirically determined.

// Use w command to get load average, to compute wait time until next server check.
$waitTime = 1000.;

// 5-minute average:
// $cmd = "w | sed -ne 's/.*load average: *[0-9\.][0-9\.]*, *//p' | sed -e 's/,.*//'";
// 1-minute average:
$cmd = "w | sed -ne 's/.*load average: *//p' | sed -e 's/,.*//'";

$descriptorspec = array(
    0 => array("pipe", "r"),
    1 => array("pipe", "w")
);

$process = proc_open($cmd, $descriptorspec, $pipes);

if (is_resource($process)) {

//    fwrite($pipes[0], $stdin);
    fclose($pipes[0]);

    // Normalized load average = load-average / idle-load-average
    $normLoadAverage = stream_get_contents($pipes[1]) / $idleLoadAverage;
    fclose($pipes[1]);

    $return_value = proc_close($process);

    if ($normLoadAverage > 0.) {
        // Time to wait before another query = base-wait-time * normLoadAverage^2
        $waitTime *= $normLoadAverage * $normLoadAverage;
    }

    if ($waitTime < $minimumWaitTime) {
        $waitTime = $minimumWaitTime;
    } else if ($waitTime > $maximumWaitTime) {
        $waitTime = $maximumWaitTime;
    }

    header('Content-type: text/plain');
}

// First number is maximum query size. For now this is hardwired.
echo "$maximumQuerySize\n";
echo (int) $waitTime;

?>
