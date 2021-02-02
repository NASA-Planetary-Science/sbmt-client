package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkPolyData;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Interface for spectra renderer
 * @author steelrj1
 *
 */
public interface IBasicSpectrumRenderer<S extends BasicSpectrum> extends PropertyChangeListener, Model
{
	void generateFootprint();

	List<vtkProp> getProps();

	void propertyChange(PropertyChangeEvent evt);

	void addPropertyChangeListener(PropertyChangeListener listener);

	void removePropertyChangeListener(PropertyChangeListener listener);

	void shiftFootprintToHeight(double h);

	public double[] getSpacecraftPosition();

	/**
	 * The shifted footprint is the original footprint shifted slightly in the
	 * normal direction so that it will be rendered correctly and not obscured
	 * by the asteroid.
	 *
	 * @return
	 */
	vtkPolyData getShiftedFootprint();

	/**
	 * The original footprint whose cells exactly overlap the original asteroid.
	 * If rendered as is, it would interfere with the asteroid.
	 *
	 * @return
	 */
	vtkPolyData getUnshiftedFootprint();

	void Delete();

	void setSelected();

	void setUnselected();

	void setShowFrustum(boolean show);

	void setVisible(boolean b);

	boolean isVisible();

	void updateChannelColoring();

	void setShowToSunVector(boolean b);

	void setShowOutline(boolean b);

	vtkPolyData getSelectionPolyData();

	double getMinFootprintHeight();

	boolean isSelected();

	boolean isToSunVectorShowing();

	boolean isOutlineShowing();

	public S getSpectrum();

	public boolean isFrustumShowing();

	public vtkActor getOutlineActor();

	public double[] getColor();

	public void setColor(double[] color);

	public double getMinIncidence();

	public double getMaxIncidence();

	public double getMinEmission();

	public double getMaxEmission();

	public double getMinPhase();

	public double getMaxPhase();

	public double getMinRange();

	public double getMaxRange();

}