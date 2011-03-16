package edu.jhuapl.near.model.eros;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkGeometryFilter;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.util.DoublePair;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;

public class NLRDataPerDay extends Model
{
    private vtkPolyData polydata;
    private vtkPoints originalPoints;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private double startPercentage = 0.0;
    private double stopPercentage = 1.0;
    private vtkGeometryFilter geometryFilter;
    private String filepath;
    private ArrayList<String> times = new ArrayList<String>();

    public NLRDataPerDay(String path) throws IOException
    {
        File file = FileCache.getFileFromServer(path);

        if (file == null)
            throw new IOException(path + " could not be loaded");

        filepath = path;

        ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());

        polydata = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray vert = new vtkCellArray();
        polydata.SetPoints( points );
        polydata.SetVerts( vert );

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);
        points.SetNumberOfPoints(lines.size()-2);

        for (int i=2; i<lines.size(); ++i)
        {
            String[] vals = lines.get(i).trim().split("\\s+");

            // Don't include noise
            if (vals[7].equals("1"))
                continue;

            points.SetPoint(i-2,
                    Double.parseDouble(vals[14])/1000.0,
                    Double.parseDouble(vals[15])/1000.0,
                    Double.parseDouble(vals[16])/1000.0);
            idList.SetId(0, i-2);
            vert.InsertNextCell(idList);

            times.add(vals[4]);
        }

        originalPoints = new vtkPoints();
        originalPoints.DeepCopy(points);

        geometryFilter = new vtkGeometryFilter();
        geometryFilter.SetInput(polydata);
        geometryFilter.PointClippingOn();
        geometryFilter.CellClippingOff();
        geometryFilter.ExtentClippingOff();
        geometryFilter.MergingOff();
        geometryFilter.SetPointMinimum(0);
        geometryFilter.SetPointMaximum(lines.size()-2);

        vtkPolyDataMapper pointsMapper = new vtkPolyDataMapper();
        pointsMapper.SetInput(geometryFilter.GetOutput());
        //pointsMapper.SetResolveCoincidentTopologyToPolygonOffset();
        //pointsMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, -1.0);

        vtkActor actor = new vtkActor();
        actor.SetMapper(pointsMapper);
        actor.GetProperty().SetColor(0.0, 0.0, 1.0);
        actor.GetProperty().SetPointSize(2.0);

        actors.add(actor);
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
        File file = new File(filepath);
        return "NLR " + file.getName().substring(0, 8) + " acquired at " + times.get(cellId);
    }

    public ArrayList<vtkProp> getProps()
    {
        return actors;
    }
}

