package edu.jhuapl.sbmt.image2.pipeline.cylindricalImages;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.CylindricalBounds;
import edu.jhuapl.sbmt.image2.model.CylindricalImageRenderables;
import edu.jhuapl.sbmt.image2.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.pipeline.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInPNGHeaderReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInPNGReader;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInVTKReader;
import edu.jhuapl.sbmt.image2.pipeline.rendering.cylindricalImage.RenderableCylindricalImageGenerator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.cylindricalImage.SceneCylindricalImageBuilderOperator;
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
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class RenderCylindricalImageToScenePipeline implements RenderableImageActorPipeline
{
	List<vtkActor> renderableImageActors = Lists.newArrayList();
	Pair<List<vtkActor>, List<CylindricalImageRenderables>>[] sceneOutputs;

	public RenderCylindricalImageToScenePipeline(IPerspectiveImage image, /*String fileName, CylindricalBounds bounds,*/ List<SmallBodyModel> smallBodyModel) throws Exception
	{
		//***********************
		//generate image layer
		//***********************
		//TODO: eventually replace this with a GDAL call to read in the data
		IPipelinePublisher<Layer> reader = null;
		String fileName = image.getFilename();
		CylindricalBounds bounds = image.getBounds();
		IPipelinePublisher<HashMap<String, String>> metadataReader = null;
		if (FilenameUtils.getExtension(fileName).toLowerCase().equals("fit") || FilenameUtils.getExtension(fileName).toLowerCase().equals("fits"))
		{
			reader = new BuiltInFitsReader(fileName, new double[] {});
			metadataReader = new BuiltInFitsHeaderReader(fileName);
		}
		else if (FilenameUtils.getExtension(fileName).toLowerCase().equals("png"))
		{
			reader = new BuiltInPNGReader(fileName);
			metadataReader = new BuiltInPNGHeaderReader(fileName);
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
		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		List<HashMap<String, String>> metadataReaders = Lists.newArrayList();
		List<CylindricalBounds> boundsCollection = Lists.newArrayList();
		for (int i=0; i<reader.getOutputs().size(); i++)
		{
			metadataReaders.add(metadataReader.getOutput());
			boundsCollection.add(bounds);
		}
//		System.out.println("RenderCylindricalImageToScenePipeline: RenderCylindricalImageToScenePipeline: metadata size " + metadataReaders.size());
//		System.out.println("RenderCylindricalImageToScenePipeline: RenderCylindricalImageToScenePipeline: bounds collection size " + boundsCollection.size());
		IPipelinePublisher<HashMap<String, String>> metadataPipeline = Just.of(metadataReaders);
		IPipelinePublisher<CylindricalBounds> boundsPipeline = Just.of(boundsCollection);

		IPipelinePublisher<Triple<Layer, HashMap<String, String>, CylindricalBounds>> imageComponents =
				Publishers.formTriple(Just.of(updatedLayers), metadataPipeline, boundsPipeline);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, CylindricalBounds>, RenderableCylindricalImage> renderableImageGenerator =
				new RenderableCylindricalImageGenerator();
		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************
		List<RenderableCylindricalImage> renderableImages = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages)).run();
		for (RenderableCylindricalImage renderableImage : renderableImages)
		{
			renderableImage.setOffset(3.0 * smallBodyModel.get(0).getMinShiftAmount());
			renderableImage.setDefaultOffset(3.0 * smallBodyModel.get(0).getMinShiftAmount());
		}
//		System.out.println("RenderCylindricalImageToScenePipeline: RenderCylindricalImageToScenePipeline: number of renderableImages " + renderableImages.size());
		//*************************
		//zip the sources together
		//*************************
		List<SmallBodyModel> smallBodyModelCollection = Lists.newArrayList();
		for (int i=0; i<renderableImages.size(); i++)
		{
			smallBodyModelCollection.add(smallBodyModel.get(0));
		}
		IPipelinePublisher<Pair<SmallBodyModel, RenderableCylindricalImage>> sceneObjects =
				Publishers.formPair(Just.of(smallBodyModelCollection), Just.of(renderableImages));
//		System.out.println("RenderCylindricalImageToScenePipeline: RenderCylindricalImageToScenePipeline: number of scene objects " + sceneObjects.getOutputs().size());
		//*****************************************************************************************************
		//Pass them into the scene builder to perform intersection calculations, and send actors to List
		//*****************************************************************************************************
		sceneOutputs = new Pair[renderableImages.size()];
		IPipelineOperator<Pair<SmallBodyModel, RenderableCylindricalImage>, Pair<List<vtkActor>, List<CylindricalImageRenderables>>> sceneBuilder =
				new SceneCylindricalImageBuilderOperator();
		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();
//		System.out.println("RenderCylindricalImageToScenePipeline: RenderCylindricalImageToScenePipeline: number of scene outputs " + sceneOutputs.length);
	}


	public List<vtkActor> getRenderableImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
//		for (int i=0; i<sceneOutputs.length; i++)
//			System.out.println("RenderCylindricalImageToScenePipeline: getRenderableImageActors: scene output " + sceneOutputs[i]);
//		System.out.println("RenderCylindricalImageToScenePipeline: getRenderableImageActors: number of scene outputs " + sceneOutputs.length);
		for (CylindricalImageRenderables renderable : sceneOutputs[14].getRight())
		{
			imageActors.addAll(renderable.getFootprints());
		}
		return imageActors;
	}

	public List<vtkActor> getRenderableImageBoundaryActors()
	{
		List<vtkActor> imageBoundaryActors = Lists.newArrayList();
		for (CylindricalImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageBoundaryActors.addAll(renderable.getBoundaries());
		}
		return imageBoundaryActors;
	}

	public List<vtkActor> getSmallBodyActors()
	{
		return sceneOutputs[0].getLeft();
	}

	public static void main(String[] args) throws Exception
	{
		NativeLibraryLoader.loadAllVtkLibraries();

		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");
		List<SmallBodyModel> smallBodyModels = Lists.newArrayList();

		vtkReader.subscribe(Sink.of(smallBodyModels)).run();

//		RenderCylindricalImageToScenePipeline pipeline =
//				new RenderCylindricalImageToScenePipeline("/Users/steelrj1/.sbmt-stage-apl/cache/2/GASKELL/EROS/MSI/images/M0125990473F4_2P_IOF_DBL.FIT", new CylindricalBounds(-25, 30, 142, 200), smallBodyModels); //was -25, 30, -38, 20
//
////		RenderableCylindricalImageActorPipeline pipeline =
////				new RenderableCylindricalImageActorPipeline("/Users/steelrj1/Desktop/image_map.png", new CylindricalBounds(-90, 90, 0, 360), smallBodyModels);
//
//
//		List<vtkActor> actors = pipeline.getRenderableImageActors();
//
//		IPipelinePublisher<Pair<SmallBodyModel, vtkActor>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(actors));
//		IPipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();
//
//		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));
//
//		sceneObjects
//			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
//			.subscribe(preview)		//subscribe to the scene builder with the preview
//			.run();

	}

	public List<vtkActor> getRenderableImageFrustumActors() { return Lists.newArrayList(); }

	public List<vtkActor> getRenderableOfflimbImageActors() { return Lists.newArrayList(); }


	@Override
	public List<vtkActor> getRenderableOffLimbBoundaryActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageBoundaryActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageFrustumActors()
	{
		return Lists.newArrayList();
	}
}
