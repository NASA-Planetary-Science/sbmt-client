package edu.jhuapl.sbmt.pipeline;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.VTKDebug;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.pointing.SpiceBodyOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerMaskOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerRotationOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerTrimOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.ScenePointedImageBuilderOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInOBJReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInPNGHeaderReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInPNGReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInVTKReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing.SpiceReaderPublisher;
import edu.jhuapl.sbmt.image2.pipelineComponents.subscribers.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.image2.pipelineComponents.subscribers.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.util.TimeUtil;

public class PipelineTests
{

	public static void main(String[] args) throws Exception
	{
		NativeLibraryLoader.loadAllVtkLibraries();
		new PipelineTests();
	}

	public PipelineTests() throws Exception
	{
//		test1();
//		test1b();
//		test2();
		test2b();
//		test3();
//		test4();
//		test5();
	}

	private void test1() throws Exception
	{
		IPipelinePublisher<Layer> reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/M0125990619F4_2P_IOF_DBL.FIT", new double[] {});

		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(537, 412);
		LayerMaskOperator maskOperator = new LayerMaskOperator(14, 14, 2, 2);
		LayerTrimOperator trimOperator = new LayerTrimOperator(14, 14, 2, 2);
//		VtkImageRenderer renderer = new VtkImageRenderer();
//		VtkImageContrastOperator contrastOperator = new VtkImageContrastOperator(null);
//		VtkImageVtkMaskingOperator maskingOperator = new VtkImageVtkMaskingOperator(new int[] {0,0,0,0});
		VtkLayerPreview preview = new VtkLayerPreview("Test 1", new Runnable()
		{

			@Override
			public void run()
			{
				// TODO Auto-generated method stub

			}
		});


//		reader
//			.operate(linearInterpolator)
//			.operate(maskOperator)
//			.operate(trimOperator)
////			.operate(renderer)
////			.operate(contrastOperator)
////			.operate(maskingOperator)
//			.subscribe(preview)
//			.run();
	}

	private void test1b() throws Exception
	{
		IPipelinePublisher<Layer> reader = new BuiltInPNGReader("/Users/steelrj1/Downloads/FRT00002992_03_IF162S_TRR7_RED_BLUE_RATIO_colortable.png");
//		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInPNGHeaderReader("/Users/steelrj1/Downloads/FRT00002992_03_IF162S_TRR7_RED_BLUE_RATIO_colortable.png");
//		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(537, 412);
//		LayerMaskOperator maskOperator = new LayerMaskOperator(14, 14, 2, 2);
//		LayerTrimOperator trimOperator = new LayerTrimOperator(14, 14, 2, 2);
////		VtkImageRenderer renderer = new VtkImageRenderer();
////		VtkImageContrastOperator contrastOperator = new VtkImageContrastOperator(null);
////		VtkImageVtkMaskingOperator maskingOperator = new VtkImageVtkMaskingOperator(new int[] {0,0,0,0});
//		VtkLayerPreview preview = new VtkLayerPreview();
//
//		IPipelinePublisher<Pair<Layer, HashMap<String, String>>> imageComponents = Publishers.formPair(reader, metadataReader);

		VTKDebug.previewLayer(reader.getOutputs().get(0), "Test 1b");

//		imageComponents
////			.operate(linearInterpolator)
////		.operate(maskOperator)
////		.operate(trimOperator)
////		.operate(renderer)
////		.operate(contrastOperator)
////		.operate(maskingOperator)
//			.subscribe(preview)
//		.run();
	}

	private void test2() throws Exception
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
//		IPipelinePublisher<Object> imageComponents = Publishers.zip(layerPublisher, metadataReader, pointingPublisher);
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents = Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator = new RenderablePointedImageGenerator();


		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************
		List<RenderablePointedImage> renderableImages = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages)).run();

		//***********************
		//generate body polydata
		//***********************
		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");

		//*************************
		//zip the sources together
		//*************************
//		IPipelinePublisher<List<Object>> sceneObjects = Publishers.mergeLists(vtkReader, new Just<RenderableImage>(renderableImages.get(0)));
		IPipelinePublisher<Pair<SmallBodyModel, RenderablePointedImage>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(renderableImages));


		//***************************************************************************
		//Pass them into the scene builder to perform intersection calculations
		//***************************************************************************
//		IPipelineOperator<Pair<List<SmallBodyModel>, List<vtkActor>>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();
		IPipelineOperator<Pair<SmallBodyModel, RenderablePointedImage>, Pair<List<vtkActor>, List<ImageRenderable>>> sceneBuilder =
				new ScenePointedImageBuilderOperator();

		//*******************************
		//Throw them to the preview tool
		//*******************************
