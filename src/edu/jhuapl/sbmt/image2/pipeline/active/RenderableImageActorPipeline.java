package edu.jhuapl.sbmt.image2.pipeline.active;

import java.util.List;

import vtk.vtkActor;

public interface RenderableImageActorPipeline
{

	List<vtkActor> getRenderableImageActors();

	List<vtkActor> getRenderableImageBoundaryActors();

	List<vtkActor> getRenderableImageFrustumActors();

	List<vtkActor> getRenderableOfflimbImageActors();

	List<vtkActor> getSmallBodyActors();

}