package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class ChangeNormalOffsetAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	private PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	ChangeNormalOffsetAction(PerspectiveImageCollection aManager)
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
			//TODO I think this can be done differently than down below
//		    PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)this.imagePopupMenu.modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
//            NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
//            changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker));
//            changeOffsetDialog.setVisible(true);
//            int[] temp = boundaries.getBoundary(imageKey).getBoundaryColor();
//            boundaries.getBoundary(imageKey).setOffset(image.getOffset());
//            Color color = new Color(temp[0],temp[1],temp[2]);
//            boundaries.getBoundary(imageKey).setBoundaryColor(color);
		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        if (imageKeys.size() != 1)
//            return;
//        ImageKeyInterface imageKey = imageKeys.get(0);
//
//        Image image = this.imagePopupMenu.imageCollection.getImage(imageKey);
//        if (image != null)
//        {
//            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)this.imagePopupMenu.modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
//            NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
//            changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker));
//            changeOffsetDialog.setVisible(true);
//            int[] temp = boundaries.getBoundary(imageKey).getBoundaryColor();
//            boundaries.getBoundary(imageKey).setOffset(image.getOffset());
//            Color color = new Color(temp[0],temp[1],temp[2]);
//            boundaries.getBoundary(imageKey).setBoundaryColor(color);
//        }
//    }
}