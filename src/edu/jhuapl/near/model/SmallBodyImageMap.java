package edu.jhuapl.near.model;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkAppendPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkCone;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkPNGReader;
import vtk.vtkPlane;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTexture;
import vtk.vtkTransform;

import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class SmallBodyImageMap extends Model
{
    public static class ImageInfo
    {
        public String filename = "";
        public double lllat = -90.0;
        public double lllon = 0.0;
        public double urlat = 90.0;
        public double urlon = 360.0;

        @Override
        public String toString()
        {
            DecimalFormat df = new DecimalFormat("#.#####");
            return filename + "  ["
                    + df.format(lllat) + ", "
                    + df.format(lllon) + ", "
                    + df.format(urlat) + ", "
                    + df.format(urlon)
                    + "]";
        }
    }

    private vtkPolyData imageMapPolyData;
    private vtkPolyData shiftedImageMapPolyData;
    private vtkActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;
    private ArrayList<vtkProp> smallBodyActors = new ArrayList<vtkProp>();
    private double imageMapOpacity = 1.0;
    private SmallBodyModel smallBodyModel;
    private vtkTexture imageMapTexture;
    private boolean initialized = false;
    private ImageInfo imageInfo;
    private double offset;
    private boolean isShapeModelEllipsoid;

    public SmallBodyImageMap(
            SmallBodyModel smallBodyModel,
            ImageInfo imageInfo,
            boolean isShapeModelEllipsoid)
    {
        super(ModelNames.SMALL_BODY);
        this.smallBodyModel = smallBodyModel;
        this.imageInfo = imageInfo;
        this.isShapeModelEllipsoid = isShapeModelEllipsoid;

        imageMapPolyData = new vtkPolyData();
        shiftedImageMapPolyData = new vtkPolyData();

        this.offset = getDefaultOffset();

        setVisible(false);
    }

    /**
     * Clip out the area for the texture using a series of clipping planes
     * for the longitudes and generate texture coordinates (This works for
     * both ellipsoidal and non-ellipsoidal bodies). Unfortunately, this
     * function is very complicated.
     * @param smallBodyPolyData
     * @return the clipped out polydata
     */
    private vtkPolyData clipOutTextureLongitudeAndGenerateTextureCoordinates(vtkPolyData smallBodyPolyData)
    {
        // Divide along the zero longitude line and compute
        // texture coordinates for each half separately, then combine the two.
        // This avoids having a "seam" at the zero longitude line.
        final double[] origin = {0.0, 0.0, 0.0};
        final double[] zaxis = {0.0, 0.0, 1.0};

        double lllon = imageInfo.lllon;
        double urlon = imageInfo.urlon;

        // Make sure longitudes are in the interval [0, 360) by converting to
        // rectangular and back to longitude (which puts longitude in interval
        // [-180, 180]) and then adding 360 if longitude is less than zero.
        lllon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, lllon*Math.PI/180.0, 1.0))).lon*180.0/Math.PI;
        urlon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, urlon*Math.PI/180.0, 1.0))).lon*180.0/Math.PI;
        if (lllon < 0.0)
            lllon += 360.0;
        if (urlon < 0.0)
            urlon += 360.0;
        if (lllon >= 360.0)
            lllon = 0.0;
        if (urlon >= 360.0)
            urlon = 0.0;

        vtkPlane planeZeroLon = new vtkPlane();
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, 0.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            planeZeroLon.SetOrigin(origin);
            planeZeroLon.SetNormal(normal);
        }

        // First do the hemisphere from longitude 0 to 180.
        boolean needToGenerateTextures0To180 = true;
        vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
        clipPolyData1.SetClipFunction(planeZeroLon);
        clipPolyData1.SetInput(smallBodyPolyData);
        clipPolyData1.SetInsideOut(1);
        clipPolyData1.Update();
        vtkPolyData clipPolyData1Output = clipPolyData1.GetOutput();
        vtkPolyData clipPolyData1Hemi = clipPolyData1Output;
        if (lllon > 0.0 && lllon < 180.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, lllon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane1 = new vtkPlane();
            plane1.SetOrigin(origin);
            plane1.SetNormal(normal);

            clipPolyData1 = new vtkClipPolyData();
            clipPolyData1.SetClipFunction(plane1);
            clipPolyData1.SetInput(clipPolyData1Output);
            clipPolyData1.SetInsideOut(1);
            clipPolyData1.Update();
            clipPolyData1Output = clipPolyData1.GetOutput();
        }
        if (urlon > 0.0 && urlon < 180.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, urlon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane1 = new vtkPlane();
            plane1.SetOrigin(origin);
            plane1.SetNormal(normal);

            // If the following condition is true, that means there are 2 disjoint pieces in the
            // hemisphere, so we'll need to append the two together.
            if (lllon > 0.0 && lllon < 180.0 && urlon <= lllon)
            {
                clipPolyData1 = new vtkClipPolyData();
                clipPolyData1.SetClipFunction(plane1);
                clipPolyData1.SetInput(clipPolyData1Hemi);
                clipPolyData1.Update();
                vtkPolyData clipOutput = clipPolyData1.GetOutput();

                generateTextureCoordinates(clipPolyData1Output, true, false);
                generateTextureCoordinates(clipOutput, false, true);
                needToGenerateTextures0To180 = false;

                vtkAppendPolyData appendFilter = new vtkAppendPolyData();
                appendFilter.UserManagedInputsOff();
                appendFilter.AddInput(clipPolyData1Output);
                appendFilter.AddInput(clipOutput);
                appendFilter.Update();
                clipPolyData1Output = appendFilter.GetOutput();
            }
            else
            {
                clipPolyData1 = new vtkClipPolyData();
                clipPolyData1.SetClipFunction(plane1);
                clipPolyData1.SetInput(clipPolyData1Output);
                clipPolyData1.Update();
                clipPolyData1Output = clipPolyData1.GetOutput();
            }
        }


        // Next do the hemisphere from longitude 180 to 360.
        boolean needToGenerateTextures180To0 = true;
        vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
        clipPolyData2.SetClipFunction(planeZeroLon);
        clipPolyData2.SetInput(smallBodyPolyData);
        clipPolyData2.Update();
        vtkPolyData clipPolyData2Output = clipPolyData2.GetOutput();
        vtkPolyData clipPolyData2Hemi = clipPolyData2Output;
        if (lllon > 180.0 && lllon < 360.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, lllon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane2 = new vtkPlane();
            plane2.SetOrigin(origin);
            plane2.SetNormal(normal);

            clipPolyData2 = new vtkClipPolyData();
            clipPolyData2.SetClipFunction(plane2);
            clipPolyData2.SetInput(clipPolyData2Output);
            clipPolyData2.SetInsideOut(1);
            clipPolyData2.Update();
            clipPolyData2Output = clipPolyData2.GetOutput();
        }
        if (urlon > 180.0 && urlon < 360.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, urlon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane2 = new vtkPlane();
            plane2.SetOrigin(origin);
            plane2.SetNormal(normal);

            // If the following condition is true, that means there are 2 disjoint pieces in the
            // hemisphere, so we'll need to append the two together.
            if (lllon > 180.0 && lllon < 360.0 && urlon <= lllon)
            {
                clipPolyData2 = new vtkClipPolyData();
                clipPolyData2.SetClipFunction(plane2);
                clipPolyData2.SetInput(clipPolyData2Hemi);
                clipPolyData2.Update();
                vtkPolyData clipOutput = clipPolyData2.GetOutput();

                generateTextureCoordinates(clipPolyData1Output, true, false);
                generateTextureCoordinates(clipOutput, false, true);
                needToGenerateTextures180To0 = false;

                vtkAppendPolyData appendFilter = new vtkAppendPolyData();
                appendFilter.UserManagedInputsOff();
                appendFilter.AddInput(clipPolyData2Output);
                appendFilter.AddInput(clipOutput);
                appendFilter.Update();
                clipPolyData2Output = appendFilter.GetOutput();
            }
            else
            {
                clipPolyData2 = new vtkClipPolyData();
                clipPolyData2.SetClipFunction(plane2);
                clipPolyData2.SetInput(clipPolyData2Output);
                clipPolyData2.Update();
                clipPolyData2Output = clipPolyData2.GetOutput();
            }
        }



        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        appendFilter.UserManagedInputsOff();
        // We may not need to include both hemispheres. Test to see
        // if the texture is contained in each hemisphere.
        if (doLongitudeIntervalsIntersect(0.0, 180.0, lllon, urlon))
        {
            if (needToGenerateTextures0To180)
            {
                boolean isOnLeftSide = false;
                boolean isOnRightSide = false;
                if (lllon >= 0.0 && lllon < 180.0)
                    isOnLeftSide = true;
                if (urlon > 0.0 && urlon <= 180.0)
                    isOnRightSide = true;
                generateTextureCoordinates(clipPolyData1Output, isOnLeftSide, isOnRightSide);
            }
            appendFilter.AddInput(clipPolyData1Output);
        }
        if (doLongitudeIntervalsIntersect(180.0, 0.0, lllon, urlon))
        {
            if (needToGenerateTextures180To0)
            {
                boolean isOnLeftSide = false;
                boolean isOnRightSide = false;
                if (lllon >= 180.0)
                    isOnLeftSide = true;
                if (urlon > 180.0 || urlon == 0.0)
                    isOnRightSide = true;
                generateTextureCoordinates(clipPolyData2Output, isOnLeftSide, isOnRightSide);
            }
            appendFilter.AddInput(clipPolyData2Output);
        }
        appendFilter.Update();
        smallBodyPolyData = appendFilter.GetOutput();

        return smallBodyPolyData;
    }

    /**
     * Clip out the texture area for latitudes. This is optimized for ellipsoids
     * and only uses planes (not cones).
     * @param smallBodyPolyData
     * @return
     */
    private vtkPolyData clipOutTextureLatitudeEllipsoid(vtkPolyData smallBodyPolyData)
    {
        double[] zaxis = {0.0, 0.0, 1.0};
        double lllat = imageInfo.lllat * (Math.PI / 180.0);
        double urlat = imageInfo.urlat * (Math.PI / 180.0);

        double[] intersectPoint = new double[3];
        smallBodyModel.getPointAndCellIdFromLatLon(lllat, 0.0, intersectPoint);
        double[] vec = new double[]{0.0, 0.0, intersectPoint[2]};


        vtkPlane plane3 = new vtkPlane();
        plane3.SetOrigin(vec);
        plane3.SetNormal(zaxis);

        vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
        clipPolyData2.SetClipFunction(plane3);
        clipPolyData2.SetInput(smallBodyPolyData);
        vtkAlgorithmOutput clipPolyData2Output = clipPolyData2.GetOutputPort();


        smallBodyModel.getPointAndCellIdFromLatLon(urlat, 0.0, intersectPoint);
        vec = new double[]{0.0, 0.0, intersectPoint[2]};

        vtkPlane plane4 = new vtkPlane();
        plane4.SetOrigin(vec);
        plane4.SetNormal(zaxis);

        vtkClipPolyData clipPolyData3 = new vtkClipPolyData();
        clipPolyData3.SetClipFunction(plane4);
        clipPolyData3.SetInputConnection(clipPolyData2Output);
        clipPolyData3.SetInsideOut(1);

        smallBodyPolyData = clipPolyData3.GetOutput();

        return smallBodyPolyData;
    }

    /**
     * Clip out texture area for latitudes. This works for any general shape model,
     * even non-ellipsoids, and uses cones to clip out lines of latitude.
     * @param smallBodyPolyData
     * @return
     */
    private vtkPolyData clipOutTextureLatitudeGeneral(vtkPolyData smallBodyPolyData)
    {
        double[] origin = {0.0, 0.0, 0.0};
        double[] zaxis = {0.0, 0.0, 1.0};
        double lllat = imageInfo.lllat * (Math.PI / 180.0);
        double urlat = imageInfo.urlat * (Math.PI / 180.0);

        // For clipping latitude, first split the shape model in half, do the clipping
        // on each half, and then combine the 2 halves.
        vtkPlane planeZeroLat = new vtkPlane();
        planeZeroLat.SetOrigin(origin);
        planeZeroLat.SetNormal(zaxis);

        double[] yaxis = {0.0, 1.0, 0.0};
        vtkTransform transform = new vtkTransform();
        transform.Identity();
        transform.RotateWXYZ(90.0, yaxis);

        // Do northern hemisphere first
        vtkClipPolyData clipPolyDataNorth = new vtkClipPolyData();
        clipPolyDataNorth.SetClipFunction(planeZeroLat);
        clipPolyDataNorth.SetInput(smallBodyPolyData);
        vtkAlgorithmOutput clipNorthOutput = clipPolyDataNorth.GetOutputPort();
        if (lllat > 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 - imageInfo.lllat);

            clipPolyDataNorth = new vtkClipPolyData();
            clipPolyDataNorth.SetClipFunction(cone);
            clipPolyDataNorth.SetInputConnection(clipNorthOutput);
            clipPolyDataNorth.SetInsideOut(1);
            clipNorthOutput = clipPolyDataNorth.GetOutputPort();
        }
        if (urlat > 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 - imageInfo.urlat);

            clipPolyDataNorth = new vtkClipPolyData();
            clipPolyDataNorth.SetClipFunction(cone);
            clipPolyDataNorth.SetInputConnection(clipNorthOutput);
            clipNorthOutput = clipPolyDataNorth.GetOutputPort();
        }

        // Now do southern hemisphere
        vtkClipPolyData clipPolyDataSouth = new vtkClipPolyData();
        clipPolyDataSouth.SetClipFunction(planeZeroLat);
        clipPolyDataSouth.SetInput(smallBodyPolyData);
        clipPolyDataSouth.SetInsideOut(1);
        vtkAlgorithmOutput clipSouthOutput = clipPolyDataSouth.GetOutputPort();
        if (lllat < 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 + imageInfo.lllat);

            clipPolyDataSouth = new vtkClipPolyData();
            clipPolyDataSouth.SetClipFunction(cone);
            clipPolyDataSouth.SetInputConnection(clipSouthOutput);
            clipSouthOutput = clipPolyDataSouth.GetOutputPort();
        }
        if (urlat < 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 + imageInfo.urlat);

            clipPolyDataSouth = new vtkClipPolyData();
            clipPolyDataSouth.SetClipFunction(cone);
            clipPolyDataSouth.SetInputConnection(clipSouthOutput);
            clipPolyDataSouth.SetInsideOut(1);
            clipSouthOutput = clipPolyDataSouth.GetOutputPort();
        }


        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        if (urlat > 0.0)
            appendFilter.AddInputConnection(clipNorthOutput);
        if (lllat < 0.0)
            appendFilter.AddInputConnection(clipSouthOutput);

        smallBodyPolyData = appendFilter.GetOutput();

        return smallBodyPolyData;
    }

    private void initialize()
    {
        if (initialized)
            return;

        vtkPolyData smallBodyPolyData = smallBodyModel.getSmallBodyPolyData();

        double lllat = imageInfo.lllat;
        double lllon = imageInfo.lllon;
        double urlat = imageInfo.urlat;
        double urlon = imageInfo.urlon;

        // If the texture does not cover the entire model, then clip out the part
        // that it does cover.
        if (lllat != -90.0 || lllon != 0.0 || urlat != 90.0 || urlon != 360.0)
        {
            if (isShapeModelEllipsoid)
                smallBodyPolyData = clipOutTextureLatitudeEllipsoid(smallBodyPolyData);
            else
                smallBodyPolyData = clipOutTextureLatitudeGeneral(smallBodyPolyData);
        }

        smallBodyPolyData = clipOutTextureLongitudeAndGenerateTextureCoordinates(smallBodyPolyData);

        imageMapPolyData.DeepCopy(smallBodyPolyData);

        shiftedImageMapPolyData.DeepCopy(imageMapPolyData);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedImageMapPolyData, offset);

        initialized = true;
    }

    /**
     * Generates the cylindrical projection texture coordinates for the polydata.
     * If isOnLeftSide is true, that means the polydata borders the left side (the side of lllon) of the image.
     * If isOnRightSide is true, that means the polydata borders the right side (the side of urlon) of the image.
     * @param polydata
     * @param isOnLeftSide
     * @param isOnRightSide
     */
    protected void generateTextureCoordinates(
            vtkPolyData polydata,
            boolean isOnLeftSide,
            boolean isOnRightSide)
    {
        double lllat = imageInfo.lllat * (Math.PI / 180.0);
        double lllon = imageInfo.lllon * (Math.PI / 180.0);
        double urlat = imageInfo.urlat * (Math.PI / 180.0);
        double urlon = imageInfo.urlon * (Math.PI / 180.0);

        // Make sure longitudes are in the interval [0, 2*PI) by converting to
        // rectangular and back to longitude (which puts longitude in interval
        // [-PI, PI]) and then adding 2*PI if longitude is less than zero.
        lllon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, lllon, 1.0))).lon;
        urlon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, urlon, 1.0))).lon;
        if (lllon < 0.0)
            lllon += 2.0*Math.PI;
        if (urlon < 0.0)
            urlon += 2.0*Math.PI;
        if (lllon >= 2.0*Math.PI)
            lllon = 0.0;
        if (urlon >= 2.0*Math.PI)
            urlon = 0.0;

        vtkFloatArray textureCoords = new vtkFloatArray();

        int numberOfPoints = polydata.GetNumberOfPoints();

        textureCoords.SetNumberOfComponents(2);
        textureCoords.SetNumberOfTuples(numberOfPoints);

        vtkPoints points = polydata.GetPoints();

        double xsize = getDistanceBetweenLongitudes(lllon, urlon);
        // If lower left and upper right longitudes are the same, that
        // means the image extends 360 degrees around the shape model.
        if (xsize == 0.0)
            xsize = 2.0*Math.PI;
        double ysize = urlat - lllat;

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            LatLon ll = MathUtil.reclat(pt);

            if (ll.lon < 0.0)
               ll.lon += (2.0 * Math.PI);
            if (ll.lon >= 2.0 * Math.PI)
                ll.lon = 0.0;

            double dist = getDistanceBetweenLongitudes(lllon, ll.lon);
            if (isOnLeftSide)
            {
                if (Math.abs(2.0*Math.PI - dist) < 1.0e-2)
                    dist = 0.0;
            }
            else if (isOnRightSide)
            {
                if (Math.abs(dist) < 1.0e-2)
                    dist = xsize;
            }

            double u = dist / xsize;
            double v = (ll.lat - lllat) / ysize;

            if (u < 0.0) u = 0.0;
            else if (u > 1.0) u = 1.0;
            if (v < 0.0) v = 0.0;
            else if (v > 1.0) v = 1.0;

            textureCoords.SetTuple2(i, u, v);
        }

        polydata.GetPointData().SetTCoords(textureCoords);
    }

    /**
     * Assuming leftLon and rightLon are within interval [0, 2*PI], return
     * the distance between the two assuming leftLon is at a lower lon
     * than rightLon. Thus the returned result is always positive within
     * interval [0, 2*PI].
     * @param leftLon
     * @param rightLon
     * @return distance in radians
     */
    private double getDistanceBetweenLongitudes(double leftLon, double rightLon)
    {
        double dist = rightLon - leftLon;
        if (dist >= 0.0)
            return dist;
        else
            return dist + 2.0 * Math.PI;
    }

    /**
     * Same as previous but returns distance in degrees
     */
    private double getDistanceBetweenLongitudesDegrees(double leftLon, double rightLon)
    {
        double dist = rightLon - leftLon;
        if (dist >= 0.0)
            return dist;
        else
            return dist + 360.0;
    }

    /**
     * Returns if the two longitudinal intervals intersect at all. If they intersect at
     * a point, (e.g. one interval goes from 1 to 2 and the second goes from 2 to 3), false
     * is returned.
     * @param lower1
     * @param upper1
     * @param lower2
     * @param upper2
     * @return
     */
    private boolean doLongitudeIntervalsIntersect(double lower1, double upper1, double lower2, double upper2)
    {
        if (lower1 == lower2 || upper1 == upper2)
            return true;

        // First test if lower2 or upper2 is contained in the first interval
        double dist1 = getDistanceBetweenLongitudesDegrees(lower1, upper1);
        double d = getDistanceBetweenLongitudesDegrees(lower1, lower2);
        if (d > 0.0 && d < dist1)
            return true;
        d = getDistanceBetweenLongitudesDegrees(lower1, upper2);
        if (d > 0.0 && d < dist1)
            return true;

        // Then test if lower1 or upper1 is contained in the second interval
        double dist2 = getDistanceBetweenLongitudesDegrees(lower2, upper2);
        d = getDistanceBetweenLongitudesDegrees(lower2, lower1);
        if (d > 0.0 && d < dist2)
            return true;
        d = getDistanceBetweenLongitudesDegrees(lower2, upper1);
        if (d > 0.0 && d < dist2)
            return true;

        return false;
    }

    public ArrayList<vtkProp> getProps()
    {
        if (smallBodyActor == null)
        {
            smallBodyMapper = new vtkPolyDataMapper();
            smallBodyMapper.ScalarVisibilityOff();
            smallBodyMapper.SetScalarModeToDefault();
            smallBodyMapper.SetInput(shiftedImageMapPolyData);
            smallBodyMapper.Update();

            imageMapTexture = new vtkTexture();
            imageMapTexture.InterpolateOn();
            imageMapTexture.RepeatOff();
            imageMapTexture.EdgeClampOn();
            vtkImageData image = loadImageMap(imageInfo.filename);
            imageMapTexture.SetInput(image);

            smallBodyActor = new vtkActor();
            smallBodyActor.SetMapper(smallBodyMapper);
            smallBodyActor.SetTexture(imageMapTexture);

            smallBodyActors.add(smallBodyActor);
        }

        return smallBodyActors;
    }

    public void setShowImageMap(boolean b)
    {
        if (b)
            initialize();

        if (smallBodyActor != null)
            smallBodyActor.SetVisibility(b ? 1 : 0);

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

    public double getDefaultOffset()
    {
        return 2.0*smallBodyModel.getMinShiftAmount();
    }

    public void setOffset(double offset)
    {
        this.offset = offset;

        shiftedImageMapPolyData.DeepCopy(imageMapPolyData);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedImageMapPolyData, offset);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double getOffset()
    {
        return offset;
    }

    public void delete()
    {
    }
}
