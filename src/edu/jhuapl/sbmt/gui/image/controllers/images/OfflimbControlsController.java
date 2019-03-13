package edu.jhuapl.sbmt.gui.image.controllers.images;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.sbmt.gui.image.model.OfflimbModelChangedListener;
import edu.jhuapl.sbmt.gui.image.model.images.OfflimbControlsModel;
import edu.jhuapl.sbmt.gui.image.ui.images.OfflimbImageControlPanel;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class OfflimbControlsController
{
	OfflimbImageControlPanel controlsPanel;
	OfflimbControlsModel controlsModel;
	PerspectiveImage image;
	DepthSlider depthSlider;
	AlphaSlider alphaSlider;
	ContrastSlider contrastSlider;
	ShowBoundaryButton showBoundaryBtn;
	SyncContrastSlidersButton syncButton;
	JButton resetButton;

	// reference to image slider in case they are synced
	ContrastSlider imageContrastSlider;

	public OfflimbControlsController(PerspectiveImage image, int currentSlice, ContrastSlider imgContrastSlider)
	{
		this.image = image;
		this.imageContrastSlider = imgContrastSlider;

		controlsModel = new OfflimbControlsModel(image, currentSlice);


		depthSlider = new DepthSlider();
		alphaSlider = new AlphaSlider();
		contrastSlider = new ContrastSlider(image, true);

		showBoundaryBtn = new ShowBoundaryButton();
		showBoundaryBtn.setSelected(controlsModel.getShowBoundary());

		syncButton = new SyncContrastSlidersButton();
		syncButton.setSelected(controlsModel.getSyncContrast());

		resetButton = new JButton("Reset Off-limb Settings");

		controlsPanel = new OfflimbImageControlPanel(depthSlider, alphaSlider, contrastSlider, showBoundaryBtn, syncButton, resetButton);

		controlsModel.addModelChangedListener(new OfflimbModelChangedListener()
		{

			@Override
			public void currentSliceChanged(int slice)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void currentDepthChanged(int depth)
			{
				int sliderValue = depthSlider.convertDepthToSliderValue(((PerspectiveImage)controlsModel.getImage()).getOffLimbPlaneDepth());
				controlsPanel.getFootprintDepthValue().setText("" + sliderValue);
			}

			@Override
			public void currentAlphaChanged(int alpha)
			{
				controlsPanel.getFootprintTransparencyValue().setText("" + alphaSlider.getValue());
			}

			@Override
			public void showBoundaryChanged() {
				showBoundaryBtn.showBoundary(showBoundaryBtn.isSelected());
				controlsModel.setShowBoundary(showBoundaryBtn.isSelected());
			}

			@Override
			public void syncContrastChanged() {
				syncButton.syncContrast(syncButton.isSelected());
				controlsModel.setSyncContrast(syncButton.isSelected());
			}


		});

		init();
	}

	private void init()
	{
		ChangeListener changeListener = new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (e.getSource() == controlsPanel.getFootprintDepthSlider() && !controlsPanel.getFootprintDepthSlider().getValueIsAdjusting())
				{
					depthSlider.applyDepthToImage(controlsModel.getCurrentSlice());
					controlsModel.setCurrentDepth(depthSlider.getValue());
					controlsModel.getImage().firePropertyChange();
				}
				else if (e.getSource() == controlsPanel.getFootprintTransparencySlider() && !controlsPanel.getFootprintTransparencySlider().getValueIsAdjusting())
				{
					alphaSlider.applyAlphaToImage();
					controlsModel.setCurrentAlpha(alphaSlider.getValue());
					controlsModel.getImage().firePropertyChange();
				}
				else if (e.getSource() == controlsPanel.getImageContrastSlider())
				{
					if(!controlsPanel.getImageContrastSlider().getValueIsAdjusting()) {
						contrastSlider.sliderStateChanged(e);
						controlsModel.setContrastLow(contrastSlider.getLowValue());
						controlsModel.setContrastHigh(contrastSlider.getHighValue());
					}
					if (image.isContrastSynced()) {
						// adjust image contrast slider also
						imageContrastSlider.setHighValue(contrastSlider.getHighValue());
						imageContrastSlider.setLowValue(contrastSlider.getLowValue());
					}
				}
				else if (e.getSource() == controlsPanel.getShowBoundaryButton())
				{
					showBoundaryBtn.showBoundary(showBoundaryBtn.isSelected());
					controlsModel.setShowBoundary(showBoundaryBtn.isSelected());
				}
				else if (e.getSource() == controlsPanel.getSyncContrastButton())
				{
					// let everyone know that we're syncing or unsyncing
					syncButton.syncContrast(syncButton.isSelected());
					controlsModel.setSyncContrast(syncButton.isSelected());
					if (controlsPanel.getSyncContrastButton().isSelected()) {
						// if we're syncing, set the slider values to that of the img slider
						contrastSlider.setLowValue(imageContrastSlider.getLowValue());
						contrastSlider.setHighValue(imageContrastSlider.getHighValue());
						controlsModel.setContrastLow(contrastSlider.getLowValue());
						controlsModel.setContrastHigh(contrastSlider.getHighValue());
					}
				}
				else if (e.getSource() == controlsPanel.getResetButton())
				{
					// let everyone know that we're syncing or unsyncing
					syncButton.setSelected(false);
					showBoundaryBtn.setSelected(true);
					depthSlider.setValue(0);
					alphaSlider.setValue(50);
				}
			}
		};

		controlsPanel.getFootprintDepthSlider().addChangeListener(changeListener);
		controlsPanel.getFootprintTransparencySlider().addChangeListener(changeListener);
		controlsPanel.getImageContrastSlider().addChangeListener(changeListener);
		controlsPanel.getShowBoundaryButton().addChangeListener(changeListener);
		controlsPanel.getSyncContrastButton().addChangeListener(changeListener);
		controlsPanel.getResetButton().addChangeListener(changeListener);


	}

	public OfflimbImageControlPanel getControlsPanel()
	{
		return controlsPanel;
	}

	public OfflimbControlsModel getControlsModel()
	{
		return controlsModel;
	}

	public class DepthSlider extends JSlider
	{
		double depthMin, depthMax;

		public DepthSlider()
		{
			setMinimum(-50);
			setMaximum(50);
		}

		public void setDepthBounds(double depthMin, double depthMax)
		{
			this.depthMin = depthMin;
			this.depthMax = depthMax;
		}

		public void applyDepthToImage(int currentSlice)
		{
			depthMin = image.getMinFrustumDepth(currentSlice);
			depthMax = image.getMaxFrustumDepth(currentSlice);
			image.setOffLimbPlaneDepth(getDepthValue());
		}

		public double getDepthValue()
		{
			double value = getValue();
			double sliderRange = getMaximum() - getMinimum();
			double imgRange = depthMax - depthMin;
			return (int) ((value - getMinimum()) * (imgRange/sliderRange) + depthMin);
		}

		public int convertDepthToSliderValue(double depth)
		{
			double sliderRange = getMaximum() - getMinimum();
			double imgRange = depthMax - depthMin;
		    return (int) Math.ceil((sliderRange * (depth - depthMin) / imgRange + getMinimum()));
		}

	}

	public class AlphaSlider extends JSlider
	{
		public AlphaSlider()
		{
			setMinimum(0);
			setMaximum(100);
		}

		public void applyAlphaToImage()
		{
			image.setOffLimbFootprintAlpha(getAlphaValue());
		}

		public double getAlphaValue()
		{
			return (double) (getValue() - getMinimum())
					/ (double) (getMaximum() - getMinimum());
		}
	}

	public class ShowBoundaryButton extends JCheckBox
	{
		public ShowBoundaryButton()
		{
			setText("Show Boundary");
		}

		public void showBoundary(boolean selected)
		{
			image.setOffLimbBoundaryVisibility(selected);
		}

	}

	public class SyncContrastSlidersButton extends JCheckBox
	{
		public SyncContrastSlidersButton()
		{
			setText("Sync Contrast with Image");
		}

		public void syncContrast(boolean selected)
		{
			image.setContrastSynced(selected);
		}

	}


}
