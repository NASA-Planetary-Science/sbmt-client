package edu.jhuapl.sbmt.image2.pipeline.active.cylindricalImages;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.RenderableCylindricalImageFootprintOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

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
