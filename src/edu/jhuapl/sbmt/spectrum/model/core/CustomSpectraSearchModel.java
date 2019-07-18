package edu.jhuapl.sbmt.spectrum.model.core;

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
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.config.Strings;
import edu.jhuapl.sbmt.model.bennu.SpectrumSearchSpec;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.model.key.SpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.key.SpectrumKeyInterface;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;
import nom.tam.fits.FitsException;

public class CustomSpectraSearchModel extends AbstractSpectrumSearchModel
{
    String fileExtension = "";
    private List<CustomSpectrumKeyInterface> customSpectra;
    private Vector<CustomSpectraResultsListener> customSpectraListeners;
    private boolean initialized = false;
    private int numImagesInCollection = -1;
    final Key<List<CustomSpectrumKeyInterface>> customSpectraKey = Key.of("customSpectra");
    private Vector<SpectrumColoringChangedListener> colorChangedListeners = new Vector<SpectrumColoringChangedListener>();
    private Double redMinVal = 0.0;
    private Double redMaxVal;
    private Double greenMinVal = 0.0;
    private Double greenMaxVal;
    private Double blueMinVal = 0.0;
    private Double blueMaxVal;
    private boolean greyScaleSelected;
    private int redIndex;
    private int greenIndex;
    private int blueIndex;

