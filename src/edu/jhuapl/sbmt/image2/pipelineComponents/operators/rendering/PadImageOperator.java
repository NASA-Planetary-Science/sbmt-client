package edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering;

import java.io.IOException;

import vtk.vtkImageConstantPad;
import vtk.vtkImageData;
import vtk.vtkImageTranslateExtent;

import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class PadImageOperator extends BasePipelineOperator<vtkImageData, vtkImageData>
{
	private int xShift, yShift, xSize, ySize;

	public PadImageOperator(int xShift, int yShift, int xSize, int ySize)
	{
		this.xShift = xShift;
		this.yShift = yShift;
		this.xSize = xSize;
		this.ySize = ySize;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkImageData rawImage = inputs.get(0);
		if (rawImage.GetDimensions()[0] != xSize || rawImage.GetDimensions()[1] != ySize)
		{
			vtkImageTranslateExtent translateExtent = new vtkImageTranslateExtent();
			translateExtent.SetInputData(rawImage);
			translateExtent.SetTranslation(xShift, yShift, 0);
			translateExtent.Update();

			vtkImageConstantPad pad = new vtkImageConstantPad();
			pad.SetInputConnection(translateExtent.GetOutputPort());
			pad.SetOutputWholeExtent(0, xSize-1, 0, ySize-1, 0, 0);
			pad.Update();

			vtkImageData padOutput = pad.GetOutput();
			rawImage.DeepCopy(padOutput);

			// shift origin back to zero
			rawImage.SetOrigin(0.0, 0.0, 0.0);
		}

		outputs.add(rawImage);
	}
}
