<?

$startDate=$_POST['startDate'] + 0;
$stopDate=$_POST['stopDate'] + 0;
$minScDistance=(float)$_POST['minScDistance'];
$maxScDistance=(float)$_POST['maxScDistance'];
$minResolution=(float)$_POST['minResolution'];
$maxResolution=(float)$_POST['maxResolution'];
$minIncidence=(float)$_POST['minIncidence'];
$maxIncidence=(float)$_POST['maxIncidence'];
$minEmission=(float)$_POST['minEmission'];
$maxEmission=(float)$_POST['maxEmission'];
$minPhase=(float)$_POST['minPhase'];
$maxPhase=(float)$_POST['maxPhase'];
$iofdbl=$_POST['iofdbl'] + 0;
$cifdbl=$_POST['cifdbl'] + 0;
$filterType1=$_POST['filterType1'] + 0;
$filterType2=$_POST['filterType2'] + 0;
$filterType3=$_POST['filterType3'] + 0;
$filterType4=$_POST['filterType4'] + 0;
$filterType5=$_POST['filterType5'] + 0;
$filterType6=$_POST['filterType6'] + 0;
$filterType7=$_POST['filterType7'] + 0;
$cubesStr=$_POST['cubes'];
$msiSource=$_POST['msiSource'];
$limbType=$_POST['limbType'] + 0;

$filterTypes = array();
if ($filterType1 == 1)
	$filterTypes[] = 1;
if ($filterType2 == 1)
	$filterTypes[] = 2;
if ($filterType3 == 1)
	$filterTypes[] = 3;
if ($filterType4 == 1)
	$filterTypes[] = 4;
if ($filterType5 == 1)
	$filterTypes[] = 5;
if ($filterType6 == 1)
	$filterTypes[] = 6;
if ($filterType7 == 1)
	$filterTypes[] = 7;


$username="nearuser";
$password="n3ar!usr";
$database="near";
$host="sd-mysql.jhuapl.edu:3306";

if (substr($msiSource, 0, 3) == "PDS")
{
	$msiimages="msiimages";
	$msicubes="msicubes";
}
else
{
	$msiimages="msiimages_gaskell";
	$msicubes="msicubes_gaskell";
}

$link = mysql_connect($host,$username,$password);
if (!$link) {
    die('Could not connect: ' . mysql_error());
}
@mysql_select_db($database) or die("died!");

$query = "SELECT DISTINCT $msiimages.id, year, day, filter, iofcif, starttime FROM $msiimages ";
if (strlen($cubesStr) > 0)
{
	$query .= " JOIN $msicubes ON $msiimages.id = $msicubes.imageid ";
}
$query .= "WHERE starttime <= " . $stopDate;
$query .= " AND stoptime >= " . $startDate;
$query .= " AND target_center_distance >= " . $minScDistance;
$query .= " AND target_center_distance <= " . $maxScDistance;
$query .= " AND min_horizontal_pixel_scale <= " . $maxResolution;
$query .= " AND max_horizontal_pixel_scale >= " . $minResolution;

if ($iofdbl == 0)
	$query .= " AND iofcif = 1";
elseif ($cifdbl == 0)
	$query .= " AND iofcif = 0";

if (count($filterTypes) > 0)
{
	$query .= " AND ( ";
	for ($i = 0; $i < count($filterTypes); $i++)
	{
		if ($i > 0)
			$query .= " OR ";
		$query .= " filter = " . $filterTypes[$i];
	}
	$query .= " ) ";
}

if ($limbType == 1)
	$query .= " AND has_limb = 1";
elseif ($limbType == 2)
	$query .= " AND has_limb = 0";

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

	$query .= " AND $msicubes.cubeid IN (";

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
	$filter   = $row[3];
	$iofcif   = $row[4];
	$starttime = $row[5];
	

	echo "$id $year $day $filter $iofcif $starttime\n";
	
	$i++;
}

#echo "$query";

?>




