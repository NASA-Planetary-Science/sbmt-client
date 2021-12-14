package edu.jhuapl.sbmt.image2.pipeline.active.rendering;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.view.light.LightCfg;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;

public class PerspectiveImageSimulateLightingPipeline
{
	static LightCfg currentLightCfg;

	public PerspectiveImageSimulateLightingPipeline(PerspectiveImage image, Renderer renderer, boolean currentlySimulateLighting) throws Exception
	{
		PerspectiveImageToRenderableImagePipeline pipeline1 = new PerspectiveImageToRenderableImagePipeline(List.of(image));
		List<RenderablePointedImage> renderableImages = pipeline1.getRenderableImages();

		if (currentlySimulateLighting)
		{
			double[] sunPos = renderableImages.get(0).getPointing().getSunPosition();
			renderer.setLightCfgToFixedLightAtDirection(new Vector3D(sunPos));
			currentLightCfg = renderer.getLightCfg();
		}
		else
		{
			LightUtil.switchToLightKit(renderer);
		}
//		image.setSimulateLighting(currentlySimulateLighting);

	}

	public static void of(PerspectiveImage image, Renderer renderer, boolean simulateLighting) throws Exception
	{
		new PerspectiveImageSimulateLightingPipeline(image, renderer, simulateLighting);
	}
}
