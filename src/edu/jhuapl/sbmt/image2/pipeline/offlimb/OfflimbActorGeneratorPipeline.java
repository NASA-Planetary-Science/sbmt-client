package edu.jhuapl.sbmt.image2.pipeline.offlimb;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInVTKReader;
import edu.jhuapl.sbmt.image2.pipeline.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.pipeline.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.pipeline.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class OfflimbActorGeneratorPipeline
{
	public OfflimbActorGeneratorPipeline() throws Exception
	{
		//***********************
		//generate image layer
		//***********************
		IPipelinePublisher<Layer> reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT", new double[] {});
		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(537, 412);

		List<Layer> updatedLayers = Lists.newArrayList();
		reader
			.operate(linearInterpolator)
			.subscribe(Sink.of(updatedLayers)).run();

		//generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<PointingFileReader> pointingPublisher = new InfofileReaderPublisher("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.INFO");

		//generate metadata (in: filename, out: ImageMetadata)
		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT");

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents = Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator = new RenderablePointedImageGenerator();

		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************
		List<RenderablePointedImage> renderableImages = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages)).run();



		for (RenderablePointedImage renderableImg : renderableImages)
		{
			renderableImg.setFilename("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT");
			renderableImg.setMasking(new LayerMasking(new int[] {14, 14, 2, 2}));
			renderableImg.setOffset(0);
			renderableImg.setDefaultOffset(0);
		}

		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");
		RenderablePointedImageFootprintGeneratorPipeline pipeline = new RenderablePointedImageFootprintGeneratorPipeline(renderableImages.get(0), vtkReader.getOutputs());

		List<vtkPolyData> footprints = pipeline.getFootprintPolyData();
		Pair<RenderablePointedImage, vtkPolyData>[] pairSink = new Pair[1];
		List<vtkActor> actors = Lists.newArrayList();
		Just.of(renderableImages.get(0))
			.operate(new OfflimbPlaneGenerator(326.43141287061167, vtkReader.getOutputs().get(0), footprints.get(0).GetBounds(), footprints.get(0).GetNumberOfPoints()))
			.operate(new OfflimbActorOperator(null))
			.subscribe(Sink.of(actors))
			//.subscribe(PairSink.of(pairSink))
			.run();

		IPipelinePublisher<Pair<SmallBodyModel, vtkActor>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(actors));

		//***************************************************************************
		//Pass them into the scene builder to perform intersection calculations
		//***************************************************************************
		IPipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

		//*******************************
		//Throw them to the preview tool
		//*******************************
		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));

		sceneObjects
			.operate(sceneBuilder)
			.subscribe(preview)
			.run();
	}

	public static void main(String[] args) throws Exception
	{
		NativeLibraryLoader.loadAllVtkLibraries();
		OfflimbActorGeneratorPipeline pipeline = new OfflimbActorGeneratorPipeline();
	}
}