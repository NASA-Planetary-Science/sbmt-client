package edu.jhuapl.sbmt.gui.image.model.color;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Vector;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.gui.image.model.ColorImageResultsListener;
import edu.jhuapl.sbmt.model.image.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;

import nom.tam.fits.FitsException;

public class ColorImageModel
{
    private ImageKey selectedRedKey;
    private ImageKey selectedGreenKey;
    private ImageKey selectedBlueKey;
    private ColorImageCollection imageCollection;
    private Vector<ColorImageResultsListener> resultsListeners;

    public ColorImageModel()
    {
        resultsListeners = new Vector<ColorImageResultsListener>();
    }

    public ColorImageModel(ColorImageCollection collection)
    {
        this.imageCollection = collection;
    }

    public ModelNames getColorImageCollectionModelName()
    {
        return ModelNames.COLOR_IMAGES;
    }

    public ImageKey getSelectedRedKey()
    {
        return selectedRedKey;
    }

    public ImageKey getSelectedGreenKey()
    {
        return selectedGreenKey;
    }

    public ImageKey getSelectedBlueKey()
    {
        return selectedBlueKey;
    }

    public void setSelectedRedKey(ImageKey selectedRedKey)
    {
        this.selectedRedKey = selectedRedKey;
    }

    public void setSelectedGreenKey(ImageKey selectedGreenKey)
    {
        this.selectedGreenKey = selectedGreenKey;
    }

    public void setSelectedBlueKey(ImageKey selectedBlueKey)
    {
        this.selectedBlueKey = selectedBlueKey;
    }

    public void loadImage(ColorImageKey key) throws FitsException, IOException, NoOverlapException
    {
        imageCollection.addImage(key);
    }

    public void unloadImage(ColorImageKey key)
    {
        imageCollection.removeImage(key);
    }

    public void setImages(ColorImageCollection images)
    {
        this.imageCollection = images;
    }

//    private void fireResultsChanged()
//    {
//        for (ColorImageResultsListener listener : resultsListeners)
//        {
//            listener.resultsChanged(imageResults);
//        }
//    }

    public void addResultsChangedListener(ColorImageResultsListener listener)
    {
        resultsListeners.add(listener);
    }

    public void removeResultsChangedListener(ColorImageResultsListener listener)
    {
        resultsListeners.remove(listener);
    }

    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
    }

    public void generateColorImage(ActionEvent e) throws IOException, FitsException, NoOverlapException
    {
        ImageKey selectedRedKey = getSelectedRedKey();
        ImageKey selectedGreenKey = getSelectedGreenKey();
        ImageKey selectedBlueKey = getSelectedBlueKey();


        if (selectedRedKey != null && selectedGreenKey != null && selectedBlueKey != null)
        {
            ColorImageKey colorKey = new ColorImageKey(selectedRedKey, selectedGreenKey, selectedBlueKey);
            imageCollection.addImage(colorKey);
            for (ColorImageResultsListener listener : resultsListeners)
            {
                listener.colorImageAdded(colorKey);
            }

        }
    }


    public void removeColorImage(ColorImageKey colorKey)
    {
        imageCollection.removeImage(colorKey);
    }
}
