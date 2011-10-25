package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkGeometryFilter;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

import edu.jhuapl.near.util.DoublePair;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;

public class LidarDataPerUnit extends Model
{
    private vtkPolyData polydata;
    private vtkPolyData polydataSc;
    private vtkPoints originalPoints;
    private vtkPoints originalPointsSc;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private double startPercentage = 0.0;
    private double stopPercentage = 1.0;
    private vtkGeometryFilter geometryFilter;
    private vtkGeometryFilter geometryFilterSc;
    private String filepath;
    private vtkDoubleArray times;
    private vtkActor actorSpacecraft;

    public LidarDataPerUnit(String path,
            int[] xyzIndices,
            int[] scXyzIndices,
            int timeindex,
            int numberHeaderLines,
            boolean isInMeters,
            int noiseindex) throws IOException
    {
        File file = FileCache.getFileFromServer(path);

        if (file == null)
            throw new IOException(path + " could not be loaded");

        filepath = path;

        InputStream fs = new FileInputStream(file.getAbsolutePath());
        if (file.getAbsolutePath().toLowerCase().endsWith(".gz"))
            fs = new GZIPInputStream(fs);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);


        polydata = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray vert = new vtkCellArray();
        polydata.SetPoints( points );
        polydata.SetVerts( vert );


        polydataSc = new vtkPolyData();
        vtkPoints pointsSc = new vtkPoints();
        vtkCellArray vertSc = new vtkCellArray();
        polydataSc.SetPoints( pointsSc );
        polydataSc.SetVerts( vertSc );


        times = new vtkDoubleArray();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        int xindex = xyzIndices[0];
        int yindex = xyzIndices[1];
        int zindex = xyzIndices[2];

        int scxindex = scXyzIndices[0];
        int scyindex = scXyzIndices[1];
        int sczindex = scXyzIndices[2];

        for (int i=0; i<numberHeaderLines; ++i)
            in.readLine();

        String line;

        int count = 0;
        while ((line = in.readLine()) != null)
        {
            String[] vals = line.trim().split("\\s+");

            // Don't include noise
            if (noiseindex >=0 && vals[noiseindex].equals("1"))
                continue;

            double x = Double.parseDouble(vals[xindex]);
            double y = Double.parseDouble(vals[yindex]);
            double z = Double.parseDouble(vals[zindex]);
            double scx = Double.parseDouble(vals[scxindex]);
            double scy = Double.parseDouble(vals[scyindex]);
            double scz = Double.parseDouble(vals[sczindex]);
            if (isInMeters)
            {
                x /= 1000.0;
                y /= 1000.0;
                z /= 1000.0;
                scx /= 1000.0;
                scy /= 1000.0;
                scz /= 1000.0;
            }
            points.InsertNextPoint(x, y, z);
            idList.SetId(0, count);
            vert.InsertNextCell(idList);

            pointsSc.InsertNextPoint(scx, scy, scz);
            vertSc.InsertNextCell(idList);

            // We store the times in a vtk array. By storing in a vtk array, we don't have to
            // worry about java out of memory errors since java doesn't know about c++ memory. We
            // store in a double array rather than a long long array, since not sure if the conversion
            // from a java long to a c++ long long is supported by vtk wrappers. Therefore convert
            // the java long to a double using Double.longBitsToDouble function.
            double t = Double.longBitsToDouble(new DateTime(vals[timeindex], DateTimeZone.UTC).getMillis());
            times.InsertNextValue(t);

            ++count;
        }

        in.close();

        originalPoints = new vtkPoints();
        originalPoints.DeepCopy(points);

        originalPointsSc = new vtkPoints();
        originalPointsSc.DeepCopy(pointsSc);

        geometryFilter = new vtkGeometryFilter();
        geometryFilter.SetInput(polydata);
        geometryFilter.PointClippingOn();
        geometryFilter.CellClippingOff();
        geometryFilter.ExtentClippingOff();
        geometryFilter.MergingOff();
        geometryFilter.SetPointMinimum(0);
        geometryFilter.SetPointMaximum(count);

