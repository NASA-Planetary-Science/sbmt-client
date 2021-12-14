package edu.jhuapl.sbmt.image2.pipeline.active;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInVTKReader;
import edu.jhuapl.sbmt.image2.modules.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.modules.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.color.ColorImageFootprintGeneratorOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.color.ColorImageGeneratorOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.vtk.VTKImagePolyDataRenderer;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;

public class ColorImageGeneratorPipeline
{
	List<vtkActor> imageActors = Lists.newArrayList();
	List<vtkImageData> imageDatas = Lists.newArrayList();

	public ColorImageGeneratorPipeline(List<PerspectiveImage> images, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		Just.of(images)
			.operate(new ColorImageGeneratorOperator())
			.operate(new ColorImageFootprintGeneratorOperator(smallBodyModels))
			.operate(new VTKImagePolyDataRenderer())
			.subscribe(Sink.of(imageActors))
			.run();
	}

	public List<vtkActor> getImageActors()
	{
		return imageActors;
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

		List<PerspectiveImage> images = List.of(image1, image2, image3);

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
