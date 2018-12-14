package edu.jhuapl.sbmt.gui.dtm.model.browse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileCache.FileInfo;
import edu.jhuapl.saavtk.util.FileCache.UnauthorizedAccessException;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmBrowseModel
{
	protected final DEMCollection dems;
	protected final DEMBoundaryCollection boundaries;
    private SmallBodyViewConfig smallBodyViewConfig;
    private Vector<DEMKey> keys;
    private Vector<DEMBrowseModelChangedListener> listeners;
    private String currentDataSet;
    private String[] dataSets;

	public DtmBrowseModel(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig smallBodyViewConfig)
	{
		this.dems = (DEMCollection) modelManager.getModel(ModelNames.DEM);
        this.boundaries = (DEMBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY);
        this.smallBodyViewConfig = smallBodyViewConfig;
        this.keys = new Vector<DEMKey>();
        this.listeners = new Vector<DEMBrowseModelChangedListener>();
        this.dataSets = new String[smallBodyViewConfig.dtmBrowseDataSourceMap.keySet().size()];
        smallBodyViewConfig.dtmBrowseDataSourceMap.keySet().toArray(this.dataSets);
	}


	public void loadAllDtmPaths() throws FileNotFoundException
    {
        loadDtmSet("Default");
    }

	public void loadDtmSet(String sourceName) throws FileNotFoundException
	{
		InputStream is = null;
        if (smallBodyViewConfig.dtmBrowseDataSourceMap.get("Default") == null) return;
        try
        {
            if (FileCache.isFileGettable(smallBodyViewConfig.dtmBrowseDataSourceMap.get("Default")))
            {
                is = new FileInputStream(FileCache.getFileFromServer(smallBodyViewConfig.dtmBrowseDataSourceMap.get(sourceName)));
                currentDataSet = "Default";
            }
        }
        catch (UnauthorizedAccessException e)
        {
            return;
        }
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);

        String line;
        try
        {
            while ((line = in.readLine()) != null)
            {
            	String[] parts = line.split(",");
            	FileInfo fileInfoFromServer = FileCache.getFileInfoFromServer(smallBodyViewConfig.rootDirOnServer + File.separator + "dtm/browse" + File.separator + parts[0]);
            	DEMKey key = new DEMKey(fileInfoFromServer.getURL().toString(), parts[1]);
            	keys.add(key);
            }
            System.out.println("DtmBrowseModel: loadAllDtmPaths: firing listeners");
            fireKeysChangedListeners(keys);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}

	public void addModelChangedListener(DEMBrowseModelChangedListener listener)
	{
		listeners.add(listener);
	}

	public void fireKeysChangedListeners(DEMKey key)
	{
		for (DEMBrowseModelChangedListener listener : listeners)
		{
			listener.demKeysListChanged(key);
		}
	}

	public void fireKeysChangedListeners(Vector<DEMKey> keys)
	{
		for (DEMBrowseModelChangedListener listener : listeners)
		{
			listener.demKeysListChanged(keys);
		}
	}


	public String getCurrentDataSet()
	{
		return currentDataSet;
	}


	public String[] getDataSets()
	{
		return dataSets;
	}

    public DEMCollection getDems()
	{
		return dems;
	}


	public DEMBoundaryCollection getBoundaries()
	{
		return boundaries;
	}
}
