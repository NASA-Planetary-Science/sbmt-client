package edu.jhuapl.sbmt.image2.pipeline.active.cylindricalImages;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInPNGHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInPNGReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInVTKReader;
import edu.jhuapl.sbmt.image2.modules.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.modules.rendering.CylindricalImageRenderables;
import edu.jhuapl.sbmt.image2.modules.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.CylindricalBounds;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.RenderableCylindricalImageGenerator;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.SceneCylindricalImageBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class RenderableCylindricalImageActorPipeline implements RenderableImageActorPipeline
{
	List<vtkActor> renderableImageActors = Lists.newArrayList();
	Pair<List<vtkActor>, List<CylindricalImageRenderables>>[] sceneOutputs = new Pair[1];

	public RenderableCylindricalImageActorPipeline(String fileName, CylindricalBounds bounds, List<SmallBodyModel> smallBodyModel) throws Exception
	{
		//***********************
		//generate image layer
		//***********************
		//TODO: eventually replace this with a GDAL call to read in the data
		IPipelinePublisher<Layer> reader = null;
		IPipelinePublisher<HashMap<String, String>> metadataReader = null;
		System.out.println("RenderableCylindricalImageActorPipeline: RenderableCylindricalImageActorPipeline: extension is " + FilenameUtils.getExtension(fileName));
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

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
//		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, CylindricalBounds>> imageComponents =
				Publishers.formTriple(reader, metadataReader, Just.of(bounds));

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

		//*************************
		//zip the sources together
		//*************************
		IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderableCylindricalImage>>> sceneObjects =
				Publishers.formPair(Just.of(smallBodyModel), Just.of(renderableImages));

		//*****************************************************************************************************
		//Pass them into the scene builder to perform intersection calculations, and send actors to List
		//*****************************************************************************************************
		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderableCylindricalImage>>, Pair<List<vtkActor>, List<CylindricalImageRenderables>>> sceneBuilder =
				new SceneCylindricalImageBuilderOperator();

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();

	}


	public List<vtkActor> getRenderableImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
		for (CylindricalImageRenderables renderable : sceneOutputs[0].getRight())
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

		RenderableCylindricalImageActorPipeline pipeline =
				new RenderableCylindricalImageActorPipeline("/Users/steelrj1/.sbmt-stage-apl/cache/2/GASKELL/EROS/MSI/images/M0125990473F4_2P_IOF_DBL.FIT", new CylindricalBounds(-25, 30, 142, 200), smallBodyModels); //was -25, 30, -38, 20

//		RenderableCylindricalImageActorPipeline pipeline =
//				new RenderableCylindricalImageActorPipeline("/Users/steelrj1/Desktop/image_map.png", new CylindricalBounds(-90, 90, 0, 360), smallBodyModels);


		List<vtkActor> actors = pipeline.getRenderableImageActors();

		IPipelinePublisher<Pair<List<SmallBodyModel>, List<vtkActor>>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(actors));
		IPipelineOperator<Pair<List<SmallBodyModel>, List<vtkActor>>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(preview)		//subscribe to the scene builder with the preview
			.run();

	}

	public List<vtkActor> getRenderableImageFrustumActors() { return Lists.newArrayList(); }

	public List<vtkActor> getRenderableOfflimbImageActors() { return Lists.newArrayList(); }
}