    public CustomSpectraSearchModel(boolean hasHierarchicalSpectraSearch, boolean hasHypertreeBasedSpectraSearch,
    		SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification, ModelManager modelManager,
            PickManager pickManager,
            Renderer renderer, ISpectralInstrument instrument)
    {
        super(hasHierarchicalSpectraSearch, hasHypertreeBasedSpectraSearch, hierarchicalSpectraSearchSpecification, modelManager, pickManager, renderer, instrument);
        this.customSpectra = new Vector<CustomSpectrumKeyInterface>();
        this.customSpectraListeners = new Vector<CustomSpectraResultsListener>();

        setRedMaxVal(instrument.getRGBMaxVals()[0]);
        setGreenMaxVal(instrument.getRGBMaxVals()[1]);
        setBlueMaxVal(instrument.getRGBMaxVals()[2]);

        setRedIndex(instrument.getRGBDefaultIndices()[0]);
        setGreenIndex(instrument.getRGBDefaultIndices()[1]);
        setBlueIndex(instrument.getRGBDefaultIndices()[2]);

        updateColoring();
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
            SpectrumSearchSpec spectrumSpec = new SpectrumSearchSpec();
            spectrumSpec.fromFile(line);
            collection.tagSpectraWithMetadata(createSpectrumName(i), spectrumSpec);
        }
    }


    public List<CustomSpectrumKeyInterface> getcustomSpectra()
    {
        return customSpectra;
    }

    public void setCustomSpectra(List<CustomSpectrumKeyInterface> customSpectra)
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

    @Override
    public void addColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.add(listener);
    }

    @Override
    public void removeColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.remove(listener);
    }

    @Override
    public void removeAllColoringChangedListeners()
    {
        colorChangedListeners.removeAllElements();
    }

    @Override
    public void coloringOptionChanged()
    {
        fireColoringChanged();
    }

    private void fireColoringChanged()
    {
        for (SpectrumColoringChangedListener listener : colorChangedListeners)
        {
            listener.coloringChanged();
        }
    }

    @Override
    public void updateColoring()
    {
        // If we are currently editing user defined functions
        // (i.e. the dialog is open), do not update the coloring
        // since we may be in an inconsistent state.
        if (isCurrentlyEditingUserDefinedFunction())
            return;
        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
        if (isGreyScaleSelected())
        {
            collection.setChannelColoring(
                    new int[]{redIndex, redIndex, redIndex},
                    new double[]{redMinVal, redMinVal, redMinVal},
                    new double[]{redMaxVal, redMaxVal, redMaxVal},
                    instrument);
        }
        else
        {
            collection.setChannelColoring(
                    new int[]{redIndex, greenIndex, blueIndex},
                    new double[]{redMinVal, greenMinVal, blueMinVal},
                    new double[]{redMaxVal, greenMaxVal, blueMaxVal},
                    instrument);
        }
        fireColoringChanged();
    }

    public void loadSpectrum(SpectrumKeyInterface key, SpectraCollection images) throws FitsException, IOException
    {
        images.addSpectrum(key, true);
    }

    public void loadSpectra(String name, CustomSpectrumKeyInterface info)
    {
//    	FileType fileType = info.getFileType();
//    	ISpectraType spectrumType = info.getSpectrumType();
//    	String spectrumFilename = info.getSpectrumFilename();
//    	String pointingFilename = info.getPointingFilename();
//    	CustomSpectrumKeyInterface key = new CustomSpectrumKey(, fileType, instrument, spectrumType, spectrumFilename, pointingFilename);

    	try
        {
            if (!spectrumCollection.containsKey(info))
            {
                loadSpectrum(info, spectrumCollection);
            }
        }
        catch (Exception e1) {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                    "There was an error mapping the spectra.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            e1.printStackTrace();
        }
//
//        List<CustomSpectrumKeyInterface> keys = createSpectrumKeys(name, instrument);
//        for (CustomSpectrumKeyInterface key : keys)
//        {
//            key.spectrumType = info.spectraType;
////            ImageSource source = info.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
////            key.source = source;
//            key.name = getCustomDataFolder() + File.separator + info.spectrumfilename;
//            try
//            {
//                if (!spectrumCollection.containsKey(key))
//                {
//                    loadSpectrum(key, spectrumCollection);
//                }
//            }
//            catch (Exception e1) {
//                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
//                        "There was an error mapping the spectra.",
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//
//                e1.printStackTrace();
//            }
//        }
   }

    public void unloadSpectrum(SpectrumKeyInterface key, SpectraCollection spectra)
    {
        spectra.removeSpectrum(key);
    }

    public void unloadSpectrum(String name, CustomSpectrumKeyInterface key)
    {
    	unloadSpectrum(key, spectrumCollection);
//        List<SpectrumKeyInterface> keys = createSpectrumKeys(name, instrument);
//        for (SpectrumKeyInterface key : keys)
//        {
//            unloadSpectrum(key, spectrumCollection);
//        }
   }

    public List<SpectrumKeyInterface> createSpectrumKeys(String boundaryName, ISpectralInstrument instrument)
    {
        List<SpectrumKeyInterface> result = new ArrayList<SpectrumKeyInterface>();
        result.add(createSpectrumKey(boundaryName, instrument));
        return result;
    }

    public SpectrumKeyInterface createSpectrumKey(String imagePathName, ISpectralInstrument instrument)
    {
        SpectrumKeyInterface key = new SpectrumKey(getCustomDataFolder() + File.separator + imagePathName, null, null, instrument, "");
        return key;
    }

    //TODO: UPDATE THIS TO SAVE SPECTRA
    public void saveSpectrum(int index, CustomSpectrumKeyInterface oldSpectrumInfo, CustomSpectrumKeyInterface newSpectrumInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        // If newSpectrumInfo.imagefilename is null, that means we are in edit mode
        // and should continue to use the existing image
        if (newSpectrumInfo.getSpectrumFilename() == null)
        {
            newSpectrumInfo.setSpectrumFilename(oldSpectrumInfo.getSpectrumFilename());
        }
        else
        {
            String newFilename = newSpectrumInfo.getName() + "-" + uuid + ".spect";
            String newFilepath = getCustomDataFolder() + File.separator + newFilename;
            FileUtil.copyFile(newSpectrumInfo.getSpectrumFilename(),  newFilepath);
            String newFileInfoname = newSpectrumInfo.getName() + "-" + uuid + ".INFO";
            String newFileInfopath = getCustomDataFolder() + File.separator + newFileInfoname;
            FileUtil.copyFile(newSpectrumInfo.getPointingFilename(),  newFileInfopath);
            // Change newImageInfo.imagefilename to the new location of the file
            newSpectrumInfo.setSpectrumFilename(newFilename);
            newSpectrumInfo.setPointingFilename(newFileInfoname);

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
        for (CustomSpectrumKeyInterface info : customSpectra)
        {
            List<String> res = new ArrayList<String>();
            res.add(info.getSpectrumFilename());
            res.add(getCustomDataFolder() + File.separator + info.getSpectrumFilename());
            tempResults.add(res);
        }
        updateConfigFile();

        setSpectrumRawResults(tempResults);
        fireResultsChanged();
        fireResultsCountChanged(this.results.size());

    }

//    public void editButtonActionPerformed()
//    {
//      int selectedItem = getSelectedImageIndex()[0];
//      if (selectedItem >= 0)
//      {
//          CustomSpectrumKeyInterface oldSpectrumInfo = customSpectra.get(selectedItem);
//
//          CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(null, true, getInstrument());
//          dialog.setSpectrumInfo(oldSpectrumInfo, getModelManager().getPolyhedralModel().isEllipsoid());
//          dialog.setLocationRelativeTo(null);
//          dialog.setVisible(true);
//
//          // If user clicks okay replace item in list
//          if (dialog.getOkayPressed())
//          {
//              CustomSpectrumKeyInterface newSpectrumInfo = dialog.getSpectrumInfo();
//              try
//              {
//                  saveSpectrum(selectedItem, oldSpectrumInfo, newSpectrumInfo);
//                  remapSpectrumToRenderer(selectedItem);
//              }
//              catch (IOException e)
//              {
//                  e.printStackTrace();
//              }
//              catch (FitsException e)
//              {
//                  e.printStackTrace();
//              }
//          }
//      }
//  }

    /**
     * This function unmaps the image from the renderer and maps it again,
     * if it is currently shown.
     * @throws IOException
     * @throws FitsException
     */
    public void remapSpectrumToRenderer(int index) throws FitsException, IOException
    {
        CustomSpectrumKeyInterface spectrumKey = customSpectra.get(index);
        // Remove the image from the renderer

        if (spectrumCollection.containsKey(spectrumKey))
        {
            Spectrum spectrum = spectrumCollection.getSpectrumFromKey(spectrumKey);
            boolean visible = spectrum.isVisible();
            if (visible)
                spectrum.setVisible(false);
            spectrumCollection.removeSpectrum(spectrumKey);
            spectrumCollection.addSpectrum(spectrumKey);
            if (visible)
                spectrum.setVisible(true);
        }
    }

    public CustomSpectrumKeyInterface getSpectrumKeyForIndex(int index)
    {
        CustomSpectrumKeyInterface spectrumKey = customSpectra.get(index);
        return spectrumKey;
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
            String[] sumfileNames = configMap.getAsArray(Strings.SUMFILENAMES.getName());
            String[] infofileNames = configMap.getAsArray(Strings.INFOFILENAMES.getName());

            int numImages = spectrumNames.length;
            for (int i=0; i<numImages; ++i)
            {
            	String name = spectrumNames[i];
            	String spectrumFilename = spectrumFilenames[i];
            	String infoname = infofileNames[i];
            	String sumname = sumfileNames[i];
            	String pointingFilename = "";
            	FileType fileType;
            	if (infoname.equals(""))
            	{
            		pointingFilename = sumname;
            		fileType = FileType.SUM;
            	}
            	else
            	{
            		pointingFilename = infoname;
            		fileType = FileType.INFO;
            	}

                CustomSpectrumKeyInterface spectrumInfo = new CustomSpectrumKey(name, fileType, getInstrument(), null, spectrumFilename, pointingFilename);

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
    }

    public void initializeSpecList() throws IOException
    {
        if (initialized)
            return;

        boolean updated = migrateConfigFileIfNeeded();
        if (!updated)
        {
            if (!(new File(getConfigFilename()).exists())) return;
            FixedMetadata metadata = Serializers.deserialize(new File(getConfigFilename()), "CustomSpectra");
            retrieve(metadata);
        }

        List<List<String>> tempResults = new ArrayList<List<String>>();
        for (CustomSpectrumKeyInterface info : customSpectra)
        {
            List<String> res = new ArrayList<String>();
            res.add(info.getSpectrumFilename());
            res.add(getCustomDataFolder() + File.separator + info.getSpectrumFilename());
            tempResults.add(res);
        }
        setSpectrumRawResults(tempResults);

        fireResultsChanged();
        fireResultsCountChanged(customSpectra.size());
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof SpectraCollection)
            {
                // Get the actual filename of the selected image
                SpectrumKeyInterface key = ((SpectraCollection)model).getSpectrum((vtkActor)e.getPickedProp()).getKey();
                String name = new File(key.getName()).getName();

                int idx = -1;
                int size = customSpectra.size();
                for (int i=0; i<size; ++i)
                {
                    // We want to compare the actual image filename here, not the displayed name which may not be unique
                    CustomSpectrumKeyInterface SpectrumInfo = customSpectra.get(i);
                    String imageFilename = SpectrumInfo.getSpectrumFilename();
                    if (name.equals(imageFilename))
                    {
                        idx = i;
                        break;
                    }
                }
            }
        }
    }

    public void setSpectrumVisibility(SpectrumKeyInterface key, boolean visible)
    {
        if (spectrumCollection.containsKey(key))
        {
            Spectrum spectrum = spectrumCollection.getSpectrumFromKey(key);
            spectrum.setVisible(visible);
        }
    }

    @Override
    public CustomSpectrumKeyInterface[] getSelectedSpectrumKeys()
    {
        int[] indices = selectedImageIndices;
        CustomSpectrumKeyInterface[] selectedKeys = new CustomSpectrumKeyInterface[indices.length];
        if (indices.length > 0)
        {
            int i=0;
            for (int index : indices)
            {
                String spectrum = getSpectrumRawResults().get(index).get(0);
                String name = new File(spectrum).getName();
                spectrum = spectrum.substring(0, spectrum.length()-4);
                CustomSpectrumKeyInterface selectedKey = getSpectrumKeyForIndex(index);
                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }

    private String getConfigFilename()
    {
        return new File(getModelManager().getPolyhedralModel().getConfigFilename()).getParent() + File.separator + "specConfig.txt";
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
                collection.addSpectrum(SafeURLPaths.instance().getUrl(createSpectrumName(i)), instrument, style,true);

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
    	SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put(customSpectraKey, customSpectra);
    	return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
    	try
    	{
    		customSpectra = source.get(customSpectraKey);
    	}
    	catch (ClassCastException cce)
    	{
    		//not sure if we need to worry about this
//    		Key<Metadata[]> oldCustomSpectraKey = Key.of("customSpectra");
//    		Metadata[] oldCustomSpectra = source.get(oldCustomSpectraKey);
//    		List<CustomSpectrumKeyInterface> migratedSpectra = new ArrayList<CustomSpectrumKeyInterface>();
//    		for (Metadata meta : oldCustomSpectra)
//    		{
//    			migratedSpectra.add(CustomSpectrumKeyInterface.retrieveOldFormat(meta));
//    		}
//    		customSpectra = migratedSpectra;
//    		updateConfigFile();
    	}
    }

    public void saveSpectra(List<CustomSpectrumKey> customImages, String filename)
    {
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        Metadata[] infoArray = new Metadata[customImages.size()];
        int i=0;
        final Key<Metadata[]> customSpectraKey = Key.of("SavedSpectra");
        for (CustomSpectrumKeyInterface info : customSpectra)
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
                CustomSpectrumKeyInterface info = CustomSpectrumKeyInterface.retrieve(meta);
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


    public Double getRedMinVal()
    {
        return redMinVal;
    }


    public void setRedMinVal(Double redMinVal)
    {
        this.redMinVal = redMinVal;
    }


    public Double getRedMaxVal()
    {
        return redMaxVal;
    }


    public void setRedMaxVal(Double redMaxVal)
    {
        this.redMaxVal = redMaxVal;
    }


    public Double getGreenMinVal()
    {
        return greenMinVal;
    }


    public void setGreenMinVal(Double greenMinVal)
    {
        this.greenMinVal = greenMinVal;
    }


    public Double getGreenMaxVal()
    {
        return greenMaxVal;
    }


    public void setGreenMaxVal(Double greenMaxVal)
    {
        this.greenMaxVal = greenMaxVal;
    }


    public Double getBlueMinVal()
    {
        return blueMinVal;
    }


    public void setBlueMinVal(Double blueMinVal)
    {
        this.blueMinVal = blueMinVal;
    }


    public Double getBlueMaxVal()
    {
        return blueMaxVal;
    }


    public void setBlueMaxVal(Double blueMaxVal)
    {
        this.blueMaxVal = blueMaxVal;
    }


    public boolean isGreyScaleSelected()
    {
        return greyScaleSelected;
    }


    public void setGreyScaleSelected(boolean greyScaleSelected)
    {
        this.greyScaleSelected = greyScaleSelected;
    }


    public int getRedIndex()
    {
        return redIndex;
    }


    public void setRedIndex(int redIndex)
    {
        this.redIndex = redIndex;
    }


    public int getGreenIndex()
    {
        return greenIndex;
    }


    public void setGreenIndex(int greenIndex)
    {
        this.greenIndex = greenIndex;
    }


    public int getBlueIndex()
    {
        return blueIndex;
    }


    public void setBlueIndex(int blueIndex)
    {
        this.blueIndex = blueIndex;
    }


    public String getSpectrumColoringStyleName()
    {
        return spectrumColoringStyleName;
    }


    public void setSpectrumColoringStyleName(String spectrumColoringStyleName)
    {
        this.spectrumColoringStyleName = spectrumColoringStyleName;
    }
}