package edu.jhuapl.near.model;

public class ModelFactory
{
	static public SmallBodyModel createErosBodyModel()
	{
		final String[] modelNames = {
				"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver64q",
				"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver128q",
				"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver256q",
				"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver512q"
		};

		final String[] modelFiles = {
				"/edu/jhuapl/near/data/Eros_ver64q.vtk",
				"/EROS/ver128q.vtk.gz",
				"/EROS/ver256q.vtk.gz",
				"/EROS/ver512q.vtk.gz"
		};

		final String[] coloringFiles = {
				"/EROS/Eros_Dec2006_0_Elevation_PointData.txt.gz",
				"/EROS/Eros_Dec2006_0_GravitationalAcceleration_PointData.txt.gz",
				"/EROS/Eros_Dec2006_0_GravitationalPotential_PointData.txt.gz",
				"/EROS/Eros_Dec2006_0_Slope_PointData.txt.gz"
		};

		return new SmallBodyModel(modelNames, modelFiles, coloringFiles, null, true);
	}

	static public SmallBodyModel createDeimosBodyModel()
	{
		final String[] modelNames = {
				"DEIMOS"
		};

		final String[] modelFiles = {
				"/DEIMOS/DEIMOS.vtk.gz"
		};

		final String[] coloringFiles = {
				"/DEIMOS/DEIMOS_Elevation.txt.gz",
				"/DEIMOS/DEIMOS_GravitationalAcceleration.txt.gz",
				"/DEIMOS/DEIMOS_GravitationalPotential.txt.gz",
				"/DEIMOS/DEIMOS_Slope.txt.gz"
		};

		final String imageMap = "/DEIMOS/deimos_image_map.png";

		return new SmallBodyModel(modelNames, modelFiles, coloringFiles, imageMap, false);
	}
	
	static public SmallBodyModel createItokawaBodyModel()
	{
		final String[] modelNames = {
				"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver64q",
				"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver128q",
				"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver256q",
				"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver512q"
		};

		final String[] modelFiles = {
				"/ITOKAWA/ver64q.vtk.gz",
				"/ITOKAWA/ver128q.vtk.gz",
				"/ITOKAWA/ver256q.vtk.gz",
				"/ITOKAWA/ver512q.vtk.gz"
		};

		final String[] coloringFiles = null;

		return new SmallBodyModel(modelNames, modelFiles, coloringFiles, null, false);
	}

	static public SmallBodyModel createVestaBodyModel()
	{
		final String[] modelNames = {
				"VESTA"
		};

		final String[] modelFiles = {
				"/VESTA/vesta.vtk.gz"
		};

		final String[] coloringFiles = null;

		final String imageMap = null;

		return new SmallBodyModel(modelNames, modelFiles, coloringFiles, imageMap, false);
	}
	
	static public Graticule createErosGraticuleModel(SmallBodyModel smallBodyModel)
	{
		final String[] gridFiles = {
				"/EROS/coordinate_grid_res0.vtk.gz",
				"/EROS/coordinate_grid_res1.vtk.gz",
				"/EROS/coordinate_grid_res2.vtk.gz",
				"/EROS/coordinate_grid_res3.vtk.gz"
		};

		return new Graticule(smallBodyModel, gridFiles);
	}

	static public Graticule createDeimosGraticuleModel(SmallBodyModel smallBodyModel)
	{
		final String[] gridFiles = {
				"/DEIMOS/coordinate_grid_res0.vtk.gz"
		};

		return new Graticule(smallBodyModel, gridFiles);
	}

	static public Graticule createItokawaGraticuleModel(SmallBodyModel smallBodyModel)
	{
		final String[] gridFiles = {
				"/ITOKAWA/coordinate_grid_res0.vtk.gz",
				"/ITOKAWA/coordinate_grid_res1.vtk.gz",
				"/ITOKAWA/coordinate_grid_res2.vtk.gz",
				"/ITOKAWA/coordinate_grid_res3.vtk.gz"
		};

		return new Graticule(smallBodyModel, gridFiles);
	}

	static public Graticule createVestaGraticuleModel(SmallBodyModel smallBodyModel)
	{
		final String[] gridFiles = {
				"/VESTA/coordinate_grid_res0.vtk.gz"
		};

		return new Graticule(smallBodyModel, gridFiles);
	}

}
