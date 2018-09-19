package edu.jhuapl.sbmt.gui.image.ui.cubical;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTable;

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

        layerLabel = new JLabel("Layer:");
        panel_1.add(layerLabel);

        layerValue = new JLabel("0");
        panel_1.add(layerValue);

        layerSlider = new JSlider();
        panel_1.add(layerSlider);
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
