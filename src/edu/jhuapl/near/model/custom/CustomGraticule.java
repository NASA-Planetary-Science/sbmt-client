package edu.jhuapl.near.model.custom;

import java.io.File;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.PolyhedralModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;

public class CustomGraticule extends Graticule
{
    public CustomGraticule(PolyhedralModel smallBodyModel)
    {
        super(smallBodyModel,
              new String[] { FileCache.FILE_PREFIX +
                Configuration.getImportedShapeModelsDir() +
                File.separator +
                smallBodyModel.getModelName() +
                File.separator +
                "grid.vtk" });
    }
}
