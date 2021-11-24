package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.awt.Component;
import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class CustomBoundaryColorAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
   private PerspectiveImageCollection aManager;
   private final Component refParent;

	/**
	 * @param imagePopupMenu
	 */
	CustomBoundaryColorAction(PerspectiveImageCollection aManager, Component aParent)
	{
		this.aManager = aManager;
		this.refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		//TODO fix this
//		Color tmpColor = refManager.getColorProviderTarget(aItemL.get(0)).getBaseColor();
//		Color newColor = ColorChooser.showColorChooser(refParent, tmpColor);
//		if (newColor == null)
//			return;
//
//		ColorProvider tmpCP = new ConstColorProvider(newColor);
//		refManager.installCustomColorProviders(aItemL, tmpCP, tmpCP);

	}

//	public void actionPerformed(ActionEvent e)
//    {
//        PerspectiveImageBoundary boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKeys.get(0));
//        int[] currentColor = boundary.getBoundaryColor();
//        Color newColor = ColorChooser.showColorChooser(this.imagePopupMenu.invoker, currentColor);
//        if (newColor != null)
//        {
//            for (ImageKeyInterface imageKey : imageKeys)
//            {
//                boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey);
//                boundary.setBoundaryColor(newColor);
//            }
//        }
//    }
}