package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCubeSource;
import vtk.vtkMatrix4x4;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

import edu.jhuapl.sbmt.stateHistory.rendering.text.GenericVTKLabel;

/**
 * @author steelrj1
 *
 */
public class SpacecraftBody extends vtkPolyData implements DisplayableItem
{
	/**
	 * The vtkActor associated with this vtkPolyData
	 */
	private vtkActor spacecraftBodyActor;

    /**
     * The default color of the spacecraft
     */
    private double[] spacecraftColor = {1.0, 0.7, 0.4, 1.0};

    /**
     * Static instance of the white color double array
     */
    private static double[] white = {1.0, 1.0, 1.0, 1.0};

    private String labelPrefix = "Spacecraft";

    private String label;

    protected GenericVTKLabel labelActor;

    protected ArrayList<vtkProp> props;

	/**
	 * Constructor.  Loads a shapemodel with the given filename and
	 * copies the vtkPolydata into this object
	 *
	 * @param filename	Filename of the spacecraft obj to load
	 */
	public SpacecraftBody(String filename)
	{
		vtkCubeSource cube = new vtkCubeSource();
		cube.SetXLength(.25);
		cube.SetYLength(.25);
		cube.SetZLength(.25);
		cube.Update();
		try
		{
			ShallowCopy(cube.GetOutput());

//			ShallowCopy(PolyDataUtil.loadShapeModel(filename));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		labelActor = new GenericVTKLabel();
        labelActor.setText(label);
	}

	/**
	 * vtk Constructor
	 * @param id
	 */
	public SpacecraftBody(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns the vtkActor for this polydata
	 * @return the vtkActor for this polydata
	 */
	public ArrayList<vtkProp> getActor()
	{
		if (props != null)
			return props;
		props = new ArrayList<vtkProp>();

		vtkPolyDataMapper spacecraftBodyMapper = new vtkPolyDataMapper();
        spacecraftBodyMapper.SetInputData(this);
        spacecraftBodyActor = new vtkActor();
        spacecraftBodyActor.SetMapper(spacecraftBodyMapper);
        spacecraftBodyActor.GetProperty().SetDiffuseColor(spacecraftColor);
        spacecraftBodyActor.GetProperty().SetSpecularColor(white);
        spacecraftBodyActor.GetProperty().SetSpecular(0.8);
        spacecraftBodyActor.GetProperty().SetSpecularPower(80.0);
        spacecraftBodyActor.GetProperty().ShadingOn();
        spacecraftBodyActor.GetProperty().SetInterpolationToFlat();
        spacecraftBodyActor.SetScale(0.5);
        props.add(spacecraftBodyActor);
		props.add(labelActor);
		return props;
	}

	/**
	 * Sets the current scale of the spacecraft
	 * @param scale	the current scale of the spacecraft from 0 to 1
	 */
	public void setScale(double scale)
	{
		spacecraftBodyActor.SetScale(scale);
	}

	/**
	 * Sets the color of the spacecraft actor
	 * @param color	the color of the spacecraft actor, where the color is a double array with values from 0.0 to 1.0
	 */
	public void setColor(double[] color)
	{
		spacecraftBodyActor.GetProperty().SetDiffuseColor(spacecraftColor);
	}

	/**
	 * Sets the color of the spacecraft actor
	 * @param color	the color of the spacecraft actor, where the color is a Color object
	 */
	public void setColor(Color color)
	{
		spacecraftBodyActor.GetProperty().SetDiffuseColor(new double[] {color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0});
	}

	@Override
	public boolean isVisible()
	{
		return spacecraftBodyActor.GetVisibility() == 1 ? true : false;
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		spacecraftBodyActor.SetVisibility(isVisible ? 1 : 0);
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
	public Color getColor()
	{
		return new Color((float)spacecraftBodyActor.GetProperty().GetDiffuseColor()[0], (float)spacecraftBodyActor.GetProperty().GetDiffuseColor()[1], (float)spacecraftBodyActor.GetProperty().GetDiffuseColor()[2]);

	}

	@Override
	public void setPointerRadius(double radius)
	{
		this.setScale(radius);
	}

	@Override
	public double getPointerRadius()
	{
		return spacecraftBodyActor.GetScale()[0];
	}

	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return labelPrefix;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label)
	{
//		this.label = label;
		labelActor.setText(labelPrefix + ":\n" + label);
		labelActor.Modified();
	}

	/**
	 * Sets the font for the displayed string
	 * @param font
	 */
	public void setStringFont(Font font)
	{
		labelActor.GetCaptionTextProperty().SetFontSize(font.getSize());
		labelActor.GetCaptionTextProperty().SetFontFamilyAsString(font.getFamily());
		if (font.isBold()) labelActor.GetCaptionTextProperty().BoldOn(); else labelActor.GetCaptionTextProperty().BoldOff();
		if (font.isItalic()) labelActor.GetCaptionTextProperty().ItalicOn(); else labelActor.GetCaptionTextProperty().ItalicOff();
	}

	public void setUserMatrix(vtkMatrix4x4 matrix)
	{
		spacecraftBodyActor.SetUserMatrix(matrix);
	}

	public void setLabelPosition(double[] position)
	{
		labelActor.SetAttachmentPoint(position);
        labelActor.Modified();
	}

}
