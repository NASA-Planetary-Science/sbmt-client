package edu.jhuapl.sbmt.gui.image.ui.images;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbControlsController.AlphaSlider;
import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbControlsController.ContrastSlider;
import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbControlsController.DepthSlider;


public class OfflimbImageControlPanel extends JPanel
{

    private JLabel footprintDepthLabel;
    private JLabel footprintDepthValue;
    private DepthSlider footprintDepthSlider;

    private JLabel footprintTransparencyLabel;
    private JLabel footprintTransparencyValue;
    private AlphaSlider footprintTransparencySlider;

    private JLabel imageContrastLabel;
    private JLabel imageContrastValue;
    private ContrastSlider imageContrastSlider;

    public OfflimbImageControlPanel(DepthSlider depthSlider, AlphaSlider alphaSlider, ContrastSlider contrastSlider)
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel depthPanel = new JPanel();
        depthPanel.setLayout(new BoxLayout(depthPanel, BoxLayout.X_AXIS));
        footprintDepthLabel = new JLabel("Off-limb footprint depth:");
        depthPanel.add(footprintDepthLabel);

        footprintDepthValue = new JLabel("0");
        depthPanel.add(footprintDepthValue);

        footprintDepthSlider = depthSlider;
        depthPanel.add(footprintDepthSlider);

        JPanel transparencyPanel = new JPanel();
        transparencyPanel
                .setLayout(new BoxLayout(transparencyPanel, BoxLayout.X_AXIS));
        footprintTransparencyLabel = new JLabel(
                "Off-limb footprint transparency:");
        transparencyPanel.add(footprintTransparencyLabel);

        footprintTransparencyValue = new JLabel("0");
        transparencyPanel.add(footprintTransparencyValue);

        footprintTransparencySlider = alphaSlider;
        transparencyPanel.add(footprintTransparencySlider);

        JPanel contrastPanel = new JPanel();
        contrastPanel.setLayout(new BoxLayout(contrastPanel, BoxLayout.X_AXIS));
        imageContrastLabel = new JLabel("Image Contrast:");
        contrastPanel.add(imageContrastLabel);

        imageContrastValue = new JLabel("0");
        contrastPanel.add(imageContrastValue);

        imageContrastSlider = contrastSlider;
        contrastPanel.add(imageContrastSlider);

        add(depthPanel);
        add(transparencyPanel);
        add(contrastPanel);
    }

    public JLabel getFootprintDepthValue()
    {
        return footprintDepthValue;
    }

    public DepthSlider getFootprintDepthSlider()
    {
        return footprintDepthSlider;
    }

    public JLabel getFootprintTransparencyValue()
    {
        return footprintTransparencyValue;
    }

    public AlphaSlider getFootprintTransparencySlider()
    {
        return footprintTransparencySlider;
    }

    public JLabel getImageContrastValue()
    {
        return imageContrastValue;
    }

    public ContrastSlider getImageContrastSlider()
    {
        return imageContrastSlider;
    }

    public void setFootprintDepthSlider(DepthSlider footprintDepthSlider)
    {
        this.footprintDepthSlider = footprintDepthSlider;
    }

    public void setFootprintTransparencySlider(
            AlphaSlider footprintTransparencySlider)
    {
        this.footprintTransparencySlider = footprintTransparencySlider;
    }

    public void setImageContrastSlider(ContrastSlider imageContrastSlider)
    {
        this.imageContrastSlider = imageContrastSlider;
    }


}
