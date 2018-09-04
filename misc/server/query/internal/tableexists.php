<?php

    if(!isset($_POST['tableName']))
    {
        die ("Error calling tableexists.php: tableName is not defined");
    }

    // Get posted data that we know the keys to
    $databaseTable=$_POST['tableName'];

    $username="nearuser";
    $password="n3ar!usr";
    $database="near";
    $host="sd-mysql.jhuapl.edu:3306";

    $link = mysql_connect($host,$username,$password);
    if (!$link)
    {
        die('Unable to connect to database server: ' . mysql_error());
    }

    $selected = mysql_select_db($database);
    if(!$selected)
    {
        die ("Unable to select database $database: " .  mysql_error());
    }

    $val = mysql_query("SELECT 1 FROM $databaseTable LIMIT 1");

    if(!$val)
    {
        echo "false";
    }
    else
    {
        echo "true";
    }

?>


