package edu.jhuapl.sbmt.image2.modules.rendering;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class ColorImageGeneratorOperator extends BasePipelineOperator<List<PerspectiveImage>, RenderableImage>
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
