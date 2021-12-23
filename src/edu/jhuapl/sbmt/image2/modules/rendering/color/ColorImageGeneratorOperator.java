package edu.jhuapl.sbmt.image2.modules.rendering.color;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class ColorImageGeneratorOperator extends BasePipelineOperator<List<IPerspectiveImage>, RenderablePointedImage>
{
	public ColorImageGeneratorOperator()
	{
	}

	@Override
	public void processData() throws IOException, Exception
	{
		PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(inputs.get(0));
		outputs.addAll(pipeline.getRenderableImages());
	}
}
