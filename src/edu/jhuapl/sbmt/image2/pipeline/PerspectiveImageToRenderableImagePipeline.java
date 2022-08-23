package edu.jhuapl.sbmt.image2.pipeline;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInPNGHeaderReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInPNGReader;
import edu.jhuapl.sbmt.image2.pipeline.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.pipeline.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerRotationOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerXFlipOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerYFlipOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class PerspectiveImageToRenderableImagePipeline
{
	List<RenderablePointedImage> renderableImages = Lists.newArrayList();
	List<HashMap<String, String>> metadata = Lists.newArrayList();

	public PerspectiveImageToRenderableImagePipeline(List<IPerspectiveImage> images) throws Exception
	{
		for (IPerspectiveImage image : images)
		{
			String filename = image.getFilename();
			String pointingFile = image.getPointingSource();

			if (!new File(filename).exists())
			{
				filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
				pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
			}

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
			if (image.getLinearInterpolatorDims() == null || (image.getLinearInterpolatorDims()[0] == 0 && image.getLinearInterpolatorDims()[1] == 0))
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
				.operate(rotationOperator)
				.operate(flipOperator)
				.subscribe(Sink.of(updatedLayers)).run();
			//generate image pointing (in: filename, out: ImagePointing)
			IPipelinePublisher<PointingFileReader> pointingPublisher = null;
			if (image.getPointingSourceType() == ImageSource.SPICE)
				pointingPublisher = new InfofileReaderPublisher(pointingFile);
			else
				pointingPublisher = new SumfileReaderPublisher(pointingFile);

			//generate metadata (in: filename, out: ImageMetadata)
//			IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(filename);

			metadataReader.subscribe(Sink.of(metadata)).run();
			metadata.get(0).put("Name", new File(image.getFilename()).getName());
			metadata.get(0).put("Start Time", pointingPublisher.getOutputs().get(0).getStartTime());
			metadata.get(0).put("Stop Time", pointingPublisher.getOutputs().get(0).getStartTime());

			//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
			IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
			IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents = Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);

			IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator = new RenderablePointedImageGenerator();


			//***************************************************************************************
			//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
			//***************************************************************************************
			List<RenderablePointedImage> renderableImage = Lists.newArrayList();
			imageComponents
				.operate(renderableImageGenerator)
				.subscribe(Sink.of(renderableImage)).run();
			renderableImages.addAll(renderableImage);

			for (RenderablePointedImage renderableImg : renderableImages)
			{
				renderableImg.setFilename(image.getFilename());
				renderableImg.setMasking(new LayerMasking(image.getMaskValues()));
				renderableImg.setOffset(image.getOffset());
				renderableImg.setDefaultOffset(image.getDefaultOffset());
				renderableImg.setIntensityRange(image.getIntensityRange());
				renderableImg.setOfflimbIntensityRange(image.getOfflimbIntensityRange());
//				double diagonalLength = smallBodyModel.get(0).getBoundingBoxDiagonalLength();
//				System.out.println("RenderablePointedImageActorPipeline: RenderablePointedImageActorPipeline: diag length " + diagonalLength);
//				double[] scPos = renderableImages.get(0).getPointing().getSpacecraftPosition();
				renderableImg.setMinFrustumLength(image.getMinFrustumLength());
				renderableImg.setMaxFrustumLength(image.getMaxFrustumLength());
				renderableImg.setOfflimbDepth(image.getOfflimbDepth());
				renderableImg.setLinearInterpolation(image.getInterpolateState());
			}
		}
	}

	public List<RenderablePointedImage> getRenderableImages()
	{
		return renderableImages;
	}

	public List<HashMap<String, String>> getMetadata()
	{
		return metadata;
	}
}
