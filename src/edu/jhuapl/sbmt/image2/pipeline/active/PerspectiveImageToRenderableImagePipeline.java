package edu.jhuapl.sbmt.image2.pipeline.active;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.modules.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.rendering.RenderableImage;
import edu.jhuapl.sbmt.image2.modules.rendering.RenderableImageGenerator;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class PerspectiveImageToRenderableImagePipeline
{
	List<RenderableImage> renderableImages = Lists.newArrayList();

	public PerspectiveImageToRenderableImagePipeline(List<PerspectiveImage> images) throws Exception
	{
		for (PerspectiveImage image : images)
		{
			String filename = image.getFilename();
			String pointingFile = image.getPointingSources().get(ImageSource.SPICE);
			IPipelinePublisher<Layer> reader = new BuiltInFitsReader(filename, new double[] {});
	//		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(537, 412);

//			List<Layer> updatedLayers = Lists.newArrayList();
//			reader
//				.operate(linearInterpolator)
//				.subscribe(Sink.of(updatedLayers)).run();

			//generate image pointing (in: filename, out: ImagePointing)
			IPipelinePublisher<InfoFileReader> pointingPublisher = new InfofileReaderPublisher(pointingFile);

			//generate metadata (in: filename, out: ImageMetadata)
			IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(filename);

			//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
//			IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(reader.get(0));
			IPipelinePublisher<Triple<Layer, HashMap<String, String>, InfoFileReader>> imageComponents = Publishers.formTriple(reader, metadataReader, pointingPublisher);

			IPipelineOperator<Triple<Layer, HashMap<String, String>, InfoFileReader>, RenderableImage> renderableImageGenerator = new RenderableImageGenerator();


			//***************************************************************************************
			//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
			//***************************************************************************************
			List<RenderableImage> renderableImage = Lists.newArrayList();
			imageComponents
				.operate(renderableImageGenerator)
				.subscribe(Sink.of(renderableImage)).run();
			renderableImages.addAll(renderableImage);
		}
	}

	public List<RenderableImage> getRenderableImages()
	{
		return renderableImages;
	}
}
