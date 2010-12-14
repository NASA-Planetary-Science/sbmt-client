package edu.jhuapl.near.model.eros;

import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import vtk.*;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

public class MapletBoundaryCollection extends Model implements PropertyChangeListener
{
    public class Boundary extends Model implements PropertyChangeListener
    {
        private vtkActor actor;
        private vtkPolyData boundary;
        private vtkPolyDataMapper boundaryMapper;
        private DEMModel dem;

        public Boundary(DEMModel dem)
        {
            this.dem = dem;

            boundary = new vtkPolyData();
            boundaryMapper = new vtkPolyDataMapper();
            actor = new vtkActor();

            initialize();
        }

        private void initialize()
        {
            boundary.DeepCopy(dem.getBoundary());
            int numPoints = boundary.GetNumberOfPoints();
            vtkPoints points = boundary.GetPoints();
            for (int i=0; i<numPoints; ++i)
            {
                double[] pt = points.GetPoint(i);
                double[] closestPoint = erosModel.findClosestPoint(pt);
                points.SetPoint(i, closestPoint);
            }

            PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(
                    boundary, erosModel.getSmallBodyPolyData(), erosModel.getPointLocator(), 0.003);

            boundaryMapper.SetInput(boundary);

            actor.SetMapper(boundaryMapper);
            actor.GetProperty().SetColor(0.0, 0.392, 0.0);
            actor.GetProperty().SetPointSize(1.0);
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
            {
                initialize();
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
            }
        }

        @Override
        public ArrayList<vtkProp> getProps()
        {
            ArrayList<vtkProp> props = new ArrayList<vtkProp>();
            props.add(actor);
            return props;
        }

        public DEMModel getDEM()
        {
            return dem;
        }

    }


    private HashMap<Boundary, ArrayList<vtkProp>> boundaryToActorsMap = new HashMap<Boundary, ArrayList<vtkProp>>();
    private HashMap<vtkProp, Boundary> actorToBoundaryMap = new HashMap<vtkProp, Boundary>();
    private SmallBodyModel erosModel;

    public MapletBoundaryCollection(SmallBodyModel erosModel)
    {
        super(ModelNames.MAPLET_BOUNDARY);

        this.erosModel = erosModel;
    }

    private boolean containsDEM(DEMModel dem)
    {
        for (Boundary boundary : boundaryToActorsMap.keySet())
        {
            if (boundary.dem.equals(dem))
                return true;
        }

        return false;
    }

    private Boundary getBoundaryFromDEM(DEMModel dem)
    {
        for (Boundary boundary : boundaryToActorsMap.keySet())
        {
            if (boundary.dem.equals(dem))
                return boundary;
        }

        return null;
    }


    public void addBoundary(DEMModel dem)
    {
        if (containsDEM(dem))
            return;

        Boundary boundary = new Boundary(dem);

        erosModel.addPropertyChangeListener(boundary);
        boundary.addPropertyChangeListener(this);

        boundaryToActorsMap.put(boundary, new ArrayList<vtkProp>());

        ArrayList<vtkProp> boundaryPieces = boundary.getProps();

        boundaryToActorsMap.get(boundary).addAll(boundaryPieces);

        for (vtkProp act : boundaryPieces)
            actorToBoundaryMap.put(act, boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeBoundary(DEMModel dem)
    {
        Boundary boundary = getBoundaryFromDEM(dem);

        boundary.removePropertyChangeListener(this);
        erosModel.removePropertyChangeListener(boundary);

        ArrayList<vtkProp> actors = boundaryToActorsMap.get(boundary);

        for (vtkProp act : actors)
            actorToBoundaryMap.remove(act);

        boundaryToActorsMap.remove(boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

//    public void removeAllBoundaries()
//    {
//        actorToBoundaryMap.clear();
//        boundaryToActorsMap.clear();
//
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//    }

    public ArrayList<vtkProp> getProps()
    {
        return new ArrayList<vtkProp>(actorToBoundaryMap.keySet());
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        return "Boundary of maplet";
    }
/*
    public Boundary getBoundary(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor);
    }

    public Boundary getBoundary(DEMModel dem)
    {
        return getBoundaryFromDEM(dem);
    }

    public boolean containsBoundary(DEMModel dem)
    {
        return containsDEM(dem);
    }
*/
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
