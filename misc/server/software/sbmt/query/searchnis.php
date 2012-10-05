<?php

$startDate=$_POST['startDate'] + 0;
$stopDate=$_POST['stopDate'] + 0;
$minScDistance=(float)$_POST['minScDistance'];
$maxScDistance=(float)$_POST['maxScDistance'];
$minIncidence=(float)$_POST['minIncidence'];
$maxIncidence=(float)$_POST['maxIncidence'];
$minEmission=(float)$_POST['minEmission'];
$maxEmission=(float)$_POST['maxEmission'];
$minPhase=(float)$_POST['minPhase'];
$maxPhase=(float)$_POST['maxPhase'];
$polygonType0=$_POST['polygonType0'] + 0;
$polygonType1=$_POST['polygonType1'] + 0;
$polygonType2=$_POST['polygonType2'] + 0;
$polygonType3=$_POST['polygonType3'] + 0;
$cubesStr=$_POST['cubes'];

$polygonTypes = array();
if ($polygonType0 == 1)
	$polygonTypes[] = 0;
if ($polygonType1 == 1)
	$polygonTypes[] = 1;
if ($polygonType2 == 1)
	$polygonTypes[] = 2;
if ($polygonType3 == 1)
	$polygonTypes[] = 3;


$username="nearuser";
$password="n3ar!usr";
$database="near";
$host="sd-mysql.jhuapl.edu:3306";

mysql_connect($host,$username,$password);
@mysql_select_db($database) or die();

$query = "SELECT DISTINCT nisspectra.id, year, day FROM nisspectra ";
if (strlen($cubesStr) > 0)
{
	$query .= " JOIN niscubes_beta2 ON nisspectra.id = niscubes_beta2.nisspectrumid ";
}
$query .= "WHERE midtime >= " . $startDate;
$query .= " AND midtime <= " . $stopDate;
$query .= " AND nisspectra.range >= " . $minScDistance;
$query .= " AND nisspectra.range <= " . $maxScDistance;
if (count($polygonTypes) > 0)
{
	$query .= " AND ( ";
	for ($i = 0; $i < count($polygonTypes); $i++)
	{
		if ($i > 0)
			$query .= " OR ";
		$query .= " polygon_type_flag = " . $polygonTypes[$i];
	}
	$query .= " ) ";
}
$query .= " AND minincidence <= " . $maxIncidence;
$query .= " AND maxincidence >= " . $minIncidence;
$query .= " AND minemission <= " . $maxEmission;
$query .= " AND maxemission >= " . $minEmission;
$query .= " AND minphase <= " . $maxPhase;
$query .= " AND maxphase >= " . $minPhase;

if (strlen($cubesStr) > 0)
{
	// Split up the cubes list
	$cubes = explode(",", $cubesStr);

	$query .= " AND niscubes_beta2.cubeid IN (";

	for ($i = 0; $i < count($cubes); $i++)
	{
		$cubeid = $cubes[$i] + 0;
		$query .= "" . $cubeid;
		if ($i < count($cubes)-1)
		{
			$query .= ",";
		}
	}

	$query .= ")";
}

$result=mysql_query($query);

$num=mysql_numrows($result);

mysql_close();

$i=0;
while ($i < $num)
{
	$row = mysql_fetch_row($result);
	$id   = $row[0];
	$year = $row[1];
	$day  = $row[2];

	echo "$id $year $day\n";

	$i++;
}

#echo "$query";

?>
