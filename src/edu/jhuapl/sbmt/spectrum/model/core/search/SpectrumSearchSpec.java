package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.util.Hashtable;

import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Container for spectrum search metadata, describing where data can be found on the server, as well as providing some metadata about the data itself
 * @author steelrj1
 *
 */
public class SpectrumSearchSpec extends Hashtable<String, String> implements SearchSpec
{
    String dataName;
    String dataRootLocation;
    String dataPath;
    String dataListFilename;
    String source;
    String xAxisUnits;
    String yAxisUnits;
    String dataDescription;

    public SpectrumSearchSpec()
    {

    }

    public SpectrumSearchSpec(String name, String location, String dataPath, String filename, ImageSource source, String xAxisUnits, String yAxisUnits, String dataDescription)
    {
        put("dataName", dataName = name);
        put("dataRootLocation", dataRootLocation = location);
        put("dataPath", this.dataPath = dataPath);
        put("dataListFilename", this.dataListFilename = filename);
        put("source", this.source = source.toString());
        put("xAxisUnits", this.xAxisUnits = xAxisUnits);
        put("yAxisUnits", this.yAxisUnits = yAxisUnits);
        put("dataDescription", this.dataDescription = dataDescription);
    }

    public SpectrumSearchSpec(Hashtable<String, String> copy)
    {
        putAll(copy);
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getDataName()
     */
    @Override
    public String getDataName()
    {
        return get("dataName");
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getDataRootLocation()
     */
    @Override
    public String getDataRootLocation()
    {
        return get("dataRootLocation");
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getDataPath()
     */
    @Override
    public String getDataPath()
    {
        return get("dataPath");
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getDataListFilename()
     */
    @Override
    public String getDataListFilename()
    {
        return get("dataListFilename");
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getSource()
     */
    @Override
    public ImageSource getSource()
    {
        return ImageSource.valueFor(get("source"));
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getxAxisUnits()
     */
    @Override
    public String getxAxisUnits()
    {
        return get("xAxisUnits");
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getyAxisUnits()
     */
    @Override
    public String getyAxisUnits()
    {
        return get("yAxisUnits");
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.SearchSpec#getDataDescription()
     */
    @Override
    public String getDataDescription()
    {
        return get("dataDescription");
    }

    //Metadata
    private static final Key<SpectrumSearchSpec> SPECTRUMSEARCHSPEC_KEY = Key.of("SpectrumSearchSpec");
	private static final Key<String> DATANAME_KEY = Key.of("dataName");
	private static final Key<String> DATAROOTLOCATION_KEY = Key.of("dataRootLocation");
	private static final Key<String> DATAPATH_KEY = Key.of("dataPath");
	private static final Key<String> DATALISTFILENAME_KEY = Key.of("dataListFilename");
	private static final Key<String> SOURCE_KEY = Key.of("source");
	private static final Key<String> XAXISUNITS_KEY = Key.of("xAxisUnits");
	private static final Key<String> YAXISUNITS_KEY = Key.of("yAxisUnits");
	private static final Key<String> DATADESCRIPTION_KEY = Key.of("dataDescription");

    public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(SPECTRUMSEARCHSPEC_KEY, (metadata) -> {
    		String dataName = metadata.get(DATANAME_KEY);
    		String dataRootLocation = metadata.get(DATAROOTLOCATION_KEY);
    		String dataPath = metadata.get(DATAPATH_KEY);
    		String dataListFilename = metadata.get(DATALISTFILENAME_KEY);
    		String source = metadata.get(SOURCE_KEY);
    		String xAxisUnits = metadata.get(XAXISUNITS_KEY);
    		String yAxisUnits = metadata.get(YAXISUNITS_KEY);
    		String dataDescription = metadata.get(DATADESCRIPTION_KEY);

    		SpectrumSearchSpec spec = new SpectrumSearchSpec(dataName, dataRootLocation, dataPath, dataListFilename, ImageSource.valueFor(source), xAxisUnits, yAxisUnits, dataDescription);
    		return spec;

    	}, SpectrumSearchSpec.class, spec -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(DATANAME_KEY, spec.dataName);
    		result.put(DATAROOTLOCATION_KEY, spec.dataRootLocation);
    		result.put(DATAPATH_KEY, spec.dataPath);
    		result.put(DATALISTFILENAME_KEY, spec.dataListFilename);
    		result.put(SOURCE_KEY, spec.source);
    		result.put(XAXISUNITS_KEY, spec.xAxisUnits);
    		result.put(YAXISUNITS_KEY, spec.yAxisUnits);
    		result.put(DATADESCRIPTION_KEY, spec.dataDescription);
    		return result;
    	});

	}
}