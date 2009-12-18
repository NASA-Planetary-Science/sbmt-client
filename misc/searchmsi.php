<?

$startDate=$_GET['startDate'] + 0;
$stopDate=$_GET['stopDate'] + 0;
$minScDistance=(float)$_GET['minScDistance'];
$maxScDistance=(float)$_GET['maxScDistance'];
$minResolution=(float)$_GET['minResolution'];
$maxResolution=(float)$_GET['maxResolution'];
$minIncidence=(float)$_GET['minIncidence'];
$maxIncidence=(float)$_GET['maxIncidence'];
$minEmission=(float)$_GET['minEmission'];
$maxEmission=(float)$_GET['maxEmission'];
$minPhase=(float)$_GET['minPhase'];
$maxPhase=(float)$_GET['maxPhase'];
$iofdbl=$_GET['iofdbl'] + 0;
$cifdbl=$_GET['cifdbl'] + 0;
$filterType1=$_GET['filterType1'] + 0;
$filterType2=$_GET['filterType2'] + 0;
$filterType3=$_GET['filterType3'] + 0;
$filterType4=$_GET['filterType4'] + 0;
$filterType5=$_GET['filterType5'] + 0;
$filterType6=$_GET['filterType6'] + 0;
$filterType7=$_GET['filterType7'] + 0;

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


$username="";
$password="";
$database="near";
$host="sd-mysql.jhuapl.edu:3308";

mysql_connect($host,$username,$password);
@mysql_select_db($database) or die();

$query = "SELECT id, year, day, filter, iofcif FROM msiimages ";
$query .= "WHERE starttime >= " . $stopDate;
$query .= " AND stoptime <= " . $startDate;
$query .= " AND target_center_distance >= " . minScDistance;
$query .= " AND target_center_distance <= " . maxScDistance;
$query .= " AND horizontal_pixel_scale >= " . minResolution;
$query .= " AND horizontal_pixel_scale <= " . maxResolution;

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
		$query .= " filter_type_flag = " . $filterTypes[i];
	}
	$query .= " ) ";
}

$query .= " AND minincidence <= " . $maxIncidence;
$query .= " AND maxincidence >= " . $minIncidence;
$query .= " AND minemission <= " . $maxEmission;
$query .= " AND maxemission >= " . $minEmission;
$query .= " AND minphase <= " . $maxPhase;
$query .= " AND maxphase >= " . $minPhase;

$result=mysql_query($query);

$num=mysql_numrows($result);

mysql_close();

$i=0;
while ($i < $num) 
{
	
	$id = mysql_result($result,$i,"id");
	$year = mysql_result($result,$i,"year");
	$day = mysql_result($result,$i,"day");
	$filter = mysql_result($result,$i,"filter");
	$iofcif = mysql_result($result,$i,"iofcif");
	
	echo "$id $year $day $filter $iofcif\n";
	
	$i++;
}

?>
