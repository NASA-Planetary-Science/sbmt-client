package edu.jhuapl.near.model;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import vtk.vtkPolyData;

import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;

public class Polygon extends StructureModel.Structure
{
    public String name = "default";
    public int id;

    // These are the control points.
    public ArrayList<LatLon> controlPoints = new ArrayList<LatLon>();

    public vtkPolyData boundaryPolyData;
    public vtkPolyData interiorPolyData;

    public int[] color;

    private SmallBodyModel smallBodyModel;

    private double surfaceArea = 0.0;
    private double perimeterLength = 0.0;

    private static final int[] purpleColor = {255, 0, 255, 255}; // RGBA purple
    private static DecimalFormat decimalFormatter = new DecimalFormat("#.###");

    private static int maxId = 0;

    public static final String POLYGON = "polygon";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String VERTICES = "vertices";
    public static final String AREA = "area";
    public static final String LENGTH = "length";
    public static final String COLOR = "color";

    public Polygon(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
        id = ++maxId;
        boundaryPolyData = new vtkPolyData();
        interiorPolyData = new vtkPolyData();
        color = (int[])purpleColor.clone();
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
        return POLYGON;
    }

    public String getInfo()
    {
        return "Area: " + decimalFormatter.format(surfaceArea) + " km^2, Length: " + decimalFormatter.format(perimeterLength) + " km, " + controlPoints.size() + " vertices";
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
        Element polyEle = dom.createElement(POLYGON);
        polyEle.setAttribute(ID, String.valueOf(id));
        polyEle.setAttribute(NAME, name);
        polyEle.setAttribute(AREA, String.valueOf(surfaceArea));
        polyEle.setAttribute(LENGTH, String.valueOf(perimeterLength));

        String colorStr = color[0] + "," + color[1] + "," + color[2];
        polyEle.setAttribute(COLOR, colorStr);

        String vertices = "";
        int size = controlPoints.size();

        for (int i=0;i<size;++i)
        {
            LatLon ll = controlPoints.get(i);
            double latitude = ll.lat*180.0/Math.PI;
            double longitude = ll.lon*180.0/Math.PI;
            if (longitude < 0.0)
                longitude += 360.0;

            vertices += latitude + " " + longitude + " " + ll.rad;

            if (i < size-1)
                vertices += " ";
        }

        polyEle.setAttribute(VERTICES, vertices);

        return polyEle;
    }

    public void fromXmlDomElement(Element element, String shapeModelName)
    {
        controlPoints.clear();

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
            controlPoints.add(new LatLon(
                    Double.parseDouble(tokens[i++])*Math.PI/180.0,
                    Double.parseDouble(tokens[i++])*Math.PI/180.0,
                    Double.parseDouble(tokens[i++])));

            if (shapeModelName == null || !shapeModelName.equals(smallBodyModel.getModelName()))
                shiftPointOnPathToClosestPointOnAsteroid(count);

            ++count;
        }

        updatePolygon(controlPoints);

        tmp = element.getAttribute(COLOR);
        if (tmp.length() == 0)
            return;
        tokens = tmp.split(",");
        color[0] = Integer.parseInt(tokens[0]);
        color[1] = Integer.parseInt(tokens[1]);
        color[2] = Integer.parseInt(tokens[2]);
    }

    public String getClickStatusBarText()
    {
        return "Path, Id = " + id
        + ", Length = " + decimalFormatter.format(perimeterLength) + " km"
        + ", Surface Area = " + decimalFormatter.format(surfaceArea) + " km"
        + ", Number of Vertices = " + controlPoints.size();
    }

    public void updatePolygon(ArrayList<LatLon> controlPoints)
    {
        this.controlPoints = (ArrayList<LatLon>) controlPoints.clone();

        smallBodyModel.drawPolygon(controlPoints, interiorPolyData, boundaryPolyData);

        surfaceArea = PolyDataUtil.computeSurfaceArea(interiorPolyData);
        perimeterLength = PolyDataUtil.computeLength(boundaryPolyData);
    }

    public void shiftPointOnPathToClosestPointOnAsteroid(int idx)
    {
        // When the resolution changes, the control points, might no longer
        // be touching the asteroid. Therefore shift each control to the closest
        // point on the asteroid.
        LatLon llr = controlPoints.get(idx);
        double pt[] = MathUtil.latrec(llr);
        double[] closestPoint = smallBodyModel.findClosestPoint(pt);
        llr = MathUtil.reclat(closestPoint);
        controlPoints.set(idx, llr);
    }
}
