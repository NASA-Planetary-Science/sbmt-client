package edu.jhuapl.sbmt.gui.image.model.color;

import java.io.IOException;

import edu.jhuapl.saavtk.model.ModelNames;
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
    private ColorImageCollection images;

    public ColorImageModel()
    {

    }

    public ColorImageModel(ColorImageCollection collection)
    {
        this.images = collection;
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
        images.addImage(key);
    }

    public void unloadImage(ColorImageKey key)
    {
        images.removeImage(key);
    }

    public void setImages(ColorImageCollection images)
    {
        this.images = images;
    }
}
