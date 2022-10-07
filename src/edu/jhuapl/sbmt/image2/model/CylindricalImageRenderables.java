package edu.jhuapl.sbmt.image2.model;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.vtk.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.cylindricalImages.RenderableCylindricalImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class CylindricalImageRenderables extends ImageRenderable
{
	List<vtkImageData> imageData = Lists.newArrayList();

	public CylindricalImageRenderables(IRenderableImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		this.smallBodyModels = smallBodyModels;
		prepareFootprints(image);
		processFootprints(footprintPolyData, imageData, image.isLinearInterpolation());
		processBoundaries();
	}

	private void prepareFootprints(IRenderableImage renderableImage) throws IOException, Exception
	{
		//clips if the image doesn't cover the entire body, and generates texture coords
		RenderableCylindricalImageFootprintGeneratorPipeline pipeline =
				new RenderableCylindricalImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		footprintPolyData = pipeline.getFootprintPolyData();
        VtkImageRendererOperator imageRenderer = new VtkImageRendererOperator();
        Just.of(renderableImage.getLayer())
        	.operate(imageRenderer)
        	.operate(new VtkImageContrastOperator(new IntensityRange(0, 255)))
        	.operate(new VtkImageVtkMaskingOperator(renderableImage.getMasking().getMask()))
        	.subscribe(Sink.of(imageData))
        	.run();
	}
}

