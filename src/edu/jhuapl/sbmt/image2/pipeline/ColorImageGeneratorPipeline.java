package edu.jhuapl.sbmt.image2.pipeline;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkFeatureEdges;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.io.builtIn.BuiltInVTKReader;
import edu.jhuapl.sbmt.image2.pipeline.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.pipeline.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.color.ColorImageFootprintGeneratorOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.color.ColorImageGeneratorOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VTKImagePolyDataRenderer;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class ColorImageGeneratorPipeline implements RenderableImageActorPipeline
{
	List<vtkActor> imageActors = Lists.newArrayList();
	List<vtkImageData> imageDatas = Lists.newArrayList();
	Pair<vtkImageData, vtkPolyData>[] imageAndPolyData = new Pair[1];
	 List<SmallBodyModel> smallBodyModels;
	public ColorImageGeneratorPipeline(List<IPerspectiveImage> images, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		this.smallBodyModels = smallBodyModels;
		Just.of(images)
			.operate(new ColorImageGeneratorOperator())
			.operate(new ColorImageFootprintGeneratorOperator(smallBodyModels))
			.subscribe(PairSink.of(imageAndPolyData))
			.run();

		Just.of(imageAndPolyData[0])
			.operate(new VTKImagePolyDataRenderer(images.get(0).getInterpolateState()))
			.subscribe(Sink.of(imageActors))
			.run();
	}

	public List<vtkActor> getImageActors()
	{
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableImageActors()
	{
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableImageBoundaryActors()
	{
		vtkPolyData polyData = imageAndPolyData[0].getRight();
		vtkPolyData boundary;
		vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
		vtkActor boundaryActor = new vtkActor();
		vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
		List<vtkActor> boundaryActors = Lists.newArrayList();
		edgeExtracter.SetInputData(polyData);
		edgeExtracter.BoundaryEdgesOn();
		edgeExtracter.FeatureEdgesOff();
		edgeExtracter.NonManifoldEdgesOff();
		edgeExtracter.ManifoldEdgesOff();
		edgeExtracter.ColoringOff();
		edgeExtracter.Update();

		for (SmallBodyModel smallBody : smallBodyModels)
    	{
			boundary = new vtkPolyData();
			vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
//			polyData.DeepCopy(edgeExtracterOutput);
			if (boundaryMapper != null)
			{
		        boundaryMapper.SetInputData(edgeExtracterOutput);
		        boundaryMapper.Update();
		        boundaryActor.SetMapper(boundaryMapper);
		        boundaryActors.add(boundaryActor);
			}
    	}
		return boundaryActors;
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageBoundaryActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableImageFrustumActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageFrustumActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableOfflimbImageActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getSmallBodyActors()
	{
		return Lists.newArrayList();
	}

	@Override
	public List<vtkActor> getRenderableOffLimbBoundaryActors()
	{
		return Lists.newArrayList();
	}

	public static void main(String[] args) throws Exception
	{
		NativeLibraryLoader.loadAllVtkLibraries();

		PerspectiveImage image1 = new PerspectiveImage("/Users/steelrj1/.sbmt-stage-apl/cache/2/GASKELL/EROS/MSI/images/M0125990473F4_2P_IOF_DBL.FIT", ImageType.MSI_IMAGE, ImageSource.SPICE, "/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.INFO", new double[] {});
		image1.setLinearInterpolatorDims(new int[] { 537, 412 });
		image1.setMaskValues(new int[] {2, 14, 2, 14});

		PerspectiveImage image2 = new PerspectiveImage("/Users/steelrj1/.sbmt-stage-apl/cache/2/GASKELL/EROS/MSI/images/M0125990619F4_2P_IOF_DBL.FIT", ImageType.MSI_IMAGE, ImageSource.SPICE, "/Users/steelrj1/Desktop/M0125990619F4_2P_IOF_DBL.INFO", new double[] {});
		image2.setLinearInterpolatorDims(new int[] { 537, 412 });
		image2.setMaskValues(new int[] {2, 14, 2, 14});

		PerspectiveImage image3 = new PerspectiveImage("/Users/steelrj1/.sbmt-stage-apl/cache/2/GASKELL/EROS/MSI/images/M0126023535F4_2P_IOF_DBL.FIT", ImageType.MSI_IMAGE, ImageSource.SPICE, "/Users/steelrj1/Desktop/M0126023535F4_2P_IOF_DBL.INFO", new double[] {});
		image3.setLinearInterpolatorDims(new int[] { 537, 412 });
		image3.setMaskValues(new int[] {2, 14, 2, 14});

		List<IPerspectiveImage> images = List.of(new CompositePerspectiveImage(List.of(image1)), new CompositePerspectiveImage(List.of(image2)), new CompositePerspectiveImage(List.of(image3)));

		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");
		List<SmallBodyModel> smallBodyModels = Lists.newArrayList();

		vtkReader.subscribe(Sink.of(smallBodyModels)).run();

		ColorImageGeneratorPipeline pipeline = new ColorImageGeneratorPipeline(images, smallBodyModels);

		List<vtkActor> actors = pipeline.getImageActors();

		IPipelinePublisher<Pair<List<SmallBodyModel>, List<vtkActor>>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(actors));
		IPipelineOperator<Pair<List<SmallBodyModel>, List<vtkActor>>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(preview)		//subscribe to the scene builder with the preview
			.run();

	}






}
