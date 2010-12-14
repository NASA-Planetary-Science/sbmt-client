package edu.jhuapl.near.model;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Point3D;

import vtk.vtkPoints;
import vtk.vtkPolyData;

public class Line extends StructureModel.Structure
{
    public String name = "default";
    public int id;

    // Note the lat, lon, and alt is what gets stored in the saved file.
    // These are the control points.
    public ArrayList<Double> lat = new ArrayList<Double>();
    public ArrayList<Double> lon = new ArrayList<Double>();
    public ArrayList<Double> rad = new ArrayList<Double>();

    // Note xyzPointList is what's displayed. There will usually be more of these points than
    // lat, lon, alt in order to ensure the line is right above the surface of the asteroid.
    public ArrayList<Point3D> xyzPointList = new ArrayList<Point3D>();
    public ArrayList<Integer> controlPointIds = new ArrayList<Integer>();
    public int[] color;

    private SmallBodyModel smallBodyModel;

    private static final int[] purpleColor = {255, 0, 255, 255}; // RGBA purple
    private static DecimalFormat decimalFormatter = new DecimalFormat("#.###");

    private static int maxId = 0;

    public static String PATH = "path";
    public static String ID = "id";
    public static String NAME = "name";
    public static String VERTICES = "vertices";
    public static String LENGTH = "length";

    public Line(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
        id = ++maxId;
        color = purpleColor;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return PATH;
    }

    public String getInfo()
    {
        return decimalFormatter.format(getPathLength()) + " km, " + controlPointIds.size() + " vertices";
    }

    public int[] getColor()
    {
        return color;
    }

    public void setColor(int[] color)
    {
        this.color = (int[])color.clone();
    }

    public Element toXmlDomElement(Document dom)
    {
        Element linEle = dom.createElement(PATH);
        linEle.setAttribute(ID, String.valueOf(id));
        linEle.setAttribute(NAME, name);
        linEle.setAttribute(LENGTH, String.valueOf(getPathLength()));

        String vertices = "";
        int size = lat.size();

        for (int i=0;i<size;++i)
        {
            double latitude = lat.get(i)*180.0/Math.PI;
            double longitude = lon.get(i)*180.0/Math.PI;
            if (longitude < 0.0)
                longitude += 360.0;

            vertices += latitude + " " + longitude + " " + rad.get(i);

            if (i < size-1)
                vertices += " ";
        }

        linEle.setAttribute(VERTICES, vertices);

        return linEle;
    }

    public void fromXmlDomElement(Element element, String shapeModelName)
    {
        lat.clear();
        lon.clear();
        rad.clear();
        controlPointIds.clear();
        xyzPointList.clear();

        id = Integer.parseInt(element.getAttribute(ID));

        if (id > maxId)
            maxId = id;

        name = element.getAttribute(NAME);
        String tmp = element.getAttribute(VERTICES);

        if (tmp.length() == 0)
            return;

        String[] tokens = tmp.split(" ");

        int count = 0;
        for (int i=0; i<tokens.length;)
        {
            lat.add(Double.parseDouble(tokens[i++])*Math.PI/180.0);
            lon.add(Double.parseDouble(tokens[i++])*Math.PI/180.0);
            rad.add(Double.parseDouble(tokens[i++]));

            if (shapeModelName == null || !shapeModelName.equals(smallBodyModel.getModelName()))
                shiftPointOnPathToClosestPointOnAsteroid(count);

            controlPointIds.add(xyzPointList.size());

            // Note, this point will be replaced with the correct value
            // when we call updateSegment
            double[] dummy = {0.0, 0.0, 0.0};
            xyzPointList.add(new Point3D(dummy));

            if (count > 0)
                this.updateSegment(count-1);

            ++count;
        }

    }

    public String getClickStatusBarText()
    {
        return "Path, Id = " + id
        + ", Length = " + decimalFormatter.format(getPathLength()) + " km"
        + ", Number of Vertices = " + lat.size();
    }

    public double getPathLength()
    {
        int size = xyzPointList.size();
        double length = 0.0;

        for (int i=1;i<size;++i)
        {
            double dist = xyzPointList.get(i-1).distanceTo(xyzPointList.get(i));
            length += dist;
        }

        return length;
    }

    public void updateSegment(int segment)
    {
        LatLon ll1 = new LatLon(lat.get(segment), lon.get(segment), rad.get(segment));
        LatLon ll2 = new LatLon(lat.get(segment+1), lon.get(segment+1), rad.get(segment+1));
        double pt1[] = MathUtil.latrec(ll1);
        double pt2[] = MathUtil.latrec(ll2);

        int id1 = controlPointIds.get(segment);
        int id2 = controlPointIds.get(segment+1);

        // Set the 2 control points
        xyzPointList.set(id1, new Point3D(pt1));
        xyzPointList.set(id2, new Point3D(pt2));

        vtkPoints points = null;
        if (Math.abs(lat.get(segment) - lat.get(segment+1)) < 1e-8 &&
                Math.abs(lon.get(segment) - lon.get(segment+1)) < 1e-8 &&
                Math.abs(rad.get(segment) - rad.get(segment+1)) < 1e-8)
        {
            points = new vtkPoints();
            points.InsertNextPoint(pt1);
            points.InsertNextPoint(pt2);
        }
        else
        {
            vtkPolyData poly = smallBodyModel.drawPath(pt1, pt2);
            if (poly == null)
                return;

            points = poly.GetPoints();
        }

        // Remove points BETWEEN the 2 control points
        for (int i=0; i<id2-id1-1; ++i)
        {
            xyzPointList.remove(id1+1);
        }

        // Set the new points
        int numNewPoints = points.GetNumberOfPoints();
        for (int i=1; i<numNewPoints-1; ++i)
        {
            xyzPointList.add(id1+i, new Point3D(points.GetPoint(i)));
        }

        // Shift the control points ids from segment+1 till the end by the right amount.
        int shiftAmount = id1+numNewPoints-1 - id2;
        for (int i=segment+1; i<controlPointIds.size(); ++i)
        {
            controlPointIds.set(i, controlPointIds.get(i) + shiftAmount);
        }

    }

    public void shiftPointOnPathToClosestPointOnAsteroid(int idx)
    {
        // When the resolution changes, the control points, might no longer
        // be touching the asteroid. Therefore shift each control to the closest
        // point on the asteroid.
        LatLon llr = new LatLon(lat.get(idx), lon.get(idx), rad.get(idx));
        double pt[] = MathUtil.latrec(llr);
        double[] closestPoint = smallBodyModel.findClosestPoint(pt);
        LatLon ll = MathUtil.reclat(closestPoint);
        lat.set(idx, ll.lat);
        lon.set(idx, ll.lon);
        rad.set(idx, ll.rad);
    }
}
