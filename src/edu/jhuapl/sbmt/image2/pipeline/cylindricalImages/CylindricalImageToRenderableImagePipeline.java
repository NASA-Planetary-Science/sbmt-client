package edu.jhuapl.sbmt.image2.pipeline.cylindricalImages;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.CylindricalBounds;
import edu.jhuapl.sbmt.image2.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInPNGHeaderReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInPNGReader;
import edu.jhuapl.sbmt.image2.pipeline.rendering.cylindricalImage.RenderableCylindricalImageGenerator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerRotationOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerXFlipOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerYFlipOperator;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class CylindricalImageToRenderableImagePipeline
{
	List<RenderableCylindricalImage> renderableImages = Lists.newArrayList();
	List<HashMap<String, String>> metadata = Lists.newArrayList();

	public CylindricalImageToRenderableImagePipeline(List<IPerspectiveImage> images) throws Exception
	{
		for (IPerspectiveImage image : images)
		{
			String filename = image.getFilename();

			if (!new File(filename).exists())
			{
				filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
			}

//			IPipelinePublisher<Layer> reader = new BuiltInFitsReader(filename, new double[] {});
			IPipelinePublisher<Layer> reader = null;
			IPipelinePublisher<HashMap<String, String>> metadataReader = null;
			if (FilenameUtils.getExtension(filename).toLowerCase().equals("fit") || FilenameUtils.getExtension(filename).toLowerCase().equals("fits"))
			{
				reader = new BuiltInFitsReader(filename, new double[] {});
				metadataReader = new BuiltInFitsHeaderReader(filename);
			}
			else if (FilenameUtils.getExtension(filename).toLowerCase().equals("png"))
			{
				reader = new BuiltInPNGReader(filename);
				metadataReader = new BuiltInPNGHeaderReader(filename);
			}

			IPipelineOperator<Layer, Layer> linearInterpolator = null;
			if (image.getLinearInterpolatorDims() == null)
				linearInterpolator = new PassthroughOperator<>();
			else
				linearInterpolator = new LayerLinearInterpolaterOperator(image.getLinearInterpolatorDims()[0], image.getLinearInterpolatorDims()[1]);

			LayerRotationOperator rotationOperator = new LayerRotationOperator(image.getRotation());

			BasePipelineOperator<Layer, Layer> flipOperator = new PassthroughOperator<Layer>();
			if (image.getFlip().equals("X"))
				flipOperator = new LayerXFlipOperator();
			else if (image.getFlip().equals("Y"))
				flipOperator = new LayerYFlipOperator();
			List<Layer> updatedLayers = Lists.newArrayList();
			reader
				.operate(linearInterpolator)
				.operate(flipOperator)
				.operate(rotationOperator)
				.subscribe(Sink.of(updatedLayers)).run();

			//generate metadata (in: filename, out: ImageMetadata)
//			IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(filename);
			metadataReader.subscribe(Sink.of(metadata)).run();

			//combine image source (in: Layer+ImageMetadata+CylindricalBounds, out: RenderableImage)
//			IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
//			IPipelinePublisher<CylindricalBounds> boundsPublisher = Just.of(image.getBounds());
			List<HashMap<String, String>> metadataReaders = Lists.newArrayList();
			List<CylindricalBounds> boundsCollection = Lists.newArrayList();
			for (int i=0; i<reader.getOutputs().size(); i++)
			{
				metadataReaders.add(metadataReader.getOutput());
				boundsCollection.add(image.getBounds());
			}

			IPipelinePublisher<HashMap<String, String>> metadataPipeline = Just.of(metadataReaders);
			IPipelinePublisher<CylindricalBounds> boundsPipeline = Just.of(boundsCollection);
			IPipelinePublisher<Triple<Layer, HashMap<String, String>, CylindricalBounds>> imageComponents = Publishers.formTriple(Just.of(updatedLayers), metadataPipeline, boundsPipeline);
			//make a renderable image generator
			IPipelineOperator<Triple<Layer, HashMap<String, String>, CylindricalBounds>, RenderableCylindricalImage> renderableImageGenerator = new RenderableCylindricalImageGenerator();

			//*******************************************************************************************************************************
			//generate renderable image from the components using the generator (in: layer/metadata/bounds, out: RenderableCylindricalImage)
			//*******************************************************************************************************************************
			List<RenderableCylindricalImage> renderableImage = Lists.newArrayList();
			imageComponents
				.operate(renderableImageGenerator)
				.subscribe(Sink.of(renderableImage)).run();
			renderableImages.addAll(renderableImage);
		}
	}

	public List<RenderableCylindricalImage> getRenderableImages()
	{
		return renderableImages;
	}

	public List<HashMap<String, String>> getMetadata()
	{
		return metadata;
	}
}