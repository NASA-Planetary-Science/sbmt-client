package edu.jhuapl.near.gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jidesoft.swing.RangeSlider;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.util.IntensityRange;


public class ContrastChanger extends JPanel implements ChangeListener
{
    private RangeSlider slider;

    private Image image;

    public ContrastChanger()
    {
        setBorder(BorderFactory.createTitledBorder("Contrast"));

        //this.setPreferredSize(new Dimension(300,300));
        slider = new RangeSlider(0, 255, 0, 255);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        slider.setPaintTrack(true);
        slider.addChangeListener(this);
        slider.setEnabled(false);
        add(slider);
    }

    public void setImage(Image image)
    {
        if (image != null)
        {
            this.image = image;
            IntensityRange range = image.getDisplayedRange();
            slider.setLowValue(range.min);
            slider.setHighValue(range.max);
            slider.setEnabled(true);
        }
        else
        {
            slider.setEnabled(false);
        }
    }

    public void stateChanged(ChangeEvent e)
    {
        int lowVal = slider.getLowValue();
        int highVal = slider.getHighValue();
        if (image != null)
            image.setDisplayedImageRange(new IntensityRange(lowVal, highVal));
    }
}
