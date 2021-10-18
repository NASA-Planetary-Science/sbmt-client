package edu.jhuapl.sbmt.client;

import java.util.List;

import com.google.common.collect.ImmutableList;

import vtk.vtkPolyData;
import vtk.vtkTransformFilter;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.Instrument;

public class SmallBodyModel extends GenericPolyhedralModel implements ISmallBodyModel
{
    private static final String[] DEFAULT_COLORING_NAMES = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr
    };
    private static final String[] DEFAULT_COLORING_UNITS = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };
    private static final ColoringValueType DEFAULT_COLORING_VALUE_TYPE = ColoringValueType.CELLDATA;

    private final List<ImageKeyInterface> imageMapKeys;

	public ISmallBodyViewConfig getSmallBodyConfig()
    {
        return (ISmallBodyViewConfig)getConfig();
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
        this.imageMapKeys = ImmutableList.of();
    }

    /**
     * Convenience method for initializing a SmallBodyModel with just a vtkPolyData.
     * @param polyData
     */
    public SmallBodyModel(String uniqueModelId, vtkPolyData polyData)
    {
        super(uniqueModelId, polyData);
        this.imageMapKeys = ImmutableList.of();
    }

    public SmallBodyModel(BodyViewConfig config)
    {
        super(config);
        this.imageMapKeys = config.getImageMapKeys();
    }

    protected SmallBodyModel(
            BodyViewConfig config,
            String[] modelNames,
            String[] coloringFiles,
            String[] coloringNames,
            String[] coloringUnits,
            boolean[] coloringHasNulls,
            String[] imageMapNames,
            ColoringValueType coloringValueType,
            boolean lowestResolutionModelStoredInResource)
    {
        this(config, modelNames, config.getShapeModelFileNames(), coloringFiles, coloringNames, coloringUnits, coloringHasNulls, coloringValueType, lowestResolutionModelStoredInResource);
    }

    public List<ImageKeyInterface> getImageMapKeys()
    {
        return imageMapKeys;
    }

    /**
     * Note that name is used to name this small body model as a whole including all
     * resolution levels whereas modelNames is an array of names that is specific
     * for each resolution level.
     */
    private SmallBodyModel(
            BodyViewConfig config,
            String[] modelNames,
            String[] modelFiles,
            String[] coloringFiles,
            String[] coloringNames,
            String[] coloringUnits,
            boolean[] coloringHasNulls,
            ColoringValueType coloringValueType,
            boolean lowestResolutionModelStoredInResource)
    {
        super(config, modelNames, modelFiles, coloringFiles, coloringNames, coloringUnits, coloringHasNulls, coloringValueType, lowestResolutionModelStoredInResource);

        this.imageMapKeys = config.getImageMapKeys();
    }

    protected void initializeConfigParameters(
            String[] imageMapNames,
            boolean lowestResolutionModelStoredInResource)
    {
        SmallBodyViewConfig config = (SmallBodyViewConfig)getSmallBodyConfig();
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
        SmallBodyViewConfig config = (SmallBodyViewConfig)getSmallBodyConfig();
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
                config.hasColoringData ? DEFAULT_COLORING_VALUE_TYPE : null,
                lowestResolutionModelStoredInResource);
    }

    public void transformBody(vtkTransformFilter transformFilter)
    {
    	vtkPolyData polydata = transformFilter.GetPolyDataOutput();
    	setSmallBodyPolyData(polydata);
    }

}
