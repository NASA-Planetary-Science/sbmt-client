package edu.jhuapl.sbmt.image2.pipeline.rendering.vtk;

import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelVector;
import edu.jhuapl.sbmt.layer.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

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
		int layerWidth = inputs.get(0).iSize();
		int layerHeight = inputs.get(0).jSize();
		int layerDepth = inputs.get(0).dataSizes().get(0);
		output = new vtkImageData();
		output.SetSpacing(1.0, 1.0, 1.0);
        output.SetOrigin(0.0, 0.0, 0.0);
        output.SetDimensions(layerWidth, layerHeight, layerDepth);
        if (layerDepth == 1)
        {
			PixelDouble pixel = pixelDoubleFactory.of(0, -Double.NaN, -Double.NaN);
	        output.AllocateScalars(VtkDataTypes.VTK_FLOAT, 1);

			for (int i = 0; i < layerWidth; i++)
			{
				for (int j = 0; j < layerHeight; j++)
				{
					inputs.get(0).get(i, j, pixel);
					output.SetScalarComponentFromDouble(i, j, 0, 0, pixel.get());
				}
			}
        }
        else if (layerDepth >= 3)
        {
        	PixelVector pixel = new PixelVectorDoubleFactory().of(layerDepth, -Double.NaN, -Double.NaN);
        	output.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR, layerDepth);
    		for (int i = 0; i < layerWidth; i++)
    		{
    			for (int j = 0; j < layerHeight; j++)
    			{
    				inputs.get(0).get(i, j, pixel);
    				for (int k = 0; k < layerDepth; k++)
    				{
    					PixelDouble vecPixel = (PixelDouble)pixel.get(k);
    					output.SetScalarComponentFromFloat(i, j, 0, k, vecPixel.get());
    				}
    			}
    		}
        }
		outputs.add(output);


//		outputs = new ArrayList<vtkImageData>();
//		PixelVector pixel = new PixelVectorDoubleFactory().of(3, -Double.NaN, -Double.NaN);
//		int layerWidth = inputs.get(0).iSize();
//		int layerHeight = inputs.get(0).jSize();
//		int layerDepth = inputs.get(0).dataSizes().get(0);
//		output = new vtkImageData();
////		 if (transpose)
//        output.SetDimensions(layerWidth, layerHeight, layerDepth);
////	        else
////        output.SetDimensions(layerHeight, layerWidth, 1);
//        output.SetSpacing(1.0, 1.0, 1.0);
//        output.SetOrigin(0.0, 0.0, 0.0);
//        output.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR, layerDepth);
//
//		for (int i = 0; i < layerWidth; i++)
//		{
//			for (int j = 0; j < layerHeight; j++)
//			{
//				inputs.get(0).get(i, j, pixel);
//				for (int k = 0; k < layerDepth; k++)
//				{
//					PixelDouble vecPixel = (PixelDouble)pixel.get(k);
//					output.SetScalarComponentFromFloat(i, j, 0, k, vecPixel.get());
//				}
//			}
//		}
//		outputs.add(output);
	}
}
