package edu.jhuapl.near.model;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;
import vtk.vtkTransform;
import vtk.vtkTriangle;

import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;


/**
 * Model of circle structures drawn on a body. This class adds the ability
 * to create a new circle by simply clicking any 3 points on the perimeter of
 * the circle. The circle is formed by using VTK's vtkTriangle::Circumcircle
 * function.
 *
 * @author
 *
 */

public class CircleModel extends AbstractEllipsePolygonModel
{
    private vtkPolyData selectionPolyData;
    private vtkPolyDataMapper lineSelectionMapper;
    private vtkActor lineSelectionActor;


    public CircleModel(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, 20, Mode.CIRCLE_MODE, "circle", ModelNames.CIRCLE_STRUCTURES);
        setInteriorOpacity(0.0);
        int[] color = {255, 0, 255};
        setDefaultColor(color);

        selectionPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray cells = new vtkCellArray();
        selectionPolyData.SetPoints(points);
        selectionPolyData.SetVerts(cells);

        lineSelectionMapper = new vtkPolyDataMapper();
        lineSelectionMapper.SetInput(selectionPolyData);
        lineSelectionMapper.Update();

        lineSelectionActor = new vtkActor();
        lineSelectionActor.PickableOff();
        vtkProperty lineSelectionProperty = lineSelectionActor.GetProperty();
        lineSelectionProperty.SetColor(0.0, 0.0, 1.0);
        lineSelectionProperty.SetPointSize(7.0);

        lineSelectionActor.SetMapper(lineSelectionMapper);
        lineSelectionActor.Modified();

        getProps().add(lineSelectionActor);
    }

    /**
     * Adds a new point on the perimeter of the circle. When the point
     * count equals 3, a circle is created and the intermediate points are
     * deleted.
     *
     * @param pt
     * @return
     */
    public boolean addCircumferencePoint(double[] pt)
    {
        vtkPoints points = selectionPolyData.GetPoints();
        vtkCellArray vert = selectionPolyData.GetVerts();

        int numPoints = points.GetNumberOfPoints();

        if (numPoints < 2)
        {
            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(1);
            idList.SetId(0, numPoints);

            points.InsertNextPoint(pt);
            vert.InsertNextCell(idList);

            idList.Delete();

            selectionPolyData.Modified();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
        else
        {
            // Take the 3 points and compute a circle that passes through them.
            // To do this, first form a triangle from these 3 points.
            // Compute the normal and rotate the 3 points so that its normal
            // is facing the positive z direction. Then use VTK's Circumcircle
            // function which computes the center and radius of a circle that
            // passes through these points.
            double[] pt1 = points.GetPoint(0);
            double[] pt2 = points.GetPoint(1);
            double[] pt3 = pt;
            double[] normal = new double[3];

            vtkTriangle tri = new vtkTriangle();

            // Note Circumcircle ignores z component, so first need to rotate
            // triangle so normal points in z direction.
            tri.ComputeNormal(pt1, pt2, pt3, normal);

            if (MathUtil.vnorm(normal) == 0.0)
            {
                // Cannot fit a circle so reset and return
                resetCircumferencePoints();
                return false;
            }

            double[] zaxis = {0.0, 0.0, 1.0};
            double[] cross = new double[3];
            MathUtil.vcrss(zaxis, normal, cross);
            // Compute angle between normal and zaxis
            double sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

            vtkTransform transform = new vtkTransform();
            transform.RotateWXYZ(sepAngle, cross);

            pt1 = transform.TransformDoublePoint(pt1);
            pt2 = transform.TransformDoublePoint(pt2);
            pt3 = transform.TransformDoublePoint(pt3);

            double[] center = new double[3];
            double radius = Math.sqrt(tri.Circumcircle(pt1, pt2, pt3, center));
            // Note Circumcircle ignores z component, so set it here.
            center[2] = pt1[2];

            center = transform.GetInverse().TransformDoublePoint(center);

            setDefaultRadius(radius);
            addNewStructure(center);

            resetCircumferencePoints();
        }

        return true;
    }

    /**
     * Cancels the new circle currently being created so you can start again.
     * Intermediate points are deleted.
     */
    public void resetCircumferencePoints()
    {
        if (selectionPolyData.GetNumberOfPoints() > 0)
        {
            vtkPoints points = new vtkPoints();
            vtkCellArray cells = new vtkCellArray();
            selectionPolyData.SetPoints(points);
            selectionPolyData.SetVerts(cells);

            selectionPolyData.Modified();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }
}