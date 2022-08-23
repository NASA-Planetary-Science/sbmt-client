package edu.jhuapl.sbmt.image2.controllers;

import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.pipeline.ColorImageGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.pipeline.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.ui.color.ColorImageBuilderPanel;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;

public class ColorImageBuilderController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	ColorImageBuilderPanel panel;
	PerspectiveImageCollection<G1> imageCollection;
	Optional<G1> existingImage;

	public ColorImageBuilderController(List<SmallBodyModel> smallBodyModels, PerspectiveImageCollection<G1> imageCollection, Optional<G1> existingImage)
	{
		this.existingImage = existingImage;
		this.panel = new ColorImageBuilderPanel(smallBodyModels);
		this.imageCollection = imageCollection;
		initGUI(smallBodyModels);
	}

	private void initGUI(List<SmallBodyModel> smallBodyModels)
	{
		panel.getPreviewButton().addActionListener(e -> {
			try {
				List<IPerspectiveImage> images = panel.getImages();
				ColorImageGeneratorPipeline pipeline = new ColorImageGeneratorPipeline(images, smallBodyModels);
				List<vtkActor> actors = pipeline.getImageActors();

				IPipelinePublisher<Pair<SmallBodyModel, vtkActor>> sceneObjects = Publishers.formPair(Just.of(smallBodyModels), Just.of(actors));
				IPipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

				VtkRendererPreview preview = new VtkRendererPreview(smallBodyModels.get(0));

				sceneObjects
					.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
					.subscribe(preview)		//subscribe to the scene builder with the preview
					.run();

				panel.updatePreviewPanel(preview);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		});

		panel.getSaveAndCloseButton().addActionListener(e -> {

			existingImage.ifPresent(image -> imageCollection.removeUserImage(image));

			CompositePerspectiveImage colorImage = new CompositePerspectiveImage(panel.getImages());
			imageCollection.addUserImage((G1)colorImage);
			SwingUtilities.getWindowAncestor(panel).setVisible(false);
		});
	}

	public void setImages(List<IPerspectiveImage> images)
	{
		this.panel.setImages(images);
	}

	public JPanel getView()
	{
		return panel;
	}
}