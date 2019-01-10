package edu.jhuapl.sbmt.tools;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;

public class DBRunInfo implements MetadataManager
{
    public String pathToFileList;
    public String databasePrefix;
    public String remotePathToFileList;
    public String name;
    private final Key<String> nameKey = Key.of("name");
    private final Key<String> pathToFileListKey = Key.of("pathToFileList");
    private final Key<String> databasePrefixKey = Key.of("databasePrefix");
    private final Key<String> remotePathToFileListKey = Key.of("remotePathToFileListConfig");

    public DBRunInfo()
    {

    }

    public DBRunInfo(String name, String pathToFileList)
    {
    	this.name = name;
        this.pathToFileList = pathToFileList;
        this.databasePrefix = "";
        this.remotePathToFileList = null;
    }

    public DBRunInfo(String name, String pathToFileList, String databasePrefix)
    {
    	this.name = name;
        this.pathToFileList = pathToFileList;
        this.databasePrefix = databasePrefix;
        this.remotePathToFileList = null;
    }

    public DBRunInfo(String name, String pathToFileList, String databasePrefix, String remotePathToFileList)
    {
    	this.name = name;
        this.pathToFileList = pathToFileList;
        this.databasePrefix = databasePrefix;
        this.remotePathToFileList = remotePathToFileList;
    }

	@Override
	public Metadata store()
	{
		SettableMetadata metadata = SettableMetadata.of(Version.of(1, 0));
		metadata.put(nameKey, name);
		metadata.put(pathToFileListKey, pathToFileList);
		metadata.put(databasePrefixKey, databasePrefix);
		metadata.put(remotePathToFileListKey, remotePathToFileList);

		return metadata;
	}

	@Override
	public void retrieve(Metadata source)
	{
		name = source.get(nameKey);
		pathToFileList = source.get(pathToFileListKey);
		databasePrefix = source.get(databasePrefixKey);
		remotePathToFileList = source.get(remotePathToFileListKey);
	}
}
