package edu.jhuapl.sbmt.image2.pipeline.active.rendering;

import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;

public class PerspectiveImageCenterImagePipeline
{
	PerspectiveImageCenterImagePipeline(PerspectiveImage image, Renderer renderer, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		PerspectiveImageToRenderableImagePipeline pipeline1 = new PerspectiveImageToRenderableImagePipeline(List.of(image));
		List<RenderablePointedImage> renderableImages = pipeline1.getRenderableImages();

		CameraOrientationPipeline pipeline2 = CameraOrientationPipeline.of(renderableImages.get(0), smallBodyModels);

		double viewAngle = renderableImages.get(0).getMaxFovAngle();
		renderer.setCameraOrientation(pipeline2.getSpacecraftPosition(), pipeline2.getFocalPoint(), pipeline2.getUpVector(), viewAngle);
	}

	public static void of(PerspectiveImage image, Renderer renderer, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		new PerspectiveImageCenterImagePipeline(image, renderer, smallBodyModels);
	}
}