        geometryFilterSc = new vtkGeometryFilter();
        geometryFilterSc.SetInput(polydataSc);
        geometryFilterSc.PointClippingOn();
        geometryFilterSc.CellClippingOff();
        geometryFilterSc.ExtentClippingOff();
        geometryFilterSc.MergingOff();
        geometryFilterSc.SetPointMinimum(0);
        geometryFilterSc.SetPointMaximum(count);

        vtkPolyDataMapper pointsMapper = new vtkPolyDataMapper();
        pointsMapper.SetInput(geometryFilter.GetOutput());

        vtkActor actor = new vtkActor();
        actor.SetMapper(pointsMapper);
        actor.GetProperty().SetColor(0.0, 0.0, 1.0);
        if (path.contains("cdr_optimized"))
            actor.GetProperty().SetColor(1.0, 1.0, 0.0);

        actor.GetProperty().SetPointSize(2.0);


        vtkPolyDataMapper pointsMapperSc = new vtkPolyDataMapper();
        pointsMapperSc.SetInput(geometryFilterSc.GetOutput());

        actorSpacecraft = new vtkActor();
        actorSpacecraft.SetMapper(pointsMapperSc);
        actorSpacecraft.GetProperty().SetColor(0.0, 1.0, 0.0);
        // for Itokawa optimized lidar data, show in different color.
        if (path.contains("optimized"))
            actorSpacecraft.GetProperty().SetColor(1.0, 0.0, 1.0);

        actorSpacecraft.GetProperty().SetPointSize(2.0);



        actors.add(actor);
        actors.add(actorSpacecraft);
    }

    public void setPercentageShown(double startPercent, double stopPercent)
    {
        startPercentage = startPercent;
        stopPercentage = stopPercent;

        double numberOfPoints = originalPoints.GetNumberOfPoints();
        int firstPointId = (int)(numberOfPoints * startPercentage);
        int lastPointId = (int)(numberOfPoints * stopPercentage) - 1;
        if (lastPointId < firstPointId)
        {
            lastPointId = firstPointId;
        }

        geometryFilter.SetPointMinimum(firstPointId);
        geometryFilter.SetPointMaximum(lastPointId);
        geometryFilter.Update();

        geometryFilterSc.SetPointMinimum(firstPointId);
        geometryFilterSc.SetPointMaximum(lastPointId);
        geometryFilterSc.Update();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public DoublePair getPercentageShown()
    {
        return new DoublePair(startPercentage, stopPercentage);
    }

    public void setRadialOffset(double offset)
    {
        vtkPoints points = polydata.GetPoints();

        int numberOfPoints = points.GetNumberOfPoints();

        for (int i=0;i<numberOfPoints;++i)
        {
            double[] pt = originalPoints.GetPoint(i);
            LatLon lla = MathUtil.reclat(pt);
            lla.rad += offset;
            pt = MathUtil.latrec(lla);
            points.SetPoint(i, pt);
        }

        polydata.Modified();
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        cellId = geometryFilter.GetPointMinimum() + cellId;
        String filepath2 = filepath;
        if (filepath2.toLowerCase().endsWith(".gz"))
            filepath2 = filepath2.substring(0, filepath2.length()-3);
        File file = new File(filepath2);

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        long time = Double.doubleToRawLongBits(times.GetValue(cellId));
        String timeStr = new DateTime(time, DateTimeZone.UTC).toString(fmt);

        return "Lidar point " + file.getName() + " acquired at " + timeStr;
    }

    public ArrayList<vtkProp> getProps()
    {
        return actors;
    }

    public void setShowSpacecraftPosition(boolean show)
    {
        if (actorSpacecraft != null)
            actorSpacecraft.SetVisibility(show ? 1 : 0);
    }
}

