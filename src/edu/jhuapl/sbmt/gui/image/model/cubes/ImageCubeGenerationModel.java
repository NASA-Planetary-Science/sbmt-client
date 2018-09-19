package edu.jhuapl.sbmt.gui.image.model.cubes;

import java.io.IOException;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;

import nom.tam.fits.FitsException;

public class ImageCubeGenerationModel
{
    int nbands = 0;
    private ImageCubeCollection images;

    public ImageCubeGenerationModel()
    {
        // TODO Auto-generated constructor stub
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

//    public void loadImages(String name)
//    {
//
//        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
//        for (ImageKey key : keys)
//        {
//            try
//            {
//                ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
//                if (!images.containsImage(key))
//                {
//                    loadImage(key, images);
//                }
//            }
//            catch (Exception e1) {
//                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
//                        "There was an error mapping the image.",
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//
//                e1.printStackTrace();
//            }
//
//        }
//   }

    public void unloadImage(ImageCubeKey key)
    {
        images.removeImage(key);
    }

    public void setImages(ImageCubeCollection images)
    {
        this.images = images;
    }

//    public void unloadImages(String name)
//    {
//
//        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
//        for (ImageKey key : keys)
//        {
//            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
//            unloadImage(key, images);
//        }
//   }

}
