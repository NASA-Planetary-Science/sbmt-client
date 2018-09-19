package edu.jhuapl.sbmt.gui.image.ui.cubical;

import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubeGenerationPanel;

public class SpectralImageCubeGenerationPanel extends ImageCubeGenerationPanel
{
    private JLabel layerLabel;
    private JLabel layerValue;
    private JSlider layerSlider;
    private DefaultBoundedRangeModel monoBoundedRangeModel;
    private int nbands;

    public SpectralImageCubeGenerationPanel()
    {
        super();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        layerLabel = new JLabel("Layer:");
        panel.add(layerLabel);

        layerValue = new JLabel("0");
        panel.add(layerValue);

        layerSlider = new JSlider();
        panel.add(layerSlider);

        panel_1.add(panel);
    }

    public void setNBands(int nBands)
    {
        int midband = (nbands-1) / 2;
        String midbandString = Integer.toString(midband);
        layerValue.setText(midbandString);

        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
        layerSlider = new JSlider(monoBoundedRangeModel);
    }

    public JLabel getLayerValue()
    {
        return layerValue;
    }


    public JSlider getLayerSlider()
    {
        return layerSlider;
    }
}
