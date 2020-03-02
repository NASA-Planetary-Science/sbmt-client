package edu.jhuapl.sbmt.client;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import vtk.vtkFloatArray;
import vtk.vtkPolyData;
import vtk.vtksbCellLocator;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Frustum;

public interface ISmallBodyModel
{

	ISmallBodyViewConfig getSmallBodyConfig();

	String serverPath(String fileName);

	double getBoundingBoxDiagonalLength();

	vtkPolyData computeMultipleFrustumIntersection(List<Frustum> frusta);

	double getMinShiftAmount();

	public void addPropertyChangeListener( PropertyChangeListener listener );

	public void removePropertyChangeListener( PropertyChangeListener listener );

	int getPointAndCellIdFromLatLon(double lllat, double d, double[] intersectPoint);

	vtkPolyData getSmallBodyPolyData();

	boolean isEllipsoid();

	vtksbCellLocator getCellLocator();

	String getModelName();

	vtkPolyData computeFrustumIntersection(double[] ds, double[] ds2, double[] ds3, double[] ds4, double[] ds5);

	int getModelResolution();

	vtkFloatArray getCellNormals();

	double[] getAllColoringValues(double[] closestPoint) throws IOException;

	int computeRayIntersection(double[] spacecraftPosition, double[] direction, double[] focalPoint);

	void shiftPolyLineInNormalDirection(vtkPolyData boundary, double offset);

	public void drawRegularPolygonLowRes(double[] center, double radius, int numberOfSides, vtkPolyData outputInterior, vtkPolyData outputBoundary);

	TreeSet<Integer> getIntersectingCubes(vtkPolyData interiorPoly);

	BoundingBox getBoundingBox();

	String getCustomDataFolder();

}