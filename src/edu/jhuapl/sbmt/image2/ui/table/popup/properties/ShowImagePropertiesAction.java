package edu.jhuapl.sbmt.image2.ui.table.popup.properties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkImageData;
import vtk.vtkPolyData;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.IRenderableImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.color.ColorImageFootprintGeneratorOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.color.ColorImageGeneratorOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.cylindricalImages.CylindricalImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToDerivedMetadataPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.image2.pipelineComponents.subscribers.preview.VtkImagePreview;
import edu.jhuapl.sbmt.image2.pipelineComponents.subscribers.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;

import glum.gui.action.PopAction;

public class ShowImagePropertiesAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
    /**
	 *
	 */
	private final SmallBodyModel smallBodyModel;
	private VtkLayerPreview preview = null;
	private VtkImagePreview imageDataPreview = null;
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
				List<Pair<Layer, List<HashMap<String, String>>>> inputList = Lists.newArrayList();
				List<HashMap<String, String>> metadatas = List.of(pipeline.getMetadata().get(0));
				for (int i=0; i<renderableImages.size(); i++)
					inputList.add(Pair.of(renderableImages.get(i).getLayer(), metadatas));
				Just.of(inputList)
					.subscribe(preview)
					.run();

				preview.getPanel().setVisible(true);
			}
			else
			{
				if (image.getNumberOfLayers() == 3)
				{
					Pair<vtkImageData, vtkPolyData>[] imageAndPolyData = new Pair[1];

					Just.of(image.getImages())
						.operate(new ColorImageGeneratorOperator())
						.operate(new ColorImageFootprintGeneratorOperator(List.of(smallBodyModel)))
						.subscribe(PairSink.of(imageAndPolyData))
						.run();

					HashMap<String, String> metadata = new HashMap<String, String>();
					metadata.put("Image 1", image.getImages().get(0).getFilename());
					metadata.put("Image 2", image.getImages().get(1).getFilename());
					metadata.put("Image 3", image.getImages().get(2).getFilename());
					imageDataPreview = new VtkImagePreview("Color Image Properties", metadata, false);

					Just.of(List.of(imageAndPolyData[0].getLeft()))
						.subscribe(imageDataPreview)
						.run();
//					preview.getPanel().setVisible(true);
				}
				else
				{
					PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(aItemL.get(0)));
					List<IRenderableImage> renderableImages = pipeline.getRenderableImages();
					List<HashMap<String, String>> metadata = pipeline.getMetadata();
					HashMap<String, String> derivedMetadata = new PerspectiveImageToDerivedMetadataPipeline(renderableImages.get(0), List.of(smallBodyModel)).getMetadata();
//					metadata.get(0).putAll(derivedMetadata);
					System.out.println("ShowImagePropertiesAction: executeAction: image mask values " +  Arrays.toString(image.getMaskValues()));
					preview = new VtkLayerPreview("Image Properties", image.getCurrentLayer(), image.getIntensityRange(), image.getMaskValues());
					preview.setCompletionBlock(completionBlock);
					List<HashMap<String, String>> metadatas = List.of(pipeline.getMetadata().get(0), derivedMetadata);
					List<Pair<Layer, List<HashMap<String, String>>>> inputList = Lists.newArrayList();
					for (int i=0; i<renderableImages.size(); i++)
						inputList.add(Pair.of(renderableImages.get(i).getLayer(), metadatas));
					Just.of(inputList)
						.subscribe(preview)
						.run();
					preview.getPanel().setVisible(true);
				}
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