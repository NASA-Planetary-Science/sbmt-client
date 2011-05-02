package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class MSIBoundaryCollection extends Model implements PropertyChangeListener
{
    public class Boundary extends Model implements PropertyChangeListener
    {
        private vtkActor actor;
        private vtkPolyData boundary;
        private vtkPolyDataMapper boundaryMapper;
        private double[] spacecraftPosition = new double[3];
        private double[] frustum1 = new double[3];
        private double[] frustum2 = new double[3];
        private double[] frustum3 = new double[3];
        private double[] frustum4 = new double[3];
        private double[] sunVector = new double[3];
        private double[] boresightDirection = new double[3];
        private double[] upVector = new double[3];
        private ImageKey key;

        public Boundary(ImageKey key) throws IOException
        {
            this.key = key;

            File lblFile = FileCache.getFileFromServer(key.name + "_DDR.LBL");

            if (lblFile == null)
                throw new IOException("Could not download " + key.name);

            boundary = new vtkPolyData();
            boundary.SetPoints(new vtkPoints());
            boundary.SetVerts(new vtkCellArray());

            boundaryMapper = new vtkPolyDataMapper();
            actor = new vtkActor();

            String[] startTime = new String[1];
            String[] stopTime = new String[1];

            MSIImage.loadImageInfo(
                    lblFile.getAbsolutePath(),
                    startTime,
                    stopTime,
                    spacecraftPosition,
                    sunVector,
                    frustum1,
                    frustum2,
                    frustum3,
                    frustum4,
                    boresightDirection,
                    upVector);

            if (key.source.equals(ImageSource.GASKELL))
            {
                // Try to load a sumfile if there is one
                File tmp = new File(key.name);
                String sumFilename = "/MSI/sumfiles/" + tmp.getName().substring(0, 11) + ".SUM";
                File sumfile = FileCache.getFileFromServer(sumFilename);

                MSIImage.loadSumfile(sumfile.getAbsolutePath(),
                        startTime,
                        stopTime,
                        spacecraftPosition,
                        sunVector,
                        frustum1,
                        frustum2,
                        frustum3,
                        frustum4,
                        boresightDirection,
                        upVector);
            }

            initialize();
        }

        private void initialize()
        {
            // Using the frustum, go around the boundary of the frustum and intersect with
            // the asteroid.

            vtkPoints points = boundary.GetPoints();
            vtkCellArray verts = boundary.GetVerts();
            verts.SetNumberOfCells(0);
            points.SetNumberOfPoints(0);

            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(1);

            vtksbCellLocator cellLocator = erosModel.getCellLocator();

            vtkGenericCell cell = new vtkGenericCell();

            final int IMAGE_WIDTH = MSIImage.IMAGE_WIDTH;
            final int IMAGE_HEIGHT = MSIImage.IMAGE_HEIGHT;

            int count = 0;

            double[] corner1 = {
                    spacecraftPosition[0] + frustum1[0],
                    spacecraftPosition[1] + frustum1[1],
                    spacecraftPosition[2] + frustum1[2]
            };
            double[] corner2 = {
                    spacecraftPosition[0] + frustum2[0],
                    spacecraftPosition[1] + frustum2[1],
                    spacecraftPosition[2] + frustum2[2]
            };
            double[] corner3 = {
                    spacecraftPosition[0] + frustum3[0],
                    spacecraftPosition[1] + frustum3[1],
                    spacecraftPosition[2] + frustum3[2]
            };
            double[] vec12 = {
                    corner2[0] - corner1[0],
                    corner2[1] - corner1[1],
                    corner2[2] - corner1[2]
            };
            double[] vec13 = {
                    corner3[0] - corner1[0],
                    corner3[1] - corner1[1],
                    corner3[2] - corner1[2]
            };

            //double horizScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum3) / 2.0 ) / IMAGE_HEIGHT;
            //double vertScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum2) / 2.0 ) / IMAGE_WIDTH;

            double scdist = MathUtil.vnorm(spacecraftPosition);

            for (int i=0; i<IMAGE_HEIGHT; ++i)
            {
                // Compute the vector on the left of the row.
                double fracHeight = ((double)i / (double)(IMAGE_HEIGHT-1));
                double[] left = {
                        corner1[0] + fracHeight*vec13[0],
                        corner1[1] + fracHeight*vec13[1],
                        corner1[2] + fracHeight*vec13[2]
                };

                for (int j=0; j<IMAGE_WIDTH; ++j)
                {
                    if (j == 1 && i > 0 && i < IMAGE_HEIGHT-1)
                    {
                        j = IMAGE_WIDTH-2;
                        continue;
                    }

                    double fracWidth = ((double)j / (double)(IMAGE_WIDTH-1));
                    double[] vec = {
                            left[0] + fracWidth*vec12[0],
                            left[1] + fracWidth*vec12[1],
                            left[2] + fracWidth*vec12[2]
                    };
                    vec[0] -= spacecraftPosition[0];
                    vec[1] -= spacecraftPosition[1];
                    vec[2] -= spacecraftPosition[2];
                    MathUtil.unorm(vec, vec);

                    double[] lookPt = {
                            spacecraftPosition[0] + 2.0*scdist*vec[0],
                            spacecraftPosition[1] + 2.0*scdist*vec[1],
                            spacecraftPosition[2] + 2.0*scdist*vec[2]
                    };

                    double tol = 1e-6;
                    double[] t = new double[1];
                    double[] x = new double[3];
                    double[] pcoords = new double[3];
                    int[] subId = new int[1];
                    int[] cellId = new int[1];
                    int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId, cellId, cell);

                    if (result > 0)
                    {
                        double[] closestPoint = x;

                        //double horizPixelScale = closestDist * horizScaleFactor;
                        //double vertPixelScale = closestDist * vertScaleFactor;

                        points.InsertNextPoint(closestPoint);
                        idList.SetId(0, count);
                        verts.InsertNextCell(idList);

                        ++count;
                    }
                }
            }


            PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(
                    boundary, erosModel.getSmallBodyPolyData(), erosModel.getPointLocator(), 0.003);

            boundaryMapper.SetInput(boundary);

            actor.SetMapper(boundaryMapper);
            actor.GetProperty().SetColor(1.0, 0.0, 0.0);
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

        public ImageKey getKey()
        {
            return key;
        }

        public void getCameraOrientation(double[] spacecraftPosition,
                double[] focalPoint, double[] upVector)
        {
            for (int i=0; i<3; ++i)
            {
                spacecraftPosition[i] = this.spacecraftPosition[i];
                upVector[i] = this.upVector[i];
            }

            // Normalize the direction vector
            double[] direction = new double[3];
            MathUtil.unorm(boresightDirection, direction);

            int cellId = erosModel.computeRayIntersection(spacecraftPosition, direction, focalPoint);

            if (cellId < 0)
            {
                BoundingBox bb = new BoundingBox(boundary.GetBounds());
                double[] centerPoint = bb.getCenterPoint();
                //double[] centerPoint = boundary.GetPoint(0);
                double distanceToCenter = MathUtil.distanceBetween(spacecraftPosition, centerPoint);

                focalPoint[0] = spacecraftPosition[0] + distanceToCenter*direction[0];
                focalPoint[1] = spacecraftPosition[1] + distanceToCenter*direction[1];
                focalPoint[2] = spacecraftPosition[2] + distanceToCenter*direction[2];
            }
        }
    }


    private HashMap<Boundary, ArrayList<vtkProp>> boundaryToActorsMap = new HashMap<Boundary, ArrayList<vtkProp>>();
    private HashMap<vtkProp, Boundary> actorToBoundaryMap = new HashMap<vtkProp, Boundary>();
    private SmallBodyModel erosModel;

    public MSIBoundaryCollection(SmallBodyModel erosModel)
    {
        super(ModelNames.MSI_BOUNDARY);

        this.erosModel = erosModel;
    }

    private boolean containsKey(ImageKey key)
    {
        for (Boundary boundary : boundaryToActorsMap.keySet())
        {
            if (boundary.key.equals(key))
                return true;
        }

        return false;
    }

    private Boundary getBoundaryFromKey(ImageKey key)
    {
        for (Boundary boundary : boundaryToActorsMap.keySet())
        {
            if (boundary.key.equals(key))
                return boundary;
        }

        return null;
    }


    public void addBoundary(ImageKey key) throws FitsException, IOException
    {
        if (containsKey(key))
            return;

        Boundary boundary = new Boundary(key);

        erosModel.addPropertyChangeListener(boundary);
        boundary.addPropertyChangeListener(this);

        boundaryToActorsMap.put(boundary, new ArrayList<vtkProp>());

        ArrayList<vtkProp> boundaryPieces = boundary.getProps();

        boundaryToActorsMap.get(boundary).addAll(boundaryPieces);

        for (vtkProp act : boundaryPieces)
            actorToBoundaryMap.put(act, boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeBoundary(ImageKey key)
    {
        Boundary boundary = getBoundaryFromKey(key);

        ArrayList<vtkProp> actors = boundaryToActorsMap.get(boundary);

        for (vtkProp act : actors)
            actorToBoundaryMap.remove(act);

        boundaryToActorsMap.remove(boundary);

        boundary.removePropertyChangeListener(this);
        erosModel.removePropertyChangeListener(boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeAllBoundaries()
    {
        HashMap<Boundary, ArrayList<vtkProp>> map = (HashMap<Boundary, ArrayList<vtkProp>>)boundaryToActorsMap.clone();
        for (Boundary boundary : map.keySet())
            removeBoundary(boundary.key);
    }

    public ArrayList<vtkProp> getProps()
    {
        return new ArrayList<vtkProp>(actorToBoundaryMap.keySet());
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        File file = new File(actorToBoundaryMap.get(prop).key.name);
        return "Boundary of MSI image " + file.getName().substring(2, 11);
    }

    public String getBoundaryName(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor).key.name;
    }

    public Boundary getBoundary(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor);
    }

    public Boundary getBoundary(ImageKey key)
    {
        return getBoundaryFromKey(key);
    }

    public boolean containsBoundary(ImageKey key)
    {
        return containsKey(key);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
