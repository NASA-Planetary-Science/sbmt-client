package edu.jhuapl.sbmt.image2.pipeline.rendering;

import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;

public class PerspectiveImageCenterImagePipeline
{
	PerspectiveImageCenterImagePipeline(IPerspectiveImage image, Renderer renderer, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		PerspectiveImageToRenderableImagePipeline pipeline1 = new PerspectiveImageToRenderableImagePipeline(List.of(image));
		List<RenderablePointedImage> renderableImages = pipeline1.getRenderableImages();

		CameraOrientationPipeline pipeline2 = CameraOrientationPipeline.of(renderableImages.get(0), smallBodyModels, null);

		double viewAngle = renderableImages.get(0).getMaxFovAngle();
		renderer.setCameraOrientation(pipeline2.getSpacecraftPosition(), pipeline2.getFocalPoint(), pipeline2.getUpVector(), viewAngle);
	}

	public static void of(IPerspectiveImage image, Renderer renderer, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		new PerspectiveImageCenterImagePipeline(image, renderer, smallBodyModels);
	}
}
