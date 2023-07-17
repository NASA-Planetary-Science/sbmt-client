package edu.jhuapl.sbmt.spectrum.query;

import java.util.HashMap;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.DatabaseDataQuery;
import edu.jhuapl.sbmt.query.v2.DatabaseSearchMetadata;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;

public abstract class SpectrumDataQuery extends DatabaseDataQuery implements MetadataManager
{
	protected String spectraTableName;
	protected String cubeTableName;
	private String rootPath;

	protected static String STARTDATE = "startDate";
	protected static String STOPDATE = "stopDate";
	protected static String MINSCDISTANCE = "minScDistance";
	protected static String MAXSCDISTANCE = "maxScDistance";
	protected static String MININCIDENCE = "minIncidence";
	protected static String MAXINCIDENCE = "maxIncidence";
	protected static String MINEMISSION = "minEmission";
	protected static String MAXEMISSION = "maxEmission";
	protected static String MINPHASE = "minPhase";
	protected static String MAXPHASE = "maxPhase";
	protected static String SPECTRATABLENAME = "spectraTableName";
	protected static String CUBETABLENAME = "cubeTableName";
	protected static String CUBES = "cubes";


	public SpectrumDataQuery()
	{
		super(null);
	}

	public SpectrumDataQuery(DataQuerySourcesMetadata searchMetadata)
	{
		super(searchMetadata);
		this.rootPath = searchMetadata.getDataPath();

	}

	public String getSpectraTableName()
	{
		return spectraTableName;
	}

	public String getCubeTableName()
	{
		return cubeTableName;
	}

	Key<String> rootPathKey = Key.of("rootPath");
	Key<String> spectraTableNameKey = Key.of("spectraTableName");
	Key<String> cubeTableNameKey = Key.of("cubeTableName");

	@Override
	public Metadata store()
	{
		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
		write(rootPathKey, rootPath, configMetadata);
		write(spectraTableNameKey, spectraTableName, configMetadata);
		write(cubeTableNameKey, cubeTableName, configMetadata);
		return configMetadata;
	}

	@Override
	public void retrieve(Metadata source)
	{
		rootPath = read(rootPathKey, source);
		spectraTableName = read(spectraTableNameKey, source);
		cubeTableName = read(cubeTableNameKey, source);
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

	protected HashMap<String, String> convertSearchParamsToDBArgsMap(FixedMetadata metadata)
	{
		double fromIncidence = metadata.get(DatabaseSearchMetadata.FROM_INCIDENCE);
        double toIncidence = metadata.get(DatabaseSearchMetadata.TO_INCIDENCE);
        double fromEmission = metadata.get(DatabaseSearchMetadata.FROM_EMISSION);
        double toEmission = metadata.get(DatabaseSearchMetadata.TO_EMISSION);
        String searchString = metadata.get(DatabaseSearchMetadata.SEARCH_STRING);
        double fromPhase = metadata.get(DatabaseSearchMetadata.FROM_PHASE);
        double toPhase = metadata.get(DatabaseSearchMetadata.TO_PHASE);
        double startDistance = metadata.get(DatabaseSearchMetadata.FROM_DISTANCE);
        double stopDistance = metadata.get(DatabaseSearchMetadata.TO_DISTANCE);
        DateTime startDate = new DateTime(metadata.get(DatabaseSearchMetadata.START_DATE));
        DateTime stopDate = new DateTime(metadata.get(DatabaseSearchMetadata.STOP_DATE));
       
        
        double minIncidence = Math.min(fromIncidence, toIncidence);
        double maxIncidence = Math.max(fromIncidence, toIncidence);
        double minEmission = Math.min(fromEmission, toEmission);
        double maxEmission = Math.max(fromEmission, toEmission);
        double minPhase = Math.min(fromPhase, toPhase);
        double maxPhase = Math.max(fromPhase, toPhase);
        double minScDistance = Math.min(startDistance, stopDistance);
        double maxScDistance = Math.max(startDistance, stopDistance);
        
        HashMap<String, String> args = new HashMap<>();
        args.put(STARTDATE, String.valueOf(startDate.getMillis()));
        args.put(STOPDATE, String.valueOf(stopDate.getMillis()));
        args.put(MINSCDISTANCE, String.valueOf(minScDistance));
        args.put(MAXSCDISTANCE, String.valueOf(maxScDistance));
        args.put(MININCIDENCE, String.valueOf(minIncidence));
        args.put(MAXINCIDENCE, String.valueOf(maxIncidence));
        args.put(MINEMISSION, String.valueOf(minEmission));
        args.put(MAXEMISSION, String.valueOf(maxEmission));
        args.put(MINPHASE, String.valueOf(minPhase));
        args.put(MAXPHASE, String.valueOf(maxPhase));
        args.put(SPECTRATABLENAME, spectraTableName);
        args.put(CUBETABLENAME, cubeTableName);
        
        return args;
	}

}