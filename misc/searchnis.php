<?

$startDate=$_GET['startDate'] + 0;
$stopDate=$_GET['stopDate'] + 0;
$minScDistance=(float)$_GET['minScDistance'];
$maxScDistance=(float)$_GET['maxScDistance'];
$minIncidence=(float)$_GET['minIncidence'];
$maxIncidence=(float)$_GET['maxIncidence'];
$minEmission=(float)$_GET['minEmission'];
$maxEmission=(float)$_GET['maxEmission'];
$minPhase=(float)$_GET['minPhase'];
$maxPhase=(float)$_GET['maxPhase'];
$polygonType0=$_GET['polygonType0'] + 0;
$polygonType1=$_GET['polygonType1'] + 0;
$polygonType2=$_GET['polygonType2'] + 0;
$polygonType3=$_GET['polygonType3'] + 0;

$polygonTypes = array();
if ($polygonType0 == 1)
	$polygonTypes[] = 0;
if ($polygonType1 == 1)
	$polygonTypes[] = 1;
if ($polygonType2 == 1)
	$polygonTypes[] = 2;
if ($polygonType3 == 1)
	$polygonTypes[] = 3;


$username="";
$password="";
$database="near";
$host="sd-mysql.jhuapl.edu:3308";

mysql_connect($host,$username,$password);
@mysql_select_db($database) or die();

$query = "SELECT id, year, day FROM nisspectra ";
$query .= "WHERE midtime >= " . $startDate;
$query .= " AND midtime <= " . $stopDate;
$query .= " AND range >= " . $minScDistance;
$query .= " AND range <= " . $maxScDistance;
if (count($polygonTypes) > 0)
{
	$query .= " AND ( ";
	for ($i = 0; $i < count($polygonTypes); $i++)
	{
		if ($i > 0)
			$query .= " OR ";
		$query .= " polygon_type_flag = " . $polygonTypes[i];
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
	
	echo "$id $year $day\n";
	
	$i++;
}

?>
