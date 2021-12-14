package edu.jhuapl.sbmt.image2.modules.rendering.vtk;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;
import vtk.vtkTexture;

import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class VTKImagePolyDataRenderer extends BasePipelineOperator<Pair<vtkImageData, vtkPolyData>, vtkActor>
{
	public VTKImagePolyDataRenderer()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkPolyData footprint = inputs.get(0).getRight();

		vtkTexture imageTexture = new vtkTexture();
        imageTexture.InterpolateOn();
        imageTexture.RepeatOff();
        imageTexture.EdgeClampOn();
        imageTexture.SetInputData(inputs.get(0).getLeft());

		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.ScalarVisibilityOff();
        mapper.SetScalarModeToDefault();
		mapper.SetInputData(footprint);
		mapper.Update();

		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		actor.SetTexture(imageTexture);
        vtkProperty footprintProperty = actor.GetProperty();
        footprintProperty.LightingOff();

        outputs.add(actor);
	}

}
