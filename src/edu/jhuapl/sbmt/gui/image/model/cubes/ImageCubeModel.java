package edu.jhuapl.sbmt.gui.image.model.cubes;

import java.io.IOException;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;

import nom.tam.fits.FitsException;

public class ImageCubeModel
{
    int nbands = 0;
    private ImageCubeCollection images;

    public ImageCubeModel()
    {

    }

    public ModelNames getImageCubeCollectionModelName()
    {
        return ModelNames.CUBE_IMAGES;
    }

    public int getNbands()
    {
        return nbands;
    }

    public void setNbands(int nbands)
    {
        this.nbands = nbands;
    }

    public void loadImage(ImageCubeKey key) throws FitsException, IOException, NoOverlapException
    {
        images.addImage(key);
    }

    public void unloadImage(ImageCubeKey key)
    {
        images.removeImage(key);
    }

    public void setImages(ImageCubeCollection images)
    {
        this.images = images;
    }

    public ImageCubeCollection getImages()
    {
        return images;
    }
}
