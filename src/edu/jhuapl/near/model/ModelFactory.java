package edu.jhuapl.near.model;

import edu.jhuapl.near.model.SmallBodyModel.ColoringValueType;

public class ModelFactory
{
	static private final String SlopeStr = "Slope";
	static private final String ElevStr = "Elevation";
	static private final String GravAccStr = "Gravitational Acceleration";
	static private final String GravPotStr = "Gravitational Potential";
	static private final String SlopeUnitsStr = "deg";
	static private final String ElevUnitsStr = "m";
	static private final String GravAccUnitsStr = "m/s^2";
	static private final String GravPotUnitsStr = "J/kg";

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
				"/EROS/Eros_Dec2006_0_Slope.txt.gz",
				"/EROS/Eros_Dec2006_0_Elevation.txt.gz",
				"/EROS/Eros_Dec2006_0_GravitationalAcceleration.txt.gz",
				"/EROS/Eros_Dec2006_0_GravitationalPotential.txt.gz"
		};

		final String[] coloringNames = {
				SlopeStr, ElevStr, GravAccStr, GravPotStr
		};

		final String[] coloringUnits = {
				SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
		};

		return new SmallBodyModel(
				modelNames,
				modelFiles,
				coloringFiles,
				coloringNames,
				coloringUnits,
				null,
				false,
				null,
				ColoringValueType.CELLDATA,
				true);
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
				"/DEIMOS/DEIMOS_Slope.txt.gz",
				"/DEIMOS/DEIMOS_Elevation.txt.gz",
				"/DEIMOS/DEIMOS_GravitationalAcceleration.txt.gz",
				"/DEIMOS/DEIMOS_GravitationalPotential.txt.gz"
		};

		final String imageMap = "/DEIMOS/deimos_image_map.png";

		final String[] coloringNames = {
				SlopeStr, ElevStr, GravAccStr, GravPotStr
		};

		final String[] coloringUnits = {
				SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
		};

		return new SmallBodyModel(
				modelNames,
				modelFiles,
				coloringFiles,
				coloringNames,
				coloringUnits,
				null,
				false,
				imageMap,
				ColoringValueType.CELLDATA,
				false);
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

		return new SmallBodyModel(modelNames, modelFiles, null, null, null, null, false, null, ColoringValueType.CELLDATA, false);
	}

	static public SmallBodyModel createVestaBodyModel()
	{
		final String[] modelNames = {
				"VESTA"
		};

		final String[] modelFiles = {
				"/VESTA/VESTA.vtk.gz"
		};

		final String[] coloringFiles = {
				"/VESTA/VESTA_Slope.txt.gz",
				"/VESTA/VESTA_Elevation.txt.gz",
				"/VESTA/VESTA_GravitationalAcceleration.txt.gz",
				"/VESTA/VESTA_GravitationalPotential.txt.gz",
				"/VESTA/VESTA_439.txt.gz",
				"/VESTA/VESTA_673.txt.gz",
				"/VESTA/VESTA_953.txt.gz",
				"/VESTA/VESTA_1042.txt.gz"
		};

		final String[] coloringNames = {
				SlopeStr, ElevStr, GravAccStr, GravPotStr, "439 nm", "673 nm", "953 nm", "1042 nm"
		};

		final String[] coloringUnits = {
				SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr, "", "", "", ""
		};

		final boolean[] coloringHasNulls = {
				false, false, false, false, true, true, true, true
		};

		return new SmallBodyModel(
				modelNames,
				modelFiles,
				coloringFiles,
				coloringNames,
				coloringUnits,
				coloringHasNulls,
				true,
				null,
				ColoringValueType.CELLDATA,
				false);
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
