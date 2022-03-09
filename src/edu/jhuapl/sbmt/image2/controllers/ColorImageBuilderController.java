package edu.jhuapl.sbmt.image2.controllers;

import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.ColorImageBuilderModel;
import edu.jhuapl.sbmt.image2.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.modules.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.modules.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.ColorImageGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.ui.color.ColorImageBuilderPanel;

public class ColorImageBuilderController
{

	ColorImageBuilderPanel panel;
	ColorImageBuilderModel model;
	PerspectiveImageCollection imageCollection;
	Optional<IPerspectiveImage> existingImage;

	public ColorImageBuilderController(List<SmallBodyModel> smallBodyModels, PerspectiveImageCollection imageCollection, Optional<IPerspectiveImage> existingImage)
	{
		this.model = new ColorImageBuilderModel();
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

				IPipelinePublisher<Pair<List<SmallBodyModel>, List<vtkActor>>> sceneObjects = Publishers.formPair(Just.of(smallBodyModels), Just.of(actors));
				IPipelineOperator<Pair<List<SmallBodyModel>, List<vtkActor>>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

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
			imageCollection.addUserImage(colorImage);
			SwingUtilities.getWindowAncestor(panel).setVisible(false);
//			panel.getParent().setVisible(false);
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
