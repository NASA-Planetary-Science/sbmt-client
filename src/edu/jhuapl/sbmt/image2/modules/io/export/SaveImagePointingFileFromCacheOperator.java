package edu.jhuapl.sbmt.image2.modules.io.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.InfoFileWriter;

public class SaveImagePointingFileFromCacheOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<G1, File>
{

	@Override
	public void processData() throws IOException, Exception
	{
		File file = null;
		try
		{
			String defaultFileName = FilenameUtils.getBaseName(inputs.get(0).getPointingSource());
			String defaultFileType = inputs.get(0).getPointingSourceType() == ImageSource.GASKELL ? "SUM" : "INFO";
			file = CustomFileChooser.showSaveDialog(null, "Save Pointing file as...", defaultFileName + "." + defaultFileType);
			if (file == null) return;

			String filename = file.getAbsolutePath();

			PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(inputs.get(0)));
			InfoFileWriter writer = new InfoFileWriter(filename, pipeline.getRenderableImages().get(0).getPointing(), false);
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
