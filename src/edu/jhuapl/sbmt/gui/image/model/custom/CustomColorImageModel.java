package edu.jhuapl.sbmt.gui.image.model.custom;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Vector;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.gui.image.model.ColorImageResultsListener;
import edu.jhuapl.sbmt.gui.image.model.color.ColorImageModel;
import edu.jhuapl.sbmt.model.image.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;

import nom.tam.fits.FitsException;

public class CustomColorImageModel extends ColorImageModel
{

    public CustomColorImageModel()
    {
        resultsListeners = new Vector<ColorImageResultsListener>();
    }

    public CustomColorImageModel(ColorImageCollection collection)
    {
        this.imageCollection = collection;
    }

    public ModelNames getImageCollectionModelName()
    {
        return ModelNames.CUSTOM_COLOR_IMAGES;
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

}
