package edu.jhuapl.sbmt.spectrum.model.core;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.interfaces.InstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Class to associate instruments with search metadata found in a SearchSpec based object
 * @author steelrj1
 *
 * @param <S>
 */
public class SpectrumInstrumentMetadata<S extends SearchSpec> implements InstrumentMetadata<S>
{
    String instrumentName;
    String queryType;
    List<S> searchMetadata = new ArrayList<S>();

    public SpectrumInstrumentMetadata()
    {

    }

    public SpectrumInstrumentMetadata(String instName)
    {
        this.instrumentName = instName;
    }

    public SpectrumInstrumentMetadata(String instName, List<S> specs)
    {
        this.instrumentName = instName;
        this.searchMetadata = specs;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#setSpecs(java.util.ArrayList)
     */
    @Override
    public void setSpecs(ArrayList<S> specs)
    {
        this.searchMetadata = specs;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#getSpecs()
     */
    @Override
    public List<S> getSpecs()
    {
        return searchMetadata;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#addSearchSpecs(java.util.List)
     */
    @Override
    public void addSearchSpecs(List<S> specs)
    {
        this.searchMetadata.addAll(specs);
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#addSearchSpec(S)
     */
    @Override
    public void addSearchSpec(S spec)
    {
        searchMetadata.add(spec);
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#getInstrumentName()
     */
    @Override
    public String getInstrumentName()
    {
        return instrumentName;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#setInstrumentName(java.lang.String)
     */
    @Override
    public void setInstrumentName(String instrumentName)
    {
        this.instrumentName = instrumentName;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#getQueryType()
     */
    @Override
    public String getQueryType()
    {
        return queryType;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#setQueryType(java.lang.String)
     */
    @Override
    public void setQueryType(String queryType)
    {
        this.queryType = queryType;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadata#toString()
     */
    @Override
    public String toString()
    {
        return "SpectrumInstrumentMetadata [instrumentName="
                + instrumentName + ", specs=" + searchMetadata + "]";
    }

    private static final Key<SpectrumInstrumentMetadata> SPECTRUMINSTRUMENTMETADATA_KEY = Key.of("spectrumInstrumentMetadata");
	private static final Key<String> INSTNAME_KEY = Key.of("instrumentName");
	private static final Key<List<SpectrumSearchSpec>> SEARCHMETADATA_KEY = Key.of("searchMetadata");

    public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(SPECTRUMINSTRUMENTMETADATA_KEY, (metadata) -> {
    		String instrumentName = metadata.get(INSTNAME_KEY);
    		List<SpectrumSearchSpec> searchMetadata = metadata.get(SEARCHMETADATA_KEY);
    		SpectrumInstrumentMetadata<SpectrumSearchSpec> spec = new SpectrumInstrumentMetadata<>(instrumentName, searchMetadata);
    		return spec;

    	}, SpectrumInstrumentMetadata.class, spec -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(INSTNAME_KEY, spec.getInstrumentName());
    		result.put(SEARCHMETADATA_KEY, spec.getSpecs());

    		return result;
    	});

	}
}