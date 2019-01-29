package edu.jhuapl.sbmt.gui.image.model.images;

import java.util.Vector;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.sbmt.gui.image.model.OfflimbModelChangedListener;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class OfflimbControlsModel implements Controller.Model, MetadataManager
{
    private PerspectiveImage image;
    private int currentSlice;
    private int currentAlpha;
    private int currentDepth;
    private boolean showBoundary = true; // true by default
    Vector<OfflimbModelChangedListener> listeners;

    final Key<Integer> currentSliceKey = Key.of("currentSlice");
    final Key<Integer> currentAlphaKey = Key.of("currentAlpha");
    final Key<Integer> currentDepthKey = Key.of("currentDepth");

    public OfflimbControlsModel(PerspectiveImage image, int currentSlice)
    {
        this.image = image;
        this.currentSlice = currentSlice;
        this.listeners = new Vector<OfflimbModelChangedListener>();
    }

    public void addModelChangedListener(OfflimbModelChangedListener listener)
    {
        listeners.add(listener);
    }

    public PerspectiveImage getImage()
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


    public boolean getShowBoundary()
    {
        return showBoundary;
    }

    public void setImage(PerspectiveImage image)
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

    @Override
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        result.put(currentAlphaKey, currentAlpha);
        result.put(currentDepthKey, currentDepth);
        result.put(currentSliceKey, currentSlice);

        return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
        currentAlpha = source.get(currentAlphaKey);
        currentDepth = source.get(currentDepthKey);
        currentSlice = source.get(currentSliceKey);
    }

}
