package edu.jhuapl.near.model;

import java.util.ArrayList;

public class ExampleConfig extends PolyhedralModelConfig
{
    static public ExampleConfig getExampleConfig(ShapeModelBody name, ShapeModelAuthor author)
    {
        return (ExampleConfig)getPolyhedralModelConfig(name, author, null);
    }

    static public ExampleConfig getExampleConfig(ShapeModelBody name, ShapeModelAuthor author, String version)
    {
        return (ExampleConfig)getPolyhedralModelConfig(name, author, version);
    }

    public static void initialize()
    {
        ArrayList<Config> configArray = getBuiltInConfigs();

        // Gaskell Eros
//        ExampleConfig c = new ExampleConfig();
//        c.body = ShapeModelBody.EROS;
//        c.type = ShapeModelType.ASTEROID;
//        c.population = ShapeModelPopulation.NEO;
//        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//        c.author = ShapeModelAuthor.GASKELL;
//        c.rootDirOnServer = "/GASKELL/EROS";
//        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
//        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
//        c.hasImageMap = false;
//
//        c.hasLidarData = true;
//        c.hasSpectralData = true;
//        c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime();
//        c.lidarSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
//        c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
//        c.lidarSearchDataSourceMap.put("Default", "/NLR/cubes");
//        c.lidarBrowseXYZIndices = new int[]{14, 15, 16};
//        c.lidarBrowseSpacecraftIndices = new int[]{8, 9, 10};
//        c.lidarBrowseIsSpacecraftInSphericalCoordinates = true;
//        c.lidarBrowseTimeIndex = 4;
//        c.lidarBrowseNoiseIndex = 7;
//        c.lidarBrowseFileListResourcePath = "/edu/jhuapl/near/data/NlrFiles.txt";
//        c.lidarBrowseNumberHeaderLines = 2;
//        c.lidarBrowseIsInMeters = true;
//        c.lidarOffsetScale = 0.025;
//        c.lidarInstrumentName = Instrument.NLR;
//        configArray.add(c);

    }

    public ExampleConfig clone() // throws CloneNotSupportedException
    {
        ExampleConfig c = (ExampleConfig)super.clone();

        return c;
    }

}
