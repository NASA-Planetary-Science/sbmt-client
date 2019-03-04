package edu.jhuapl.sbmt.gui.image.controllers.images;

import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.sbmt.gui.image.model.OfflimbModelChangedListener;
import edu.jhuapl.sbmt.gui.image.model.images.OfflimbControlsModel;
import edu.jhuapl.sbmt.gui.image.ui.images.OfflimbControlsFrame;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class OfflimbControlsController
{
    OfflimbControlsFrame controlsFrame;
    OfflimbControlsModel controlsModel;
    PerspectiveImage image;
    DepthSlider depthSlider;
    AlphaSlider alphaSlider;
    ContrastSlider contrastSlider;
    ShowBoundaryButton showBoundaryBtn;
    SyncContrastSlidersButton syncButton;

    public OfflimbControlsController(PerspectiveImage image, int currentSlice)
    {
        this.image = image;
        controlsModel = new OfflimbControlsModel(image, currentSlice);


        depthSlider = new DepthSlider();
        alphaSlider = new AlphaSlider();
        contrastSlider = new ContrastSlider(image, true);

        showBoundaryBtn = new ShowBoundaryButton();
        showBoundaryBtn.setSelected(controlsModel.getShowBoundary());

        syncButton = new SyncContrastSlidersButton();
        syncButton.setSelected(controlsModel.getSyncContrast());

        controlsFrame = new OfflimbControlsFrame(depthSlider, alphaSlider, contrastSlider, showBoundaryBtn, syncButton);

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
                controlsFrame.getPanel().getFootprintDepthValue().setText("" + sliderValue);
            }

            @Override
            public void currentContrastLowChanged(int contrastMin)
            {
                controlsFrame.getPanel().getImageContrastValue().setText("(" + contrastSlider.getLowValue() + "," + contrastSlider.getHighValue() + ")");
            }

            @Override
            public void currentContrastHighChanged(int contrastMax)
            {
                controlsFrame.getPanel().getImageContrastValue().setText("(" + contrastSlider.getLowValue() + "," + contrastSlider.getHighValue() + ")");
            }

            @Override
            public void currentAlphaChanged(int alpha)
            {
                controlsFrame.getPanel().getFootprintTransparencyValue().setText("" + alphaSlider.getValue());
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
                if (e.getSource() == controlsFrame.getPanel().getFootprintDepthSlider() && !controlsFrame.getPanel().getFootprintDepthSlider().getValueIsAdjusting())
                {
                    depthSlider.applyDepthToImage(controlsModel.getCurrentSlice());
                    controlsModel.setCurrentDepth(depthSlider.getValue());
                    controlsModel.getImage().firePropertyChange();
                }
                else if (e.getSource() == controlsFrame.getPanel().getFootprintTransparencySlider() && !controlsFrame.getPanel().getFootprintTransparencySlider().getValueIsAdjusting())
                {
                    alphaSlider.applyAlphaToImage();
                    controlsModel.setCurrentAlpha(alphaSlider.getValue());
                    controlsModel.getImage().firePropertyChange();
                }
                else if (e.getSource() == controlsFrame.getPanel().getImageContrastSlider() && !controlsFrame.getPanel().getImageContrastSlider().getValueIsAdjusting())
                {
                    contrastSlider.sliderStateChanged(e);
                    controlsModel.setContrastLow(contrastSlider.getLowValue());
                    controlsModel.setContrastHigh(contrastSlider.getHighValue());
                }
                else if (e.getSource() == controlsFrame.getPanel().getShowBoundaryButton())
                {
                    showBoundaryBtn.showBoundary(showBoundaryBtn.isSelected());
                    controlsModel.setShowBoundary(showBoundaryBtn.isSelected());
                }
                else if (e.getSource() == controlsFrame.getPanel().getSyncContrastButton())
                {
                    syncButton.syncContrast(syncButton.isSelected());
                    controlsModel.setSyncContrast(syncButton.isSelected());
                }
            }
        };

        controlsFrame.getPanel().getFootprintDepthSlider().addChangeListener(changeListener);
        controlsFrame.getPanel().getFootprintTransparencySlider().addChangeListener(changeListener);
        controlsFrame.getPanel().getImageContrastSlider().addChangeListener(changeListener);
        controlsFrame.getPanel().getShowBoundaryButton().addChangeListener(changeListener);
        controlsFrame.getPanel().getSyncContrastButton().addChangeListener(changeListener);


    }

    public OfflimbControlsFrame getControlsFrame()
    {
        return controlsFrame;
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
            setMinimum(0);
            setMaximum(100);
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
            double depthNorm = (double) (getValue() - getMinimum())
                    / (double) (getMaximum() - getMinimum());
            return depthMin + depthNorm * (depthMax - depthMin);
        }

        public int convertDepthToSliderValue(double depth)
        {
            double depthNorm = (image.getOffLimbPlaneDepth() - depthMin)
                    / (depthMax - depthMin);
            return (int) ((double) (getMaximum() - getMinimum()) * depthNorm);
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
