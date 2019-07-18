package edu.jhuapl.sbmt.spectrum.model.core;

import edu.jhuapl.sbmt.query.QueryBase;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public abstract class BasicSpectrumInstrument implements ISpectralInstrument, MetadataManager
{
	protected String bandCenterUnit;
    protected String displayName;
    protected QueryBase queryBase;
    protected SpectrumMath spectrumMath;
    protected Double[] bandCenters;

    public BasicSpectrumInstrument()
    {

    }

    public BasicSpectrumInstrument(String bandCenterUnit, String displayName,
            QueryBase queryBase, SpectrumMath spectrumMath)
    {
        super();
        this.bandCenterUnit = bandCenterUnit;
        this.displayName = displayName;
        this.queryBase = queryBase;
        this.spectrumMath = spectrumMath;
    }

    @Override
    public Double[] getBandCenters()
    {
        return bandCenters;
    }

    @Override
    public String getBandCenterUnit()
    {
        return bandCenterUnit;
    }

    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public QueryBase getQueryBase()
    {
        return queryBase;
    }

    @Override
    public SpectrumMath getSpectrumMath()
    {
        return spectrumMath;
    }

    //metadata interface
    Key<String> spectraNameKey = Key.of("displayName");
    Key<QueryBase> queryBaseKey = Key.of("queryBase");
    Key<SpectrumMath> spectrumMathKey = Key.of("spectrumMath");
    Key<Double[]> bandCentersKey = Key.of("bandCenters");
    Key<String> bandCenterUnitKey = Key.of("bandCenterUnit");

    @Override
    public void retrieve(Metadata source)
    {
        displayName = read(spectraNameKey, source);
        this.queryBase = read(queryBaseKey, source);
        this.spectrumMath = read(spectrumMathKey, source);
        this.bandCenters = read(bandCentersKey, source);
        this.bandCenterUnit = read(bandCenterUnitKey, source);
    }

    @Override
    public Metadata store()
    {
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        write(spectraNameKey, displayName, configMetadata);
        write(queryBaseKey, queryBase, configMetadata);
        write(spectrumMathKey, spectrumMath, configMetadata);
        write(bandCenterUnitKey, bandCenterUnit, configMetadata);
        return configMetadata;
    }

    private <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }

    private <T> T read(Key<T> key, Metadata configMetadata)
    {
        T value = configMetadata.get(key);
        if (value != null)
            return value;
        return null;
    }

}
