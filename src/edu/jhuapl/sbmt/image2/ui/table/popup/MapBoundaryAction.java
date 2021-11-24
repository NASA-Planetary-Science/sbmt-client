package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class MapBoundaryAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	/**
	 *
	 */
	private PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	MapBoundaryAction(PerspectiveImageCollection aManager)
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
			aManager.setImageBoundaryShowing(aItem, !aItem.isBoundaryShowing());
		}
	}

//	public void actionPerformed(ActionEvent e)
//	{
//		for (PerspectiveImage aItem : aItemL)
//		{
//			try
//			{
//				if (this.imagePopupMenu.mapBoundaryMenuItem.isSelected())
//				{
//					this.imagePopupMenu.imageBoundaryCollection.addBoundary(imageKey);
//					Image image = this.imagePopupMenu.imageCollection.getImage(imageKey);
//					if (image != null)
//					{
//						this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey).setOffset(image.getOffset());
//					}
//				} else
//					this.imagePopupMenu.imageBoundaryCollection.removeBoundary(imageKey);
//			} catch (FitsException e1)
//			{
//				e1.printStackTrace();
//			} catch (IOException e1)
//			{
//				e1.printStackTrace();
//			}
//		}
//
//		this.imagePopupMenu.updateMenuItems();
//	}
}