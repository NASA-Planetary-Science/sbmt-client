package edu.jhuapl.sbmt.image2.ui.color;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.image2.modules.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.ColorImageGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;

public class ColorImageBuilderPanel extends JPanel
{
	private List<SmallBodyModel> smallBodyModels;
	private JPanel previewPanel;
	SingleImagePreviewPanel redPreview;
	SingleImagePreviewPanel greenPreview;
	SingleImagePreviewPanel bluePreview;

	public ColorImageBuilderPanel(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
		redPreview = new SingleImagePreviewPanel("Red Image");
		greenPreview = new SingleImagePreviewPanel("Green Image");
		bluePreview = new SingleImagePreviewPanel("Blue Image");
		initGUI();
	}

	private void initGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(makeImagePanel());
		add(Box.createVerticalStrut(5));
		add(makeButtonPanel());
		add(Box.createVerticalStrut(5));
		add(makePreviewPanel());

		setSize(750, 1200);
	}

	private JPanel makeImagePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(redPreview);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(greenPreview);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(bluePreview);
		return panel;
	}

	private JPanel makePreviewPanel()
	{
		previewPanel = new JPanel();
		previewPanel.setSize(750, 650);
		previewPanel.add(new JLabel("Select 3 images, then clip the preview button above."));
		previewPanel.setPreferredSize(new Dimension(750, 650));
		previewPanel.setMaximumSize(new Dimension(750, 650));

		return previewPanel;
	}

	private JPanel makeButtonPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(e -> {

			try {
				List<PerspectiveImage> images = List.of(redPreview.getPerspectiveImage(), greenPreview.getPerspectiveImage(), bluePreview.getPerspectiveImage());
				ColorImageGeneratorPipeline pipeline = new ColorImageGeneratorPipeline(images, smallBodyModels);
				List<vtkActor> actors = pipeline.getImageActors();

				IPipelinePublisher<Pair<List<SmallBodyModel>, List<vtkActor>>> sceneObjects = Publishers.formPair(Just.of(smallBodyModels), Just.of(actors));
				IPipelineOperator<Pair<List<SmallBodyModel>, List<vtkActor>>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

				VtkRendererPreview preview = new VtkRendererPreview(smallBodyModels.get(0));

				sceneObjects
					.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
					.subscribe(preview)		//subscribe to the scene builder with the preview
					.run();

				previewPanel.removeAll();
				JPanel renderPanel = (JPanel)preview.getPanel();
				renderPanel.setMinimumSize(new Dimension(750, 650));
				renderPanel.setPreferredSize(new Dimension(750, 650));
				renderPanel.setMaximumSize(new Dimension(750, 650));
				previewPanel.add(renderPanel);
				previewPanel.repaint();
				previewPanel.validate();
				add(previewPanel);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

		});

		JButton saveAndCloseButton = new JButton("Save and Close");
		panel.add(previewButton);
		panel.add(saveAndCloseButton);

		return panel;

	}
}