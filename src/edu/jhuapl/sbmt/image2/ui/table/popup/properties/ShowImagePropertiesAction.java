package edu.jhuapl.sbmt.image2.ui.table.popup.properties;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.modules.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.CylindricalImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;

import glum.gui.action.PopAction;

public class ShowImagePropertiesAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection<G1> aManager;
	private final SbmtInfoWindowManager infoPanel;

	/**
	 * @param imagePopupMenu
	 */
	public ShowImagePropertiesAction(PerspectiveImageCollection<G1> aManager, SbmtInfoWindowManager infoPanel)
	{
		this.aManager = aManager;
		this.infoPanel = infoPanel;
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
			System.out.println("ShowImagePropertiesAction: executeAction: pointing type " + image.getPointingSourceType());
			if (image.getPointingSourceType() == ImageSource.LOCAL_CYLINDRICAL)
			{

				CylindricalImageToRenderableImagePipeline pipeline = new  CylindricalImageToRenderableImagePipeline(List.of(aItemL.get(0)));
				List<HashMap<String, String>> metadata = pipeline.getMetadata();
				List<RenderableCylindricalImage> renderableImages = pipeline.getRenderableImages();
				VtkLayerPreview preview = new VtkLayerPreview("Image Properties");
				Pair<Layer, HashMap<String, String>> inputs = Pair.of(renderableImages.get(0).getLayer(), metadata.get(0));
				Just.of(inputs)
					.subscribe(preview)
					.run();

				preview.getPanel().setVisible(true);
			}
			else
			{
				PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(aItemL.get(0)));
				List<HashMap<String, String>> metadata = pipeline.getMetadata();
				List<RenderablePointedImage> renderableImages = pipeline.getRenderableImages();
				VtkLayerPreview preview = new VtkLayerPreview("Image Properties");
				Pair<Layer, HashMap<String, String>> inputs = Pair.of(renderableImages.get(0).getLayer(), metadata.get(0));
				Just.of(inputs)
					.subscribe(preview)
					.run();

				preview.getPanel().setVisible(true);
			}
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (IPerspectiveImage aItem : aItemL)
		{
			//TODO fix this
//			this.infoPanel.addData(aItem);
		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        if (imageKeys.size() != 1)
//            return;
//        ImageKeyInterface imageKey = imageKeys.get(0);
//
//        try
//        {
//            this.imagePopupMenu.imageCollection.addImage(imageKey);
//            this.imagePopupMenu.infoPanelManager.addData(this.imagePopupMenu.imageCollection.getImage(imageKey));
//
//            this.imagePopupMenu.updateMenuItems();
//        }
//        catch (FitsException e1) {
//            e1.printStackTrace();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//    }
}