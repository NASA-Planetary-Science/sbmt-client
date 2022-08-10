package edu.jhuapl.sbmt.image2.pipeline.pointedImages;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkImageData;
import vtk.vtkPolyData;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImageFootprintOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;

public class RenderablePointedImageFootprintGeneratorPipeline
{
	Pair<List<vtkImageData>, List<vtkPolyData>>[] outputs = new Pair[1];

	public RenderablePointedImageFootprintGeneratorPipeline(RenderablePointedImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		Just.of(image)
			.operate(new RenderablePointedImageFootprintOperator(smallBodyModels))
			.subscribe(PairSink.of(outputs))
			.run();
	}

	public List<vtkPolyData> getFootprintPolyData()
	{
		return outputs[0].getRight();
	}

	public List<vtkImageData> getImageData()
	{
		return outputs[0].getLeft();
	}
}
