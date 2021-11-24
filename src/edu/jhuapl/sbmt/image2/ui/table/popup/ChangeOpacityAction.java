package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class ChangeOpacityAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
    private PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	ChangeOpacityAction(PerspectiveImageCollection aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		for (PerspectiveImage aItem : aItemL)
		{
			//TODO fix this
//			OpacityChanger opacityChanger = new OpacityChanger(image);
//            opacityChanger.setLocationRelativeTo(this.imagePopupMenu.renderer);
//            opacityChanger.setVisible(true);
		}
	}
}