package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vtk.vtkPolyData;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.LatLon;


public abstract class PolyhedralModel extends AbstractModel
{
    static public final String FlatShadingStr = "Flat";
    static public final String SmoothShadingStr = "Smooth";

    private PolyhedralModelConfig polyhedralModelConfig;
    public PolyhedralModel(PolyhedralModelConfig polyhedralModelConfig)
    {
        this.polyhedralModelConfig = polyhedralModelConfig;
    }

    public abstract void updateScaleBarValue(double pixelSizeInKm);

    public abstract void updateScaleBarPosition(int windowWidth, int windowHeight);

    public abstract vtksbCellLocator getCellLocator();

    public abstract BoundingBox getBoundingBox();

    public abstract void setShowScaleBar(boolean enabled);

    public abstract boolean getShowScaleBar();

    public abstract vtkPolyData getSmallBodyPolyData();

    public abstract boolean isEllipsoid();


    public abstract String getCustomDataFolder();

    public abstract String getConfigFilename();

    public PolyhedralModelConfig getPolyhedralModelConfig()
    {
        return polyhedralModelConfig;
    }


    public void setOpacity(double opacity)
    {
        // Do nothing. Subclasses should redefine this if they support opacity.
    }

    public double getOpacity()
    {
        // Subclasses should redefine this if they support opacity.
        return 1.0;
    }


    public double getDefaultOffset()
    {
        // Subclasses should redefine this if they support offset.
        return 0.0;
    }

    public List<vtkPolyData> getSmallBodyPolyDatas() { return null; }

    public abstract void setPointSize(double value);

    public abstract void setLineWidth(double value);

    public abstract void setSpecularCoefficient(double value);

    public abstract void setSpecularPower(double value);

    public abstract void setRepresentationToSurface();

    public abstract void setRepresentationToWireframe();

    public abstract void setRepresentationToPoints();

    public abstract void setRepresentationToSurfaceWithEdges();

    public abstract void setCullFrontface(boolean enable);

    public abstract void setShadingToFlat();

    public abstract void setShadingToSmooth();

    public abstract int getColoringIndex();

    public abstract void setColoringIndex(int index) throws IOException;

    public abstract double[] getCurrentColoringRange(int coloringIndex);

    public abstract void setCurrentColoringRange(int coloringIndex, double[] range) throws IOException;

    public abstract double[] getDefaultColoringRange(int coloringIndex);

    public abstract String getColoringName(int i);

    public abstract void drawEllipticalPolygon(
            double[] center,
            double radius,
            double flattening,
            double angle,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary);

    public abstract double getBoundingBoxDiagonalLength();

    public abstract void shiftPolyLineInNormalDirection(
            vtkPolyData polyLine,
            double shiftAmount);

    public abstract int getPointAndCellIdFromLatLon(double lat, double lon, double[] intersectPoint);

    public abstract double[] getNormalAtPoint(double[] point);

    public abstract boolean isColoringDataAvailable();

    public abstract double[] getAllColoringValues(double[] pt);

    public abstract double[] getGravityVector(double[] pt);

    public abstract double getMinShiftAmount();

    public abstract void savePlateDataInsidePolydata(vtkPolyData polydata, File file) throws IOException;

    public abstract String getModelName();

    public abstract vtkPolyData drawPath(
            double[] pt1,
            double[] pt2);

    public abstract double[] findClosestPoint(double[] pt);

    public abstract double getColoringValue(int index, double[] pt);

    public abstract int getNumberOfColors();

    public abstract String getColoringUnits(int i);

    public abstract double getDensity();

    public abstract double getRotationRate();

    public abstract double getReferencePotential();

    public abstract double[] getClosestNormal(double[] point);

    public abstract void drawPolygon(
            ArrayList<LatLon> controlPoints,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary);







}
