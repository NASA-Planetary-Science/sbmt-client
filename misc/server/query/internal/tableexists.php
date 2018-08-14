<?php
    
    // Get posted data that we know the keys to
    $imagesDatabase=$_POST['imagesDatabase'];
    
    $username="nearuser";
    $password="n3ar!usr";
    $database="near";
    $host="sd-mysql.jhuapl.edu:3306";
    
    $link = mysql_connect($host,$username,$password);
    if (!$link)
    {
        die('Could not connect: ' . mysql_error());
    }
    @mysql_select_db($database) or die("died!");
    
    $val = mysql_query("SELECT 1 FROM $imagesDatabase LIMIT 1");
    
    if(!$val)
    {
        echo "false";
    }
    
    ?>


