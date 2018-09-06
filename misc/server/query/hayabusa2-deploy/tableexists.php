<?php

//if(!isset($_POST['tableName']))
//{
//      die ("Error calling tableexists.php: tableName is not defined");
//}


// Get posted data that we know the keys to
$databaseTable=$_POST['tableName'];

$username="sbmt";
$password="Hyb24Ever!";
$database="sbmt";
$host="hyb2sbmt.u-aizu.ac.jp:3306";

$link = mysqli_connect($host,$username,$password,$database);
// Check connection
if (mysqli_connect_errno())
{
    echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

$val = mysqli_query($link, "SELECT 1 FROM $databaseTable LIMIT 1");

if(!$val)
{
        echo "false";
}
else
{
        echo "true";
}

?>