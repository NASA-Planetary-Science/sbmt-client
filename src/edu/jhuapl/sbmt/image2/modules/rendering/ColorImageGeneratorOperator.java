package edu.jhuapl.sbmt.image2.modules.rendering;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class ColorImageGeneratorOperator extends BasePipelineOperator<RenderableImage, Layer>
{
	private List<SmallBodyModel> smallBodyModels;

	public ColorImageGeneratorOperator(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		RenderableImage redImage = inputs.get(0);
		RenderableImage greenImage = inputs.get(1);
		RenderableImage blueImage = inputs.get(2);

//        float[][] redPixelData = ImageDataUtil.vtkImageDataToArray2D(redImage.getRawImage(), 0);
//        float[][] greenPixelData = ImageDataUtil.vtkImageDataToArray2D(greenImage.getRawImage(), 0);
//        float[][] bluePixelData = ImageDataUtil.vtkImageDataToArray2D(blueImage.getRawImage(), 0);

        outputs.add(redImage.getLayer());
        outputs.add(greenImage.getLayer());
        outputs.add(blueImage.getLayer());
	}
}
