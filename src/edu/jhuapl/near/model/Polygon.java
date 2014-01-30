package edu.jhuapl.near.model;

import vtk.vtkPolyData;

import edu.jhuapl.near.util.PolyDataUtil;

public class Polygon extends Line
{
    public vtkPolyData interiorPolyData;
    private SmallBodyModel smallBodyModel;
    private double surfaceArea = 0.0;
    private boolean showInterior = false;

    public static final String POLYGON = "polygon";

    public Polygon(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, true);

        this.smallBodyModel = smallBodyModel;
        interiorPolyData = new vtkPolyData();
    }

    public String getType()
    {
        return POLYGON;
    }

    public String getInfo()
    {
        return "Area: " + decimalFormatter.format(surfaceArea) + " km^2, Length: " + decimalFormatter.format(getPathLength()) + " km, " + controlPoints.size() + " vertices";
    }

    public String getClickStatusBarText()
    {
        return "Polygon, Id = " + id
        + ", Length = " + decimalFormatter.format(getPathLength()) + " km"
        + ", Surface Area = " + decimalFormatter.format(surfaceArea) + " km^2"
        + ", Number of Vertices = " + controlPoints.size();
    }

    public void setShowInterior(boolean showInterior)
    {
        this.showInterior = showInterior;

        if (showInterior)
        {
            smallBodyModel.drawPolygon(controlPoints, interiorPolyData, null);
            surfaceArea = PolyDataUtil.computeSurfaceArea(interiorPolyData);
        }
        else
        {
            PolyDataUtil.clearPolyData(interiorPolyData);
            surfaceArea = 0.0;
        }
    }

    public boolean isShowInterior()
    {
        return showInterior;
    }
}
