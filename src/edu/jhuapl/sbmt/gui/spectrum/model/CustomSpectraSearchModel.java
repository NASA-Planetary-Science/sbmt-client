package edu.jhuapl.sbmt.gui.spectrum.model;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.metadata.serialization.Serializers;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ImageInfo;
import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog;
import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog.SpectrumInfo;
import edu.jhuapl.sbmt.model.bennu.OREXSearchSpec;
import edu.jhuapl.sbmt.model.image.CustomPerspectiveImage;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.SpectraType;
import edu.jhuapl.sbmt.model.spectrum.Spectrum;
import edu.jhuapl.sbmt.model.spectrum.Spectrum.SpectrumKey;
import edu.jhuapl.sbmt.model.spectrum.coloring.SpectrumColoringStyle;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

import nom.tam.fits.FitsException;

public class CustomSpectraSearchModel extends SpectrumSearchModel
{
    String fileExtension = "";
    private List<SpectrumInfo> customSpectra;
    private Vector<CustomSpectraResultsListener> customSpectraListeners;
    private boolean initialized = false;
    private int numImagesInCollection = -1;
    final Key<Metadata[]> customSpectraKey = Key.of("customSpectra");


    public CustomSpectraSearchModel(SmallBodyViewConfig smallBodyConfig, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, PickManager pickManager,
            Renderer renderer, SpectralInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, pickManager, renderer, instrument);
        this.customSpectra = new Vector<SpectrumInfo>();
        this.customSpectraListeners = new Vector<CustomSpectraResultsListener>();

        setRedMaxVal(0.000001);
        setGreenMaxVal(0.000001);
        setBlueMaxVal(0.000001);

