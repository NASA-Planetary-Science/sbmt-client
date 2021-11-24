package edu.jhuapl.sbmt.image2.pipeline.active;

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
import edu.jhuapl.sbmt.image2.modules.rendering.ImageRenderables;
import edu.jhuapl.sbmt.image2.modules.rendering.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.LayerRotationOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.LayerXFlipOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.LayerYFlipOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.RenderableImage;
import edu.jhuapl.sbmt.image2.modules.rendering.RenderableImageGenerator;
import edu.jhuapl.sbmt.image2.modules.rendering.SceneBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.LayerPassthroughOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class RenderableImageActorPipeline
{
	List<vtkActor> renderableImageActors = Lists.newArrayList();
	Pair<List<vtkActor>, List<ImageRenderables>>[] sceneOutputs = new Pair[1];

	public RenderableImageActorPipeline(PerspectiveImage image, List<SmallBodyModel> smallBodyModel) throws Exception
	{
		String filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
		String infoFileName = FileCache.getFileFromServer(image.getPointingSources().get(ImageSource.SPICE)).getAbsolutePath();
		String flip = image.getFlip();
		double rotation = image.getRotation();

		//***********************
		//generate image layer
		//***********************
		//TODO: eventually replace this with a GDAL call to read in the data
		IPipelinePublisher<Layer> reader = new BuiltInFitsReader(filename, image.getFillValues());
		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(537, 412);
		LayerRotationOperator rotationOperator = new LayerRotationOperator(rotation);
		BasePipelineOperator<Layer, Layer> flipOperator = new LayerPassthroughOperator();
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

		//generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<InfoFileReader> pointingPublisher = new InfofileReaderPublisher(infoFileName);

		//generate metadata (in: filename, out: ImageMetadata)
		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(filename);

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, InfoFileReader>> imageComponents =
				Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, InfoFileReader>, RenderableImage> renderableImageGenerator =
				new RenderableImageGenerator();


		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************
		List<RenderableImage> renderableImages = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages)).run();

		//***********************
		//generate body polydata
		//***********************
//		IPipelinePublisher<SmallBodyModel> vtkReader =
//				new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");

		//*************************
		//zip the sources together
		//*************************
		IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderableImage>>> sceneObjects =
				Publishers.formPair(Just.of(smallBodyModel), Just.of(renderableImages));

		//*****************************************************************************************************
		//Pass them into the scene builder to perform intersection calculations, and send actors to List
		//*****************************************************************************************************
		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderableImage>>, Pair<List<vtkActor>, List<ImageRenderables>>> sceneBuilder =
				new SceneBuilderOperator();

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();

	}


	public List<vtkActor> getRenderableImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
		for (ImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageActors.addAll(renderable.getFootprints());
		}
		return imageActors;
	}

	public List<vtkActor> getRenderableImageBoundaryActors()
	{
		List<vtkActor> imageBoundaryActors = Lists.newArrayList();
		for (ImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageBoundaryActors.addAll(renderable.getBoundaries());
		}
		return imageBoundaryActors;
	}

	public List<vtkActor> getRenderableImageFrustumActors()
	{
		List<vtkActor> frustumActors = Lists.newArrayList();
		for (ImageRenderables renderable : sceneOutputs[0].getRight())
		{
			frustumActors.add(renderable.getFrustum());
		}
		return frustumActors;
	}

	public List<vtkActor> getRenderableOfflimbImageActors()
	{
		List<vtkActor> offLimbActors = Lists.newArrayList();
		for (ImageRenderables renderable : sceneOutputs[0].getRight())
		{
			offLimbActors.add(renderable.getOffLimb());
			offLimbActors.add(renderable.getOffLimbBoundary());
		}
		return offLimbActors;
	}

	public List<vtkActor> getSmallBodyActors()
	{
		return sceneOutputs[0].getLeft();
	}
}
