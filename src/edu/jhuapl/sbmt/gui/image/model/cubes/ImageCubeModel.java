package edu.jhuapl.sbmt.gui.image.model.cubes;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.gui.image.model.ImageCubeResultsListener;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCube;
import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

import nom.tam.fits.FitsException;

public class ImageCubeModel
{
    int nbands = 0;
    private ImageCollection imageCollection;
    private ImageCubeCollection imageCubeCollection;
    private Vector<ImageCubeResultsListener> resultsListeners;
    private ImageSearchModel imageSearchModel;


    public ImageCubeModel()
    {
        resultsListeners = new Vector<ImageCubeResultsListener>();
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
        imageCubeCollection.addImage(key);
    }

    public void unloadImage(ImageCubeKey key)
    {
        imageCubeCollection.removeImage(key);
    }

    public void setColorImageCollection(ImageCubeCollection images)
    {
        this.imageCubeCollection = images;
    }

    public ImageCubeCollection getColorImageCollection()
    {
        return imageCubeCollection;
    }

    public ImageCollection getImageCollection()
    {
        return imageCollection;
    }

    public void setImageCollection(ImageCollection imageCollection)
    {
        this.imageCollection = imageCollection;
    }

    public void addResultsChangedListener(ImageCubeResultsListener listener)
    {
        resultsListeners.add(listener);
    }

    public void removeResultsChangedListener(ImageCubeResultsListener listener)
    {
        resultsListeners.remove(listener);
    }

    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
    }

    private void fireErrorMessage(String message)
    {
        for (ImageCubeResultsListener listener : resultsListeners)
        {
          listener.presentErrorMessage(message);
        }
    }

    private void fireInformationalMessage(String message)
    {
        for (ImageCubeResultsListener listener : resultsListeners)
        {
          listener.presentInformationalMessage(message);
        }
    }

    public ImageSearchModel getImageSearchModel()
    {
        return imageSearchModel;
    }

    public void setImageSearchModel(ImageSearchModel imageSearchModel)
    {
        this.imageSearchModel = imageSearchModel;
    }

    public void generateImageCube(ActionEvent e) //throws edu.jhuapl.sbmt.model.image.ImageCube.NoOverlapException, IOException, FitsException
    {
        ImageKey firstKey = null;
        boolean multipleFrustumVisible = false;

        List<ImageKey> selectedKeys = new ArrayList<>();
        for (ImageKey key : imageSearchModel.getSelectedImageKeys()) { selectedKeys.add(key); }
        for (ImageKey selectedKey : selectedKeys)
        {
            PerspectiveImage selectedImage = (PerspectiveImage)imageCollection.getImage(selectedKey);
            if(selectedImage == null)
            {
                // We are in here because the image is not mapped, display an error message and exit
                fireErrorMessage("All selected images must be mapped when generating an image cube.");
                return;
            }

            // "first key" is indicated by the first image with a visible frustum
            if (selectedImage.isFrustumShowing())
             {
                if(firstKey == null)
                {
                    firstKey = selectedKey;
                }
                else
                {
                    multipleFrustumVisible = true;
                }
            }
        }

        if(selectedKeys.size() == 0)
        {
            // We are in here because no images were selected by user
            fireErrorMessage("At least one image must be selected when generating an image cube.");
            return;
        }
        else if(firstKey == null)
        {
            // We are in here because no frustum was selected by user
            fireErrorMessage("At least one selected image must have its frustum showing when generating an image cube.");
            return;
        }
        else
        {
            PerspectiveImage firstImage = (PerspectiveImage)imageCollection.getImage(firstKey);
            ImageCubeKey imageCubeKey = new ImageCubeKey(selectedKeys, firstKey, firstImage.getLabelfileFullPath(), firstImage.getInfoFileFullPath(), firstImage.getSumfileFullPath());

            try
            {
                if (!imageCubeCollection.containsImage(imageCubeKey))
                {
                    imageCubeCollection.addImage(imageCubeKey);
                    for (ImageCubeResultsListener listener : resultsListeners)
                    {
                        listener.imageCubeAdded(imageCubeKey);
                    }

                    if(multipleFrustumVisible)
                    {
                        fireInformationalMessage("More than one selected image has a visible frustum, image cube was generated using the first such frustum in order of appearance in the image list.");
                    }
                }
                else
                {
                    fireInformationalMessage("Image cube consisting of same images already exists, no new image cube was generated.");
                }
            }
            catch (IOException e1)
            {
                fireErrorMessage("There was an error mapping the image.");
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                fireErrorMessage("There was an error mapping the image.");
                e1.printStackTrace();
            }
            catch (ImageCube.NoOverlapException e1)
            {
                fireErrorMessage("Cube Generation: The images you selected do not overlap.");
            }
        }
    }

    public void removeImageCube(ImageCubeKey imageCubeKey)
    {
        imageCubeCollection.removeImage(imageCubeKey);
    }
}
