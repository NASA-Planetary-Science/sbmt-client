package edu.jhuapl.sbmt.image2.pipeline;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.io.PerspectiveImageToDerviedMetadataOperator;
import edu.jhuapl.sbmt.image2.pipeline.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.FootprintToIlluminationAttributesOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.ImageIllumination;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.ImagePixelScale;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImageToPixelScaleAttributesOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class PerspectiveImageToDerivedMetadataPipeline
{
	List<HashMap<String, String>> metadata = Lists.newArrayList();
	List<ImageIllumination> illumAtts = Lists.newArrayList();
	List<ImagePixelScale> pixelAtts = Lists.newArrayList();

	public PerspectiveImageToDerivedMetadataPipeline(RenderablePointedImage image, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		RenderablePointedImageFootprintGeneratorPipeline footprintPipeline =
				new RenderablePointedImageFootprintGeneratorPipeline(image, smallBodyModels);
		List<vtkPolyData> polyData = footprintPipeline.getFootprintPolyData();
//		VTKDebug.writePolyDataToFile(polyData.get(0), "/Users/steelrj1/Desktop" + File.separator + "model_from_pipeline.vtk");

		RenderablePointedImageToPixelScaleAttributesOperator pixelOperator = new RenderablePointedImageToPixelScaleAttributesOperator();
		FootprintToIlluminationAttributesOperator illuminationOperator = new FootprintToIlluminationAttributesOperator();


		Just.of(Pair.of(polyData.get(0), image))
			.operate(pixelOperator)
			.subscribe(Sink.of(pixelAtts))
			.run();


		Just.of(Pair.of(polyData.get(0), image.getPointing()))
			.operate(illuminationOperator)
			.subscribe(Sink.of(illumAtts))
			.run();

		Triple<RenderablePointedImage, ImagePixelScale, ImageIllumination> inputs =
				Triple.of(image,
						pixelAtts.get(0),
						illumAtts.get(0));

		Just.of(inputs)
			.operate(new PerspectiveImageToDerviedMetadataOperator())
			.subscribe(Sink.of(metadata))
			.run();

	}

	public HashMap<String, String> getMetadata()
	{
		return metadata.get(0);
	}

	public List<ImageIllumination> getIllumAtts()
	{
		return illumAtts;
	}

	public List<ImagePixelScale> getPixelAtts()
	{
		return pixelAtts;
	}
}
