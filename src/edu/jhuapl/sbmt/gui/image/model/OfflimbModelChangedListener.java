package edu.jhuapl.sbmt.gui.image.model;

public interface OfflimbModelChangedListener
{
    public void currentSliceChanged(int slice);
    public void currentDepthChanged(int depth);
    public void currentAlphaChanged(int alpha);
}
