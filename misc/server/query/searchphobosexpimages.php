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
$phobos2=$_POST['phobos2'] + 0;
$vikingOrbiter1A=$_POST['vikingOrbiter1A'] + 0;
$vikingOrbiter1B=$_POST['vikingOrbiter1B'] + 0;
$vikingOrbiter2A=$_POST['vikingOrbiter2A'] + 0;
$vikingOrbiter2B=$_POST['vikingOrbiter2B'] + 0;
$mexHrsc=$_POST['mexHrsc'] + 0;
$filterType1=$_POST['filterType1'] + 0;
$filterType2=$_POST['filterType2'] + 0;
$filterType3=$_POST['filterType3'] + 0;
$filterType4=$_POST['filterType4'] + 0;
$filterType5=$_POST['filterType5'] + 0;
$filterType6=$_POST['filterType6'] + 0;
$filterType7=$_POST['filterType7'] + 0;
$filterType8=$_POST['filterType8'] + 0;
$filterType9=$_POST['filterType9'] + 0;
$filterType10=$_POST['filterType10'] + 0;
$cubesStr=$_POST['cubes'];
$imageSource=$_POST['imageSource'];
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
if ($filterType8 == 1)
	$filterTypes[] = 8;
if ($filterType9 == 1)
	$filterTypes[] = 9;
// the following filter is a dummy filter for HRSC
if ($filterType10 == 1)
	$filterTypes[] = -1;

$cameraTypes = array();
if ($phobos2 == 1)
	$cameraTypes[] = 1;
if ($vikingOrbiter1A == 1)
	$cameraTypes[] = 2;
if ($vikingOrbiter1B == 1)
	$cameraTypes[] = 3;
if ($vikingOrbiter2A == 1)
	$cameraTypes[] = 4;
if ($vikingOrbiter2B == 1)
	$cameraTypes[] = 5;
if ($mexHrsc == 1)
	$cameraTypes[] = 6;

$username="nearuser";
$password="n3ar!usr";
$database="near";
$host="sd-mysql.jhuapl.edu:3306";

if (substr($imageSource, 0, 3) == "PDS")
{
	$phobosimages="phobosexpimages_pds";
	$phoboscubes="phobosexpcubes_pds";
}
else
{
	$phobosimages="phobosexpimages_gaskell";
	$phoboscubes="phobosexpcubes_gaskell";
}

$link = mysql_connect($host,$username,$password);
if (!$link) {
    die('Could not connect: ' . mysql_error());
}
@mysql_select_db($database) or die("died!");

$query = "SELECT DISTINCT filename, starttime FROM $phobosimages ";
if (strlen($cubesStr) > 0)
{
	$query .= " JOIN $phoboscubes ON $phobosimages.id = $phoboscubes.imageid ";
}
$query .= "WHERE starttime <= " . $stopDate;
$query .= " AND stoptime >= " . $startDate;
$query .= " AND target_center_distance >= " . $minScDistance;
$query .= " AND target_center_distance <= " . $maxScDistance;
$query .= " AND min_horizontal_pixel_scale <= " . $maxResolution;
$query .= " AND max_horizontal_pixel_scale >= " . $minResolution;

if (count($cameraTypes) > 0)
{
	$query .= " AND ( ";
	for ($i = 0; $i < count($cameraTypes); $i++)
	{
		if ($i > 0)
			$query .= " OR ";
		$query .= " camera = " . $cameraTypes[$i];
	}
	$query .= " ) ";
}

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

	$query .= " AND $phoboscubes.cubeid IN (";

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

$query .= " ORDER BY starttime";

$result=mysql_query($query);

$num=mysql_numrows($result);

mysql_close();

$i=0;
while ($i < $num) 
{
	$row       = mysql_fetch_row($result);	
	$filename  = $row[0];
	$starttime = $row[1];
	

	echo "$filename $starttime\n";
	
	$i++;
}

#echo "$query";

?>
