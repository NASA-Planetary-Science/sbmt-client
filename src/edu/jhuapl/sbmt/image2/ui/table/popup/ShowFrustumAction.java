package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class ShowFrustumAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	ShowFrustumAction(PerspectiveImageCollection aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() == 0)
			return;

		for (PerspectiveImage aItem : aItemL)
		{
			aManager.setImageFrustumVisible(aItem, !aItem.isFrustumShowing());
		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        for (ImageKeyInterface imageKey : imageKeys)
//        {
//            try
//            {
//                this.imagePopupMenu.imageCollection.addImage(imageKey);
//                PerspectiveImage image = (PerspectiveImage)this.imagePopupMenu.imageCollection.getImage(imageKey);
//                image.setShowFrustum(this.imagePopupMenu.showFrustumMenuItem.isSelected());
//            }
//            catch (Exception ex)
//            {
//                ex.printStackTrace();
//            }
//        }
//
//        this.imagePopupMenu.updateMenuItems();
//    }
}