package edu.jhuapl.sbmt.image2.ui.table.popup.rendering;

import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class MapImageAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	/**
	 *
	 */
	private PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	public MapImageAction(PerspectiveImageCollection aManager)
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
			aManager.setImageMapped(aItem, !aItem.isMapped());
		}
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// If any items are not visible then set checkbox to unselected
		// in order to allow all chosen items to be toggled on
		boolean isSelected = true;
		for (G1 aItem : aItemC)
			isSelected &= aItem.isMapped() == true;
		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);
	}

//	public void actionPerformed(ActionEvent e)
//	{
////            System.out.println("MapImageAction.actionPerformed()");
//		for (ImageKeyInterface imageKey : imageKeys)
//		{
//			try
//			{
//				if (this.imagePopupMenu.mapImageMenuItem.isSelected())
//				{
//					this.imagePopupMenu.imageCollection.addImage(imageKey);
//					// keySet.add(imageKey);
//				}
//
//				else
//				{
//					this.imagePopupMenu.imageCollection.removeImage(imageKey);
//					// keySet.remove(imageKey);
////                        renderer.setLighting(LightingType.LIGHT_KIT); //removed due to request in #1667
//				}
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