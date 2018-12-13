package edu.jhuapl.sbmt.gui.image.model.custom;


import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import com.google.common.io.Files;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;
import vtk.vtkPNGWriter;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.model.CustomImageResultsListener;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ImageInfo;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.image.CustomPerspectiveImage;
import edu.jhuapl.sbmt.model.image.CylindricalImage;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.util.VtkENVIReader;

import nom.tam.fits.FitsException;

public class CustomImagesModel extends ImageSearchModel
{
    private List<ImageInfo> customImages;
    private Vector<CustomImageResultsListener> customImageListeners;
    private boolean initialized = false;
//    private int numImagesInCollection = -1;
    final Key<Vector<Metadata>> customImagesKey = Key.of("customImages");
    private PerspectiveImageBoundaryCollection boundaries;

    public CustomImagesModel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            Renderer renderer,
            ImagingInstrument instrument)
    {
        super(smallBodyConfig, modelManager, renderer, instrument);
        this.customImages = new Vector<ImageInfo>();
        this.customImageListeners = new Vector<CustomImageResultsListener>();

        this.imageCollection = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        this.boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
    }

    public CustomImagesModel(ImageSearchModel model)
    {
        this(model.getSmallBodyConfig(), model.getModelManager(), model.getRenderer(), model.getInstrument());
    }

    public ModelNames getImageCollectionModelName()
    {
        return ModelNames.CUSTOM_IMAGES;
    }

    public ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_CUSTOM_IMAGE_BOUNDARIES;
    }

    public List<ImageInfo> getCustomImages()
    {
        return customImages;
    }

    public void setCustomImages(List<ImageInfo> customImages)
    {
        this.customImages = customImages;
    }

    public void addResultsChangedListener(CustomImageResultsListener listener)
    {
        customImageListeners.add(listener);
    }

    public void removeResultsChangedListener(CustomImageResultsListener listener)
    {
        customImageListeners.remove(listener);
    }

    private void fireResultsChanged()
    {
        for (CustomImageResultsListener listener : customImageListeners)
        {
            listener.resultsChanged(customImages);
        }
    }

    public void loadImage(ImageKey key, ImageCollection images) throws FitsException, IOException
    {
        images.addImage(key);
    }

    public void loadImages(String name, ImageInfo info)
    {

        ImageSource source = info.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
        FileType fileType = info.sumfilename != null && !info.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
        for (ImageKey key : keys)
        {
            ImageKey revisedKey = new ImageKey(getCustomDataFolder() + File.separator + info.imagefilename, source, fileType, info.imageType, key.instrument, key.band, key.slice);
            try
            {
                if (!imageCollection.containsImage(revisedKey))
                {
                    loadImage(revisedKey, imageCollection);
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
            unloadImage(key, imageCollection);
        }
   }

    public void removeAllButtonActionPerformed(ActionEvent evt)
    {
        boundaries.removeAllBoundaries();
        setResultIntervalCurrentlyShown(null);
    }

    public void removeAllImagesButtonActionPerformed(ActionEvent evt)
    {
        imageCollection.removeImages(ImageSource.GASKELL);
        imageCollection.removeImages(ImageSource.GASKELL_UPDATED);
        imageCollection.removeImages(ImageSource.SPICE);
        imageCollection.removeImages(ImageSource.CORRECTED_SPICE);
        imageCollection.removeImages(ImageSource.CORRECTED);
        imageCollection.removeImages(ImageSource.LOCAL_CYLINDRICAL);
        imageCollection.removeImages(ImageSource.LOCAL_PERSPECTIVE);
    }

    public void saveImage(int index, ImageInfo oldImageInfo, ImageInfo newImageInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        // If newImageInfo.imagefilename is null, that means we are in edit mode
        // and should continue to use the existing image
        if (newImageInfo.imagefilename == null)
        {
            newImageInfo.imagefilename = oldImageInfo.imagefilename;
        }
        else
        {
            // Check if this image is any of the supported formats
            if(VtkENVIReader.isENVIFilename(newImageInfo.imagefilename)){
                // We were given an ENVI file (binary or header)
                // Can assume at this point that both binary + header files exist in the same directory
                String extension = FilenameUtils.getExtension(newImageInfo.imagefilename);
                // Get filenames of the binary and header files
                String enviBinaryFilename = VtkENVIReader.getBinaryFilename(newImageInfo.imagefilename);
                String enviHeaderFilename = VtkENVIReader.getHeaderFilename(newImageInfo.imagefilename);
                // Rename newImageInfo as that of the binary file
                newImageInfo.imagefilename = "image-" + uuid + "." + extension;

                // Copy over the binary file
                Files.copy(new File(enviBinaryFilename + "." + extension),
                        new File(getCustomDataFolder() + File.separator
                                + newImageInfo.imagefilename));

                // Copy over the header file
                Files.copy(new File(enviHeaderFilename),
                        new File(getCustomDataFolder() + File.separator
                                + VtkENVIReader.getHeaderFilename(newImageInfo.imagefilename)));
            }
            else if(newImageInfo.imagefilename.endsWith(".fit") || newImageInfo.imagefilename.endsWith(".fits") ||
                    newImageInfo.imagefilename.endsWith(".FIT") || newImageInfo.imagefilename.endsWith(".FITS"))
            {
                // Copy FIT file to cache
                String newFilename = "image-" + uuid + ".fit";
                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                FileUtil.copyFile(newImageInfo.imagefilename,  newFilepath);
                // Change newImageInfo.imagefilename to the new location of the file
                newImageInfo.imagefilename = newFilename;
            }
            else
            {

                // Convert native VTK supported image to PNG and save to cache
                vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
                vtkImageReader2 imageReader = imageFactory.CreateImageReader2(newImageInfo.imagefilename);
                if (imageReader == null)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                        "The format of the specified file is not supported.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                imageReader.SetFileName(newImageInfo.imagefilename);
                imageReader.Update();

                vtkAlgorithmOutput imageReaderOutput = imageReader.GetOutputPort();
                vtkPNGWriter imageWriter = new vtkPNGWriter();
                imageWriter.SetInputConnection(imageReaderOutput);
                // We save out the image using a new name that makes use of a UUID
                newImageInfo.imagefilename = "image-" + uuid + ".png";
                imageWriter.SetFileName(getCustomDataFolder() + File.separator + newImageInfo.imagefilename);
                //imageWriter.SetFileTypeToBinary();
                imageWriter.Write();
            }
        }

        // Operations specific for perspective projection type
        if (newImageInfo.projectionType == ProjectionType.PERSPECTIVE)
        {
            // If newImageInfo.sumfilename and infofilename are both null, that means we are in edit mode
            // and should continue to use the existing sumfile
            if (newImageInfo.sumfilename == null && newImageInfo.infofilename == null)
            {
                newImageInfo.sumfilename = oldImageInfo.sumfilename;
                newImageInfo.infofilename = oldImageInfo.infofilename;
            }
            else
            {
                if (newImageInfo.sumfilename != null)
                {
                    // We save out the sumfile using a new name that makes use of a UUID
                    String newFilename = "sumfile-" + uuid + ".SUM";
                    String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                    FileUtil.copyFile(newImageInfo.sumfilename, newFilepath);
                    // Change newImageInfo.sumfilename to the new location of the file
                    newImageInfo.sumfilename = newFilename;
                }
                else if (newImageInfo.infofilename != null)
                {
                    // We save out the infofile using a new name that makes use of a UUID
                    String newFilename = "infofile-" + uuid + ".INFO";
                    String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                    FileUtil.copyFile(newImageInfo.infofilename, newFilepath);
                    // Change newImageInfo.infofilename to the new location of the file
                    newImageInfo.infofilename = newFilename;
                }
            }
        }
        if (index >= customImages.size())
        {
            customImages.add(newImageInfo);
        }
        else
        {
            customImages.set(index, newImageInfo);
        }

        updateConfigFile();
        fireResultsChanged();
        try
        {
            remapImageToRenderer(index);
        }
        catch (FitsException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void editButtonActionPerformed(ActionEvent evt)
    {
      int selectedItem = getSelectedImageIndex()[0];
      if (selectedItem >= 0)
      {
          ImageInfo oldImageInfo = customImages.get(selectedItem);

          CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, true, getInstrument());
          dialog.setImageInfo(oldImageInfo, getModelManager().getPolyhedralModel().isEllipsoid());
          dialog.setLocationRelativeTo(null);
          dialog.setVisible(true);

          // If user clicks okay replace item in list
          if (dialog.getOkayPressed())
          {
              ImageInfo newImageInfo = dialog.getImageInfo();
              try
              {
                  saveImage(selectedItem, oldImageInfo, newImageInfo);
                  remapImageToRenderer(selectedItem);
              }
              catch (IOException e)
              {
                  e.printStackTrace();
              }
              catch (FitsException e)
              {
                  e.printStackTrace();
              }
          }
      }
  }

    @Override
    public ImageKey createImageKey(String imagePathName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        for (ImageInfo info : customImages)
        {
            if (info.name.contains(imagePathName))
            {
                return getKeyForImageInfo(info);
            }
        }
        return super.createImageKey(imagePathName, sourceOfLastQuery, instrument);
    }

    private ImageKey getKeyForImageInfo(ImageInfo imageInfo)
    {
        String name = getCustomDataFolder() + File.separator + imageInfo.imagefilename;
        ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
        FileType fileType = imageInfo.sumfilename != null && !imageInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
        ImageType imageType = imageInfo.imageType;
        ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(imageInfo.rotation, imageInfo.flip) : null;
        ImageKey imageKey = new ImageKey(name, source, fileType, imageType, instrument, null, 0);
        return imageKey;
    }

    /**
     * This function unmaps the image from the renderer and maps it again,
     * if it is currently shown.
     * @throws IOException
     * @throws FitsException
     */
    public void remapImageToRenderer(int index) throws FitsException, IOException
    {
        ImageInfo imageInfo = customImages.get(index);
        // Remove the image from the renderer
        String name = getCustomDataFolder() + File.separator + imageInfo.imagefilename;
        ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
        FileType fileType = imageInfo.sumfilename != null && !imageInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
        ImageType imageType = imageInfo.imageType;
        ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(imageInfo.rotation, imageInfo.flip) : null;
        ImageKey imageKey = new ImageKey(name, source, fileType, imageType, instrument, null, 0);

//        ImageCollection imageCollection = (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);

        if (imageCollection.containsImage(imageKey))
        {
            Image image = imageCollection.getImage(imageKey);
            boolean visible = image.isVisible();
            if (visible)
                image.setVisible(false);
            imageCollection.removeImage(imageKey);
            imageCollection.addImage(imageKey);
            if (visible)
                image.setVisible(true);
        }
    }

    public ImageKey getImageKeyForIndex(int index)
    {
        ImageInfo imageInfo = customImages.get(index);
        // Remove the image from the renderer
        String name = getCustomDataFolder() + File.separator + imageInfo.imagefilename;
        ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
        FileType fileType = imageInfo.sumfilename != null && !imageInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
        ImageType imageType = imageInfo.imageType;
        ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(imageInfo.rotation, imageInfo.flip) : null;
        ImageKey imageKey = new ImageKey(name, source, fileType, imageType, instrument, null, 0);
        return imageKey;
    }

    public void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getConfigFilename());

        String imageNames = "";
        String imageFilenames = "";
        String projectionTypes = "";
        String imageTypes = "";
        String imageRotations = "";
        String imageFlips = "";
        String lllats = "";
        String lllons = "";
        String urlats = "";
        String urlons = "";
        String sumfilenames = "";
        String infofilenames = "";

        for (int i=0; i<customImages.size(); ++i)
        {
            ImageInfo imageInfo = customImages.get(i);

            imageFilenames += imageInfo.imagefilename;
            imageNames += imageInfo.name;
            projectionTypes += imageInfo.projectionType;
            imageTypes += imageInfo.imageType;
            imageRotations += Math.floor(imageInfo.rotation / 90.0) * 90.0;
            imageFlips += imageInfo.flip;
            lllats += String.valueOf(imageInfo.lllat);
            lllons += String.valueOf(imageInfo.lllon);
            urlats += String.valueOf(imageInfo.urlat);
            urlons += String.valueOf(imageInfo.urlon);
            sumfilenames += imageInfo.sumfilename;
            infofilenames += imageInfo.infofilename;

            if (i < customImages.size()-1)
            {
                imageNames += CustomShapeModel.LIST_SEPARATOR;
                imageFilenames += CustomShapeModel.LIST_SEPARATOR;
                projectionTypes += CustomShapeModel.LIST_SEPARATOR;
                imageTypes += CustomShapeModel.LIST_SEPARATOR;
                imageRotations += CustomShapeModel.LIST_SEPARATOR;
                imageFlips += CustomShapeModel.LIST_SEPARATOR;
                lllats += CustomShapeModel.LIST_SEPARATOR;
                lllons += CustomShapeModel.LIST_SEPARATOR;
                urlats += CustomShapeModel.LIST_SEPARATOR;
                urlons += CustomShapeModel.LIST_SEPARATOR;
                sumfilenames += CustomShapeModel.LIST_SEPARATOR;
                infofilenames += CustomShapeModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(Image.IMAGE_NAMES, imageNames);
        newMap.put(Image.IMAGE_FILENAMES, imageFilenames);
        newMap.put(Image.PROJECTION_TYPES, projectionTypes);
        newMap.put(Image.IMAGE_TYPES, imageTypes);
        newMap.put(Image.IMAGE_ROTATIONS, imageRotations);
        newMap.put(Image.IMAGE_FLIPS, imageFlips);
        newMap.put(CylindricalImage.LOWER_LEFT_LATITUDES, lllats);
        newMap.put(CylindricalImage.LOWER_LEFT_LONGITUDES, lllons);
        newMap.put(CylindricalImage.UPPER_RIGHT_LATITUDES, urlats);
        newMap.put(CylindricalImage.UPPER_RIGHT_LONGITUDES, urlons);
        newMap.put(CustomPerspectiveImage.SUMFILENAMES, sumfilenames);
        newMap.put(CustomPerspectiveImage.INFOFILENAMES, infofilenames);

        configMap.put(newMap);
    }

    public void initializeImageList() throws IOException
    {
        if (initialized)
            return;

        MapUtil configMap = new MapUtil(getConfigFilename());

        if (configMap.containsKey(CylindricalImage.LOWER_LEFT_LATITUDES) || configMap.containsKey(Image.PROJECTION_TYPES))
        {
            boolean needToUpgradeConfigFile = false;
            String[] imageNames = configMap.getAsArray(Image.IMAGE_NAMES);
            String[] imageFilenames = configMap.getAsArray(Image.IMAGE_FILENAMES);
            String[] projectionTypes = configMap.getAsArray(Image.PROJECTION_TYPES);
            String[] imageTypes = configMap.getAsArray(Image.IMAGE_TYPES);
            String[] imageRotations = configMap.getAsArray(Image.IMAGE_ROTATIONS);
            String[] imageFlips = configMap.getAsArray(Image.IMAGE_FLIPS);
            if (imageFilenames == null)
            {
                // for backwards compatibility
                imageFilenames = configMap.getAsArray(Image.IMAGE_MAP_PATHS);
                imageNames = new String[imageFilenames.length];
                projectionTypes = new String[imageFilenames.length];
                imageTypes = new String[imageFilenames.length];
                imageRotations = new String[imageFilenames.length];
                imageFlips = new String[imageFilenames.length];
                for (int i=0; i<imageFilenames.length; ++i)
                {
                    imageNames[i] = new File(imageFilenames[i]).getName();
                    imageFilenames[i] = "image" + i + ".png";
                    projectionTypes[i] = ProjectionType.CYLINDRICAL.toString();
                    imageTypes[i] = ImageType.GENERIC_IMAGE.toString();
                    imageRotations[i] = Double.toString(0.0);
                    imageFlips[i] = "None";
                }

                // Mark that we need to upgrade config file to latest version
                // which we'll do at end of function.
                needToUpgradeConfigFile = true;
            }
            double[] lllats = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LATITUDES);
            double[] lllons = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LONGITUDES);
            double[] urlats = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LATITUDES);
            double[] urlons = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LONGITUDES);
            String[] sumfileNames = configMap.getAsArray(CustomPerspectiveImage.SUMFILENAMES);
            String[] infofileNames = configMap.getAsArray(CustomPerspectiveImage.INFOFILENAMES);

            int numImages = lllats != null ? lllats.length : (projectionTypes != null ? projectionTypes.length : 0);
            for (int i=0; i<numImages; ++i)
            {
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.name = imageNames[i];
                imageInfo.imagefilename = imageFilenames[i];
                imageInfo.projectionType = ProjectionType.valueOf(projectionTypes[i]);
                imageInfo.imageType = imageTypes == null ? ImageType.GENERIC_IMAGE : ImageType.valueOf(imageTypes[i]);
                imageInfo.rotation = imageRotations == null ? 0.0 : Double.valueOf(imageRotations[i]);
                imageInfo.flip = imageFlips == null ? "None" : imageFlips[i];

                if (projectionTypes == null || ProjectionType.CYLINDRICAL.toString().equals(projectionTypes[i]))
                {
                    imageInfo.lllat = lllats[i];
                    imageInfo.lllon = lllons[i];
                    imageInfo.urlat = urlats[i];
                    imageInfo.urlon = urlons[i];
                }
                else if (ProjectionType.PERSPECTIVE.toString().equals(projectionTypes[i]))
                {
                    imageInfo.sumfilename = sumfileNames[i];
                    imageInfo.infofilename = infofileNames[i];
                }

                customImages.add(imageInfo);
            }

            if (needToUpgradeConfigFile)
                updateConfigFile();
        }

        initialized = true;
        fireResultsChanged();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof ImageCollection)// || model instanceof PerspectiveImageBoundaryCollection)
            {
                // Get the actual filename of the selected image
                ImageKey key = ((ImageCollection)model).getImage((vtkActor)e.getPickedProp()).getKey();
                String name = new File(key.name).getName();

                int idx = -1;
                int size = customImages.size();
                for (int i=0; i<size; ++i)
                {
                    // We want to compare the actual image filename here, not the displayed name which may not be unique
                    ImageInfo imageInfo = customImages.get(i);
                    String imageFilename = imageInfo.imagefilename;
                    if (name.equals(imageFilename))
                    {
                        idx = i;
                        break;
                    }
                }

//                if (idx >= 0)
//                {
//                    imageList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = imageList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        imageList.scrollRectToVisible(cellBounds);
//                }
            }
        }
        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