//		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));
//
//		sceneObjects
//			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
//			.subscribe(preview)		//subscribe to the scene builder with the preview
//			.run();
	}

	private void test2b() throws Exception
	{
		//***********************
		//generate image layer
		//***********************
//		IPipelinePublisher<Layer> reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT", new double[] {});
//		LayerLinearInterpolaterOperator linearInterpolator = new LayerLinearInterpolaterOperator(537, 412);
//
		List<Layer> updatedLayers = Lists.newArrayList();
//		reader
//			.operate(linearInterpolator)
//			.subscribe(Sink.of(updatedLayers)).run();

		IPipelinePublisher<Layer> reader = new BuiltInPNGReader("/Users/steelrj1/Downloads/FRT00002992_03_IF162S_TRR7_RED_BLUE_RATIO_colortable.png");
		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInPNGHeaderReader("/Users/steelrj1/Downloads/FRT00002992_03_IF162S_TRR7_RED_BLUE_RATIO_colortable.png");
		reader
			.subscribe(Sink.of(updatedLayers)).run();

		VTKDebug.previewLayer(reader.getOutputs().get(0), "Layer right after input");
		//generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<PointingFileReader> pointingPublisher = new InfofileReaderPublisher("/Users/steelrj1/Downloads/FRT00002992_03_IF162S_TRR7_RED_BLUE_RATIO_colortable.INFO");

		//generate metadata (in: filename, out: ImageMetadata)
//		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT");

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
//		IPipelinePublisher<Object> imageComponents = Publishers.zip(layerPublisher, metadataReader, pointingPublisher);
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents = Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator = new RenderablePointedImageGenerator();


		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************
		List<RenderablePointedImage> renderableImages = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages)).run();

		//***********************
		//generate body polydata
		//***********************
		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInOBJReader(new String[] {"/Users/steelrj1/.sbmt-apl/cache/2/phobos/ernst2018/shape/shape0.obj"}, "Phobos");

		//*************************
		//zip the sources together
		//*************************
//		IPipelinePublisher<List<Object>> sceneObjects = Publishers.mergeLists(vtkReader, new Just<RenderableImage>(renderableImages.get(0)));
		IPipelinePublisher<Pair<SmallBodyModel, RenderablePointedImage>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(renderableImages));

		//***************************************************************************
		//Pass them into the scene builder to perform intersection calculations
		//***************************************************************************
//		IPipelineOperator<Pair<List<SmallBodyModel>, List<vtkActor>>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();
		IPipelineOperator<Pair<SmallBodyModel, RenderablePointedImage>, Pair<List<vtkActor>, List<ImageRenderable>>> sceneBuilder =
				new ScenePointedImageBuilderOperator();

		//*******************************
		//Throw them to the preview tool
		//*******************************
		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));
		Pair<List<vtkActor>, List<ImageRenderable>>[] sceneOutputs = new Pair[1];

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();
//		System.out.println("PipelineTests: test2b: number of actors " + sceneOutputs[0].getLeft().size());

		List<vtkActor> imageActors = Lists.newArrayList();
		for (ImageRenderable renderable : sceneOutputs[0].getRight())
		{
			imageActors.addAll(renderable.getFootprints());
		}

		Just.of(imageActors.get(0))
			.subscribe(preview)		//subscribe to the scene builder with the preview
			.run();
	}

	private void test3() throws Exception
	{
		//***********************
		//generate image layer
		//***********************
		IPipelinePublisher<Layer> reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/dart_717891977_782_01.fits", new double[] {-32768.0, -32767.0, 4095.0});
		LayerRotationOperator rotationOperator = new LayerRotationOperator(270);

		List<Layer> updatedLayers = Lists.newArrayList();
		reader
			.operate(rotationOperator)
			.subscribe(Sink.of(updatedLayers))
			.run();

		//generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<PointingFileReader> pointingPublisher = new InfofileReaderPublisher("/Users/steelrj1/Desktop/dart_717891977_782_01.INFO");

		//generate metadata (in: filename, out: ImageMetadata)
		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader("/Users/steelrj1/Desktop/dart_717891977_782_01.fits");

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
//		IPipelinePublisher<Object> imageComponents = Publishers.zip(layerPublisher, metadataReader, pointingPublisher);
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents = Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);
//		System.out.println("PipelineTests: test3: number of image components " + imageComponents.getOutputs().size());
		IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator = new RenderablePointedImageGenerator();


		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************

		List<RenderablePointedImage> renderableImages = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages))
			.run();
		//***********************
		//generate body polydata
		//***********************
		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInOBJReader(new String[]{"/Users/steelrj1/.sbmt1dart/cache/didymos/ideal-impact1-20200629-v01/shape/shape0.obj",
					"/Users/steelrj1/.sbmt1dart/cache/dimorphos/ideal-impact1-20200629-v01/shape/shape0.obj"}, "DIDYMOS", "DIMORPHOS");

		//*********************************
		//Use SPICE to position the bodies
		//*********************************
		SpiceInfo spiceInfo = new SpiceInfo("DART", "920065803_FIXED", "DART_SPACECRAFT", "DIDYMOS", new String[] {"DIMORPHOS"}, new String[] {"DART_DRACO_2X2", "120065803_FIXED"});
		IPipelinePublisher<SpicePointingProvider> pointingProviders = new SpiceReaderPublisher("/Users/steelrj1/dartspice/draco/impact.tm", spiceInfo, "DART_DRACO_2X2");
