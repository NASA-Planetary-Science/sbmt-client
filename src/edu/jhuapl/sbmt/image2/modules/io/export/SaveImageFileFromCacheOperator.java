package edu.jhuapl.sbmt.image2.modules.io.export;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class SaveImageFileFromCacheOperator extends BasePipelineOperator<PerspectiveImage, File>
{
	@Override
	public void processData() throws IOException, Exception
	{
		File file = null;
		try
		{
			String path = inputs.get(0).getFilename();
			String extension = FilenameUtils.getExtension(path);
			String imageFileName = FilenameUtils.getBaseName(path);

			file = CustomFileChooser.showSaveDialog(null, "Save FITS image", imageFileName,
					extension);
			if (file != null)
			{
				File fitFile = FileCache.getFileFromServer(inputs.get(0).getFilename());

				FileUtil.copyFile(fitFile, file);
				outputs.add(file);
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
