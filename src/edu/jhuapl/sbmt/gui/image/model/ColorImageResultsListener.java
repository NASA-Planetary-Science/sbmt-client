package edu.jhuapl.sbmt.gui.image.model;

import edu.jhuapl.sbmt.model.image.ColorImage.ColorImageKey;

public interface ColorImageResultsListener
{
    public void colorImageAdded(ColorImageKey colorImageKey);

    public void colorImageRemoved(ColorImageKey image);
}
