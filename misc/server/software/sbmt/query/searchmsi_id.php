<?

$id=$_POST['id'] + 0;
$msiSource=$_POST['msiSource'];

$username="nearuser";
$password="n3ar!usr";
$database="near";
$host="sd-mysql.jhuapl.edu:3306";

if (substr($msiSource, 0, 3) == "PDS")
	$msiimages="msiimages_beta2";
else
	$msiimages="msiimages_gaskell_beta3";

mysql_connect($host,$username,$password);
@mysql_select_db($database) or die();

$query = "SELECT id, year, day, filter, iofcif, starttime FROM $msiimages ";
$query .= "WHERE id = " . $id;

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

?>
