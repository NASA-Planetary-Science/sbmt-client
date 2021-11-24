package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class ShowInfoAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection aManager;
	private final SbmtInfoWindowManager infoPanel;

	/**
	 * @param imagePopupMenu
	 */
	ShowInfoAction(PerspectiveImageCollection aManager, SbmtInfoWindowManager infoPanel)
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

		for (PerspectiveImage aItem : aItemL)
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