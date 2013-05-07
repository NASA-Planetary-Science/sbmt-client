package edu.jhuapl.near.model.deimos;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;

public class Deimos extends SmallBodyModel
{
    public static final String NAME = ModelFactory.DEIMOS;
    public static final String AUTHOR = ModelFactory.THOMAS;

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
        super(NAME,
                AUTHOR,
                new String[]{NAME.toUpperCase()},
                modelFiles,
                coloringFiles,
                coloringNames,
                coloringUnits,
                null,
                imageMap,
                ColoringValueType.CELLDATA,
                false);
    }
}
