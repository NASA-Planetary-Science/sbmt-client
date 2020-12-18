package edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers;

import java.awt.Color;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

import edu.jhuapl.sbmt.stateHistory.rendering.DisplayableItem;
import edu.jhuapl.sbmt.stateHistory.rendering.text.GenericVTKLabel;

public abstract class BaseDirectionMarker extends vtkConeSource implements DisplayableItem
{

	/**
	 * Easy access for white color
	 */
	protected double[] white =
	{ 1.0, 1.0, 1.0, 1.0 };

	/**
	 * Easy access for zAxis
	 */
	protected double[] zAxis =
	{ 1, 0, 0 };

	/**
	 * Scale of the marker
	 */
	protected double scale = 1.0;

	/**
	 * Radius of the marker
	 */
	protected double markerRadius;

	/**
	 * Height of the marker above the surface
	 */
	protected double markerHeight;

	/**
	 * Center coordinates for marker
	 */
	protected double centerX, centerY, centerZ;

	/**
	 * The vtkActor representing the marker head
	 */
	protected vtkActor markerHeadActor;

	/**
	 * The color of the marker
	 */
	protected double[] markerColor =
	{ 0.0, 0.0, 1.0, 1.0 };

	/**
	 *
	 */
	protected double specularColorValue;

	protected String label;

	protected double pointerRadius = 0.5;

	protected GenericVTKLabel labelActor;

	protected ArrayList<vtkProp> props;

	public BaseDirectionMarker(long id)
	{
		super(id);
	}

	/**
	 * @param markerRadius
	 * @param markerHeight
	 * @param centerX
	 * @param centerY
	 * @param centerZ
	 */
	public BaseDirectionMarker(double markerRadius, double markerHeight, double centerX, double centerY, double centerZ,
			String label)
	{
		this.markerHeight = markerHeight;
		this.markerRadius = markerRadius;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.label = label;
        labelActor = new GenericVTKLabel();
        labelActor.setText(label);
		updateSource();
	}

	/**
	 * @return
	 */
	public ArrayList<vtkProp> getActor()
	{
		if (props != null)
			return props;
		props = new ArrayList<vtkProp>();

		vtkPolyDataMapper markerHeadMapper = new vtkPolyDataMapper();
		markerHeadMapper.SetInputData(GetOutput());
		markerHeadActor = new vtkActor();
		markerHeadActor.SetMapper(markerHeadMapper);
		markerHeadActor.GetProperty().SetDiffuseColor(markerColor);
		markerHeadActor.GetProperty().SetSpecularColor(white);
		markerHeadActor.GetProperty().SetSpecular(specularColorValue);
		markerHeadActor.GetProperty().SetSpecularPower(80.0);
		markerHeadActor.GetProperty().ShadingOn();
		markerHeadActor.GetProperty().SetInterpolationToPhong();
		markerHeadActor.SetScale(scale);
		props.add(markerHeadActor);
		props.add(labelActor);
		return props;
	}

	/**
	 * @param color
	 */
	public void setColor(Color color)
	{
		markerHeadActor.GetProperty().SetDiffuseColor(new double[]
		{ color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0 });
	}

	/**
	 * @param radius
	 */
	public void setPointerSize(double radius)
	{
		scale = (2.66e-4 * Math.pow((double) 100*radius, 2) + 1e-4 * (double) 100*radius + .33);
		markerHeadActor.SetScale(scale);
		markerHeadActor.Modified();
	}

	/**
	 *
	 */
	private void updateSource()
	{
		SetRadius(markerRadius);
		SetHeight(markerHeight);
		SetCenter(centerX, centerY, centerZ);
		SetResolution(50);
		Update();
	}

	@Override
	public Color getColor()
	{
		return new Color((float) markerHeadActor.GetProperty().GetDiffuseColor()[0],
				(float) markerHeadActor.GetProperty().GetDiffuseColor()[1],
				(float) markerHeadActor.GetProperty().GetDiffuseColor()[2]);
	}

	@Override
	public boolean isVisible()
	{
		return markerHeadActor.GetVisibility() == 1 ? true : false;
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		markerHeadActor.SetVisibility(isVisible ? 1 : 0);
	}

	@Override
	public boolean isLabelVisible()
	{
		return labelActor.GetVisibility() == 1 ? true : false;
	}

	@Override
	public void setLabelVisible(boolean isVisible)
	{
		labelActor.SetVisibility(isVisible ? 1 : 0);
	}

	@Override
	public void setPointerRadius(double radius)
	{
		this.pointerRadius = radius;
		this.setPointerSize(radius);
	}

	@Override
	public double getPointerRadius()
	{
		return pointerRadius;
	}

	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label)
	{
		this.label = label;
		labelActor.setText(label);
		labelActor.Modified();
	}

}
