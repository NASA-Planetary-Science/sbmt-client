package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.config.Strings;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.CustomSpectraResultsListener;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumListIO;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

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

    public List<CustomSpectrumKeyInterface> getCustomSpectra()
    {
        return customSpectraKeys;
    }

    public void setCustomSpectra(List<CustomSpectrumKeyInterface> customSpectra)
    {
        this.customSpectraKeys = customSpectra;
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
            listener.resultsChanged(customSpectraKeys);
        }
    }

    protected void fireResultsLoaded()
    {
        for (CustomSpectraResultsListener listener : customSpectraListeners)
        {
            listener.resultsLoaded(customSpectraKeys);
        }
    }

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
            String newFilepath = customDataFolder + File.separator + newFilename;
            FileUtil.copyFile(newSpectrumInfo.getSpectrumFilename(),  newFilepath);
            String newFileInfoname = newSpectrumInfo.getName() + "-" + uuid + ".INFO";
            String newFileInfopath = customDataFolder + File.separator + newFileInfoname;
            FileUtil.copyFile(newSpectrumInfo.getPointingFilename(),  newFileInfopath);
            // Change newImageInfo.imagefilename to the new location of the file
            newSpectrumInfo.setSpectrumFilename(newFilename);
            newSpectrumInfo.setPointingFilename(newFileInfoname);
        }

        if (index >= customSpectraKeys.size())
        {
            customSpectraKeys.add(newSpectrumInfo);
        }
        else
        {
            customSpectraKeys.set(index, newSpectrumInfo);
        }

        List<S> tempResults = new ArrayList<S>();
        for (CustomSpectrumKeyInterface info : customSpectraKeys)
        {
        	IBasicSpectrumRenderer<S> renderer = null;
			try
			{
				renderer = SbmtSpectrumModelFactory.createSpectrumRenderer(customDataFolder + File.separator + info.getSpectrumFilename(), SpectrumInstrumentFactory.getInstrumentForName(instrument.getDisplayName()));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tempResults.add(renderer.getSpectrum());
        }
        updateConfigFile();
        setSpectrumRawResults(tempResults);
    }

    public void deleteSpectrum(int[] indices)
    {
    	List<CustomSpectrumKeyInterface> keysToRemove = new ArrayList<CustomSpectrumKeyInterface>();
    	for (int i = indices.length-1; i > -1; i--)
    	{
    		customSpectraKeys.remove(indices[i]);
    	}
    	updateConfigFile();
    	fireResultsChanged();
    	fireResultsCountChanged(customSpectraKeys.size());
    }

    //TODO is this really needed?
//    /**
//     * This function unmaps the image from the renderer and maps it again,
//     * if it is currently shown.
//     * @throws IOException
//     * @throws FitsException
//     */
//    public void remapSpectrumToRenderer(int index) throws FitsException, IOException
//    {
//        CustomSpectrumKeyInterface spectrumKey = customSpectra.get(index);
//        // Remove the image from the renderer
//
//        if (spectrumCollection.containsKey(spectrumKey))
//        {
//            IBasicSpectrumRenderer spectrum = spectrumCollection.getSpectrumFromKey(spectrumKey);
//            boolean visible = spectrum.isVisible();
//            if (visible)
//                spectrum.setVisible(false);
//            spectrumCollection.removeSpectrum(spectrumKey);
//            spectrumCollection.addSpectrum(spectrumKey);
//            if (visible)
//                spectrum.setVisible(true);
//        }
//    }

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

                customSpectraKeys.add(spectrumInfo);
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

        List<S> tempResults = new ArrayList<S>();
        for (CustomSpectrumKeyInterface info : customSpectraKeys)
        {
        	S spectrum = (S)SbmtSpectrumModelFactory.createSpectrum(customDataFolder + File.separator + info.getSpectrumFilename(), SpectrumInstrumentFactory.getInstrumentForName(instrument.getDisplayName()));
			spectrum.isCustomSpectra = true;
			tempResults.add(spectrum);
        }

        this.results = tempResults;
        fireResultsLoaded();
        fireResultsCountChanged(customSpectraKeys.size());
    }

    public void setSpectrumVisibility(S spectrum, boolean visible)
    {
    	fireFootprintVisibilityChanged(spectrum, visible);
    }

    private String getConfigFilename()
    {
        return customDataFolder + File.separator + "specConfig.txt";
    }

    public void showFootprints(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= getSpectrumRawResults().size())
                break;
            fireFootprintVisibilityChanged(results.get(i), true);
        }
    }

    @Override
    public Metadata store()
    {
    	SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
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

    public void saveSpectra(List<CustomSpectrumKeyInterface> customSpectra, String filename)
    {
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));

        final Key<List<CustomSpectrumKeyInterface>> customSpectraKey = Key.of("SavedSpectra");

        configMetadata.put(customSpectraKey, customSpectra);
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
            final Key<List<CustomSpectrumKeyInterface>> customSpectraKey = Key.of("SavedSpectra");
            metadata = Serializers.deserialize(new File(file), "SavedSpectra");
            List<CustomSpectrumKeyInterface> customSpectraList = metadata.get(customSpectraKey);
            customSpectraKeys.addAll(customSpectraList);
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
    public void loadSpectrumListFromFile(File file) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	SpectrumListIO.loadCustomSpectrumListButtonActionPerformed(file, customSpectraKeys, instrument, new Runnable()
		{

			@Override
			public void run()
			{
				updateConfigFile();
				fireResultsChanged();
				setResultIntervalCurrentlyShown(new IdPair(0, getNumberOfBoundariesToShow()));
		        showFootprints(getResultIntervalCurrentlyShown());
			}
		});
    }
}