package edu.jhuapl.sbmt.image2.pipeline.active;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.modules.rendering.RenderableImage;
import edu.jhuapl.sbmt.image2.modules.rendering.RenderableImageFootprintOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class RenderableImageFootprintGeneratorPipeline
{
	List<vtkPolyData> footprints = Lists.newArrayList();

	public RenderableImageFootprintGeneratorPipeline(RenderableImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		Just.of(image)
			.operate(new RenderableImageFootprintOperator(smallBodyModels))
			.subscribe(Sink.of(footprints))
			.run();
	}

	public List<vtkPolyData> getFootprintPolyData()
	{
		return footprints;
	}
}
