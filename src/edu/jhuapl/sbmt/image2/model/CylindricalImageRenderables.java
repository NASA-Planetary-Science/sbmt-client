package edu.jhuapl.sbmt.image2.model;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkFeatureEdges;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.cylindricalImages.RenderableCylindricalImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VTKImagePolyDataRenderer;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class CylindricalImageRenderables
{
	private List<vtkActor> footprintActors = Lists.newArrayList();
	private List<vtkPolyData> footprintPolyData = Lists.newArrayList();
	private vtkActor frustumActor;
	private vtkActor offLimbActor;
	private vtkActor offLimbBoundaryActor;
	private List<vtkActor> boundaryActors = Lists.newArrayList();
	private List<SmallBodyModel> smallBodyModels;
	public double maxFrustumDepth;
	public double minFrustumDepth;

	public CylindricalImageRenderables(RenderableCylindricalImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		this.smallBodyModels = smallBodyModels;
		processFootprints(image);
		processBoundaries(image);
	}

	private void processFootprints(RenderableCylindricalImage renderableImage) throws IOException, Exception
	{
		//clips if the image doesn't cover the entire body, and generates texture coords
		RenderableCylindricalImageFootprintGeneratorPipeline pipeline =
				new RenderableCylindricalImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		List<vtkPolyData> footprints = pipeline.getFootprintPolyData();
        VtkImageRendererOperator imageRenderer = new VtkImageRendererOperator();
        List<vtkImageData> imageData = Lists.newArrayList();
//        vtkImageData imageData = new vtkImageData();
        Just.of(renderableImage.getLayer())
        	.operate(imageRenderer)
        	.operate(new VtkImageContrastOperator(new IntensityRange(0, 255)))
        	.operate(new VtkImageVtkMaskingOperator(renderableImage.getMasking().getMask()))
        	.subscribe(Sink.of(imageData))
        	.run();
        int i=0;
    	for (SmallBodyModel smallBody : smallBodyModels)
    	{
    		vtkPolyData footprint = footprints.get(i++);
	        footprintPolyData.add(footprint);
	        List<vtkActor> actors = Lists.newArrayList();
	        Just.of(Pair.of(imageData.get(0), footprint))
	        	.operate(new VTKImagePolyDataRenderer(true))
	        	.subscribe(Sink.of(actors))
	        	.run();
	        footprintActors.addAll(actors);
    	}
	}

	private void processBoundaries(RenderableCylindricalImage renderableImage) throws IOException, Exception
	{
		vtkPolyData boundary;
		vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
		vtkActor boundaryActor = new vtkActor();

    	for (vtkPolyData footprint : footprintPolyData)
    	{
			vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
			edgeExtracter.SetInputData(footprint);
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
				boundary.DeepCopy(edgeExtracterOutput);
				if (boundaryMapper != null)
				{
			        boundaryMapper.SetInputData(boundary);
			        boundaryMapper.Update();
			        boundaryActor.SetMapper(boundaryMapper);
			        boundaryActors.add(boundaryActor);
					return;
				}
	    	}
    	}
	}

	public List<vtkActor> getFootprints()
	{
		return footprintActors;
	}

	public vtkActor getFrustum()
	{
		return frustumActor;
	}

	public vtkActor getOffLimb()
	{
		return offLimbActor;
	}

	public vtkActor getOffLimbBoundary()
	{
		return offLimbBoundaryActor;
	}

	public List<vtkActor> getBoundaries()
	{
		return boundaryActors;
	}
}
