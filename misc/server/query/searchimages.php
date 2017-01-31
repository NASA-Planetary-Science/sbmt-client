<?php

// Get posted data that we know the keys to
$imagesDatabase=$_POST['imagesDatabase'];
$cubesDatabase=$_POST['cubesDatabase'];
$searchString=$_POST['searchString'];
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
$cubesStr=$_POST['cubes'];
$limbType=$_POST['limbType'] + 0;

// Go through all posted data and create lists of selected camera and filter type numbers
$sumOfProductsSearch=$_POST['sumOfProductsSearch'] + 0;
$cameraTypes = array();
$filterTypes = array();

// Extract user specified camera/filter types based on what kind of search we are doing
if($sumOfProductsSearch === 1)
{
	// Sum-of-products (hierarchical) search
	$numProducts = $_POST['numProducts'];
	for ($i=0; $i<$numProducts; $i++)
	{
		// Save the selected camera/filter pair
		$cameraTypes[] = (int) $_POST['cameraType' . $i];
		$filterTypes[] = (int) $_POST['filterType' . $i];
	}
}
else
{
	// Product-of-sums (legacy) search
	foreach ($_POST as $key => $value) 
	{
		if(substr($key, 0, 10) === 'cameraType' && $value === "1")
		{
			// Save the selected camera number
			$cameraTypes[] = (int) substr($key, 10, strlen($key)-10);
		}
		elseif(substr($key, 0, 10) === 'filterType' && $value === "1")
		{
			// Save the selected filter number
			$filterTypes[] = (int) substr($key, 10, strlen($key)-10);
		}
	}
}

$username="nearuser";
$password="n3ar!usr";
$database="near";
$host="sd-mysql.jhuapl.edu:3306";


$link = mysql_connect($host,$username,$password);
if (!$link) 
{
    die('Could not connect: ' . mysql_error());
}
@mysql_select_db($database) or die("died!");


$query = "";

if( isset($_POST['searchString']) )
{
    $query = "SELECT filename, starttime FROM $imagesDatabase ";
    $query .= 'WHERE filename LIKE "%' . $searchString . '%"';
}
else
{
    $query = "SELECT DISTINCT filename, starttime FROM $imagesDatabase ";
    if (strlen($cubesStr) > 0)
    {
        $query .= " JOIN $cubesDatabase ON $imagesDatabase.id = $cubesDatabase.imageid ";
    }
    $query .= "WHERE starttime <= " . $stopDate;
    $query .= " AND stoptime >= " . $startDate;
    $query .= " AND target_center_distance >= " . $minScDistance;
    $query .= " AND target_center_distance <= " . $maxScDistance;
    $query .= " AND min_horizontal_pixel_scale <= " . $maxResolution;
    $query .= " AND max_horizontal_pixel_scale >= " . $minResolution;

	if($sumOfProductsSearch === 1)
	{
		// Sum-of-products (hierarchical) search
		if (count($cameraTypes) > 0)
		{	
        	$query .= " AND ( ";
			for ($i = 0; $i < count($cameraTypes); $i++)
			{
            	if ($i > 0)
                	$query .= " OR ";
            	$query .= "( camera = " . $cameraTypes[$i] . " AND filter = " . $filterTypes[$i] . " )";
			}
       		$query .= " )";					
		}
		else
		{
			// If no pairs were selected then form impossible condition so query will return nothing
			$query .= " AND ( camera = 0 AND camera = 1 )";
		}
	}
	else
	{
		// Product-of-sums (legacy) search
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

        	// Note we need to include filters with id equal to -1 since that
        	// will match cameras with only one filter (which get assigned to -1)
        	$query .= " AND ( filter = -1 ";

        	for ($i = 0; $i < count($filterTypes); $i++)
        	{
           	 	$query .= " OR ";
            	$query .= " filter = " . $filterTypes[$i];
     	   	}
       		$query .= " )";
    	}
    	else
    	{
			// If no cameras were selected then form impossible condition so query will return nothing
			$query .= " AND ( camera = 0 AND camera = 1 )";    	
    	}
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

        $query .= " AND $cubesDatabase.cubeid IN (";

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
}

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
