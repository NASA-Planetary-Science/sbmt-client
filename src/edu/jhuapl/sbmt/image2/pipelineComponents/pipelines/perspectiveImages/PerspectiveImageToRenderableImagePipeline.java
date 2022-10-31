package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.perspectiveImages;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.IRenderableImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class PerspectiveImageToRenderableImagePipeline
{
	List<RenderablePointedImage> renderableImages = Lists.newArrayList();
	List<HashMap<String, String>> metadata = Lists.newArrayList();

	public PerspectiveImageToRenderableImagePipeline(List<IPerspectiveImage> images) throws InvalidGDALFileTypeException, Exception
	{
		for (IPerspectiveImage image : images)
		{
			String filename = image.getFilename();
			String pointingFile = image.getPointingSource();
			synchronized(PerspectiveImageToRenderableImagePipeline.class) {
				if (!new File(filename).exists())
				{
					filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
					pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
				}
				processFile(filename, pointingFile, image);
			}
		}
	}

	private void processFile(String filename, String pointingFile, IPerspectiveImage image) throws InvalidGDALFileTypeException, Exception
	{
		IPerspectiveImageToLayerAndMetadataPipeline inputPipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(image);
		List<Layer> updatedLayers = inputPipeline.getLayers();
		metadata = inputPipeline.getMetadata();

		//generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<PointingFileReader> pointingPublisher = null;
		if (image.getPointingSourceType() == ImageSource.SPICE || image.getPointingSourceType() == ImageSource.CORRECTED_SPICE)
			pointingPublisher = new InfofileReaderPublisher(pointingFile);
		else
			pointingPublisher = new SumfileReaderPublisher(pointingFile);
		metadata.get(0).put("Name", new File(image.getFilename()).getName());
		metadata.get(0).put("Start Time", pointingPublisher.getOutputs().get(0).getStartTime());
		metadata.get(0).put("Stop Time", pointingPublisher.getOutputs().get(0).getStartTime());

		if (metadata.get(0).get("WINDOWH") != null)
		{
			int windowH = Integer.parseInt(metadata.get(0).get("WINDOWH"));
			int windowX = Integer.parseInt(metadata.get(0).get("WINDOWX"));
			int windowY = Integer.parseInt(metadata.get(0).get("WINDOWY"));
			image.setMaskValues(new int[] {windowH - windowY, windowY, windowH - windowX,  windowX});
		}

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents = Publishers.formTriple(layerPublisher, Just.of(metadata), pointingPublisher);

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
			renderableImg.setImageSource(image.getPointingSourceType());
			renderableImg.setFilename(image.getFilename());
			renderableImg.setMasking(new LayerMasking(image.getMaskValues()));
			renderableImg.setOffset(image.getOffset());
			renderableImg.setDefaultOffset(image.getDefaultOffset());
			renderableImg.setIntensityRange(image.getIntensityRange());
			renderableImg.setOfflimbIntensityRange(image.getOfflimbIntensityRange());
//			double diagonalLength = smallBodyModel.get(0).getBoundingBoxDiagonalLength();
//			System.out.println("RenderablePointedImageActorPipeline: RenderablePointedImageActorPipeline: diag length " + diagonalLength);
//			double[] scPos = renderableImages.get(0).getPointing().getSpacecraftPosition();
			renderableImg.setMinFrustumLength(image.getMinFrustumLength());
			renderableImg.setMaxFrustumLength(image.getMaxFrustumLength());
			renderableImg.setOfflimbDepth(image.getOfflimbDepth());
			renderableImg.setLinearInterpolation(image.getInterpolateState());
		}
	}

	public List<IRenderableImage> getRenderableImages()
	{
		List<IRenderableImage> images = Lists.newArrayList();
		images.addAll(renderableImages);
		return images;
	}

	public List<HashMap<String, String>> getMetadata()
	{
		return metadata;
	}
}
