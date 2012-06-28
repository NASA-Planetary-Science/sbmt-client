<?

$id=$_POST['id'] + 0;
$imageSource=$_POST['imageSource'];

$username="nearuser";
$password="n3ar!usr";
$database="near";
$host="sd-mysql.jhuapl.edu:3306";

if (substr($imageSource, 0, 3) == "PDS")
	$fcimages="fcimages_pds";
else
	$fcimages="fcimages_gaskell";

mysql_connect($host,$username,$password);
@mysql_select_db($database) or die();

$query = "SELECT filename, starttime FROM $fcimages ";
$query .= "WHERE id = " . $id;

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

?>
