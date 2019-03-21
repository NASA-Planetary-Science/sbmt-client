package edu.jhuapl.sbmt.dtm.model;


import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.StorableAsMetadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class DEMKey implements StorableAsMetadata<DEMKey>
{
    public String name = ""; // name to call this image for display purposes
    public String demfilename = ""; // filename of image on disk

    public DEMKey(String fileName, String displayName)
    {
        this.demfilename = fileName;
        this.name = displayName;
    }

    // Copy constructor
    public DEMKey(DEMKey copyKey)
    {
        demfilename = copyKey.demfilename;
        this.name = copyKey.name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object obj)
    {
        return demfilename.equals(((DEMKey)obj).demfilename);
    }

    @Override
    public int hashCode()
    {
        return demfilename.hashCode();
    }

    private static final Key<String> nameKey = Key.of("name");
    private static final  Key<String> demfilenameKey = Key.of("demfilename");
    private static final Key<DEMKey> DEM_KEY = Key.of("demKey");

	@Override
	public Metadata store()
	{
		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
		write(nameKey, name, configMetadata);
		write(demfilenameKey, demfilename, configMetadata);
		return configMetadata;
	}

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(DEM_KEY, (metadata) -> {

	        String name = metadata.get(nameKey);
	        String demfilename = metadata.get(demfilenameKey);
			return new DEMKey(demfilename, name);
		});
	}

	public static DEMKey retrieveOldFormat(Metadata metadata)
	{
		 Key<String> nameKey = Key.of("name");
		 Key<String> demFileNameKey = Key.of("demfilename");
		 DEMKey key = new DEMKey(metadata.get(demFileNameKey), metadata.get(nameKey));
		 return key;
	}

	@Override
	public Key<DEMKey> getKey()
	{
		return DEM_KEY;
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
}
