package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.config.Strings;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumIOException;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.CustomSpectraResultsListener;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumListIO;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

/**
 * Model that holds custom spectra
 * @author steelrj1
 *
 * @param <S>
 */
public class CustomSpectraSearchModel<S extends BasicSpectrum> extends BaseSpectrumSearchModel<S>
{
    String fileExtension = "";
    private List<CustomSpectrumKeyInterface> customSpectraKeys;
    private Vector<CustomSpectraResultsListener> customSpectraListeners;
    private boolean initialized = false;
    final Key<List<CustomSpectrumKeyInterface>> customSpectraKey = Key.of("customSpectra");

    public CustomSpectraSearchModel(ModelManager modelManager,
    		BasicSpectrumInstrument instrument)
    {
        super(modelManager, instrument);
        this.customSpectraKeys = new Vector<CustomSpectrumKeyInterface>();
        this.customSpectraListeners = new Vector<CustomSpectraResultsListener>();
    }

    /**
     * Returns the list of keys that are stored in this model
     * @return
     */
    public List<CustomSpectrumKeyInterface> getCustomSpectra()
    {
        return customSpectraKeys;
    }

    /**
     * Sets the custom spectra list
     * @param customSpectra
     */
    public void setCustomSpectra(List<CustomSpectrumKeyInterface> customSpectra)
    {
        this.customSpectraKeys = customSpectra;
    }

    /**
     * Adds a results changed listener
     * @param listener
     */
    public void addResultsChangedListener(CustomSpectraResultsListener listener)
    {
        customSpectraListeners.add(listener);
    }

    /**
     * Removed a results changed listener
     * @param listener
     */
    public void removeResultsChangedListener(CustomSpectraResultsListener listener)
    {
        customSpectraListeners.remove(listener);
    }

    /**
     * Fires the results changed listeners
     */
    protected void fireResultAdded(CustomSpectrumKeyInterface result)
    {
        for (CustomSpectraResultsListener listener : customSpectraListeners)
        {
            listener.resultAdded(result);
        }
    }

    /**
     * Fires the results deleted listeners
     */
    protected void fireResultDeleted(CustomSpectrumKeyInterface result)
    {
        for (CustomSpectraResultsListener listener : customSpectraListeners)
        {
            listener.resultDeleted(result);
        }
    }

    /**
     * Fires the results changed listeners
     */
    protected void fireResultChanged(CustomSpectrumKeyInterface result)
    {
        for (CustomSpectraResultsListener listener : customSpectraListeners)
        {
            listener.resultChanged(result);
        }
    }

    /**
     * Fires the results changed listeners
     */
    protected void fireResultsChanged()
    {
        for (CustomSpectraResultsListener listener : customSpectraListeners)
        {
            listener.resultsChanged(customSpectraKeys);
        }
    }

    /**
     * Fires the results loaded listeners
     */
    protected void fireResultsLoaded()
    {
        for (CustomSpectraResultsListener listener : customSpectraListeners)
        {
            listener.resultsLoaded(customSpectraKeys);
        }
    }