        setRedIndex(1);
        setGreenIndex(1);
        setBlueIndex(1);
    }

    @Override
    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
        //TODO This need to really be shifted to use classes and not string representation until the end

        List<String> matchedImages=Lists.newArrayList();
        if (matchedImages.size() > 0)
            fileExtension = FilenameUtils.getExtension(matchedImages.get(0));
        super.setSpectrumRawResults(spectrumRawResults);
        fireResultsChanged();
        fireResultsCountChanged(this.results.size());
    }

    @Override
    public String createSpectrumName(int index)
    {
        return getSpectrumRawResults().get(index).get(1);
    }

    @Override
    public void populateSpectrumMetadata(String line)
    {
        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
        for (int i=0; i<results.size(); ++i)
        {
            OREXSearchSpec spectrumSpec = new OREXSearchSpec();
            spectrumSpec.fromFile(line);
            collection.tagSpectraWithMetadata(createSpectrumName(i), spectrumSpec);
        }
    }


    public List<SpectrumInfo> getcustomSpectra()
    {
        return customSpectra;
    }

    public void setCustomSpectra(List<SpectrumInfo> customSpectra)
    {
        this.customSpectra = customSpectra;
    }

    public void addResultsChangedListener(CustomSpectraResultsListener listener)
    {
        customSpectraListeners.add(listener);
    }

    public void removeResultsChangedListener(CustomSpectraResultsListener listener)
    {
        customSpectraListeners.remove(listener);
    }

    protected void fireResultsChanged()
    {
        for (CustomSpectraResultsListener listener : customSpectraListeners)
        {
            listener.resultsChanged(customSpectra);
        }
    }

    public void loadSpectrum(SpectrumKey key, SpectraCollection images) throws FitsException, IOException
    {
        images.addSpectrum(key);
    }

    public void loadSpectra(String name, SpectrumInfo info)
    {

        List<SpectrumKey> keys = createSpectrumKeys(name, instrument);
        for (SpectrumKey key : keys)
        {
            key.spectrumType = info.spectraType;
//            ImageSource source = info.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
//            key.source = source;
            key.name = getCustomDataFolder() + File.separator + info.spectrumfilename;
            try
            {
                if (!spectrumCollection.containsKey(key))
                {
                    loadSpectrum(key, spectrumCollection);
                }
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                        "There was an error mapping the spectra.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }
        }
   }

    public void unloadSpectrum(SpectrumKey key, SpectraCollection spectra)
    {
        spectra.removeSpectrum(key);
    }

    public void unloadSpectrum(String name)
    {
        List<SpectrumKey> keys = createSpectrumKeys(name, instrument);
        for (SpectrumKey key : keys)
        {
            unloadSpectrum(key, spectrumCollection);
        }
   }

    public List<SpectrumKey> createSpectrumKeys(String boundaryName, SpectralInstrument instrument)
    {
        List<SpectrumKey> result = new ArrayList<SpectrumKey>();
        result.add(createSpectrumKey(boundaryName, instrument));
        return result;
    }

    public SpectrumKey createSpectrumKey(String imagePathName, SpectralInstrument instrument)
    {
        SpectrumKey key = new SpectrumKey(getCustomDataFolder() + File.separator + imagePathName, null, null, instrument);
        return key;
    }

    //TODO: UPDATE THIS TO SAVE SPECTRA
    public void saveSpectrum(int index, SpectrumInfo oldSpectrumInfo, SpectrumInfo newSpectrumInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        // If newSpectrumInfo.imagefilename is null, that means we are in edit mode
        // and should continue to use the existing image
        if (newSpectrumInfo.spectrumfilename == null)
        {
            newSpectrumInfo.spectrumfilename = oldSpectrumInfo.spectrumfilename;
        }
        else
        {
            String newFilename = "spectrum-" + uuid + ".spect";
            String newFilepath = getCustomDataFolder() + File.separator + newFilename;
            FileUtil.copyFile(newSpectrumInfo.spectrumfilename,  newFilepath);
            String newFileInfoname = "spectrum-" + uuid + ".INFO";
            String newFileInfopath = getCustomDataFolder() + File.separator + newFileInfoname;
            FileUtil.copyFile(newSpectrumInfo.infofilename,  newFileInfopath);
            // Change newImageInfo.imagefilename to the new location of the file
            newSpectrumInfo.spectrumfilename = newFilename;
            newSpectrumInfo.infofilename = newFileInfoname;

//            // Check if this image is any of the supported formats
//            if(VtkENVIReader.isENVIFilename(newSpectrumInfo.spectrumfilename)){
//                // We were given an ENVI file (binary or header)
//                // Can assume at this point that both binary + header files exist in the same directory
//
//                // Get filenames of the binary and header files
//                String enviBinaryFilename = VtkENVIReader.getBinaryFilename(newSpectrumInfo.spectrumfilename);
//                String enviHeaderFilename = VtkENVIReader.getHeaderFilename(newSpectrumInfo.spectrumfilename);
//
//                // Rename newSpectrumInfo as that of the binary file
//                newSpectrumInfo.spectrumfilename = "image-" + uuid;
//
//                // Copy over the binary file
//                Files.copy(new File(enviBinaryFilename),
//                        new File(getCustomDataFolder() + File.separator
//                                + newSpectrumInfo.spectrumfilename));
//
//                // Copy over the header file
//                Files.copy(new File(enviHeaderFilename),
//                        new File(getCustomDataFolder() + File.separator
//                                + VtkENVIReader.getHeaderFilename(newSpectrumInfo.spectrumfilename)));
//            }
//            else if(newSpectrumInfo.spectrumfilename.endsWith(".fit") || newSpectrumInfo.spectrumfilename.endsWith(".fits") ||
//                    newSpectrumInfo.spectrumfilename.endsWith(".FIT") || newSpectrumInfo.spectrumfilename.endsWith(".FITS"))
//            {
//                // Copy FIT file to cache
//                String newFilename = "image-" + uuid + ".fit";
//                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
//                FileUtil.copyFile(newSpectrumInfo.spectrumfilename,  newFilepath);
//                // Change newSpectrumInfo.spectrumfilename to the new location of the file
//                newSpectrumInfo.spectrumfilename = newFilename;
//            }
//            else
//            {
//
//                // Convert native VTK supported image to PNG and save to cache
//                vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
//                vtkImageReader2 imageReader = imageFactory.CreateImageReader2(newSpectrumInfo.spectrumfilename);
//                if (imageReader == null)
//                {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
//                        "The format of the specified file is not supported.",
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//                imageReader.SetFileName(newSpectrumInfo.spectrumfilename);
//                imageReader.Update();
//
//                vtkAlgorithmOutput imageReaderOutput = imageReader.GetOutputPort();
//                vtkPNGWriter imageWriter = new vtkPNGWriter();
//                imageWriter.SetInputConnection(imageReaderOutput);
//                // We save out the image using a new name that makes use of a UUID
//                newSpectrumInfo.spectrumfilename = "image-" + uuid + ".png";
//                imageWriter.SetFileName(getCustomDataFolder() + File.separator + newSpectrumInfo.spectrumfilename);
//                //imageWriter.SetFileTypeToBinary();
//                imageWriter.Write();
//            }
        }

//        // Operations specific for perspective projection type
//        if (newSpectrumInfo.projectionType == ProjectionType.PERSPECTIVE)
//        {
//            // If newSpectrumInfo.sumfilename and infofilename are both null, that means we are in edit mode
//            // and should continue to use the existing sumfile
//            if (newSpectrumInfo.sumfilename == null && newSpectrumInfo.infofilename == null)
//            {
//                newSpectrumInfo.sumfilename = oldSpectrumInfo.sumfilename;
//                newSpectrumInfo.infofilename = oldSpectrumInfo.infofilename;
//            }
//            else
//            {
//                if (newSpectrumInfo.sumfilename != null)
//                {
//                    // We save out the sumfile using a new name that makes use of a UUID
//                    String newFilename = "sumfile-" + uuid + ".SUM";
//                    String newFilepath = getCustomDataFolder() + File.separator + newFilename;
//                    FileUtil.copyFile(newSpectrumInfo.sumfilename, newFilepath);
//                    // Change newSpectrumInfo.sumfilename to the new location of the file
//                    newSpectrumInfo.sumfilename = newFilename;
//                }
//                else if (newSpectrumInfo.infofilename != null)
//                {
//                    // We save out the infofile using a new name that makes use of a UUID
//                    String newFilename = "infofile-" + uuid + ".INFO";
//                    String newFilepath = getCustomDataFolder() + File.separator + newFilename;
//                    FileUtil.copyFile(newSpectrumInfo.infofilename, newFilepath);
//                    // Change newSpectrumInfo.infofilename to the new location of the file
//                    newSpectrumInfo.infofilename = newFilename;
//                }
//            }
//        }
        if (index >= customSpectra.size())
        {
            customSpectra.add(newSpectrumInfo);
        }
        else
        {
            customSpectra.set(index, newSpectrumInfo);
        }

        List<List<String>> tempResults = new ArrayList<List<String>>();
        for (SpectrumInfo info : customSpectra)
        {
            List<String> res = new ArrayList<String>();
            res.add(info.spectrumfilename);
            res.add(getCustomDataFolder() + File.separator + info.spectrumfilename);
            tempResults.add(res);
        }
        updateConfigFile();

        setSpectrumRawResults(tempResults);
        fireResultsChanged();
        fireResultsCountChanged(this.results.size());

    }

    public void editButtonActionPerformed()
    {
      int selectedItem = getSelectedImageIndex()[0];
      if (selectedItem >= 0)
      {
          SpectrumInfo oldSpectrumInfo = customSpectra.get(selectedItem);

          CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(null, true, getInstrument());
          dialog.setSpectrumInfo(oldSpectrumInfo, getModelManager().getPolyhedralModel().isEllipsoid());
          dialog.setLocationRelativeTo(null);
          dialog.setVisible(true);

          // If user clicks okay replace item in list
          if (dialog.getOkayPressed())
          {
              SpectrumInfo newSpectrumInfo = dialog.getSpectrumInfo();
              try
              {
                  saveSpectrum(selectedItem, oldSpectrumInfo, newSpectrumInfo);
                  remapSpectrumToRenderer(selectedItem);
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

    /**
     * This function unmaps the image from the renderer and maps it again,
     * if it is currently shown.
     * @throws IOException
     * @throws FitsException
     */
    public void remapSpectrumToRenderer(int index) throws FitsException, IOException
    {
        SpectrumInfo SpectrumInfo = customSpectra.get(index);
        // Remove the image from the renderer
        String name = getCustomDataFolder() + File.separator + SpectrumInfo.spectrumfilename;
//        ImageSource source = SpectrumInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
        FileType fileType = SpectrumInfo.sumfilename != null && !SpectrumInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
        SpectraType spectraType = SpectrumInfo.spectraType;
//        SpectralInstrument instrument = SpectraType.findSpectraTypeForDisplayName(instrument.getDisplayName());
//        SpectralInstrument instrument = spectraType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(SpectrumInfo.rotation, SpectrumInfo.flip) : null;
        SpectrumKey SpectrumKey = new SpectrumKey(name, fileType, spectraType, instrument);

        if (spectrumCollection.containsKey(SpectrumKey))
        {
            Spectrum spectrum = spectrumCollection.getSpectrumFromKey(SpectrumKey);
            boolean visible = spectrum.isVisible();
            if (visible)
                spectrum.setVisible(false);
            spectrumCollection.removeSpectrum(SpectrumKey);
            spectrumCollection.addSpectrum(SpectrumKey);
            if (visible)
                spectrum.setVisible(true);
        }
    }

    public SpectrumKey getSpectrumKeyForIndex(int index)
    {
        SpectrumInfo SpectrumInfo = customSpectra.get(index);
        // Remove the image from the renderer
        String name = getCustomDataFolder() + File.separator + SpectrumInfo.spectrumfilename;
//        ImageSource source = SpectrumInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
        FileType fileType = SpectrumInfo.sumfilename != null && !SpectrumInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
        SpectraType spectraType = SpectrumInfo.spectraType;
//        SpectralInstrument instrument = SpectraType.findSpectraTypeForDisplayName(instrument.getDisplayName());
//        SpectralInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(SpectrumInfo.rotation, SpectrumInfo.flip) : null;
        SpectrumKey SpectrumKey = new SpectrumKey(name, fileType, spectraType, instrument);
        return SpectrumKey;
    }

    private boolean migrateConfigFileIfNeeded() throws IOException
    {
        MapUtil configMap = new MapUtil(getConfigFilename());
        if (configMap.getAsArray(Spectrum.SPECTRUM_NAMES) != null)
        {
            //backup the old config file
            FileUtils.copyFile(new File(getConfigFilename()), new File(getConfigFilename() + ".orig"));

            //migrate it to the new format
            boolean needToUpgradeConfigFile = false;
            String[] spectrumNames = configMap.getAsArray(Spectrum.SPECTRUM_NAMES);
            if (spectrumNames == null || (spectrumNames.length == 0)) return false;
            String[] spectrumFilenames = configMap.getAsArray(Spectrum.SPECTRUM_FILENAMES);
            String[] imageTypes = configMap.getAsArray(Spectrum.SPECTRUM_TYPES);
            if (spectrumFilenames == null)
            {
                // for backwards compatibility
                spectrumNames = new String[spectrumFilenames.length];
                imageTypes = new String[spectrumFilenames.length];

                for (int i=0; i<spectrumFilenames.length; ++i)
                {
                    spectrumNames[i] = new File(spectrumFilenames[i]).getName();
                    spectrumFilenames[i] = "image" + i + ".png";
                    imageTypes[i] = ImageType.GENERIC_IMAGE.toString();
                }

                // Mark that we need to upgrade config file to latest version
                // which we'll do at end of function.
                needToUpgradeConfigFile = true;
            }
            String[] sumfileNames = configMap.getAsArray(CustomPerspectiveImage.SUMFILENAMES);
            String[] infofileNames = configMap.getAsArray(CustomPerspectiveImage.INFOFILENAMES);

            int numImages = spectrumNames.length;
            for (int i=0; i<numImages; ++i)
            {
                SpectrumInfo spectrumInfo = new SpectrumInfo();
                spectrumInfo.name = spectrumNames[i];
                spectrumInfo.spectrumfilename = spectrumFilenames[i];
                if (sumfileNames.length > 0)
                    spectrumInfo.sumfilename = sumfileNames[i];
                if (infofileNames.length > 0)
                    spectrumInfo.infofilename = infofileNames[i];

                customSpectra.add(spectrumInfo);
            }

            updateConfigFile();
            return true;
        }
        else
            return false;

    }

    public void updateConfigFile()
    {
        try
        {
            Serializers.serialize("CustomSpectra", this, new File(getConfigFilename()));
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


//        MapUtil configMap = new MapUtil(getConfigFilename());
//        String spectrumNames = "";
//        String spectrumFilenames = "";
//        String projectionTypes = "";
//        String spectrumTypes = "";
//        String lllats = "";
//        String lllons = "";
//        String urlats = "";
//        String urlons = "";
//        String sumfilenames = "";
//        String infofilenames = "";
//
//        for (int i=0; i<customSpectra.size(); ++i)
//        {
//            SpectrumInfo spectrumInfo = customSpectra.get(i);
//            spectrumFilenames += spectrumInfo.spectrumfilename;
//            spectrumNames += spectrumInfo.name;
////            projectionTypes += spectrumInfo.projectionType;
//            spectrumTypes += spectrumInfo.spectraType;
//
////            lllats += String.valueOf(spectrumInfo.lllat);
////            lllons += String.valueOf(spectrumInfo.lllon);
////            urlats += String.valueOf(spectrumInfo.urlat);
////            urlons += String.valueOf(spectrumInfo.urlon);
//            sumfilenames += spectrumInfo.sumfilename;
//            infofilenames += spectrumInfo.infofilename;
//
//            if (i < customSpectra.size()-1)
//            {
//                spectrumNames += CustomShapeModel.LIST_SEPARATOR;
//                spectrumFilenames += CustomShapeModel.LIST_SEPARATOR;
//                projectionTypes += CustomShapeModel.LIST_SEPARATOR;
//                spectrumTypes += CustomShapeModel.LIST_SEPARATOR;
////                imageRotations += CustomShapeModel.LIST_SEPARATOR;
////                imageFlips += CustomShapeModel.LIST_SEPARATOR;
//                lllats += CustomShapeModel.LIST_SEPARATOR;
//                lllons += CustomShapeModel.LIST_SEPARATOR;
//                urlats += CustomShapeModel.LIST_SEPARATOR;
//                urlons += CustomShapeModel.LIST_SEPARATOR;
//                sumfilenames += CustomShapeModel.LIST_SEPARATOR;
//                infofilenames += CustomShapeModel.LIST_SEPARATOR;
//            }
//        }
//
//        Map<String, String> newMap = new LinkedHashMap<String, String>();
//
//        newMap.put(Spectrum.SPECTRUM_NAMES, spectrumNames);
//        newMap.put(Spectrum.SPECTRUM_FILENAMES, spectrumFilenames);
////        newMap.put(Image.PROJECTION_TYPES, projectionTypes);
//        newMap.put(Spectrum.SPECTRUM_TYPES, spectrumTypes);
////        newMap.put(Image.IMAGE_ROTATIONS, imageRotations);
////        newMap.put(Image.IMAGE_FLIPS, imageFlips);
////        newMap.put(CylindricalImage.LOWER_LEFT_LATITUDES, lllats);
////        newMap.put(CylindricalImage.LOWER_LEFT_LONGITUDES, lllons);
////        newMap.put(CylindricalImage.UPPER_RIGHT_LATITUDES, urlats);
////        newMap.put(CylindricalImage.UPPER_RIGHT_LONGITUDES, urlons);
//        newMap.put(CustomPerspectiveImage.SUMFILENAMES, sumfilenames);
//        newMap.put(CustomPerspectiveImage.INFOFILENAMES, infofilenames);
//
//        configMap.put(newMap);
    }

    public void initializeSpecList() throws IOException
    {
        if (initialized)
            return;

        boolean updated = migrateConfigFileIfNeeded();
        if (!updated)
        {
            if (!(new File(getConfigFilename()).exists())) return;
            System.out.println("CustomSpectraModel: initializeSpecList: config is " + getConfigFilename());
            FixedMetadata metadata = Serializers.deserialize(new File(getConfigFilename()), "CustomSpectra");
            retrieve(metadata);
        }

        fireResultsChanged();
        fireResultsCountChanged(customSpectra.size());

//        MapUtil configMap = new MapUtil(getConfigFilename());
//        if (configMap.containsKey(CylindricalImage.LOWER_LEFT_LATITUDES) /*|| configMap.containsKey(Image.PROJECTION_TYPES)*/)
//        {
//            boolean needToUpgradeConfigFile = false;
//            String[] spectrumNames = configMap.getAsArray(Spectrum.SPECTRUM_NAMES);
//            if (spectrumNames == null || (spectrumNames.length == 0)) return;
//            String[] spectrumFilenames = configMap.getAsArray(Spectrum.SPECTRUM_FILENAMES);
////            String[] projectionTypes = configMap.getAsArray(Image.PROJECTION_TYPES);
//            String[] imageTypes = configMap.getAsArray(Spectrum.SPECTRUM_TYPES);
//            if (spectrumFilenames == null)
//            {
//                // for backwards compatibility
////                imageFilenames = configMap.getAsArray(Image.IMAGE_MAP_PATHS);
//                spectrumNames = new String[spectrumFilenames.length];
////                projectionTypes = new String[imageFilenames.length];
//                imageTypes = new String[spectrumFilenames.length];
//
//                for (int i=0; i<spectrumFilenames.length; ++i)
//                {
//                    spectrumNames[i] = new File(spectrumFilenames[i]).getName();
//                    spectrumFilenames[i] = "image" + i + ".png";
////                    projectionTypes[i] = ProjectionType.CYLINDRICAL.toString();
//                    imageTypes[i] = ImageType.GENERIC_IMAGE.toString();
//
//                }
//
//                // Mark that we need to upgrade config file to latest version
//                // which we'll do at end of function.
//                needToUpgradeConfigFile = true;
//            }
////            double[] lllats = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LATITUDES);
////            double[] lllons = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LONGITUDES);
////            double[] urlats = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LATITUDES);
////            double[] urlons = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LONGITUDES);
//            String[] sumfileNames = configMap.getAsArray(CustomPerspectiveImage.SUMFILENAMES);
//            String[] infofileNames = configMap.getAsArray(CustomPerspectiveImage.INFOFILENAMES);
//
////            int numImages = lllats != null ? lllats.length : (projectionTypes != null ? projectionTypes.length : 0);
//            int numImages = spectrumNames.length;
//            for (int i=0; i<numImages; ++i)
//            {
//                SpectrumInfo spectrumInfo = new SpectrumInfo();
//                spectrumInfo.name = spectrumNames[i];
//                spectrumInfo.spectrumfilename = spectrumFilenames[i];
////                SpectrumInfo.projectionType = ProjectionType.valueOf(projectionTypes[i]);
////                SpectralInstrument instrument = instrument;
//
////                SpectrumInfo.spectraType = imageTypes == null ? ImageType.GENERIC_IMAGE : ImageType.valueOf(imageTypes[i]);
//
////                if (projectionTypes == null || ProjectionType.CYLINDRICAL.toString().equals(projectionTypes[i]))
////                {
////                    SpectrumInfo.lllat = lllats[i];
////                    SpectrumInfo.lllon = lllons[i];
////                    SpectrumInfo.urlat = urlats[i];
////                    SpectrumInfo.urlon = urlons[i];
////                }
////                else if (ProjectionType.PERSPECTIVE.toString().equals(projectionTypes[i]))
////                {
//                if (sumfileNames.length > 0)
//                    spectrumInfo.sumfilename = sumfileNames[i];
//                if (infofileNames.length > 0)
//                    spectrumInfo.infofilename = infofileNames[i];
////                }
//
//                customSpectra.add(spectrumInfo);
//            }
//
//            if (needToUpgradeConfigFile)
//                updateConfigFile();
//        }
//
//        List<List<String>> tempResults = new ArrayList<List<String>>();
//        for (SpectrumInfo info : customSpectra)
//        {
//            List<String> res = new ArrayList<String>();
//            res.add(info.spectrumfilename);
//            res.add(getCustomDataFolder() + File.separator + info.spectrumfilename);
//            tempResults.add(res);
//        }
//        updateConfigFile();
//        setSpectrumRawResults(tempResults);
//        fireResultsChanged();
//        fireResultsCountChanged(this.results.size());
//
//        initialized = true;
//        fireResultsChanged();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof SpectraCollection)// || model instanceof PerspectiveImageBoundaryCollection)
            {
                // Get the actual filename of the selected image
                SpectrumKey key = ((SpectraCollection)model).getSpectrum((vtkActor)e.getPickedProp()).getKey();
                String name = new File(key.name).getName();

                int idx = -1;
                int size = customSpectra.size();
                for (int i=0; i<size; ++i)
                {
                    // We want to compare the actual image filename here, not the displayed name which may not be unique
                    SpectrumInfo SpectrumInfo = customSpectra.get(i);
                    String imageFilename = SpectrumInfo.spectrumfilename;
                    if (name.equals(imageFilename))
                    {
                        idx = i;
                        break;
                    }
                }
            }
        }
    }

    public void setSpectrumVisibility(SpectrumKey key, boolean visible)
    {
        if (spectrumCollection.containsKey(key))
        {
            Spectrum spectrum = spectrumCollection.getSpectrumFromKey(key);
            spectrum.setVisible(visible);
        }
    }

    @Override
    public SpectrumKey[] getSelectedSpectrumKeys()
    {
        int[] indices = selectedImageIndices;
        SpectrumKey[] selectedKeys = new SpectrumKey[indices.length];
        if (indices.length > 0)
        {
            int i=0;
            for (int index : indices)
            {
                String spectrum = getSpectrumRawResults().get(index).get(0);
                String name = new File(spectrum).getName();
                spectrum = spectrum.substring(0, spectrum.length()-4);
                SpectrumKey selectedKey = getSpectrumKeyForIndex(index);
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

    public void showFootprints(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        SpectrumColoringStyle style = SpectrumColoringStyle.getStyleForName(spectrumColoringStyleName);
        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= getSpectrumRawResults().size())
                break;

            try
            {
                collection.addSpectrum(createSpectrumName(i), instrument, style,true);

            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        updateColoring();
    }

    @Override
    public Metadata store()
    {
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        Metadata[] infoArray = new Metadata[customSpectra.size()];
        int i=0;
        for (SpectrumInfo info : customSpectra)
        {
            infoArray[i++] = info.store();
        }
        write(customSpectraKey, infoArray, configMetadata);
        return configMetadata;

////        SettableMetadata data = (SettableMetadata)super.store();
//        SettableMetadata data = SettableMetadata.of(Version.of(1, 0));
//        //store the ImageInfo objects that make up this custom model
//        Vector<Metadata> images = new Vector<Metadata>();
////        ImmutableSortedSet.Builder<Metadata> images = ImmutableSortedSet.naturalOrder();
//        for (ImageInfo info : customImages)
//        {
//            images.add(info.store());
//        }
//        data.put(customImagesKey, images);
//        return data;
    }

    @Override
    public void retrieve(Metadata source)
    {

        Metadata[] metadataArray = read(customSpectraKey, source);
        for (Metadata meta : metadataArray)
        {
            SpectrumInfo info = new SpectrumInfo();
            info.retrieve(meta);
            customSpectra.add(info);
        }
////        super.retrieve(source);
//        //get the ImageInfo objects for this custom model
//        ArrayList<Metadata> images = source.get(customImagesKey);
//        for (Metadata image : images)
//        {
//            ImageInfo info = new ImageInfo();
//            info.retrieve(image);
//            customImages.add(info);
//        }
    }

    public void saveSpectra(List<ImageInfo> customImages, String filename)
    {
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        Metadata[] infoArray = new Metadata[customImages.size()];
        int i=0;
        final Key<Metadata[]> customSpectraKey = Key.of("SavedSpectra");
        for (SpectrumInfo info : customSpectra)
        {
            infoArray[i++] = info.store();
        }
        write(customSpectraKey, infoArray, configMetadata);
        try
        {
            Serializers.serialize("SavedSpectra", configMetadata, new File(filename));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadSpectra(String file)
    {
        FixedMetadata metadata;
        try
        {
            final Key<Metadata[]> customSpectraKey = Key.of("SavedSpectra");
            metadata = Serializers.deserialize(new File(file), "SavedSpectra");
//            retrieve(metadata);
            Metadata[] metadataArray = read(customSpectraKey, metadata);
            for (Metadata meta : metadataArray)
            {
                SpectrumInfo info = new SpectrumInfo();
                info.retrieve(meta);
                customSpectra.add(info);
            }
            System.out.println("CustomSpectrumModel: loadSpectra: number of spectra now " + customSpectra.size());
            updateConfigFile();
            fireResultsChanged();

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }

    protected <T> T read(Key<T> key, Metadata configMetadata)
    {
        T value = configMetadata.get(key);
        if (value != null)
            return value;
        return null;
    }
}