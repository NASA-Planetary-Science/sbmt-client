package edu.jhuapl.sbmt.dtm.model.creation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class DtmCreationModel implements MetadataManager
{
	private ModelManager modelManager;
    private Renderer renderer;
    private DEMCollection dems;
    private DEMBoundaryCollection boundaries;
    private double latitude;
    private double longitude;
    private double pixelScale;
    private int halfSize;
    private String mapmakerPath;
    private String bigmapPath;
    private List<DEMKey> infoList;
    private int[] selectedIndices;
    private Vector<DEMCreationModelChangedListener> listeners;
	Key<List<DEMKey>> demKeysKey = Key.of("demKeys");

    private boolean initialized = false;

	public DtmCreationModel(ModelManager modelManager)
	{
		this.modelManager = modelManager;
		 // Get collections
        dems = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        boundaries = (DEMBoundaryCollection)modelManager.getModel(ModelNames.DEM_BOUNDARY);
        infoList = new ArrayList<DEMKey>();
        this.listeners = new Vector<DEMCreationModelChangedListener>();
	}

    private ModelNames getDEMBoundaryCollectionModelName()
    {
        return ModelNames.DEM_BOUNDARY;
    }

    public String getCustomDataFolder()
    {
        return modelManager.getPolyhedralModel().getCustomDataFolder();
    }

    public String getCustomDataRootFolder()
    {
        return modelManager.getPolyhedralModel().getCustomDataRootFolder();
    }

    private String getDEMConfigFilename()
    {
        return modelManager.getPolyhedralModel().getDEMConfigFilename();
    }

    private boolean migrateConfigFileIfNeeded() throws IOException
    {
    	MapUtil configMap = new MapUtil(getDEMConfigFilename());

        if (configMap.getAsArray(DEM.DEM_NAMES) != null)
        {
        	//backup the old config file
            FileUtils.copyFile(new File(getDEMConfigFilename()), new File(getDEMConfigFilename() + ".orig"));

            String[] demNames = configMap.getAsArray(DEM.DEM_NAMES);
            String[] demFilenames = configMap.getAsArray(DEM.DEM_FILENAMES);
            if (demFilenames == null)
            {
                // for backwards compatibility
                demFilenames = configMap.getAsArray(DEM.DEM_MAP_PATHS);
                demNames = new String[demFilenames.length];
                for (int i=0; i<demFilenames.length; ++i)
                {
                    demNames[i] = new File(demFilenames[i]).getName();
                    demFilenames[i] = "dem" + i + ".FIT";
                }
            }

            int numDems = demNames != null ? demNames.length : 0;
            for (int i=0; i<numDems; ++i)
            {
                DEMKey demInfo = new DEMKey(new File(getDEMConfigFilename()).getParent() + File.separator + demFilenames[i], demNames[i]);
                infoList.add(demInfo);
            }

            updateConfigFile();
            return true;
        }
        else
        	return false;
    }

    // Initializes the DEM list from config
    public void initializeDEMList() throws IOException
    {
        if (initialized)
            return;

        boolean updated = migrateConfigFileIfNeeded();
        if (!updated)
        {
	        if (new File(getDEMConfigFilename()).exists())
	        {
		        FixedMetadata metadata = Serializers.deserialize(new File(getDEMConfigFilename()), "CustomDEMs");
		        retrieve(metadata);
	        }
        }

        for (DEMKey info : infoList)
        {
        	info.demfilename = SafeURLPaths.instance().getUrl(info.demfilename);
        	fireInfoChangedListeners(info);
        }

        initialized = true;
    }

    public void updateConfigFile()
    {
    	try
		{
			Serializers.serialize("CustomDEMs", this, new File(getDEMConfigFilename()));
		}
    	catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    // Removes a DEM
    public void removeDEM(int[] index)
    {
        // Get the DEM info
    	for (int idx : index)
    	{
    		DEMKey demInfo = infoList.get(idx);
    		removeDEM(new DEMKey[] { demInfo });
    	}
    }

    public void removeDEM(DEMKey[] demInfos)
    {
    	// Remove from cache
    	for (DEMKey demInfo : demInfos)
    	{
	        String name = getCustomDataFolder() + File.separator + demInfo.demfilename;
	        new File(name).delete();

	        // Remove the DEM from the renderer
	        DEMKey demKey = dems.getDEMKeyFromInfo(demInfo);
	        if (demKey == null)
	        {
	        	 infoList.remove(demInfo);
	        	 boundaries.removeBoundary(demKey);
	        	 continue;
	        }
	        dems.removeDEM(demKey);

	        // Remove from the list
	        infoList.remove(demInfo);
	        boundaries.removeBoundary(demKey);

	        updateConfigFile();
    	}
    }

    public void saveDEM(DEMKey demInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        // Copy FIT file to cache
        String newFilename = "dem-" + uuid + ".fit";
        String newFilepath = getCustomDataFolder() + File.separator + newFilename;
        FileUtil.copyFile(demInfo.demfilename,  newFilepath);
        // Change demInfo.demfilename to the new location of the file
        demInfo.demfilename = SafeURLPaths.instance().getUrl(newFilepath);

        infoList.add(demInfo);
        fireInfoChangedListeners(demInfo);
        updateConfigFile();
    }

    public void loadFiles(File[] files, Runnable invalidNameResponse)
    {
    	for (File file : files)
        {
            // Check if the file provided is valid
            if (file == null || !file.exists()) continue;	// Not valid, do nothing

            // Valid, load it in
            DEMKey demKey = new DEMKey(file.getAbsolutePath(), file.getName());
            demKey.demfilename = file.getAbsolutePath();
            demKey.name = file.getName();

            // Save it to the list of DEMs
            try
            {
            	 if (demKey.demfilename.toLowerCase().endsWith(".fit") || demKey.demfilename.toLowerCase().endsWith(".fits"))
                 {
            		 saveDEM(demKey);
                 }
            	 else
                 {
                     invalidNameResponse.run();
                 }
            }
            catch (IOException e2)
            {
                e2.printStackTrace();
            }
        }
    }

    public void renameDEM(DEMKey key, String name)
    {
        key.name = name;
        updateConfigFile();
        fireInfoChangedListeners(getKeyList());
    }

	public ModelManager getModelManager()
	{
		return modelManager;
	}

	public Renderer getRenderer()
	{
		return renderer;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public double getPixelScale()
	{
		return pixelScale;
	}

	public void setPixelScale(double pixelScale)
	{
		this.pixelScale = pixelScale;
	}

	public int getHalfSize()
	{
		return halfSize;
	}

	public void setHalfSize(int halfSize)
	{
		this.halfSize = halfSize;
	}

	public String getMapmakerPath()
	{
		return mapmakerPath;
	}

	public void setMapmakerPath(String mapmakerPath)
	{
		this.mapmakerPath = mapmakerPath;
	}

	public String getBigmapPath()
	{
		return bigmapPath;
	}

	public void setBigmapPath(String bigmapPath)
	{
		this.bigmapPath = bigmapPath;
	}

	public int[] getSelectedIndices()
	{
		if (selectedIndices == null) return null;
		Arrays.sort(selectedIndices);
		return selectedIndices;
	}

	public void setSelectedIndex(int[] selectedIndices)
	{
		this.selectedIndices = selectedIndices;
	}

	public DEMKey getSelectedItem()
	{
		return infoList.get(selectedIndices[0]);
	}

	public DEMKey[] getSelectedItems()
	{
		DEMKey[] infos = new DEMKey[getSelectedIndices().length];
		for (int i=0; i<infos.length; i++)
		{
			infos[i] = infoList.get(getSelectedIndices()[i]);
		}
		return infos;
	}

	public List<DEMKey> getKeyList()
	{
		return infoList;
	}

	public void addModelChangedListener(DEMCreationModelChangedListener listener)
	{
		listeners.add(listener);
	}

	public void fireInfoChangedListeners(DEMKey info)
	{
		listeners.forEach(l -> l.demKeyListChanged(info));
	}

	public void fireInfoChangedListeners(List<DEMKey> infos)
	{
		listeners.forEach(l -> l.demKeysListChanged(infos));
	}

	public void fireInfoRemovedListeners(DEMKey info)
	{
		listeners.forEach(l -> l.demKeyRemoved(info));
	}

	public void fireInfosRemovedListeners(DEMKey[] info)
	{
		listeners.forEach(l -> l.demKeysRemoved(info));
	}

	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put(demKeysKey, infoList);
    	return result;
	}

	@Override
	public void retrieve(Metadata source)
	{
		try
		{
			infoList = source.get(demKeysKey);
		}
    	catch (ClassCastException | IllegalArgumentException ex)
    	{
    		Key<Metadata[]> oldCustomDEMKey = Key.of("demInfos");
    		Metadata[] oldCustomImages = source.get(oldCustomDEMKey);
    		List<DEMKey> migratedImages = new ArrayList<DEMKey>();
    		for (Metadata meta : oldCustomImages)
    		{
    			migratedImages.add(DEMKey.retrieveOldFormat(meta));
    		}
    		infoList = migratedImages;
            try
			{
				FileUtils.copyFile(new File(getDEMConfigFilename()), new File(getDEMConfigFilename() + ".orig"));
			}
            catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    		updateConfigFile();
    	}
	}
}