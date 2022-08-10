package edu.jhuapl.sbmt.image2.pipeline.cylindricalImages;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.pipeline.rendering.cylindricalImage.RenderableCylindricalImageFootprintOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class RenderableCylindricalImageFootprintGeneratorPipeline
{
	List<vtkPolyData> footprints = Lists.newArrayList();

	public RenderableCylindricalImageFootprintGeneratorPipeline(RenderableCylindricalImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		Just.of(image)
			.operate(new RenderableCylindricalImageFootprintOperator(smallBodyModels))
			.subscribe(Sink.of(footprints))
			.run();
	}

	public List<vtkPolyData> getFootprintPolyData()
	{
		return footprints;
	}
}
