package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkAppendPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkCone;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkImplicitBoolean;
import vtk.vtkImplicitFunction;
import vtk.vtkPNGReader;
import vtk.vtkPlane;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTexture;
import vtk.vtkTransform;

import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class SmallBodyImageMap extends Model
{
    private vtkPolyData imageMapPolyData;
    private vtkActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;
    private ArrayList<vtkProp> smallBodyActors = new ArrayList<vtkProp>();
    private double imageMapOpacity = 1.0;
    private SmallBodyModel smallBodyModel;
    private vtkTexture imageMapTexture;
    private boolean initialized = false;


    public SmallBodyImageMap(
            SmallBodyModel smallBodyModel)
    {
        super(ModelNames.SMALL_BODY);
        this.smallBodyModel = smallBodyModel;

        imageMapPolyData = new vtkPolyData();

        setVisible(false);
    }

    private void initialize()
    {
        if (initialized)
            return;

        vtkPolyData smallBodyPolyData = smallBodyModel.getSmallBodyPolyData();

        double[] origin = {0.0, 0.0, 0.0};
        double[] zaxis = {0.0, 0.0, 1.0};

        double[] corners = loadTextureCorners();
        double lllat = corners[0];
        double lllon = corners[1];
        double urlat = corners[2];
        double urlon = corners[3];

        if (lllat != -90.0 || lllon != 0.0 || urlat != 90.0 || urlon != 360.0)
        {
            lllon *= (Math.PI / 180.0);
            urlon *= (Math.PI / 180.0);
            lllon -= (Math.PI);
            urlon -= (Math.PI);

            // If the texture does not cover the entire body, cut out from the body
            // the part that it covers

            double[] vec = MathUtil.latrec(new LatLon(0.0, lllon, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);

            vtkPlane plane1 = new vtkPlane();
            plane1.SetOrigin(origin);
            plane1.SetNormal(normal);

            vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
            clipPolyData1.SetClipFunction(plane1);
            clipPolyData1.SetInput(smallBodyPolyData);
            vtkAlgorithmOutput clipPolyData1Output = clipPolyData1.GetOutputPort();

            vec = MathUtil.latrec(new LatLon(0.0, urlon, 1.0));
            MathUtil.vcrss(vec, zaxis, normal);

            vtkPlane plane2 = new vtkPlane();
            plane2.SetOrigin(origin);
            plane2.SetNormal(normal);

            vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
            clipPolyData2.SetClipFunction(plane2);
            clipPolyData2.SetInputConnection(clipPolyData1Output);
            clipPolyData2.SetInsideOut(1);
            vtkAlgorithmOutput clipPolyData2Output = clipPolyData2.GetOutputPort();



            double[] yaxis = {0.0, 1.0, 0.0};
            vtkTransform transform = new vtkTransform();
            transform.Identity();
            transform.RotateWXYZ(90.0, yaxis);

            vtkPlane zeroLatPlane = new vtkPlane();
            zeroLatPlane.SetOrigin(origin);
            zeroLatPlane.SetNormal(zaxis);

            vtkImplicitFunction clipFunction1 = null;
            if (lllat == 0.0)
            {
                clipFunction1 = zeroLatPlane;
            }
            else
            {
                vtkCone cone = new vtkCone();
                cone.SetTransform(transform);
                cone.SetAngle(90 - Math.abs(lllat));

                vtkImplicitBoolean implicitBoolean = new vtkImplicitBoolean();
                implicitBoolean.SetOperationTypeToIntersection();
                implicitBoolean.AddFunction(zeroLatPlane);
                implicitBoolean.AddFunction(cone);

                clipFunction1 = implicitBoolean;
            }

            vtkClipPolyData clipPolyData3 = new vtkClipPolyData();
            clipPolyData3.SetClipFunction(clipFunction1);
            clipPolyData3.SetInputConnection(clipPolyData2Output);
            vtkAlgorithmOutput clipPolyData3Output = clipPolyData3.GetOutputPort();

            double[] mzaxis = {0.0, 0.0, -1.0};
            vtkPlane mzeroLatPlane = new vtkPlane();
            mzeroLatPlane.SetOrigin(origin);
            mzeroLatPlane.SetNormal(mzaxis);

            vtkImplicitFunction clipFunction2 = null;
            if (urlat == 0.0)
            {
                clipFunction2 = mzeroLatPlane;
            }
            else
            {
                vtkCone cone = new vtkCone();
                cone.SetTransform(transform);
                cone.SetAngle(90 - Math.abs(urlat));

                vtkImplicitBoolean implicitBoolean = new vtkImplicitBoolean();
                implicitBoolean.SetOperationTypeToIntersection();
                implicitBoolean.AddFunction(mzeroLatPlane);
                implicitBoolean.AddFunction(cone);

                clipFunction2 = implicitBoolean;
            }

            vtkClipPolyData clipPolyData4 = new vtkClipPolyData();
            clipPolyData4.SetClipFunction(clipFunction2);
            clipPolyData4.SetInputConnection(clipPolyData3Output);
            clipPolyData4.Update();

            smallBodyPolyData = clipPolyData4.GetOutput();
        }

        // Now divide the above along the zero longitude line and compute
        // texture coordinates for each half separately, then combine the two.
        // This avoids having a "seam" at the zero longitude line.

        double[] vec = MathUtil.latrec(new LatLon(0.0, 0.0, 1.0));
        double[] normal = new double[3];
        MathUtil.vcrss(vec, zaxis, normal);

        vtkPlane plane1 = new vtkPlane();
        plane1.SetOrigin(origin);
        plane1.SetNormal(normal);

        vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
        clipPolyData1.SetClipFunction(plane1);
        clipPolyData1.SetInput(smallBodyPolyData);
        clipPolyData1.Update();
        vtkPolyData clipPolyData1Output = clipPolyData1.GetOutput();

        generateTextureCoordinates(clipPolyData1Output, true);



        vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
        clipPolyData2.SetClipFunction(plane1);
        clipPolyData2.SetInput(smallBodyPolyData);
        clipPolyData2.SetInsideOut(1);
        clipPolyData2.Update();
        vtkPolyData clipPolyData2Output = clipPolyData2.GetOutput();

        generateTextureCoordinates(clipPolyData2Output, false);



        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        appendFilter.UserManagedInputsOn();
        appendFilter.SetNumberOfInputs(2);
        appendFilter.SetInputByNumber(0, clipPolyData1Output);
        appendFilter.SetInputByNumber(1, clipPolyData2Output);
        appendFilter.Update();

        vtkPolyData appendFilterOutput = appendFilter.GetOutput();
        imageMapPolyData.DeepCopy(appendFilterOutput);

        PolyDataUtil.shiftPolyDataInNormalDirection(imageMapPolyData, 2.0*smallBodyModel.getMinShiftAmount());

        initialized = true;
    }

    private double[] loadTextureCorners()
    {
        try
        {
            // Load in the corners.txt file
            String cornersFilename = Configuration.getImportedShapeModelsDir() +
                    File.separator +
                    smallBodyModel.getModelName() +
                    File.separator +
                    "corners.txt";

            ArrayList<String> words = FileUtil.getFileWordsAsStringList(cornersFilename);
            double lllat = Double.parseDouble(words.get(0));
            double lllon = Double.parseDouble(words.get(1));
            double urlat = Double.parseDouble(words.get(2));
            double urlon = Double.parseDouble(words.get(3));

            return new double[]{lllat, lllon, urlat, urlon};
        }
        catch (IOException ex)
        {
            // silently ignore
        }

        return new double[]{-90.0, 0.0, 90.0, 360.0};
    }

    protected void generateTextureCoordinates(vtkPolyData polydata, boolean mapZeroLongitudeToRight)
    {
        double[] corners = loadTextureCorners();
        double lllat = corners[0];
        double lllon = corners[1];
        double urlat = corners[2];
        double urlon = corners[3];

        lllat *= (Math.PI / 180.0);
        lllon *= (Math.PI / 180.0);
        urlat *= (Math.PI / 180.0);
        urlon *= (Math.PI / 180.0);

        vtkFloatArray textureCoords = new vtkFloatArray();

        int numberOfPoints = polydata.GetNumberOfPoints();

        textureCoords.SetNumberOfComponents(2);
        textureCoords.SetNumberOfTuples(numberOfPoints);

        vtkPoints points = polydata.GetPoints();

        double xsize = urlon - lllon;
        if (xsize < 0.0)
            xsize += 2.0 * Math.PI;
        double ysize = urlat - lllat;

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            LatLon ll = MathUtil.reclat(pt);

            if (ll.lon < 0.0)
               ll.lon += (2.0 * Math.PI);
            if (ll.lon >= 2.0 * Math.PI)
                ll.lon = 0.0;

            double u = 0.0;
            double v = 0.0;

            double dlon = ll.lon - lllon;
            if (dlon < 0.0)
                dlon += 2.0 * Math.PI;

            u = dlon / xsize;
            v = (ll.lat - lllat) / ysize;

            if (u < 0.0) u = 0.0;
            else if (u > 1.0) u = 1.0;
            if (v < 0.0) v = 0.0;
            else if (v > 1.0) v = 1.0;

            if (ll.lon == 0.0 && u == 0.0 && mapZeroLongitudeToRight)
                u = 1.0;

            textureCoords.SetTuple2(i, u, v);
        }

        polydata.GetPointData().SetTCoords(textureCoords);
    }

    public ArrayList<vtkProp> getProps()
    {
        if (smallBodyActor == null)
        {
            smallBodyMapper = new vtkPolyDataMapper();
            smallBodyMapper.ScalarVisibilityOff();
            smallBodyMapper.SetScalarModeToDefault();
            smallBodyMapper.SetInput(imageMapPolyData);
            smallBodyMapper.Update();

            imageMapTexture = new vtkTexture();
            imageMapTexture.InterpolateOn();
            imageMapTexture.RepeatOff();
            imageMapTexture.EdgeClampOn();
            vtkImageData image = loadImageMap(smallBodyModel.getImageMapName());
            imageMapTexture.SetInput(image);

            smallBodyActor = new vtkActor();
            smallBodyActor.SetMapper(smallBodyMapper);
            smallBodyActor.SetTexture(imageMapTexture);

            smallBodyActors.add(smallBodyActor);
        }

        return smallBodyActors;
    }

    public boolean isImageMapAvailable()
    {
        return smallBodyModel.getImageMapName() != null;
    }

    public void setShowImageMap(boolean b)
    {
        if (b)
            initialize();

        super.setVisible(b);
    }

    public double getImageMapOpacity()
    {
        return imageMapOpacity;
    }

    public void setImageMapOpacity(double imageMapOpacity)
    {
        this.imageMapOpacity = imageMapOpacity;
        vtkProperty smallBodyProperty = smallBodyActor.GetProperty();
        smallBodyProperty.SetOpacity(imageMapOpacity);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    protected vtkImageData loadImageMap(String name)
    {
        File imageFile = FileCache.getFileFromServer(name);
        vtkPNGReader reader = new vtkPNGReader();
        reader.SetFileName(imageFile.getAbsolutePath());
        reader.Update();
        return reader.GetOutput();
    }

    public void delete()
    {
    }
}
