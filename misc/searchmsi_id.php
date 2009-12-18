<?

$id=$_GET['id'] + 0;


$username="";
$password="";
$database="near";
$host="sd-mysql.jhuapl.edu:3308";

mysql_connect($host,$username,$password);
@mysql_select_db($database) or die();

$query = "SELECT id, year, day, filter, iofcif FROM msiimages ";
$query .= "WHERE id = " . $id;

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
