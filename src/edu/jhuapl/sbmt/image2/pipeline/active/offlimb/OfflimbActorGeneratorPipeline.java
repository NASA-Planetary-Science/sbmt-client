package edu.jhuapl.sbmt.image2.pipeline.active.offlimb;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInVTKReader;
import edu.jhuapl.sbmt.image2.modules.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.modules.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMaskOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.PointingFileReader;

public class OfflimbActorGeneratorPipeline
{

	public OfflimbActorGeneratorPipeline() throws Exception
	{
		//***********************
		//generate image layer
		//***********************
		IPipelinePublisher<Layer> reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT", new double[] {});
		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(537, 412);
		LayerMaskOperator maskOperator = new LayerMaskOperator(14, 14, 2, 2);

		List<Layer> updatedLayers = Lists.newArrayList();
		reader
			.operate(linearInterpolator)
//			.operate(maskOperator)
			.subscribe(Sink.of(updatedLayers)).run();

		//generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<PointingFileReader> pointingPublisher = new InfofileReaderPublisher("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.INFO");

		//generate metadata (in: filename, out: ImageMetadata)
		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT");

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
//				IPipelinePublisher<Object> imageComponents = Publishers.zip(layerPublisher, metadataReader, pointingPublisher);
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
			renderableImg.setMasking(new LayerMasking(new int[] {14, 14, 2, 2}));
			renderableImg.setOffset(0);
			renderableImg.setDefaultOffset(0);
		}

		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");
		Pair<RenderablePointedImage, vtkPolyData>[] pairSink = new Pair[1];
		List<vtkActor> actors = Lists.newArrayList();
		Just.of(renderableImages.get(0))
			.operate(new OfflimbPlaneGenerator(326.43141287061167, vtkReader.getOutputs().get(0)))
			.operate(new OfflimbActorOperator())
			.subscribe(Sink.of(actors))
			//.subscribe(PairSink.of(pairSink))
			.run();


//		IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderablePointedImage>>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(renderableImages));

		IPipelinePublisher<Pair<List<SmallBodyModel>, List<vtkActor>>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(actors));

		//***************************************************************************
		//Pass them into the scene builder to perform intersection calculations
		//***************************************************************************
		IPipelineOperator<Pair<List<SmallBodyModel>, List<vtkActor>>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

		//*******************************
		//Throw them to the preview tool
		//*******************************
		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));


		System.out.println("OfflimbActorGeneratorPipeline: OfflimbActorGeneratorPipeline: running preview pipeline");
		sceneObjects
			.operate(sceneBuilder)
			.subscribe(preview)
			.run();

//		sceneObjects
//			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
//			.subscribe(preview)		//subscribe to the scene builder with the preview
//			.run();

	}

	public static void main(String[] args) throws Exception
	{
		NativeLibraryLoader.loadAllVtkLibraries();
		OfflimbActorGeneratorPipeline pipeline = new OfflimbActorGeneratorPipeline();
	}

}
