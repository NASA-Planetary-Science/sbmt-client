package edu.jhuapl.sbmt.image2.modules.rendering.vtk;

import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.api.PixelVector;
import edu.jhuapl.sbmt.image2.impl.BasicPixelVectorDouble.ScalarPixel;
import edu.jhuapl.sbmt.image2.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.image2.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class VtkImageRendererOperator
		extends BasePipelineOperator<Layer, vtkImageData>
{
	vtkImageData output;
	PixelDoubleFactory pixelDoubleFactory;

	public VtkImageRendererOperator()
	{
		pixelDoubleFactory = new PixelDoubleFactory();
	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs = new ArrayList<vtkImageData>();
		PixelVector pixel = new PixelVectorDoubleFactory().of(3, -Double.NaN, -Double.NaN);
		int layerWidth = inputs.get(0).iSize();
		int layerHeight = inputs.get(0).jSize();
		int layerDepth = inputs.get(0).dataSizes().get(0);
		output = new vtkImageData();
//		 if (transpose)
        output.SetDimensions(layerWidth, layerHeight, layerDepth);
//	        else
//        output.SetDimensions(layerHeight, layerWidth, 1);
        output.SetSpacing(1.0, 1.0, 1.0);
        output.SetOrigin(0.0, 0.0, 0.0);
        output.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR, layerDepth);

		for (int i = 0; i < layerWidth; i++)
		{
			for (int j = 0; j < layerHeight; j++)
			{
				inputs.get(0).get(i, j, pixel);
				for (int k = 0; k < layerDepth; k++)
				{
					ScalarPixel vecPixel = (ScalarPixel)pixel.get(k);
					output.SetScalarComponentFromFloat(i, j, 0, k, vecPixel.get());
				}
			}
		}
		outputs.add(output);
	}
}
