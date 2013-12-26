package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

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

import edu.jhuapl.near.model.ModelFactory.ModelConfig;
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
            ModelConfig modelConfig) throws IOException
    {
        int[] xyzIndices = modelConfig.lidarBrowseXYZIndices;
        int[] scXyzIndices = modelConfig.lidarBrowseSpacecraftIndices;
        boolean isSpacecraftInSphericalCoordinates = modelConfig.lidarBrowseIsSpacecraftInSphericalCoordinates;
        int timeindex = modelConfig.lidarBrowseTimeIndex;
        int numberHeaderLines = modelConfig.lidarBrowseNumberHeaderLines;
        boolean isInMeters = modelConfig.lidarBrowseIsInMeters;
        int noiseindex = modelConfig.lidarBrowseNoiseIndex;
        boolean isBinary = modelConfig.lidarBrowseIsBinary;
        int binaryRecordSize = modelConfig.lidarBrowseBinaryRecordSize;

        File file = FileCache.getFileFromServer(path);

        if (file == null)
            throw new IOException(path + " could not be loaded");

        filepath = path;


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

        int count = 0;

        FileInputStream fs = new FileInputStream(file.getAbsolutePath());

        if (isBinary)
        {
            FileChannel channel = fs.getChannel();
            ByteBuffer bb = ByteBuffer.allocateDirect((int) file.length());
            bb.clear();
            bb.order(ByteOrder.LITTLE_ENDIAN);
            if (channel.read(bb) != file.length())
            {
                fs.close();
                throw new IOException("Error reading " + path);
            }

            byte[] utcArray = new byte[24];

            int numRecords = (int) (file.length() / binaryRecordSize);
            for (count = 0; count < numRecords; ++count)
            {
                int xoffset = count*binaryRecordSize + xindex;
                int yoffset = count*binaryRecordSize + yindex;
                int zoffset = count*binaryRecordSize + zindex;
                int scxoffset = count*binaryRecordSize + scxindex;
                int scyoffset = count*binaryRecordSize + scyindex;
                int sczoffset = count*binaryRecordSize + sczindex;

                double x = bb.getDouble(xoffset);
                double y = bb.getDouble(yoffset);
                double z = bb.getDouble(zoffset);
                double scx = bb.getDouble(scxoffset);
                double scy = bb.getDouble(scyoffset);
                double scz = bb.getDouble(sczoffset);

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

                // assume no spacecraft position for now
                pointsSc.InsertNextPoint(scx, scy, scz);
                vertSc.InsertNextCell(idList);

                int timeoffset = count*binaryRecordSize + timeindex;

                bb.position(timeoffset);
                bb.get(utcArray);
                String utc = new String(utcArray);

                // We store the times in a vtk array. By storing in a vtk array, we don't have to
                // worry about java out of memory errors since java doesn't know about c++ memory. We
                // store in a double array rather than a long long array, since not sure if the conversion
                // from a java long to a c++ long long is supported by vtk wrappers. Therefore convert
                // the java long to a double using Double.longBitsToDouble function.
                double t = Double.longBitsToDouble(new DateTime(utc, DateTimeZone.UTC).getMillis());
                times.InsertNextValue(t);
            }

            fs.close();
        }
        else
        {
            InputStreamReader isr = new InputStreamReader(fs);
            BufferedReader in = new BufferedReader(isr);

            for (int i=0; i<numberHeaderLines; ++i)
                in.readLine();

            String line;

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

                // If spacecraft position is in spherical coordinates,
                // do the conversion here.
                if (isSpacecraftInSphericalCoordinates)
                {
                    double[] xyz = MathUtil.latrec(new LatLon(scy*Math.PI/180.0, scx*Math.PI/180.0, scz));
                    scx = xyz[0];
                    scy = xyz[1];
                    scz = xyz[2];
                }

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
        }


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
        if (path.contains("_v2"))
            actor.GetProperty().SetColor(1.0, 1.0, 0.0);

        actor.GetProperty().SetPointSize(2.0);


        vtkPolyDataMapper pointsMapperSc = new vtkPolyDataMapper();
        pointsMapperSc.SetInput(geometryFilterSc.GetOutput());

        actorSpacecraft = new vtkActor();
        actorSpacecraft.SetMapper(pointsMapperSc);
        actorSpacecraft.GetProperty().SetColor(0.0, 1.0, 0.0);
        // for Itokawa optimized lidar data, show in different color.
        if (path.contains("_v2"))
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

    public void setOffset(double offset)
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

