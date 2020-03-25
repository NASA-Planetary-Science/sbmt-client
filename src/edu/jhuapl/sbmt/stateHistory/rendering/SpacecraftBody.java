package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.util.PolyDataUtil;

/**
 * @author steelrj1
 *
 */
public class SpacecraftBody extends vtkPolyData
{
	/**
	 *
	 */
	private vtkActor spacecraftBodyActor;
    /**
     *
     */
    private double[] spacecraftColor = {1.0, 0.7, 0.4, 1.0};
    /**
     *
     */
    private double[] white = {1.0, 1.0, 1.0, 1.0};


	/**
	 * @param filename
	 */
	public SpacecraftBody(String filename)
	{
		try
		{
			ShallowCopy(PolyDataUtil.loadShapeModel(filename));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param id
	 */
	public SpacecraftBody(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return
	 */
	public vtkActor getActor()
	{
		if (spacecraftBodyActor != null) return spacecraftBodyActor;
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
        spacecraftBodyActor.SetScale(0.01);
        return spacecraftBodyActor;
	}

	/**
	 * @param scale
	 */
	public void setScale(double scale)
	{
		spacecraftBodyActor.SetScale(scale);
	}

	/**
	 * @param color
	 */
	public void setColor(double[] color)
	{
		spacecraftBodyActor.GetProperty().SetDiffuseColor(spacecraftColor);
	}

	/**
	 * @param color
	 */
	public void setColor(Color color)
	{
		spacecraftBodyActor.GetProperty().SetDiffuseColor(new double[] {color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0});
	}

}
