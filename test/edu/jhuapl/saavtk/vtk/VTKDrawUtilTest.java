package edu.jhuapl.saavtk.vtk;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VTKDrawUtilTest
{

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	void testDrawEllipseOn()
	{
//		System.setProperty("edu.jhuapl.sbmt.mission", "TEST_APL_INTERNAL");
//		NativeLibraryLoader.loadVtkLibraries();
//		new SmallBodyMappingToolAPL();
//	    Mission.configureMission();
//	    SbmtMultiMissionTool.getMission();
//
//	    // basic default configuration, most of these will be overwritten by the configureMission() method
//	    Configuration.setAPLVersion(true);
//	    Configuration.setRootURL("https://sbmt.jhuapl.edu/sbmt/prod");
//
//	    // authentication
//	    Configuration.authenticate();
//
//	    // initialize view config
//	    SmallBodyViewConfig.fromServer = true;
//
//	    SmallBodyViewConfig.initialize();
//
//	    SmallBodyViewConfig smallBodyConfig = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL);
//	    SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(smallBodyConfig);
//
//		Vector3D center = new Vector3D(-5.3972485027, 2.6357411578, 5.4450649162);
//		double flattening = 1.0;
//		double angle = 0.0;
//
//		// Determine the VTK vars to use (ensure low res data)
//		vtkPolyData vSurfacePD = smallBodyModel.getSmallBodyPolyDataAtPosition();
//		vtkPointLocator vSurfacePL = smallBodyModel.getPointLocator();
//
//		int resLevel = 0;
//		vtkPolyData interiorPoly = new vtkPolyData();
//
//		if (resLevel != 0)
//		{
//			smallBodyModel.initializeLowResData();
//
//			vSurfacePD = smallBodyModel.getLowResSmallBodyPolyData();
//			vSurfacePL = smallBodyModel.getPointLocator();
//		}
//		var aNumSides = 20;
//		var aRadius = 0.6183546034554288;
//
//		// Render the circle
//		VtkDrawUtil.drawEllipseOn(vSurfacePD, vSurfacePL, center, aRadius, flattening, angle, aNumSides,
//				interiorPoly, null);
//		double[] bounding = interiorPoly.GetBounds();
//
//		int resLevel1 = 0;
//		vtkPolyData interiorPoly1 = new vtkPolyData();
//
////		if (resLevel1 != 0)
////		{
////			smallBodyModel.initializeLowResData();
////
////			vSurfacePD = smallBodyModel.getLowResSmallBodyPolyData();
//////			vSurfacePD = smallBodyModel.getSmallBodyPolyDataAtPosition();
//////			assertEquals(smallBodyModel.getSmallBodyPolyDataAtPosition(), vSurfacePD);
////
////			vSurfacePL = smallBodyModel.getPointLocator();
////		}
//
//		// Render the circle
//		VtkDrawUtil.drawEllipseOn(vSurfacePD, vSurfacePL, center, aRadius, flattening, angle, aNumSides,
//				interiorPoly1, null);
//		double[] bounding1 = interiorPoly1.GetBounds();
//
//		//assert that the interior polys match
//		assertArrayEquals(bounding, bounding1);
////
////		int resLevel2 = 2;
////		vtkPolyData interiorPoly2 = new vtkPolyData();
////
////		if (resLevel2 != 0)
////		{
////			smallBodyModel.initializeLowResData();
////
////			vSurfacePD = smallBodyModel.getLowResSmallBodyPolyData();
////			vSurfacePL = smallBodyModel.getPointLocator();
////		}
//////		var aNumSides = 20;
//////		var aRadius = 0.6183546034554288;
////
////		// Render the circle
////		VtkDrawUtil.drawEllipseOn(vSurfacePD, vSurfacePL, center, aRadius, flattening, angle, aNumSides,
////				interiorPoly2, null);
////		double[] bounding2 = interiorPoly2.GetBounds();
////
////		assertArrayEquals(bounding, bounding2);
	}

	@Test
	void testDrawPathPolyOn()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDrawPathSimpleOn()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDrawPolygonOn()
	{
		fail("Not yet implemented");
	}

}
