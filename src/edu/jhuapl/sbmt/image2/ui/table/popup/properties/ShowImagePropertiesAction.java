package edu.jhuapl.sbmt.image2.ui.table.popup.properties;

import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.IRenderableImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.cylindricalImages.CylindricalImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToDerivedMetadataPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.image2.pipelineComponents.subscribers.preview.VtkLayerPreview;
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
	private PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public ShowImagePropertiesAction(SmallBodyModel smallBodyModel, PerspectiveImageCollection<G1> aManager)
	{
		this.smallBodyModel = smallBodyModel;
		this.aManager = aManager;
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
//					SwingUtilities.invokeLater(() -> {
						image.setIntensityRange(preview.getIntensityRange());
						image.setTrimValues(preview.getMaskValues());
						image.setMaskValues(preview.getMaskValues());
						image.setCurrentLayer(preview.getDisplayedLayerIndex());
						aManager.updateImage(image);
//					});

				}
			};

			if (image.getPointingSourceType() == ImageSource.LOCAL_CYLINDRICAL)
			{
				CylindricalImageToRenderableImagePipeline pipeline = CylindricalImageToRenderableImagePipeline.of(List.of(aItemL.get(0)));
				List<HashMap<String, String>> metadata = pipeline.getMetadata();
				List<IRenderableImage> renderableImages = pipeline.getRenderableImages();
				preview = new VtkLayerPreview("Image Properties", image.getCurrentLayer(), image.getIntensityRange(), image.getMaskValues());
				preview.setCompletionBlock(completionBlock);
				List<Pair<Layer, HashMap<String, String>>> inputList = Lists.newArrayList();
				for (int i=0; i<renderableImages.size(); i++)
					inputList.add(Pair.of(renderableImages.get(i).getLayer(), metadata.get(0)));
				Just.of(inputList)
					.subscribe(preview)
					.run();

				preview.getPanel().setVisible(true);
			}
			else
			{
				PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(aItemL.get(0)));
				List<IRenderableImage> renderableImages = pipeline.getRenderableImages();
				List<HashMap<String, String>> metadata = pipeline.getMetadata();
				HashMap<String, String> derivedMetadata = new PerspectiveImageToDerivedMetadataPipeline(renderableImages.get(0), List.of(smallBodyModel)).getMetadata();
				metadata.get(0).putAll(derivedMetadata);
				preview = new VtkLayerPreview("Image Properties", image.getCurrentLayer(), image.getIntensityRange(), image.getMaskValues());
				preview.setCompletionBlock(completionBlock);
				List<Pair<Layer, HashMap<String, String>>> inputList = Lists.newArrayList();
				for (int i=0; i<renderableImages.size(); i++)
					inputList.add(Pair.of(renderableImages.get(i).getLayer(), metadata.get(0)));
				Just.of(inputList)
					.subscribe(preview)
					.run();
				preview.getPanel().setVisible(true);
			}
		}
		catch (InvalidGDALFileTypeException e)
		{
			 JOptionPane.showMessageDialog(null,
                        e.getMessage(),
                        "Invalid file type encountered",
                        JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}