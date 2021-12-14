package edu.jhuapl.sbmt.image2.pipeline.active;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.modules.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.PointingFileReader;

public class PerspectiveImageToRenderableImagePipeline
{
	List<RenderablePointedImage> renderableImages = Lists.newArrayList();

	public PerspectiveImageToRenderableImagePipeline(List<PerspectiveImage> images) throws Exception
	{
		for (PerspectiveImage image : images)
		{
			String filename = image.getFilename();
			String pointingFile = image.getPointingSource();

			if (!new File(filename).exists())
			{
				filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
				pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
			}

			IPipelinePublisher<Layer> reader = new BuiltInFitsReader(filename, new double[] {});
			LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(image.getLinearInterpolatorDims()[0], image.getLinearInterpolatorDims()[1]);

			List<Layer> updatedLayers = Lists.newArrayList();
			reader
				.operate(linearInterpolator)
				.subscribe(Sink.of(updatedLayers)).run();
			//generate image pointing (in: filename, out: ImagePointing)
			IPipelinePublisher<PointingFileReader> pointingPublisher = null;
			if (image.getPointingSourceType() == ImageSource.SPICE)
				pointingPublisher = new InfofileReaderPublisher(pointingFile);
			else
				pointingPublisher = new SumfileReaderPublisher(pointingFile);

			//generate metadata (in: filename, out: ImageMetadata)
			IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(filename);

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
				renderableImg.setMasking(new LayerMasking(image.getMaskValues()));
				renderableImg.setOffset(image.getOffset());
				renderableImg.setDefaultOffset(image.getDefaultOffset());
			}
		}
	}

	public List<RenderablePointedImage> getRenderableImages()
	{
		return renderableImages;
	}
}
