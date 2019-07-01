package edu.jhuapl.sbmt.dtm.model.browse;

import java.io.FileNotFoundException;
import java.util.Vector;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.service.io.DEMBrowseIO;

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
		DEMBrowseIO browseIO = new DEMBrowseIO(sourceName, smallBodyViewConfig.dtmBrowseDataSourceMap, smallBodyViewConfig.rootDirOnServer);
		browseIO.loadBrowseProducts(new Runnable()
		{
			@Override
			public void run()
			{
				currentDataSet = browseIO.getCurrentDataSet();
				keys = browseIO.getKeys();
				fireKeysChangedListeners(keys);

			}
		});
	}

	public void addModelChangedListener(DEMBrowseModelChangedListener listener)
	{
		listeners.add(listener);
	}

	public void fireKeysChangedListeners(DEMKey key)
	{
		listeners.forEach(l -> l.demKeyListChanged(key));
	}

	public void fireKeysChangedListeners(Vector<DEMKey> keys)
	{
		listeners.forEach(l -> l.demKeysListChanged(keys));
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
