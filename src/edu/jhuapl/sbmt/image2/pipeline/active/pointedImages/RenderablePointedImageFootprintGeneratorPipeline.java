package edu.jhuapl.sbmt.image2.pipeline.active.pointedImages;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImageFootprintOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class RenderablePointedImageFootprintGeneratorPipeline
{
	List<vtkPolyData> footprints = Lists.newArrayList();

	public RenderablePointedImageFootprintGeneratorPipeline(RenderablePointedImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		Just.of(image)
			.operate(new RenderablePointedImageFootprintOperator(smallBodyModels))
			.subscribe(Sink.of(footprints))
			.run();
	}

	public List<vtkPolyData> getFootprintPolyData()
	{
		return footprints;
	}
}
