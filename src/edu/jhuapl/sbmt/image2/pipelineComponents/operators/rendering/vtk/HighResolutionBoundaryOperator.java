package edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.vtk;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkFeatureEdges;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class HighResolutionBoundaryOperator extends BasePipelineOperator<Pair<List<vtkPolyData>, List<SmallBodyModel>>, vtkActor>
{

	public HighResolutionBoundaryOperator()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processData() throws IOException, Exception
	{
		List<vtkActor> boundaryActors = Lists.newArrayList();
		List<vtkPolyData> footprintPolyData = inputs.get(0).getLeft();
		List<SmallBodyModel> smallBodyModels = inputs.get(0).getRight();
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
				}
	    	}
    	}
    	outputs.addAll(boundaryActors);
	}

}
