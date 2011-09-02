package edu.jhuapl.near.model.deimos;

import edu.jhuapl.near.model.SmallBodyModel;

public class Deimos extends SmallBodyModel
{
    static private final String[] modelNames = {
            "DEIMOS"
    };

    static private final String[] modelFiles = {
            "/DEIMOS/DEIMOS.vtk.gz"
    };

    static private final String[] coloringFiles = {
            "/DEIMOS/DEIMOS_Slope",
            "/DEIMOS/DEIMOS_Elevation",
            "/DEIMOS/DEIMOS_GravitationalAcceleration",
            "/DEIMOS/DEIMOS_GravitationalPotential"
    };

    static private final String[] imageMap = {
        "/DEIMOS/deimos_image_map.png"
    };

    static private final String[] coloringNames = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public Deimos()
    {
        super(
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
}
