package edu.jhuapl.sbmt.dtm.service.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.NoInternetAccessException;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

public class DEMBrowseIO
{
	String sourceName;
	Map<String, String> dtmBrowseDataSourceMap;
	private Vector<DEMKey> keys;
	private String rootDir;
	private String currentDataSet;

	public DEMBrowseIO(String sourceName, Map<String, String> dtmBrowseDataSourceMap, String rootDir)
	{
		this.sourceName = sourceName;
		this.dtmBrowseDataSourceMap = dtmBrowseDataSourceMap;
		this.rootDir = rootDir;
		this.keys = new Vector<DEMKey>();
	}

	public void loadBrowseProducts(Runnable completionBlock)
	{
		InputStream is = null;
        if (dtmBrowseDataSourceMap.get("Default") == null) return;
        try
        {
            if (FileCache.isFileGettable(dtmBrowseDataSourceMap.get(sourceName)))
            {
                is = new FileInputStream(FileCache.getFileFromServer(dtmBrowseDataSourceMap.get(sourceName)));
                currentDataSet = "Default";
            }
        }
        catch (UnauthorizedAccessException e)
        {
            return;
        }
        catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (NoInternetAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);

        try
        {
            String line;
            String[] parts;
            while ((line = in.readLine()) != null)
            {
            	parts = line.split(",");
            	URL url = FileCache.instance().getUrl(SafeURLPaths.instance().getString(rootDir, "dtm", "browse", parts[0]));
            	DEMKey key = new DEMKey(url.toString(), parts[1], false);
            	keys.add(key);
            }
            completionBlock.run();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}

	public Vector<DEMKey> getKeys()
	{
		return keys;
	}

	public String getCurrentDataSet()
	{
		return currentDataSet;
	}

}
