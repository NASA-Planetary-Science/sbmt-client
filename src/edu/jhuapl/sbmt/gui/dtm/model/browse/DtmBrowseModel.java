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

	public DtmBrowseModel(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig smallBodyViewConfig)
	{
		this.dems = (DEMCollection) modelManager.getModel(ModelNames.DEM);
        this.boundaries = (DEMBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY);
        this.smallBodyViewConfig = smallBodyViewConfig;
        this.keys = new Vector<DEMKey>();
        this.listeners = new Vector<DEMBrowseModelChangedListener>();
	}


	public void loadAllDtmPaths() throws FileNotFoundException
    {
//        List<LidarDataFileSpec> lidarSpecs = new ArrayList<LidarDataFileSpec>();

        InputStream is = null;
//        if (polyhedralModelConfig.lidarBrowseFileListResourcePath.startsWith("/edu"))
//        {
//            is = getClass().getResourceAsStream(polyhedralModelConfig.lidarBrowseFileListResourcePath);
//        }
//        else
//        {
        if (smallBodyViewConfig.dtmBrowseDataSourceMap.get("Default") == null) return;
            try
            {
                if (FileCache.isFileGettable(smallBodyViewConfig.dtmBrowseDataSourceMap.get("Default")))
                    is = new FileInputStream(FileCache.getFileFromServer(smallBodyViewConfig.dtmBrowseDataSourceMap.get("Default")));
            }
            catch (UnauthorizedAccessException e)
            {
                return;
            }
//        }
        System.out.println("DtmBrowseModel: loadAllDtmPaths: generating dem keys");
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
//            	DEMKey key = new DEMKey(smallBodyViewConfig.rootDirOnServer + File.separator + "dtm/browse" + File.separator + parts[0], parts[1]);
            	keys.add(key);

//            	FileCache.getFileFromServer(parts[0]);
//            	dems.addDEM(key);
//                LidarDataFileSpec lidarSpec = new LidarDataFileSpec();
//                int indexFirstSpace = line.indexOf(' ');
//                if (indexFirstSpace == -1)
//                {
//                    lidarSpec.path = line;
//                    lidarSpec.comment = "";
//                }
//                else
//                {
//                    lidarSpec.path = line.substring(0,indexFirstSpace);
//                    lidarSpec.comment = line.substring(indexFirstSpace+1);
//                }
//                lidarSpec.name = new File(lidarSpec.path).getName();
//                if (lidarSpec.name.toLowerCase().endsWith(".gz"))
//                    lidarSpec.name = lidarSpec.name.substring(0, lidarSpec.name.length()-3);
//                lidarSpecs.add(lidarSpec);
            }
            System.out.println("DtmBrowseModel: loadAllDtmPaths: firing listeners");
            fireKeysChangedListeners(keys);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
//        catch (FitsException e)
//        {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//        return lidarSpecs;
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
		System.out.println("DtmBrowseModel: fireKeysChangedListeners: number of listeners " + listeners.size());
		for (DEMBrowseModelChangedListener listener : listeners)
		{
			listener.demKeysListChanged(keys);
		}
	}

}
