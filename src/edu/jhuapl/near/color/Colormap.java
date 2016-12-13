package edu.jhuapl.near.color;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import vtk.vtkLookupTable;

public interface Colormap
{
	public vtkLookupTable getLookupTable();
	public double getRangeMin();
	public double getRangeMax();
	public void setRangeMin(double val);
	public void setRangeMax(double val);
	public void setLog(boolean flag);
	public boolean getLog();	// true if log, false if linear
	public void setNumberOfLevels(int n);
	public int getNumberOfLevels();
	public String getName();
	public Color getColor(double val);
	public Color getNanColor();
	public void addPropertyChangeListener(PropertyChangeListener l);
	public void removePropertyChangeListener(PropertyChangeListener l);
}