//            // If an image was added/removed, then
//            ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
//            int currImagesInCollection = images.getImages().size();
//
//            if(currImagesInCollection != numImagesInCollection)
//            {
//                // Update count of number of images in collection and update slider
//                numImagesInCollection = currImagesInCollection;
////                valueChanged(null);
//            }
        }
    }

    public void setImageVisibility(ImageKey key, boolean visible)
    {
//        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
//        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
//        for (ImageKey key : keys)
//        {
            if (imageCollection.containsImage(key))
            {
                Image image = imageCollection.getImage(key);
                image.setVisible(visible);
            }
//        }
    }

    @Override
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
//                ImageKey selectedKey = createImageKey(image, imageSourceOfLastQuery, instrument);
                ImageKey selectedKey = getImageKeyForIndex(index);
//                if (!selectedKey.band.equals("0"))
//                    name = selectedKey.band + ":" + name;
                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }

    private String getConfigFilename()
    {
        return getModelManager().getPolyhedralModel().getConfigFilename();
    }

    public String getCustomDataFolder()
    {
        return getModelManager().getPolyhedralModel().getCustomDataFolder();
    }

    @Override
    public Metadata store()
    {
        SettableMetadata data = (SettableMetadata)super.store();
        //store the ImageInfo objects that make up this custom model
        Vector<Metadata> images = new Vector<Metadata>();
//        ImmutableSortedSet.Builder<Metadata> images = ImmutableSortedSet.naturalOrder();
        for (ImageInfo info : customImages)
        {
            images.add(info.store());
        }
        data.put(customImagesKey, images);
        return data;
    }

    @Override
    public void retrieve(Metadata source)
    {
        super.retrieve(source);
        //get the ImageInfo objects for this custom model
        Vector<Metadata> images = source.get(customImagesKey);
        for (Metadata image : images)
        {
            ImageInfo info = new ImageInfo();
            info.retrieve(image);
            customImages.add(info);
        }
    }
}


