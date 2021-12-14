package edu.jhuapl.sbmt.image2.pipeline.active.pointedImages;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.modules.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.rendering.PointedImageRenderables;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerRotationOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerXFlipOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerYFlipOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.ScenePointedImageBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.PointingFileReader;

public class RenderablePointedImageActorPipeline implements RenderableImageActorPipeline
{
	List<vtkActor> renderableImageActors = Lists.newArrayList();
	Pair<List<vtkActor>, List<PointedImageRenderables>>[] sceneOutputs = new Pair[1];

	public RenderablePointedImageActorPipeline(PerspectiveImage image, List<SmallBodyModel> smallBodyModel) throws Exception
	{
		String filename = image.getFilename();
		String pointingFile = image.getPointingSource();

		if (!new File(filename).exists())
		{
			filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
			pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
		}

//		String filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
//		String pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
		String flip = image.getFlip();
		double rotation = image.getRotation();

		//***********************
		//generate image layer
		//***********************
		//TODO: eventually replace this with a GDAL call to read in the data
		IPipelinePublisher<Layer> reader = new BuiltInFitsReader(filename, image.getFillValues());
		int[] interpolationDims = new int[] { reader.getOutputs().get(0).iSize(), reader.getOutputs().get(0).jSize()};
		if (image.getLinearInterpolatorDims() != null) interpolationDims = image.getLinearInterpolatorDims();
		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(interpolationDims[0], interpolationDims[1]);
		LayerRotationOperator rotationOperator = new LayerRotationOperator(rotation);

		BasePipelineOperator<Layer, Layer> flipOperator = new PassthroughOperator<Layer>();
		if (flip.equals("X"))
			flipOperator = new LayerXFlipOperator();
		else if (flip.equals("Y"))
			flipOperator = new LayerYFlipOperator();

		List<Layer> updatedLayers = Lists.newArrayList();
		reader
			.operate(linearInterpolator)
			.operate(rotationOperator)
			.operate(flipOperator)
			.subscribe(Sink.of(updatedLayers)).run();

		IPipelinePublisher<PointingFileReader> pointingPublisher = null;
		if (image.getPointingSourceType() == ImageSource.SPICE)
			pointingPublisher = new InfofileReaderPublisher(pointingFile);
		else
			pointingPublisher = new SumfileReaderPublisher(pointingFile);

		//generate metadata (in: filename, out: ImageMetadata)
		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(filename);

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents =
				Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator =
				new RenderablePointedImageGenerator();


		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************
		List<RenderablePointedImage> renderableImages = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages)).run();

		image.setDefaultOffset(3.0 * smallBodyModel.get(0).getMinShiftAmount());
		if (image.getOffset() == -1) image.setOffset(image.getDefaultOffset());

		for (RenderablePointedImage renderableImage : renderableImages)
		{
			renderableImage.setMasking(new LayerMasking(image.getMaskValues()));
			renderableImage.setOffset(image.getOffset());
			renderableImage.setDefaultOffset(image.getDefaultOffset());
		}

		//*************************
		//zip the sources together
		//*************************
		IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderablePointedImage>>> sceneObjects =
				Publishers.formPair(Just.of(smallBodyModel), Just.of(renderableImages));

		//*****************************************************************************************************
		//Pass them into the scene builder to perform intersection calculations, and send actors to List
		//*****************************************************************************************************
		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderablePointedImage>>, Pair<List<vtkActor>, List<PointedImageRenderables>>> sceneBuilder =
				new ScenePointedImageBuilderOperator();

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();

	}


	@Override
	public List<vtkActor> getRenderableImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageActors.addAll(renderable.getFootprints());
		}
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableImageBoundaryActors()
	{
		List<vtkActor> imageBoundaryActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageBoundaryActors.addAll(renderable.getBoundaries());
		}
		return imageBoundaryActors;
	}

	public List<vtkActor> getRenderableImageFrustumActors()
	{
		List<vtkActor> frustumActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			frustumActors.add(renderable.getFrustum());
		}
		return frustumActors;
	}

	public List<vtkActor> getRenderableOfflimbImageActors()
	{
		List<vtkActor> offLimbActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			if (renderable.getOffLimb() == null) continue;
			offLimbActors.add(renderable.getOffLimb());
			offLimbActors.add(renderable.getOffLimbBoundary());
		}
		return offLimbActors;
	}

	@Override
	public List<vtkActor> getSmallBodyActors()
	{
		return sceneOutputs[0].getLeft();
	}
}
