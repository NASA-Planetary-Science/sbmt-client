package edu.jhuapl.sbmt.gui.image.model.images;

import java.util.Vector;

import edu.jhuapl.sbmt.gui.image.model.OfflimbModelChangedListener;
import edu.jhuapl.sbmt.model.rosetta.OsirisImage;

public class OfflimbControlsModel
{
    private OsirisImage image;
    private int currentSlice;
    private int currentAlpha;
    private int currentDepth;
    private int contrastLow;
    private int contrastHigh;
    private boolean showBoundary = true; // true by default
    Vector<OfflimbModelChangedListener> listeners;

    public OfflimbControlsModel(OsirisImage image, int currentSlice)
    {
        this.image = image;
        this.currentSlice = currentSlice;
        this.listeners = new Vector<OfflimbModelChangedListener>();
    }

    public void addModelChangedListener(OfflimbModelChangedListener listener)
    {
        listeners.add(listener);
    }

    public OsirisImage getImage()
    {
        return image;
    }

    public int getCurrentSlice()
    {
        return currentSlice;
    }

    public int getCurrentAlpha()
    {
        return currentAlpha;
    }

    public void setCurrentAlpha(int currentAlpha)
    {
        this.currentAlpha = currentAlpha;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentAlphaChanged(currentAlpha);
        }
    }

    public int getCurrentDepth()
    {
        return currentDepth;
    }

    public void setCurrentDepth(int currentDepth)
    {
        this.currentDepth = currentDepth;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentDepthChanged(currentDepth);
        }
    }

    public int getContrastLow()
    {
        return contrastLow;
    }

    public void setContrastLow(int contrastLow)
    {
        this.contrastLow = contrastLow;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentContrastLowChanged(contrastLow);
        }
    }

    public int getContrastHigh()
    {
        return contrastHigh;
    }


    public boolean getShowBoundary()
    {
        return showBoundary;
    }

    public void setContrastHigh(int contrastHigh)
    {
        this.contrastHigh = contrastHigh;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentContrastHighChanged(contrastHigh);
        }
    }

    public void setImage(OsirisImage image)
    {
        this.image = image;
    }

    public void setShowBoundary(boolean show)
    {
        this.showBoundary = show;
    }

    public void setCurrentSlice(int currentSlice)
    {
        this.currentSlice = currentSlice;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentSliceChanged(currentSlice);
        }
    }

}
