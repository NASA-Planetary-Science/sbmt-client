package edu.jhuapl.sbmt.gui.image.ui.images;

import java.awt.Dimension;
import java.awt.HeadlessException;

import javax.swing.JFrame;

import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbControlsController.AlphaSlider;
import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbControlsController.ContrastSlider;
import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbControlsController.DepthSlider;

public class OfflimbControlsFrame extends JFrame
{
    private OfflimbImageControlPanel panel;

    public OfflimbControlsFrame(DepthSlider depthSlider, AlphaSlider alphaSlider, ContrastSlider contrastSlider) throws HeadlessException
    {
        panel = new OfflimbImageControlPanel(depthSlider, alphaSlider, contrastSlider);
        init();
    }

    private void init()
    {
        add(panel);
        setSize(400, 125);
        setMaximumSize(new Dimension(425, 125));
        setVisible(true);
        setTitle("Offlimb Properties");
    }

    public OfflimbImageControlPanel getPanel()
    {
        return panel;
    }
}
