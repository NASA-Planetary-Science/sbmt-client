package edu.jhuapl.near.gui;


import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.near.model.Model;


public class RadialOffsetChanger extends JPanel implements ChangeListener
{
    private JSlider slider;

    private Model model;

    private double offsetScale = 0.025;

    public RadialOffsetChanger(Model model, String title)
    {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        this.model = model;
        setBorder(BorderFactory.createTitledBorder(title));

        slider = new JSlider(0, 30, 15);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(5);
        slider.setPaintTrack(true);
        slider.addChangeListener(this);
        add(slider);
    }

    public void setOffsetScale(double scale)
    {
        this.offsetScale = scale;
    }
    public void stateChanged(ChangeEvent e)
    {
        if (slider.getValueIsAdjusting())
        {
            int val = slider.getValue();
            int max = slider.getMaximum();
            int min = slider.getMinimum();
            double offset = (val - (max-min)/2.0) * offsetScale;
            model.setRadialOffset(offset);
        }
    }
}
