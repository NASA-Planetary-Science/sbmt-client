
<?

$first=$_GET['first'];

$username="";
$password="";
$database="near";
$host="sd-mysql.jhuapl.edu:3308";

mysql_connect($host,$username,$password);
@mysql_select_db($database) or die( "Unable to select database");

//$query="SELECT * FROM contacts";
$query = "SELECT id, year, day FROM nisspectra ";
				query .= "WHERE midtime >= " + startDate.getMillis();
				query .= " AND midtime <= " + stopDate.getMillis();
				query .= " AND range >= " + minScDistance;
				query .= " AND range <= " + maxScDistance;
				if (!polygonTypes.isEmpty())
				{
					query .= " AND ( ";
					int count = 0;
					for (Integer i : polygonTypes)
					{
						if (count++ > 0)
							query += " OR ";
						query .= " polygon_type_flag = " + i;
					}
					query .= " ) ";
				}
				query .= " AND minincidence <= " + maxIncidence;
				query .= " AND maxincidence >= " + minIncidence;
				query .= " AND minemission <= " + maxEmission;
				query .= " AND maxemission >= " + minEmission;
				query .= " AND minphase <= " + maxPhase;
				query .= " AND maxphase >= " + minPhase;

$result=mysql_query($query);

$num=mysql_numrows($result);

mysql_close();

echo "<b><center>Database Output</center></b><br><br>";

$i=0;
while ($i < $num) 
{

		$first=mysql_result($result,$i,"first");
		$last=mysql_result($result,$i,"last");
		$phone=mysql_result($result,$i,"phone");
		$mobile=mysql_result($result,$i,"mobile");
		$fax=mysql_result($result,$i,"fax");
		$email=mysql_result($result,$i,"email");
		$web=mysql_result($result,$i,"web");

echo "<b>$first $last</b><br>Phone: $phone<br>Mobile: $mobile<br>Fax: $fax<br>E-mail: $email<br>Web: $web<br><hr><br>";

$i++;
}

?>
