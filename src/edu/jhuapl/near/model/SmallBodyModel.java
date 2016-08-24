package edu.jhuapl.near.model;

import vtk.vtkPolyData;

public class SmallBodyModel extends GenericPolyhedralModel
{
    /**
     * Default constructor. Must be followed by a call to setSmallBodyPolyData.
     */
    public SmallBodyModel()
    {
        super();
    }

    /**
     * Convenience method for initializing a SmallBodyModel with just a vtkPolyData.
     * @param polyData
     */
    public SmallBodyModel(vtkPolyData polyData)
    {
        super(polyData);
    }

    /**
     * Note that name is used to name this small body model as a whole including all
     * resolution levels whereas modelNames is an array of names that is specific
     * for each resolution level.
     */
    public SmallBodyModel(
            SmallBodyConfig config,
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
}
