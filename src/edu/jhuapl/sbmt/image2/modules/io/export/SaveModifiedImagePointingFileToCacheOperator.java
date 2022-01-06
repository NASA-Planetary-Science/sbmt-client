package edu.jhuapl.sbmt.image2.modules.io.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.modules.pointing.offset.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image2.modules.pointing.offset.SpacecraftPointingState;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.model.image.ModifiedInfoFileWriter;

public class SaveModifiedImagePointingFileToCacheOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<Triple<G1, SpacecraftPointingState, SpacecraftPointingDelta>, File>
{
	ModifiedInfoFileWriter<G1> writer = null;
	File file = null;

	@Override
	public void processData() throws IOException, Exception
	{

		File directory = null;

		try
		{
			Triple<G1, SpacecraftPointingState, SpacecraftPointingDelta> input = inputs.get(0);
			G1 image = input.getLeft();
			SpacecraftPointingState state = input.getMiddle();
			SpacecraftPointingDelta delta = input.getRight();
//			System.out.println("SaveModifiedImagePointingFileToCacheOperator: processData: pointing source " + image.getPointingSource());
			File cachedFile = FileCache.getFileFromServer(image.getPointingSource());
			String nameNoExtension = FilenameUtils.removeExtension(cachedFile.getAbsolutePath());
			String defaultFileName = FilenameUtils.getBaseName(image.getPointingSource());
//			directory = new File(image.getPointingSource()).getParentFile();
			file = new File(nameNoExtension + ".INFO");
			PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(image));
//			System.out.println("SaveModifiedImagePointingFileToCacheOperator: processData: file " + file.getAbsolutePath());
//			System.out.println("SaveModifiedImagePointingFileToCacheOperator: processData: modified pointing " + pipeline.getRenderableImages().get(0).getModifiedPointing().isPresent());
//			pipeline.getRenderableImages().get(0).getModifiedPointing().ifPresent(pointing -> {

//			System.out.println("SaveModifiedImagePointingFileToCacheOperator: processData: cached file " + cachedFile);
			writer = new ModifiedInfoFileWriter<G1>(file.getAbsolutePath(), image, state, delta, true);
			writer.write();
//			});
			outputs.add(new File(file.getAbsolutePath() + ".adjusted"));
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null,
					"Unable to save file to " + directory.getAbsolutePath(), "Error Saving Modified File", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
}
