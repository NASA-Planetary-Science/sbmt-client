package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;

import glum.gui.action.PopAction;

class ExportInfofileAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	/**
	 *
	 */
	private PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	ExportInfofileAction(PerspectiveImageCollection aManager)
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
//			String fullPathName = aItem.getFilename();
//			String imageFileName = new File(fullPathName).getName();
//			String defaultFileName = null;
//			if (imageFileName != null)
//				defaultFileName = imageFileName.substring(0,
//						imageFileName.length() - FilenameUtils.getExtension(imageFileName).length()) + "INFO";

			String defaultFileName = FilenameUtils.getBaseName(aItem.getPointingSources().get(ImageSource.SPICE));
			File file = CustomFileChooser.showSaveDialog(null, "Save INFO file as...",
					defaultFileName);
			if (file == null)
			{
				return;
			}

			String filename = file.getAbsolutePath();

			//TODO fix this
//			image.saveImageInfo(filename);
		}
	}

//	public void actionPerformed(ActionEvent e)
//	{
//		for (ImageKeyInterface imageKey : imageKeys)
//		{
//			if (this.imagePopupMenu.imageCollection.getImage(imageKey) instanceof PerspectiveImage)
//			{
//				try
//				{
//					this.imagePopupMenu.imageCollection.addImage(imageKey);
//					PerspectiveImage image = (PerspectiveImage) this.imagePopupMenu.imageCollection.getImage(imageKey);
//					String fullPathName = image.getFitFileFullPath();
//					if (fullPathName == null)
//						fullPathName = image.getPngFileFullPath();
//					String imageFileName = new File(fullPathName).getName();
//
//					String defaultFileName = null;
//					if (imageFileName != null)
//						defaultFileName = imageFileName.substring(0,
//								imageFileName.length() - FilenameUtils.getExtension(imageFileName).length()) + "INFO";
//
//					File file = CustomFileChooser.showSaveDialog(this.imagePopupMenu.invoker, "Save INFO file as...",
//							defaultFileName);
//					if (file == null)
//					{
//						return;
//					}
//
//					String filename = file.getAbsolutePath();
//
////                        System.out.println("Exporting INFO file for " + image.getImageName() + " to " + filename);
//
//					image.saveImageInfo(filename);
//				} catch (Exception ex)
//				{
//					ex.printStackTrace();
//				}
//			}
//		}
//
//		this.imagePopupMenu.updateMenuItems();
//	}
}