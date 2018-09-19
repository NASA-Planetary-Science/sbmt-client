package edu.jhuapl.sbmt.gui.image.ui.cubes;

import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class OfflimbImageCubeGenerationPanel extends ImageCubeGenerationPanel
{

    private JLabel footprintDepthLabel;
    private JLabel footprintDepthValue;
    private JSlider footprintDepthSlider;

    private JLabel footprintTransparencyLabel;
    private JLabel footprintTransparencyValue;
    private JSlider footprintTransparencySlider;

    private JLabel imageContrastLabel;
    private JLabel imageContrastValue;
    private JSlider imageContrastSlider;
    private DefaultBoundedRangeModel monoBoundedRangeModel;
    private int nbands;

    public OfflimbImageCubeGenerationPanel()
    {
        super();

        JPanel depthPanel = new JPanel();
        depthPanel.setLayout(new BoxLayout(depthPanel, BoxLayout.X_AXIS));
        footprintDepthLabel = new JLabel("Off-limb footprint depth:");
        depthPanel.add(footprintDepthLabel);

        footprintDepthValue = new JLabel("0");
        depthPanel.add(footprintDepthValue);

        footprintDepthSlider = new JSlider();
        depthPanel.add(footprintDepthSlider);


        JPanel transparencyPanel = new JPanel();
        transparencyPanel.setLayout(new BoxLayout(transparencyPanel, BoxLayout.X_AXIS));
        footprintTransparencyLabel = new JLabel("Off-limb footprint transparency:");
        transparencyPanel.add(footprintTransparencyLabel);

        footprintTransparencyValue = new JLabel("0");
        transparencyPanel.add(footprintTransparencyValue);

        footprintTransparencySlider = new JSlider();
        transparencyPanel.add(footprintTransparencySlider);

        JPanel contrastPanel = new JPanel();
        contrastPanel.setLayout(new BoxLayout(contrastPanel, BoxLayout.X_AXIS));
        imageContrastLabel = new JLabel("Image Contrast:");
        contrastPanel.add(imageContrastLabel);

        imageContrastValue = new JLabel("0");
        contrastPanel.add(imageContrastValue);

        imageContrastSlider = new JSlider();
        contrastPanel.add(imageContrastSlider);

        panel_1.add(depthPanel);
        panel_1.add(transparencyPanel);
        panel_1.add(contrastPanel);
    }

    public void setNBands(int nBands)
    {
        int midband = (nbands-1) / 2;
        String midbandString = Integer.toString(midband);
        footprintDepthValue.setText(midbandString);

        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
        footprintDepthSlider = new JSlider(monoBoundedRangeModel);
    }

    public JLabel getFootprintDepthValue()
    {
        return footprintDepthValue;
    }


    public JSlider getFootprintDepthSlider()
    {
        return footprintDepthSlider;
    }
}
