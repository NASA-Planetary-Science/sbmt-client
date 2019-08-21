package edu.jhuapl.sbmt.tools;

import edu.jhuapl.sbmt.model.image.ImageSource;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class DBRunInfo implements MetadataManager
{
    public String pathToFileList;
    public String databasePrefix;
    public String remotePathToFileList;
    public String name;
    public ImageSource imageSource;
    private final Key<String> nameKey = Key.of("name");
    private final Key<String> pathToFileListKey = Key.of("pathToFileList");
    private final Key<String> databasePrefixKey = Key.of("databasePrefix");
    private final Key<String> remotePathToFileListKey = Key.of("remotePathToFileListConfig");
    private final Key<String> imageSourceKey = Key.of("imageSource");

    public DBRunInfo()
    {

    }

    public DBRunInfo(ImageSource source, String name, String pathToFileList)
    {
    	this.name = name;
        this.pathToFileList = pathToFileList;
        this.databasePrefix = "";
        this.remotePathToFileList = null;
        this.imageSource = source;
    }

    public DBRunInfo(ImageSource source, String name, String pathToFileList, String databasePrefix)
    {
    	this.name = name;
        this.pathToFileList = pathToFileList;
        this.databasePrefix = databasePrefix;
        this.remotePathToFileList = null;
        this.imageSource = source;
    }

    public DBRunInfo(ImageSource source, String name, String pathToFileList, String databasePrefix, String remotePathToFileList)
    {
    	this.name = name;
        this.pathToFileList = pathToFileList;
        this.databasePrefix = databasePrefix;
        this.remotePathToFileList = remotePathToFileList;
        this.imageSource = source;
    }

	@Override
	public Metadata store()
	{
		SettableMetadata metadata = SettableMetadata.of(Version.of(1, 0));
		metadata.put(nameKey, name);
		metadata.put(pathToFileListKey, pathToFileList);
		metadata.put(databasePrefixKey, databasePrefix);
		metadata.put(remotePathToFileListKey, remotePathToFileList);
		metadata.put(imageSourceKey, imageSource.toString());
		return metadata;
	}

	@Override
	public void retrieve(Metadata source)
	{
		name = source.get(nameKey);
		pathToFileList = source.get(pathToFileListKey);
		databasePrefix = source.get(databasePrefixKey);
		remotePathToFileList = source.get(remotePathToFileListKey);
		imageSource = ImageSource.valueFor(source.get(imageSourceKey));
	}
}
