package edu.jhuapl.sbmt.image2.ui.table.popup.properties;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.pipeline.PerspectiveImageToDerivedMetadataPipeline;
import edu.jhuapl.sbmt.image2.pipeline.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.cylindricalImages.CylindricalImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

import glum.gui.action.PopAction;

public class ShowImagePropertiesAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
    /**
	 *
	 */
	private final SmallBodyModel smallBodyModel;
	private VtkLayerPreview preview = null;

	/**
	 * @param imagePopupMenu
	 */
	public ShowImagePropertiesAction(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		try
		{
			G1 image = aItemL.get(0);

			Runnable completionBlock = new Runnable()
			{
				@Override
				public void run()
				{
					image.setIntensityRange(preview.getIntensityRange());
					image.setTrimValues(preview.getMaskValues());
				}
			};

			if (image.getPointingSourceType() == ImageSource.LOCAL_CYLINDRICAL)
			{
				CylindricalImageToRenderableImagePipeline pipeline = new CylindricalImageToRenderableImagePipeline(List.of(aItemL.get(0)));
				List<HashMap<String, String>> metadata = pipeline.getMetadata();
				List<RenderableCylindricalImage> renderableImages = pipeline.getRenderableImages();
				preview = new VtkLayerPreview("Image Properties");
				preview.setCompletionBlock(completionBlock);
				Pair<Layer, HashMap<String, String>> inputs = Pair.of(renderableImages.get(0).getLayer(), metadata.get(0));
				Just.of(inputs)
					.subscribe(preview)
					.run();

				preview.getPanel().setVisible(true);
			}
			else
			{
				PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(aItemL.get(0)));
				List<RenderablePointedImage> renderableImages = pipeline.getRenderableImages();
				List<HashMap<String, String>> metadata = pipeline.getMetadata();
				HashMap<String, String> derivedMetadata = new PerspectiveImageToDerivedMetadataPipeline(renderableImages.get(0), List.of(smallBodyModel)).getMetadata();
				metadata.get(0).putAll(derivedMetadata);
				preview = new VtkLayerPreview("Image Properties");
				preview.setCompletionBlock(completionBlock);
				Pair<Layer, HashMap<String, String>> inputs = Pair.of(renderableImages.get(0).getLayer(), metadata.get(0));
				Just.of(inputs)
					.subscribe(preview)
					.run();

				preview.getPanel().setVisible(true);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}