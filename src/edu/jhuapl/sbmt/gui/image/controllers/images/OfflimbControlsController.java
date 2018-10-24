package edu.jhuapl.sbmt.gui.image.controllers.images;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jidesoft.swing.RangeSlider;

import edu.jhuapl.saavtk.util.IntensityRange;
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

    public OfflimbControlsController(PerspectiveImage image, int currentSlice)
    {
        this.image = image;
        controlsModel = new OfflimbControlsModel(image, currentSlice);


        depthSlider = new DepthSlider();
        alphaSlider = new AlphaSlider();
        contrastSlider = new ContrastSlider();
        showBoundaryBtn = new ShowBoundaryButton();
        showBoundaryBtn.setSelected(controlsModel.getShowBoundary());
        showBoundaryBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                showBoundaryBtn.showBoundary(showBoundaryBtn.isSelected());
                controlsModel.setShowBoundary(showBoundaryBtn.isSelected());
            }

        });
        controlsFrame = new OfflimbControlsFrame(depthSlider, alphaSlider, contrastSlider, showBoundaryBtn);
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
        });

        controlsFrame.getPanel().getFootprintDepthValue().setText("" + controlsFrame.getPanel().getFootprintDepthSlider().getValue());
        controlsFrame.getPanel().getFootprintTransparencyValue().setText("" + controlsFrame.getPanel().getFootprintTransparencySlider().getValue());
        controlsFrame.getPanel().getImageContrastValue().setText("(" + controlsFrame.getPanel().getImageContrastSlider().getLowValue() + "," + controlsFrame.getPanel().getImageContrastSlider().getHighValue() + ")");
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
                }
                else if (e.getSource() == controlsFrame.getPanel().getFootprintTransparencySlider() && !controlsFrame.getPanel().getFootprintTransparencySlider().getValueIsAdjusting())
                {
                    alphaSlider.applyAlphaToImage();
                    controlsModel.setCurrentAlpha(alphaSlider.getValue());
                }
                else if (e.getSource() == controlsFrame.getPanel().getImageContrastSlider() && !controlsFrame.getPanel().getImageContrastSlider().getValueIsAdjusting())
                {
                    contrastSlider.applyContrastToImage();
                    controlsModel.setContrastLow(contrastSlider.getLowValue());
                    controlsModel.setContrastHigh(contrastSlider.getHighValue());
                }
                else if (e.getSource() == controlsFrame.getPanel().getShowBoundaryButton())
                {
                    showBoundaryBtn.showBoundary(showBoundaryBtn.isSelected());
                    controlsModel.setShowBoundary(showBoundaryBtn.isSelected());
                }
                controlsModel.getImage().firePropertyChange();
            }
        };

        controlsFrame.getPanel().getFootprintDepthSlider().addChangeListener(changeListener);
        controlsFrame.getPanel().getImageContrastSlider().addChangeListener(changeListener);
        controlsFrame.getPanel().getFootprintTransparencySlider().addChangeListener(changeListener);


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
            image.loadOffLimbPlane(getDepthValue());
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

    public class ContrastSlider extends RangeSlider
    {
        public ContrastSlider()
        {
            setMinimum(0);
            setMaximum(255);
        }

        public void applyContrastToImage()
        {
            image.setDisplayedImageRange(
                    new IntensityRange(getLowValue(), getHighValue()));
        }
    }

    public class ShowBoundaryButton extends JRadioButton
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


}
