package edu.jhuapl.sbmt.image2.pipeline;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipeline.pointedImages.RenderablePointedImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.preview.PointedImageEditingWindow;
import edu.jhuapl.sbmt.image2.pipeline.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;

public class PointedRenderableImageEditingPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	public PointedRenderableImageEditingPipeline(G1 image, List<SmallBodyModel> smallBodies) throws Exception
	{
		RenderablePointedImageActorPipeline<G1> actorPipeline =
				new RenderablePointedImageActorPipeline<G1>(image, smallBodies);
		List<vtkActor> actors = actorPipeline.getRenderableImageActors();

		IPipelinePublisher<Pair<SmallBodyModel, vtkActor>> sceneObjects =
				Publishers.formPair(Just.of(smallBodies), Just.of(actors));

		//***************************************************************************
		//Pass them into the scene builder to perform intersection calculations
		//***************************************************************************
		IPipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

		//*******************************
		//Throw them to the preview tool
		//*******************************
		PointedImageEditingWindow<G1> preview = new PointedImageEditingWindow<G1>(image, smallBodies.get(0));

		sceneObjects
			.operate(sceneBuilder)
			.subscribe(preview)
			.run();
	}
}