//@Override
//public void stateChanged(ChangeEvent e)
//{
//    // Custom image slider moved
//    int index = imageList.getSelectedIndex();
//    Object selectedValue = imageList.getSelectedValue();
//    if (selectedValue == null)
//        return;
//
//    // Get the actual filename of the selected image
//    String imagename = ((ImageInfo)selectedValue).imagefilename;
//
//    JSlider source = (JSlider)e.getSource();
//    currentSlice = (int)source.getValue();
//    bandValue.setText(Integer.toString(currentSlice));
//
//    ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
//    Set<Image> imageSet = images.getImages();
//    for (Image i : imageSet)
//    {
//        if (i instanceof PerspectiveImage)
//        {
//            // We want to compare the actual image filename here, not the displayed name which may not be unique
//            PerspectiveImage image = (PerspectiveImage)i;
//            ImageKey key = image.getKey();
//            String name = new File(key.name).getName();
//
//            if (name.equals(imagename))
//            {
//                image.setCurrentSlice(currentSlice);
//                image.setDisplayedImageRange(null);
//                if (!source.getValueIsAdjusting())
//                {
//                     image.loadFootprint();
//                     image.firePropertyChange();
//                }
//                return; // twupy1: Only change band for a single image now even if multiple ones are highlighted since differeent cubical images can have different numbers of bands.
//            }
//        }
//    }
//
////        System.out.println("State changed: " + fps);
//}
