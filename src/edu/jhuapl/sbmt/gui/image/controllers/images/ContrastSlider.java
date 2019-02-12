package edu.jhuapl.sbmt.gui.image.controllers.images;

import com.jidesoft.swing.RangeSlider;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class ContrastSlider extends RangeSlider
{

	Image image;

    public ContrastSlider(Image image)
    {
    	this.image = image;
        setMinimum(0);
        setMaximum(255);
        int lowValue = 0;
        int hiValue = 255;
        // get existing contrast and set slider appropriately
        if (image!=null && image instanceof PerspectiveImage)
        {
           lowValue = ((PerspectiveImage)image).getDisplayedRange().min;
           hiValue  = ((PerspectiveImage)image).getDisplayedRange().max;
        }
        this.setHighValue(hiValue);
        this.setLowValue(lowValue);
        setPaintTicks(true);
        setMajorTickSpacing(10);

    }

    public void applyContrastToImage()
    {
    	if (image != null)
	        image.setDisplayedImageRange(
	                new IntensityRange(getLowValue(), getHighValue()));
    }

    public void sliderStateChanged(javax.swing.event.ChangeEvent evt)
    {
        if (getValueIsAdjusting())
            return;

        applyContrastToImage();
    }

}