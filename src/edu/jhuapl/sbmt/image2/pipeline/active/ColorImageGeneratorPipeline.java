package edu.jhuapl.sbmt.image2.pipeline.active;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkImageData;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.rendering.ColorImageFootprintGeneratorOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.ColorImageGeneratorOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class ColorImageGeneratorPipeline
{
	public ColorImageGeneratorPipeline(List<PerspectiveImage> images, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		List<vtkImageData> imageData = Lists.newArrayList();
//		List<RenderableImage> imageData = Lists.newArrayList();
		Just.of(images)
			.operate(new ColorImageGeneratorOperator())
			.operate(new ColorImageFootprintGeneratorOperator(smallBodyModels))
			.operate(new VtkImageVtkMaskingOperator(new int[] {0, 0, 0, 0}))
			.operate(new VtkImageContrastOperator(null))
			.subscribe(Sink.of(imageData))
			.run();

	}
}
