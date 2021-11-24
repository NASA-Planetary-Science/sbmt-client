package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class ShowSpectrumAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection aManager;

	private final SbmtSpectrumWindowManager spectrumPanelManager;

	/**
	 * @param imagePopupMenu
	 */
	ShowSpectrumAction(PerspectiveImageCollection aManager, SbmtSpectrumWindowManager spectrumPanelManager)
	{
		this.aManager = aManager;
		this.spectrumPanelManager = spectrumPanelManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		for (PerspectiveImage aItem : aItemL)
		{
			//TODO make only valid for images with nLayers > 1 (spectra-type)
			//TODO update to accept new PerspectiveImage class
//			spectrumPanelManager.addData(aItem);
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
//            Image image = this.imagePopupMenu.imageCollection.getImage(imageKey);
//            if (image instanceof LEISAJupiterImage || image instanceof MVICQuadJupiterImage)
//                this.imagePopupMenu.spectrumPanelManager.addData(this.imagePopupMenu.imageCollection.getImage(imageKey));
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