package edu.jhuapl.sbmt.gui.image.model.images;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchResultsListener;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;

import nom.tam.fits.FitsException;

public class ImageSearchModel
{
    private SmallBodyViewConfig smallBodyConfig;
    protected ModelManager modelManager;
    protected IdPair resultIntervalCurrentlyShown = null;
    protected List<List<String>> imageResults = new ArrayList<List<String>>();
    protected ImageCollection imageCollection;
    protected ImagingInstrument instrument;
    protected ImageSource imageSourceOfLastQuery = ImageSource.SPICE;
    private Date startDate = null;
    private Date endDate = null;
    public int currentSlice;
    public int currentBand;
    private Renderer renderer;
    private Vector<ImageSearchResultsListener> resultsListeners;
    protected int[] selectedImageIndices;

    public ImageSearchModel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            Renderer renderer,
            ImagingInstrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.renderer = renderer;
        this.resultsListeners = new Vector<ImageSearchResultsListener>();
        this.instrument = instrument;
        this.imageCollection = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        this.startDate = smallBodyConfig.imageSearchDefaultStartDate;
        this.endDate = smallBodyConfig.imageSearchDefaultEndDate;
    }


    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public ModelNames getImageCollectionModelName()
    {
        return ModelNames.IMAGES;
    }

    public ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES;
    }

    public void loadImage(ImageKey key, ImageCollection images) throws FitsException, IOException
    {
        images.addImage(key);
    }

    public void loadImages(String name)
    {

        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
        for (ImageKey key : keys)
        {
            try
            {
                ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
                if (!images.containsImage(key))
                {
                    loadImage(key, images);
                }
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }

        }
   }

    public void unloadImage(ImageKey key, ImageCollection images)
    {
        images.removeImage(key);
    }

    public void unloadImages(String name)
    {

        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
        for (ImageKey key : keys)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            unloadImage(key, images);
        }
   }

    public void setImageVisibility(String name, boolean visible)
    {
        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        for (ImageKey key : keys)
        {
            if (images.containsImage(key))
            {
                Image image = images.getImage(key);
                image.setVisible(visible);
            }
        }
    }

    public IdPair getResultIntervalCurrentlyShown()
    {
        return resultIntervalCurrentlyShown;
    }


    public void setResultIntervalCurrentlyShown(IdPair resultIntervalCurrentlyShown)
    {
        this.resultIntervalCurrentlyShown = resultIntervalCurrentlyShown;
    }


    public List<List<String>> getImageResults()
    {
        return imageResults;
    }


    public void setImageResults(List<List<String>> imageRawResults)
    {
        this.imageResults = imageRawResults;
        fireResultsChanged();
    }

    public ImageCollection getImageCollection()
    {
        return imageCollection;
    }


    public void setImageCollection(ImageCollection imageCollection)
    {
        this.imageCollection = imageCollection;
    }


    public ImagingInstrument getInstrument()
    {
        return instrument;
    }

    public void setInstrument(ImagingInstrument instrument)
    {
        this.instrument = instrument;
    }

    public ImageSource getImageSourceOfLastQuery()
    {
        return imageSourceOfLastQuery;
    }

    public void setImageSourceOfLastQuery(ImageSource imageSourceOfLastQuery)
    {
        this.imageSourceOfLastQuery = imageSourceOfLastQuery;
    }


    public Date getStartDate()
    {
        return startDate;
    }


    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }


    public Date getEndDate()
    {
        return endDate;
    }


    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }


    public void setModelManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;
    }

    public int getCurrentSlice() { return 0; }

    public String getCurrentBand() { return "0"; }

    public SmallBodyViewConfig getSmallBodyConfig()
    {
        return smallBodyConfig;
    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public void setCurrentSlice(int currentSlice)
    {
        this.currentSlice = currentSlice;
    }


    public void setCurrentBand(int currentBand)
    {
        this.currentBand = currentBand;
    }

    public List<ImageKey> createImageKeys(String boundaryName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        List<ImageKey> result = new ArrayList<ImageKey>();
        result.add(createImageKey(boundaryName, sourceOfLastQuery, instrument));
        return result;
    }

    public ImageKey createImageKey(String imagePathName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        int slice = this.getCurrentSlice();
        String band = this.getCurrentBand();
        ImageKey key = new ImageKey(imagePathName, sourceOfLastQuery, null, null, instrument, band, slice);
        return key;
    }

    public int getNumberOfFiltersActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchFilterNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    public int getNumberOfUserDefinedCheckBoxesActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    public List<List<String>> processResults(List<List<String>> input)
    {
        return input;
    }

    private void fireResultsChanged()
    {
        for (ImageSearchResultsListener listener : resultsListeners)
        {
            listener.resultsChanged(imageResults);
        }
    }

    public void addResultsChangedListener(ImageSearchResultsListener listener)
    {
        resultsListeners.add(listener);
    }

    public void removeResultsChangedListener(ImageSearchResultsListener listener)
    {
        resultsListeners.remove(listener);
    }

    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
    }

    public ImageKey[] getSelectedImageKeys()
    {
        int[] indices = selectedImageIndices;
        ImageKey[] selectedKeys = new ImageKey[indices.length];
        if (indices.length > 0)
        {
            int i=0;
            for (int index : indices)
            {
                String image = imageResults.get(index).get(0);
                String name = new File(image).getName();
                image = image.substring(0,image.length()-4);
                ImageKey selectedKey = createImageKey(image, imageSourceOfLastQuery, instrument);
                if (!selectedKey.band.equals("0"))
                    name = selectedKey.band + ":" + name;
                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }


    public void setSelectedImageIndex(int[] selectedImageIndex)
    {
        this.selectedImageIndices = selectedImageIndex;
    }

    public int[] getSelectedImageIndex()
    {
        return selectedImageIndices;
    }

}
