package edu.jhuapl.sbmt.image2.ui.color;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.preview.VtkRendererPreview;

public class ColorImageBuilderPanel extends JPanel
{
	private List<SmallBodyModel> smallBodyModels;
	private JPanel previewPanel;
	private SingleImagePreviewPanel redPreview;
	private SingleImagePreviewPanel greenPreview;
	private SingleImagePreviewPanel bluePreview;
	private JButton saveAndCloseButton;
	private JButton previewButton;

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

		previewButton = new JButton("Preview");

		saveAndCloseButton = new JButton("Save and Close");
		panel.add(previewButton);
		panel.add(saveAndCloseButton);

		return panel;

	}

	public List<IPerspectiveImage> getImages()
	{
		return List.of(redPreview.getPerspectiveImage(), greenPreview.getPerspectiveImage(), bluePreview.getPerspectiveImage());
	}

	public void setImages(List<IPerspectiveImage> images)
	{
		redPreview.setPerspectiveImage(images.get(0));
		greenPreview.setPerspectiveImage(images.get(1));
		bluePreview.setPerspectiveImage(images.get(2));
	}

	/**
	 * @return the saveAndCloseButton
	 */
	public JButton getSaveAndCloseButton()
	{
		return saveAndCloseButton;
	}

	/**
	 * @return the previewButton
	 */
	public JButton getPreviewButton()
	{
		return previewButton;
	}

	/**
	 * @return the previewPanel
	 */
	public JPanel getPreviewPanel()
	{
		return previewPanel;
	}

	public void updatePreviewPanel(VtkRendererPreview preview)
	{
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
}