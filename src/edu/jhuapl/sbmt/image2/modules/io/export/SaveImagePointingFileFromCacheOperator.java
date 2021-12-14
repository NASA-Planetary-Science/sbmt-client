package edu.jhuapl.sbmt.image2.modules.io.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.model.image.InfoFileWriter;

public class SaveImagePointingFileFromCacheOperator extends BasePipelineOperator<PerspectiveImage, File>
{

	@Override
	public void processData() throws IOException, Exception
	{
		File file = null;
		try
		{
			String defaultFileName = FilenameUtils.getBaseName(inputs.get(0).getPointingSource());
			file = CustomFileChooser.showSaveDialog(null, "Save Pointing file as...", defaultFileName);
			if (file == null) return;

			String filename = file.getAbsolutePath();

			PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(inputs.get(0)));

			InfoFileWriter writer = new InfoFileWriter(defaultFileName, pipeline.getRenderableImages().get(0).getPointing());
			writer.write();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null,
					"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
}