//		IPipelinePublisher<List<Object>> spiceBodyObjects = Publishers.mergeLists(vtkReader, pointingProviders);
		IPipelinePublisher<Pair<SmallBodyModel, SpicePointingProvider>> spiceBodyObjects = Publishers.formPair(vtkReader, pointingProviders);
		IPipelineOperator<Pair<SmallBodyModel, SpicePointingProvider>, SmallBodyModel> spiceBodyOperator = new SpiceBodyOperator("DIDYMOS", TimeUtil.str2et("2022-10-01T10:25:08.599"));
		List<SmallBodyModel> updatedBodies = Lists.newArrayList();
		spiceBodyObjects
			.operate(spiceBodyOperator)
			.subscribe(Sink.of(updatedBodies))
			.run();


		//*************************
		//zip the sources together
		//*************************
//		IPipelinePublisher<List<Object>> sceneObjects = Publishers.mergeLists(Just.of(updatedBodies), Just.of(renderableImages.get(0)));
		IPipelinePublisher<Pair<SmallBodyModel, RenderablePointedImage>> sceneObjects = Publishers.formPair(Just.of(updatedBodies), Just.of(renderableImages));

//		//***************************************************************************
//		//Pass them into the scene builder to perform intersection calculations
//		//***************************************************************************
//		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderableImage>>, vtkActor> sceneBuilder = new SceneBuilderOperator();
//
//		//*******************************
//		//Throw them to the preview tool
//		//*******************************
//		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));
//
//		sceneObjects
//			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
//			.subscribe(preview)		//subscribe to the scene builder with the preview
//			.run();
	}

	private void test4() throws Exception
	{
//		IPipelinePublisher<Layer> reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/dart_717891977_782_01.fits", new double[] {-32768.0, -32767.0, 4095.0});
//		VtkLayerPreview preview = new VtkLayerPreview();
//		reader
//			.subscribe(preview)
//			.run();
	}

//	private void test5() throws Exception
//	{
//		SpiceInfo spiceInfo1 = new SpiceInfo("DART", "920065803_FIXED", "DART_SPACECRAFT", "DIDYMOS", new String[] {"DIMORPHOS"}, new String[] {"DART_DRACO_2X2", "120065803_FIXED"});
//		SpiceInfo spiceInfo2 = new SpiceInfo("DART", "120065803_FIXED", "DART_SPACECRAFT", "DIMORPHOS", new String[] {"DIDYMOS"}, new String[] {"DART_DRACO_2X2", "920065803_FIXED"});
//		SpiceInfo[] spiceInfos = new SpiceInfo[] {spiceInfo1, spiceInfo2};
//		String[] bodies = new String[]{"/Users/steelrj1/.sbmt1dart/cache/didymos/ideal-impact1-20200629-v01/shape/shape0.obj",
//			"/Users/steelrj1/.sbmt1dart/cache/dimorphos/ideal-impact1-20200629-v01/shape/shape0.obj"};
//		String[] bodyNames = new String[]{"DIDYMOS", "DIMORPHOS"};
//		String[] imageFiles = new String[] {"/Users/steelrj1/Desktop/dart_717891977_782_01.fits"};
//		String[] pointingFiles = new String[] {"/Users/steelrj1/Desktop/dart_717891977_782_01.INFO"};
//		VtkRendererPreview2 preview = new VtkRendererPreview2(imageFiles, pointingFiles, bodies, bodyNames, spiceInfos, "/Users/steelrj1/dartspice/draco/impact.tm", "DIDYMOS", "2022-10-01T10:25:08.599", "DART_DRACO_2X2");
//	}


}