package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class CenterImageAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	private PerspectiveImageCollection aManager;
	private Renderer renderer;

	/**
	 * @param imagePopupMenu
	 */
	CenterImageAction(PerspectiveImageCollection aManager, Renderer renderer)
	{
		this.aManager = aManager;
		this.renderer = renderer;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		for (PerspectiveImage aItem : aItemL)
		{
	        double[] spacecraftPosition = new double[3];
	        double[] focalPoint = new double[3];
	        double[] upVector = new double[3];
	        double viewAngle = 0.0;

	        //TODO fix this
//	        aItem.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//            viewAngle = image.getMaxFovAngle();

	        renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        if (imageKeys.size() != 1)
//            return;
//        ImageKeyInterface imageKey = imageKeys.get(0);
//
//        double[] spacecraftPosition = new double[3];
//        double[] focalPoint = new double[3];
//        double[] upVector = new double[3];
//        double viewAngle = 0.0;
//
//        if (this.imagePopupMenu.imageBoundaryCollection != null && this.imagePopupMenu.imageBoundaryCollection.containsBoundary(imageKey))
//        {
//            PerspectiveImageBoundary boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey);
//            boundary.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//            viewAngle = boundary.getImage().getMaxFovAngle();
//        }
//        else if (this.imagePopupMenu.imageCollection.containsImage(imageKey))
//        {
//            PerspectiveImage image = (PerspectiveImage)this.imagePopupMenu.imageCollection.getImage(imageKey);
//            image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//            viewAngle = image.getMaxFovAngle();
//        }
//        else
//        {
//            return;
//        }
//
//        this.imagePopupMenu.renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
//    }
}