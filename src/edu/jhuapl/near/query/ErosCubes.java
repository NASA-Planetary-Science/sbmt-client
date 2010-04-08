package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.TreeSet;

import vtk.vtkPolyData;
import edu.jhuapl.near.model.ErosModel;
import edu.jhuapl.near.util.BoundingBox;

public class ErosCubes
{
	private BoundingBox erosBB;
	private ArrayList<BoundingBox> allCubes = new ArrayList<BoundingBox>();
	private double cubeSize = 1.0;
	private double buffer = 0.01;
	
	public ErosCubes(ErosModel eros)
	{
		erosBB = eros.computeBoundingBox();

		erosBB.xmax += buffer;
		erosBB.xmin -= buffer;
		erosBB.ymax += buffer;
		erosBB.ymin -= buffer;
		erosBB.zmax += buffer;
		erosBB.zmin -= buffer;
		
		
		int numCubesX = (int)(Math.ceil(erosBB.xmax - erosBB.xmin) / cubeSize);
		int numCubesY = (int)(Math.ceil(erosBB.ymax - erosBB.ymin) / cubeSize);
		int numCubesZ = (int)(Math.ceil(erosBB.zmax - erosBB.zmin) / cubeSize);
		
		for (int k=0; k<numCubesZ; ++k)
		{
			double zmin = erosBB.zmin + k * cubeSize;
			double zmax = erosBB.zmin + (k+1) * cubeSize;
			for (int j=0; j<numCubesY; ++j)
			{
				double ymin = erosBB.ymin + j * cubeSize;
				double ymax = erosBB.ymin + (j+1) * cubeSize;
				for (int i=0; i<numCubesX; ++i)
				{
					double xmin = erosBB.xmin + i * cubeSize;
					double xmax = erosBB.xmin + (i+1) * cubeSize;
					BoundingBox bb = new BoundingBox();
					bb.xmin = xmin;
					bb.xmax = xmax;
					bb.ymin = ymin;
					bb.ymax = ymax;
					bb.zmin = zmin;
					bb.zmax = zmax;
					allCubes.add(bb);
				}
			}
		}

		// Change the following to false to actually compute the 
		// values stored in the erosIntersectingCubes array. This can take
		// a long time which is why we hard code the values into this class.
		boolean useHardCodedValues = true;
		if (useHardCodedValues)
		{
			ArrayList<BoundingBox> tmpCubes = new ArrayList<BoundingBox>();
			for (int i : erosIntersectingCubes)
			{
				tmpCubes.add(allCubes.get(i));
			}
			allCubes = tmpCubes;
		}
		else
		{
			System.out.println("numCubesX " + numCubesX);
			System.out.println("numCubesY " + numCubesY);
			System.out.println("numCubesZ " + numCubesZ);

			System.out.println("total cubes before reduction = " + allCubes.size());
			System.out.println("int[] erosIntersectingCubes = {");

			// Remove from allCubes all cubes that do not intersect the asteroid
			long t0 = System.currentTimeMillis();
			TreeSet<Integer> intersectingCubes = getIntersectingCubes(eros.getErosPolyData());
			System.out.println("Time elapsed:  " + ((double)System.currentTimeMillis()-t0)/1000.0);

			ArrayList<BoundingBox> tmpCubes = new ArrayList<BoundingBox>();
			int count = 0;
			for (Integer i : intersectingCubes)
			{
				tmpCubes.add(allCubes.get(i));
				System.out.print(i);
				if (count < intersectingCubes.size()-1)
					System.out.print(",");
				++count;
				if (count % 15 == 0)
					System.out.println("");
			}
			System.out.println("};");

			allCubes = tmpCubes;

			System.out.println("finished initializing cubes, total = " + allCubes.size());
		}
	}
	
	public BoundingBox getCube(int cubeId)
	{
		return allCubes.get(cubeId);
	}

	public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
	{
		TreeSet<Integer> cubeIds = new TreeSet<Integer>();

		// Iterate through each cube and check if it intersects
		// with the bounding box of any of the polygons of the polydata

		BoundingBox polydataBB = new BoundingBox(polydata.GetBounds());
		int numberPolygons = polydata.GetNumberOfCells();
	

		// Store all the bounding boxes of all the individual polygons in an array first
		// since the call to GetCellBounds is very slow.
		double[] cellBounds = new double[6];
		ArrayList<BoundingBox> polyCellsBB = new ArrayList<BoundingBox>();
		for (int j=0; j<numberPolygons; ++j)
		{
			polydata.GetCellBounds(j, cellBounds);
			polyCellsBB.add(new BoundingBox(cellBounds));
		}
		
		
		int numberCubes = allCubes.size();
		for (int i=0; i<numberCubes; ++i)
		{
			// Before checking each polygon individually, first see if the
			// polydata as a whole intersects the cube
			BoundingBox cube = getCube(i);
			if (cube.intersects(polydataBB))
			{
				for (int j=0; j<numberPolygons; ++j)
				{
					BoundingBox bb = polyCellsBB.get(j);
					if (cube.intersects(bb))
					{
						cubeIds.add(i);
						break;
					}
				}
			}
		}
		
		return cubeIds;
	}

