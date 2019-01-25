package edu.jhuapl.sbmt.client;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.sbmt.model.image.Instrument;

public class SmallBodyModel extends GenericPolyhedralModel
{
    private static final String[] DEFAULT_COLORING_NAMES = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr
    };
    private static final String[] DEFAULT_COLORING_UNITS = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };
    private static final ColoringValueType DEFAULT_COLORING_VALUE_TYPE = ColoringValueType.CELLDATA;

	public SmallBodyViewConfig getSmallBodyConfig()
    {
        return (SmallBodyViewConfig)getConfig();
    }

	public String serverPath(String fileName)
    {
        return getSmallBodyConfig().serverPath(fileName);
    }

    public String serverPath(String fileName, Instrument instrument)
    {
        return getSmallBodyConfig().serverPath(fileName, instrument);
    }

    /**
     * Default constructor. Must be followed by a call to setSmallBodyPolyData.
     */
    public SmallBodyModel(String uniqueModelId)
    {
        super(uniqueModelId);
    }

    /**
     * Convenience method for initializing a SmallBodyModel with just a vtkPolyData.
     * @param polyData
     */
    public SmallBodyModel(String uniqueModelId, vtkPolyData polyData)
    {
        super(uniqueModelId, polyData);
    }

    public SmallBodyModel(ViewConfig config)
    {
        super(config);
    }

    /**
     * Note that name is used to name this small body model as a whole including all
     * resolution levels whereas modelNames is an array of names that is specific
     * for each resolution level.
     */
    public SmallBodyModel(
            BodyViewConfig config,
            String[] modelNames,
            String[] modelFiles,
            String[] coloringFiles,
            String[] coloringNames,
            String[] coloringUnits,
            boolean[] coloringHasNulls,
            String[] imageMapNames,
            ColoringValueType coloringValueType,
            boolean lowestResolutionModelStoredInResource)
    {
        super(config, modelNames, modelFiles, coloringFiles, coloringNames, coloringUnits, coloringHasNulls, imageMapNames, coloringValueType, lowestResolutionModelStoredInResource);

//        if (lowestResolutionModelStoredInResource)
//            defaultModelFile = ConvertResourceToFile.convertResourceToRealFile(
//                    this,
//                    modelFiles[0],
//                    Configuration.getApplicationDataDir());
//        else
//            defaultModelFile = FileCache.getFileFromServer(modelFiles[0]);
//
//        initialize(defaultModelFile);
    }

    protected void initializeConfigParameters(
            String[] imageMapNames,
            boolean lowestResolutionModelStoredInResource)
    {
        SmallBodyViewConfig config = getSmallBodyConfig();
        String [] modelFiles = config.getShapeModelFileNames();

        initializeConfigParameters(
                modelFiles,
                imageMapNames,
                lowestResolutionModelStoredInResource);
    }

    protected void initializeConfigParameters(
            String[] modelFiles,
            String[] imageMapNames,
            boolean lowestResolutionModelStoredInResource)
    {
        SmallBodyViewConfig config = getSmallBodyConfig();
        final String[] coloringFiles = {
                serverPath("coloring/Slope"),
                serverPath("coloring/Elevation"),
                serverPath("coloring/GravitationalAcceleration"),
                serverPath("coloring/GravitationalPotential")
        };
        final boolean[] coloringHasNulls = null;
        initializeConfigParameters(
                modelFiles,
                config.hasColoringData ? coloringFiles : null,
                config.hasColoringData ? DEFAULT_COLORING_NAMES : null,
                config.hasColoringData ? DEFAULT_COLORING_UNITS : null,
                coloringHasNulls,
                imageMapNames,
                config.hasColoringData ? DEFAULT_COLORING_VALUE_TYPE : null,
                lowestResolutionModelStoredInResource);
    }

}