    /**
     * Saves the spectrum the users loads into the local custom-data folder in the cache
     * @param index
     * @param oldSpectrumInfo
     * @param newSpectrumInfo
     * @throws IOException
     */
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
            String newFilename = FilenameUtils.getBaseName(newSpectrumInfo.getSpectrumFilename()) + "-" + uuid + ".spect";
            String newFilepath = customDataFolder + File.separator + newFilename;
            FileUtil.copyFile(newSpectrumInfo.getSpectrumFilename(),  newFilepath);
            String newFileInfoname = FilenameUtils.getBaseName(newSpectrumInfo.getSpectrumFilename()) + "-" + uuid + ".INFO";
            String newFileInfopath = customDataFolder + File.separator + newFileInfoname;
            FileUtil.copyFile(newSpectrumInfo.getPointingFilename(),  newFileInfopath);
            // Change newImageInfo.imagefilename to the new location of the file
            newSpectrumInfo.setSpectrumFilename(newFilename);
            newSpectrumInfo.setPointingFilename(newFileInfoname);
        }

        if (index >= customSpectraKeys.size())
        {
            customSpectraKeys.add(newSpectrumInfo);
            fireResultAdded(newSpectrumInfo);
        }
        else
        {
            customSpectraKeys.set(index, newSpectrumInfo);
            fireResultChanged(newSpectrumInfo);
        }

        updateConfigFile();

    }

    /**
     * Deletes the keys specified by the indices from the custom list
     * @param indices
     */
    public void deleteSpectrum(int[] indices)
    {
    	List<CustomSpectrumKeyInterface> keysToRemove = new ArrayList<CustomSpectrumKeyInterface>();
    	for (int i = indices.length-1; i > -1; i--)
    	{
    		fireResultDeleted(customSpectraKeys.get(indices[i]));
    		customSpectraKeys.remove(indices[i]);
    	}
    	updateConfigFile();
    	fireResultsCountChanged(customSpectraKeys.size());
    }

    private void deleteSpectraFromList(List<CustomSpectrumKeyInterface> customSpectraKeys)
    {
    	for (int i = customSpectraKeys.size()-1; i > -1; i--)
    	{
    		fireResultDeleted(customSpectraKeys.get(i));
    		customSpectraKeys.remove(i);
    	}
    	updateConfigFile();
    	fireResultsCountChanged(customSpectraKeys.size());
    }

    /**
     * Internal method to migrate the spectrum config file from the old to the new format
     * @return
     * @throws IOException
     */
    private Boolean migrateConfigFileIfNeeded() throws IOException
    {
    	File version1File = new File(getOriginalConfigFilename());
    	File version1_1File = new File(getConfigFilename());
        MapUtil configMap = new MapUtil(getConfigFilename());
        if (configMap.getAsArray(Spectrum.SPECTRUM_NAMES) != null)
        {
            //backup the old config file
            FileUtils.copyFile(new File(getConfigFilename()), new File(getConfigFilename() + ".orig"));

            //migrate it to the new format
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

                CustomSpectrumKeyInterface spectrumInfo = new CustomSpectrumKey(name, fileType, getInstrument(), null, spectrumFilename, pointingFilename, null);

                customSpectraKeys.add(spectrumInfo);
            }

            updateConfigFile();
            return true;
        }
        else if (!version1_1File.exists())
        {
        	initializeSpecList(version1File.getAbsolutePath(), false);
        	updateConfigFile();
        	return null;
        }
        else
            return false;

    }

    /**
     * Flushes the keys out the config file in the custom-data folder
     */
    public void updateConfigFile()
    {
    	//write out the current version
        try
        {
            Serializers.serialize("CustomSpectra", this, new File(getConfigFilename()));
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //write out the legacy version
        try
        {
        	SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        	List<CustomSpectrumKeyInterface> legacyKeys = new ArrayList<CustomSpectrumKeyInterface>(customSpectraKeys);
        	for (CustomSpectrumKeyInterface key : legacyKeys)
        	{
        		key.setSpectraSearchSpec(null);
        		if (!key.getSpectrumType().getDisplayName().endsWith("_SPECTRA"))
        				key.getSpectrumType().setDisplayName(key.getSpectrumType().getDisplayName() + "_SPECTRA");
        	}
        	result.put(customSpectraKey, legacyKeys);
        	Serializers.serialize("CustomSpectra", result, new File(getOriginalConfigFilename()));
        }
        catch (IOException ioe)
        {
        	ioe.printStackTrace();
        }
    }

    public void initializeSpecList() throws IOException
    {
    	initializeSpecList(getConfigFilename(), true);
    }

    /**
     * Initializes the spectrum table with the information in the custom spectra metadata file, if available
     * @throws IOException
     */
    private void initializeSpecList(String filename, boolean attemptToMigrate) throws IOException
    {
        if (initialized)
            return;
        Boolean updated = false;
        if (attemptToMigrate == true)
        	updated = migrateConfigFileIfNeeded();
        if (updated == null) return;
        if (!updated)
        {
            if (!(new File(filename).exists())) return;
            FixedMetadata metadata = Serializers.deserialize(new File(filename), "CustomSpectra");
            retrieve(metadata);
        }

        List<S> tempResults = new ArrayList<S>();
        for (CustomSpectrumKeyInterface info : customSpectraKeys)
        {
        	S spectrum = (S)SbmtSpectrumModelFactory.createSpectrum(customDataFolder + File.separator + info.getSpectrumFilename(), SpectrumInstrumentFactory.getInstrumentForName(instrument.getDisplayName()));
			spectrum.isCustomSpectra = true;
			spectrum.spectrumName = info.getName();
			if (info.getSpectraSpec() != null)
				spectrum.setMetadata(info.getSpectraSpec());
			tempResults.add(spectrum);
        }
        this.results = tempResults;
        fireResultsLoaded();
        fireResultsCountChanged(customSpectraKeys.size());
    }

    /**
     * Returns the name of the customspectrum config metadata file
     * @return
     */
    private String getConfigFilename()
    {
        return customDataFolder + File.separator + instrument.getDisplayName() + "_specConfig_v1.1.txt";
    }

    /**
     * Returns the name of the original customspectrum config metadata file
     * @return
     */
    private String getOriginalConfigFilename()
    {
        return customDataFolder + File.separator + instrument.getDisplayName() + "_specConfig.txt";
    }

    @Override
    public Metadata store()
    {
    	SettableMetadata result = SettableMetadata.of(Version.of(1, 1));
    	result.put(customSpectraKey, customSpectraKeys);
    	return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
    	try
    	{
    		customSpectraKeys = source.get(customSpectraKey);
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

    /**
     * Returns the model name
     * @return
     */
    @Override
    public ModelNames getSpectrumCollectionModelName()
    {
        return ModelNames.CUSTOM_SPECTRA;
    }

    /**
     * Returns the boundary model name
     * @return
     */
    @Override
    public ModelNames getSpectrumBoundaryCollectionModelName()
    {
        return ModelNames.CUSTOM_SPECTRA_BOUNDARIES;
    }

    /**
     * Saves the selected spectra to a file
     * @param file
     * @param selectedIndices
     * @throws Exception
     */
    @Override
    public void saveSelectedSpectrumListToFile(File file, int[] selectedIndices) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	List<CustomSpectrumKeyInterface> selectedKeys = new ArrayList<CustomSpectrumKeyInterface>();
    	for (int i : selectedIndices)
    	{
    		selectedKeys.add(customSpectraKeys.get(i));
    	}
    	SpectrumListIO.saveCustomSelectedSpectrumListButtonActionPerformed(customDataFolder, file, selectedKeys, selectedIndices);
    }

    /**
     * Saves the entire list of spectra to a file
     * @param file
     * @throws Exception
     */
    @Override
    public void saveSpectrumListToFile(File file) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	SpectrumListIO.saveCustomSpectrumListButtonActionPerformed(customDataFolder, file, customSpectraKeys);
    }

    /**
     * Loads spectra from the given file.  Calls a completion block upon successful load to update the displays
     * @param file
     * @throws Exception
     */
    @Override
    public void loadSpectrumListFromFile(File file, boolean append) throws SpectrumIOException
    {
    	Preconditions.checkNotNull(customDataFolder);
    	List<CustomSpectrumKeyInterface> oldKeys = new ArrayList<CustomSpectrumKeyInterface>(customSpectraKeys);
    	SpectrumListIO.loadCustomSpectrumListButtonActionPerformed(file, append, customSpectraKeys, instrument, new Runnable()
		{
			@Override
			public void run()
			{
				if (append == false)
				{
					deleteSpectraFromList(oldKeys);
					updateConfigFile();
					fireResultsChanged();
					setResultIntervalCurrentlyShown(new IdPair(0, getNumberOfBoundariesToShow()));
				}
				else
				{
					updateConfigFile();
					for (CustomSpectrumKeyInterface key : customSpectraKeys)
					{
						if (!oldKeys.contains(key))
						{
							fireResultAdded(key);
						}
					}
					setResultIntervalCurrentlyShown(new IdPair(0, getNumberOfBoundariesToShow()));

				}
			}
		});
    }
}