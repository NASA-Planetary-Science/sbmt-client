package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

class SaveImageAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	/**
	 *
	 */
	private final PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	SaveImageAction(PerspectiveImageCollection aManager)
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
			File file = null;
			try
			{
				String path = aItem.getFilename();
				String extension = FilenameUtils.getExtension(path);
				String imageFileName = FilenameUtils.getBaseName(path);

				file = CustomFileChooser.showSaveDialog(null, "Save FITS image", imageFileName,
						extension);
				if (file != null)
				{
					File fitFile = FileCache.getFileFromServer(aItem.getFilename());

					FileUtil.copyFile(fitFile, file);
				}
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(null,
						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}

//	public void actionPerformed(ActionEvent e)
//	{
////        	JFileChooser outputDirChooser = new JFileChooser();
////        	outputDirChooser.setDialogTitle("Save FITS File(s) to...");
////        	outputDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
////        	outputDirChooser.setAcceptAllFileFilterUsed(false);
////
////        	if (outputDirChooser.showOpenDialog(ImagePopupMenu.this) != JFileChooser.APPROVE_OPTION) return;
////        	File outDir = outputDirChooser.getSelectedFile();
////        	System.out.println("ImagePopupMenu.SaveImageAction: actionPerformed: outdir " + outDir);
////        	for (ImageKeyInterface imageKey : imageKeys)
////        	{
////        		if (!(imageCollection.getImage(imageKey) instanceof PerspectiveImage)) continue;
////	            File file = null;
////	            try
////	            {
////	            	//save fits
////	                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
////	                String path = image.getFitFileFullPath();
////	                String extension = FilenameUtils.getExtension(path);
////	                String imageFileName = FilenameUtils.getBaseName(path);
//////	                file = new File(outDir, imageKey.getImageFilename().substring(imageKey.getImageFilename().lastIndexOf("/")) + extension);
////                    file = new File(outDir, imageFileName + "." + extension);
////	                File fitFile = FileCache.getFileFromServer(imageKey.getName() + "." + extension);
////                    FileUtil.copyFile(fitFile, file);
////	            }
////	            catch(Exception ex)
////	            {
////	                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
////	                        "Unable to save file to " + file.getAbsolutePath(),
////	                        "Error Saving File",
////	                        JOptionPane.ERROR_MESSAGE);
////	                ex.printStackTrace();
////	            }
////
////        	}
//
//		if (imageKeys.size() != 1)
//			return;
//		ImageKeyInterface imageKey = imageKeys.get(0);
//
//		File file = null;
//		try
//		{
//			this.imagePopupMenu.imageCollection.addImage(imageKey);
//			PerspectiveImage image = (PerspectiveImage) this.imagePopupMenu.imageCollection.getImage(imageKey);
//			String path = image.getFitFileFullPath();
//			String extension = FilenameUtils.getExtension(path);
//			String imageFileName = FilenameUtils.getBaseName(path);
//
//			file = CustomFileChooser.showSaveDialog(this.imagePopupMenu.invoker, "Save FITS image", imageFileName,
//					extension);
//			if (file != null)
//			{
//				File fitFile = FileCache.getFileFromServer(imageKey.getName() + "." + extension);
//
//				FileUtil.copyFile(fitFile, file);
//			}
//		} catch (Exception ex)
//		{
//			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker),
//					"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
//			ex.printStackTrace();
//		}
//	}
}