	/*
	public int getCubeId(double[] pt)
	{
		double x = pt[0];
		double y = pt[1];
		double z = pt[2];
		
		return (int)Math.floor((x - erosBB.xmin) / cubeSize) +
		(int)Math.floor((y - erosBB.ymin) / cubeSize)*numCubesX +
		(int)Math.floor((z - erosBB.zmin) / cubeSize)*numCubesX*numCubesY; 
	}
	*/
	
	static private final int[] erosIntersectingCubes = {
			124,125,126,153,154,155,156,157,158,159,183,184,185,186,187,
			188,189,190,191,192,211,212,213,214,215,216,217,218,219,220,
			221,222,223,224,225,240,241,242,243,244,245,246,247,248,249,
			250,251,252,253,254,255,256,257,273,274,275,276,277,278,279,
			280,281,282,283,284,285,286,287,288,289,306,307,308,309,310,
			311,312,313,314,315,316,317,318,319,320,321,322,339,340,341,
			342,343,344,345,346,347,348,349,350,351,352,353,354,377,378,
			379,380,381,382,383,384,385,412,413,414,415,416,417,418,651,
			652,653,654,655,656,682,683,684,685,686,687,688,689,712,713,
			714,715,716,717,718,720,721,722,739,740,741,742,743,744,745,
			746,747,748,752,753,754,755,763,764,765,766,767,768,769,770,
			771,772,773,774,775,776,777,778,779,785,786,787,788,796,797,
			798,799,800,801,802,803,804,805,817,818,819,820,829,830,831,
			832,833,834,850,851,852,853,862,863,864,865,866,867,868,869,
			882,883,884,885,896,897,898,899,900,901,902,903,904,905,913,
			914,915,916,917,931,932,933,934,935,936,937,938,939,940,944,
			945,946,947,968,969,970,971,972,973,974,975,976,977,978,979,
			1002,1003,1004,1005,1006,1007,1008,1009,1037,1038,1039,1150,1179,1180,1181,
			1182,1183,1184,1185,1210,1211,1212,1213,1214,1215,1216,1217,1218,1219,1241,
			1242,1243,1244,1245,1250,1251,1252,1270,1271,1272,1273,1274,1275,1276,1283,
			1284,1285,1288,1289,1290,1291,1292,1293,1294,1295,1296,1297,1298,1299,1300,
			1301,1302,1303,1304,1305,1306,1307,1315,1316,1317,1321,1322,1323,1324,1325,
			1326,1327,1328,1329,1330,1331,1332,1333,1334,1335,1348,1349,1350,1354,1355,
			1356,1357,1359,1360,1380,1381,1382,1383,1387,1388,1389,1390,1391,1413,1414,
			1415,1421,1422,1423,1424,1425,1444,1445,1446,1447,1448,1455,1456,1457,1458,
			1459,1460,1475,1476,1477,1478,1479,1480,1481,1489,1490,1491,1492,1493,1494,
			1495,1496,1507,1508,1509,1524,1525,1526,1527,1528,1529,1530,1531,1537,1538,
			1539,1540,1541,1559,1560,1561,1562,1563,1564,1565,1566,1567,1568,1569,1570,
			1571,1572,1595,1596,1597,1598,1599,1600,1601,1630,1631,1632,1633,1709,1710,
			1711,1712,1713,1714,1740,1741,1742,1743,1744,1745,1746,1747,1770,1771,1772,
			1773,1774,1779,1780,1781,1800,1801,1802,1803,1804,1805,1812,1813,1814,1816,
			1817,1818,1819,1820,1828,1829,1830,1831,1832,1833,1834,1835,1844,1845,1846,
			1848,1849,1850,1851,1852,1853,1854,1855,1856,1857,1858,1859,1860,1861,1862,
			1863,1864,1865,1877,1878,1879,1881,1882,1883,1910,1911,1912,1914,1915,1916,
			1943,1944,1945,1947,1948,1949,1950,1976,1977,1978,1981,1982,1983,1984,2008,
			2009,2010,2015,2016,2017,2018,2037,2038,2039,2040,2041,2042,2043,2049,2050,
			2051,2052,2053,2068,2069,2070,2071,2072,2073,2074,2075,2083,2084,2085,2086,
			2087,2088,2098,2099,2100,2101,2102,2118,2119,2120,2121,2122,2123,2129,2130,
			2131,2132,2133,2153,2154,2155,2156,2157,2158,2159,2161,2162,2189,2190,2191,
			2192,2193,2194,2224,2225,2226,2269,2270,2271,2272,2273,2274,2275,2300,2301,
			2302,2303,2307,2308,2330,2331,2332,2333,2334,2341,2342,2361,2362,2363,2364,
			2374,2375,2376,2377,2378,2379,2380,2381,2382,2383,2384,2385,2386,2387,2388,
			2389,2390,2391,2392,2393,2394,2395,2407,2408,2409,2410,2414,2415,2416,2417,
			2418,2419,2420,2421,2422,2423,2424,2440,2441,2442,2473,2474,2475,2476,2505,
			2506,2507,2508,2509,2538,2539,2541,2542,2543,2570,2571,2572,2575,2576,2577,
			2599,2600,2602,2603,2604,2605,2609,2610,2611,2630,2631,2632,2633,2634,2635,
			2636,2637,2643,2644,2645,2646,2661,2662,2663,2664,2665,2678,2679,2680,2681,
			2682,2690,2691,2692,2693,2694,2695,2713,2714,2715,2716,2717,2722,2723,2749,
			2750,2751,2752,2753,2754,2755,2756,2783,2784,2785,2786,2787,2788,2829,2830,
			2831,2832,2833,2834,2835,2836,2860,2861,2862,2863,2869,2870,2891,2892,2893,
			2894,2902,2903,2922,2923,2924,2925,2936,2937,2938,2939,2940,2941,2942,2943,
			2944,2945,2946,2947,2948,2949,2950,2951,2952,2953,2954,2955,2969,2970,2971,
			3002,3003,3034,3035,3036,3067,3068,3069,3070,3100,3101,3102,3103,3132,3133,
			3136,3137,3165,3166,3170,3171,3192,3193,3194,3195,3196,3197,3198,3203,3204,
			3205,3206,3220,3221,3222,3223,3224,3225,3226,3227,3228,3229,3238,3239,3240,
			3241,3251,3252,3253,3254,3255,3256,3257,3258,3274,3275,3276,3277,3283,3284,
			3285,3309,3310,3311,3312,3315,3316,3317,3344,3345,3346,3347,3348,3349,3390,
			3391,3392,3393,3394,3395,3396,3397,3421,3422,3423,3424,3429,3430,3431,3451,
			3452,3453,3454,3455,3464,3483,3484,3485,3497,3498,3499,3500,3501,3502,3503,
			3504,3505,3506,3507,3508,3509,3510,3511,3512,3513,3514,3515,3516,3517,3530,
			3531,3532,3547,3548,3549,3563,3564,3595,3596,3597,3628,3629,3630,3661,3663,
			3664,3693,3694,3697,3698,3725,3726,3727,3730,3731,3732,3757,3758,3759,3764,
			3765,3766,3767,3780,3781,3782,3783,3785,3786,3787,3788,3789,3790,3799,3800,
			3801,3802,3803,3812,3813,3814,3815,3816,3817,3818,3819,3820,3821,3822,3835,
			3836,3837,3838,3844,3845,3870,3871,3872,3873,3876,3877,3878,3905,3906,3907,
			3908,3909,3910,3951,3952,3953,3954,3955,3956,3957,3958,3982,3983,3984,3985,
			3990,3991,3992,4013,4014,4015,4016,4024,4025,4045,4046,4047,4048,4058,4060,
			4061,4062,4063,4064,4065,4066,4067,4068,4069,4070,4071,4072,4073,4074,4075,
			4077,4078,4090,4091,4092,4093,4094,4095,4099,4100,4101,4102,4103,4104,4105,
			4106,4107,4108,4109,4110,4111,4123,4124,4125,4156,4157,4158,4188,4189,4190,
			4191,4192,4221,4222,4224,4225,4253,4254,4255,4258,4259,4285,4286,4287,4292,
			4293,4294,4317,4318,4319,4320,4326,4327,4328,4329,4330,4341,4342,4343,4344,
			4345,4348,4349,4350,4351,4361,4362,4363,4364,4365,4372,4373,4374,4376,4377,
			4378,4379,4380,4381,4382,4383,4397,4398,4399,4400,4404,4405,4406,4431,4432,
			4433,4434,4435,4436,4437,4438,4467,4468,4469,4470,4513,4514,4515,4516,4517,
			4518,4519,4544,4545,4546,4547,4548,4551,4552,4553,4575,4576,4577,4578,4579,
			4585,4586,4606,4607,4608,4609,4610,4618,4619,4623,4624,4625,4626,4627,4628,
			4629,4634,4635,4638,4639,4640,4641,4651,4652,4653,4654,4655,4656,4657,4658,
			4659,4660,4661,4662,4663,4664,4665,4666,4667,4668,4669,4670,4671,4672,4683,
			4684,4685,4686,4687,4688,4690,4691,4716,4717,4719,4720,4748,4749,4750,4752,
			4753,4781,4782,4785,4786,4787,4813,4814,4815,4819,4820,4821,4845,4846,4847,
			4853,4854,4855,4856,4857,4877,4878,4879,4887,4888,4889,4890,4891,4892,4893,
			4901,4902,4903,4904,4905,4906,4907,4908,4909,4910,4911,4912,4923,4924,4925,
			4926,4927,4928,4933,4934,4935,4939,4940,4941,4942,4943,4958,4959,4960,4961,
			4962,4963,4964,4965,4966,4967,4994,4995,4996,4997,4998,4999,5029,5030,5075,
			5076,5077,5078,5079,5106,5107,5108,5109,5110,5111,5112,5113,5138,5139,5140,
			5141,5145,5146,5169,5170,5171,5172,5173,5178,5179,5199,5200,5201,5202,5203,
			5204,5211,5212,5215,5216,5217,5218,5219,5220,5221,5222,5223,5224,5225,5226,
			5227,5228,5229,5230,5231,5232,5233,5234,5235,5236,5243,5244,5245,5247,5248,
			5249,5250,5251,5252,5253,5254,5255,5256,5257,5258,5259,5260,5261,5262,5263,
			5275,5276,5277,5278,5280,5281,5282,5283,5308,5309,5310,5314,5315,5340,5341,
			5342,5347,5348,5349,5350,5372,5373,5374,5375,5381,5382,5383,5384,5404,5405,
			5406,5407,5415,5416,5417,5418,5419,5420,5431,5432,5433,5434,5435,5436,5437,
			5438,5439,5450,5451,5452,5453,5454,5455,5456,5457,5461,5462,5463,5464,5465,
			5466,5467,5468,5469,5470,5471,5487,5488,5489,5490,5491,5492,5493,5494,5495,
			5501,5502,5503,5521,5522,5523,5524,5525,5526,5527,5558,5559,5669,5670,5671,
			5672,5673,5700,5701,5702,5703,5704,5705,5706,5732,5733,5734,5735,5736,5738,
			5739,5763,5764,5765,5766,5767,5771,5772,5790,5791,5792,5793,5794,5795,5796,
			5797,5798,5799,5803,5804,5805,5811,5812,5813,5814,5815,5816,5817,5818,5819,
			5820,5821,5822,5823,5824,5825,5826,5827,5828,5829,5830,5836,5837,5838,5843,
			5844,5845,5846,5847,5848,5849,5850,5851,5852,5853,5854,5855,5856,5857,5858,
			5859,5860,5861,5862,5868,5869,5870,5876,5877,5878,5879,5880,5881,5882,5883,
			5884,5899,5900,5901,5902,5909,5910,5911,5912,5913,5931,5932,5933,5934,5943,
			5944,5945,5946,5947,5948,5962,5963,5964,5965,5966,5978,5979,5980,5981,5982,
			5983,5984,5987,5988,5990,5991,5992,5993,5994,5995,5996,5997,5998,6014,6015,
			6016,6017,6018,6019,6020,6021,6022,6023,6024,6025,6026,6028,6029,6050,6051,
			6052,6053,6054,6055,6056,6263,6264,6265,6266,6295,6296,6297,6298,6299,6326,
			6327,6328,6329,6330,6331,6332,6358,6359,6360,6361,6362,6363,6364,6365,6390,
			6391,6392,6393,6394,6395,6396,6397,6398,6407,6408,6409,6410,6411,6412,6413,
			6414,6415,6416,6417,6418,6419,6420,6421,6422,6423,6424,6425,6426,6427,6428,
			6429,6430,6440,6441,6442,6443,6444,6445,6446,6447,6448,6449,6450,6451,6452,
			6453,6454,6455,6456,6457,6458,6459,6460,6461,6462,6473,6474,6475,6476,6477,
			6478,6479,6480,6481,6482,6483,6484,6485,6486,6487,6488,6489,6490,6491,6492,
			6493,6494,6507,6508,6509,6510,6511,6512,6513,6514,6515,6516,6517,6518,6519,
			6520,6521,6522,6523,6524,6525,6541,6542,6543,6544,6545,6546,6547,6548,6549,
			6550,6551,6552,6553,6554,6555,6556,6557,6578,6579,6580,6581,6582,6583,6584,
			6585};
}

