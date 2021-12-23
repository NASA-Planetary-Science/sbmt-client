package edu.jhuapl.sbmt.image2.ui.table.popup.properties;

import java.util.List;

import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.modules.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;

import glum.gui.action.PopAction;

public class ShowInfoAction<G1 extends IPerspectiveImage> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection aManager;
	private final SbmtInfoWindowManager infoPanel;

	/**
	 * @param imagePopupMenu
	 */
	public ShowInfoAction(PerspectiveImageCollection aManager, SbmtInfoWindowManager infoPanel)
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
			PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(aItemL.get(0)));
			List<RenderablePointedImage> renderableImages = pipeline.getRenderableImages();
			VtkLayerPreview preview = new VtkLayerPreview();

			Just.of(renderableImages.get(0).getLayer())
				.subscribe(preview)
				.run();
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