package edu.jhuapl.near.gui;

import java.io.File;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;
import vtk.vtkTransform;
import vtk.vtkTransformPolyDataFilter;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.MapUtil;
import edu.jhuapl.near.util.PolyDataUtil;

public class ShapeModelImporter
{
    /**
     * The file format of the shape model being imported
     */
    public enum FormatType
    {
        PDS,
        OBJ,
        VTK
    }

    /**
     * The type of shape model. If ELLIPSOID, and ellipsoid is generated
     * by this classes using the radii and resolution specified. If
     * FILE, the shape model is loaded from the file specified. If
     * POLYDATA, the specified vtkPolyData object containing the shape model
     * is used.
     */
    public enum ShapeModelType
    {
        ELLIPSOID,
        FILE,
        POLYDATA
    }

    private String name;
    private ShapeModelType shapeModelType = ShapeModelType.FILE;
    private double equRadius;
    private double polarRadius;
    private int resolution;
    private String modelPath;
    private FormatType format;
    private vtkPolyData shapeModelPolydata;

    public boolean importShapeModel(String[] errorMessage)
    {
        String validationErrorMessage = validateInput();
        if (validationErrorMessage != null)
        {
            errorMessage[0] = validationErrorMessage;
            return false;
        }

        LinkedHashMap<String, String> configMap = new LinkedHashMap<String, String>();

        configMap.put(CustomShapeModel.NAME, name);

        vtkPolyData shapePoly = null;

        // First either load a shape model from file or create ellipsoidal shape model
        if (shapeModelType == ShapeModelType.ELLIPSOID)
        {
            vtkSphereSource sphereSource = new vtkSphereSource();
            sphereSource.SetRadius(equRadius);
            sphereSource.SetCenter(0.0, 0.0, 0.0);
            sphereSource.SetLatLongTessellation(0);
            sphereSource.SetThetaResolution(resolution);
            sphereSource.SetPhiResolution(Math.max(3, resolution/2 + 1));
            sphereSource.Update();
            shapePoly = sphereSource.GetOutput();

            if (equRadius != polarRadius)
            {
                // Turn it into ellipsoid
                vtkTransformPolyDataFilter filter = new vtkTransformPolyDataFilter();
                filter.SetInputConnection(sphereSource.GetOutputPort());

                vtkTransform transform = new vtkTransform();
                transform.Scale(1.0, 1.0, polarRadius/equRadius);

                filter.SetTransform(transform);
                filter.Update();

                shapePoly.Delete();
                shapePoly = filter.GetOutput();
            }

            configMap.put(CustomShapeModel.TYPE, CustomShapeModel.ELLIPSOID);
            configMap.put(CustomShapeModel.EQUATORIAL_RADIUS, String.valueOf(equRadius));
            configMap.put(CustomShapeModel.POLAR_RADIUS, String.valueOf(polarRadius));
            configMap.put(CustomShapeModel.RESOLUTION, String.valueOf(resolution));
        }
        else if (shapeModelType == ShapeModelType.POLYDATA)
        {
            shapePoly = new vtkPolyData();
            shapePoly.DeepCopy(shapeModelPolydata);
        }
        else
        {
            configMap.put(CustomShapeModel.TYPE, CustomShapeModel.CUSTOM);
            configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_PATH, modelPath);

            if (format == FormatType.PDS)
            {
                try
                {
                    shapePoly = PolyDataUtil.loadPDSShapeModel(modelPath);
                }
                catch (Exception ex)
                {
                    errorMessage[0] = "The was an error loading " + modelPath + ".\nAre you sure you specified the right format?";
                    return false;
                }

                configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, CustomShapeModel.PDS_FORMAT);
            }
            else if (format == FormatType.OBJ)
            {
                try
                {
                    shapePoly = PolyDataUtil.loadOBJShapeModel(modelPath);
                }
                catch (Exception ex)
                {
                    errorMessage[0] = "The was an error loading " + modelPath + ".\nAre you sure you specified the right format?";
                    return false;
                }

                configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, CustomShapeModel.OBJ_FORMAT);
            }
            else if (format == FormatType.VTK)
            {
                try
                {
                    shapePoly = PolyDataUtil.loadVTKShapeModel(modelPath);
                }
                catch (Exception ex)
                {
                    errorMessage[0] = "The was an error loading " + modelPath + ".\nAre you sure you specified the right format?";
                    return false;
                }

                configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, CustomShapeModel.VTK_FORMAT);
            }
        }

        // Now save the shape model to the users home folder within the
        // custom-shape-models folders
        File newModelDir = new File(Configuration.getImportedShapeModelsDir() + File.separator + name);
        FileUtils.deleteQuietly(newModelDir);
        newModelDir.mkdirs();


        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInput(shapePoly);
        writer.SetFileName(newModelDir.getAbsolutePath() + File.separator + "model.vtk");
        writer.SetFileTypeToBinary();
        writer.Write();


        // Generate a graticule
        Graticule grid = new Graticule(null, null);
        grid.generateGrid(shapePoly);

        writer = new vtkPolyDataWriter();
        writer.SetInput(grid.getGridAsPolyData());
        writer.SetFileName(newModelDir.getAbsolutePath() + File.separator + "grid.vtk");
        writer.SetFileTypeToBinary();
        writer.Write();

        // Save out all information about this shape model to the config.txt file
        MapUtil map = new MapUtil(newModelDir.getAbsolutePath() + File.separator + "config.txt");
        map.put(configMap);

        return true;
    }

    private String validateInput()
    {
        if (name == null || name.trim().isEmpty())
            return "Please enter a name for the shape model.";

        // Make sure name is not empty and does not contain spaces or slashes
        if (name.contains("/") || name.contains("\\") || name.contains(" ") || name.contains("\t"))
            return "Name may not contain spaces or slashes.";

        // Check if name is already being used by another imported shape model.
        // Do not check in edit mode.
        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            for (File dir : dirs)
            {
                if (dir.getName().equalsIgnoreCase(name))
                    return "Name already exists.";
            }
        }

        if (shapeModelType == ShapeModelType.ELLIPSOID)
        {
            if (equRadius <= 0.0)
                return "Equatorial radius must be positive.";
            if (polarRadius <= 0.0)
                return "Polar radius must be positive.";
            if (resolution < 3 || resolution > 1024)
                return "Resolution may not be less than 3 or greater than 1024.";
        }
        else if (shapeModelType == ShapeModelType.FILE)
        {
            if (modelPath == null || modelPath.trim().isEmpty())
                return "Please enter the path to a shape model.";

            File file = new File(modelPath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return modelPath + " does not exist or is not readable.";
        }

        return null;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setShapeModelType(ShapeModelType type)
    {
        this.shapeModelType = type;
    }

    public void setEquRadius(double equRadius)
    {
        this.equRadius = equRadius;
    }

    public void setPolarRadius(double polarRadius)
    {
        this.polarRadius = polarRadius;
    }

    public void setResolution(int resolution)
    {
        this.resolution = resolution;
    }

    public void setModelPath(String modelPath)
    {
        this.modelPath = modelPath;
    }

    public void setFormat(FormatType format)
    {
        this.format = format;
    }

    public void setShapeModelPolydata(vtkPolyData polydata)
    {
        this.shapeModelPolydata = polydata;
    }

}
