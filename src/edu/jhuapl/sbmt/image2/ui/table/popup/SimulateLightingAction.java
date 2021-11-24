package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class SimulateLightingAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	/**
	 *
	 */
	private final PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	SimulateLightingAction(PerspectiveImageCollection aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		//TODO fix this
//		for (PerspectiveImage aItem : aItemL)
//		{
//			if (!aItem.isSimulateLigting())
//			{
//				System.out.println("Turn Simulate Lighting On");
//				// store original lighting parameters
//				this.imagePopupMenu.origLightCfg = this.imagePopupMenu.renderer.getLightCfg();
//
//				double[] sunDir = image.getSunVector();
//				this.imagePopupMenu.renderer.setLightCfgToFixedLightAtDirection(new Vector3D(sunDir));
//
//				// uncheck simulate lighting for all mapped images
//				PerspectiveImage pImage;
//				for (Image tempImage : this.imagePopupMenu.imageCollection.getImages())
//				{
//					if (tempImage instanceof PerspectiveImage)
//					{
//						pImage = (PerspectiveImage) tempImage;
//						pImage.setSimulateLighting(false);
//					}
//				}
//			} else
//			{
//				System.out.println("Turn Simulate Lighting Off");
//				LightUtil.switchToLightKit(this.imagePopupMenu.renderer);
//			}
////			aItem.setSimulateLighting(
//		}
	}

//	public void actionPerformed(ActionEvent e)
//	{
//		if (imageKeys.size() != 1)
//			return;
//		ImageKeyInterface imageKey = imageKeys.get(0);
//
//		PerspectiveImage image = (PerspectiveImage) this.imagePopupMenu.imageCollection.getImage(imageKey);
//		if (image != null)
//		{
//			if (this.imagePopupMenu.simulateLightingMenuItem.isSelected())
//			{
//				System.out.println("Simulate Lighting On");
//				// store original lighting parameters
//				this.imagePopupMenu.origLightCfg = this.imagePopupMenu.renderer.getLightCfg();
//
//				double[] sunDir = image.getSunVector();
//				this.imagePopupMenu.renderer.setLightCfgToFixedLightAtDirection(new Vector3D(sunDir));
//
//				// uncheck simulate lighting for all mapped images
//				PerspectiveImage pImage;
//				for (Image tempImage : this.imagePopupMenu.imageCollection.getImages())
//				{
//					if (tempImage instanceof PerspectiveImage)
//					{
//						pImage = (PerspectiveImage) tempImage;
//						pImage.setSimulateLighting(false);
//					}
//				}
//			} else
//			{
//				System.out.println("Simulate Lighting Off");
//				LightUtil.switchToLightKit(this.imagePopupMenu.renderer);
////                    renderer.setLightCfg(origLightCfg);
//			}
//		}
//		image.setSimulateLighting(this.imagePopupMenu.simulateLightingMenuItem.isSelected());
//		this.imagePopupMenu.updateMenuItems();
//	}
}