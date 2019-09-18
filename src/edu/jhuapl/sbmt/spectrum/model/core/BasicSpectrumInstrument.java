package edu.jhuapl.sbmt.spectrum.model.core;

import java.io.IOException;

import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

public class BasicSpectrumInstrument implements ISpectralInstrument //, StorableAsMetadata<BasicSpectrumInstrument>
{
	protected String bandCenterUnit;
    protected String displayName;
    protected QueryBase queryBase;
    protected SpectrumMath spectrumMath;
    protected Double[] bandCenters;

//    //metadata interface
//    private static final Key<BasicSpectrumInstrument> BASIC_SPECTRUM_INSTRUMENT_KEY = Key.of("basicSpectrumInstrument");
//    private static final Key<String> spectraNameKey = Key.of("displayName");
//    private static final Key<QueryBase> queryBaseKey = Key.of("queryBase");
//    private static final Key<SpectrumMath> spectrumMathKey = Key.of("spectrumMath");
//    private static final Key<Double[]> bandCentersKey = Key.of("bandCenters");
//    private static final Key<String> bandCenterUnitKey = Key.of("bandCenterUnit");

    public BasicSpectrumInstrument(String displayName)
    {
    	this.displayName = displayName;
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

//    public static void initializeSerializationProxy()
//	{
//		InstanceGetter.defaultInstanceGetter().register(BASIC_SPECTRUM_INSTRUMENT_KEY, (metadata) -> {
//
//			BasicSpectrumInstrument inst = null;
//			String displayName = metadata.get(spectraNameKey);
//			SpectraType spectraType = SpectraTypeFactory.findSpectraTypeForDisplayName(displayName);
//
//			QueryBase queryBase = spectraType.getQueryBase();
//			SpectrumMath spectrumMath = spectraType.getSpectrumMath();
//			Double[] bandCenters = spectraType.getBandCenters();
//			String bandCenterUnit = spectraType.getBandCenterUnit();
//
////		        QueryBase queryBase = metadata.get(queryBaseKey);
////		        SpectrumMath spectrumMath = metadata.get(spectrumMathKey);
////		        Double[] bandCenters = metadata.get(bandCentersKey);
////		        String bandCenterUnit = metadata.get(bandCenterUnitKey);
//	        inst = new BasicSpectrumInstrument(bandCenterUnit, displayName, queryBase, spectrumMath);
//			inst.bandCenters = bandCenters;
//
//			return inst;
//		},
//	    BasicSpectrumInstrument.class,
//	    key -> {
//			 SettableMetadata metadata = SettableMetadata.of(Version.of(1, 0));
//			 metadata.put(spectraNameKey, key.getDisplayName());
////			 metadata.put(queryBaseKey, key.getQueryBase());
////			 metadata.put(spectrumMathKey, key.getSpectrumMath());
////			 metadata.put(bandCenterUnitKey, key.getBandCenterUnit());
//			 return metadata;
//		});
//	}

	@Override
	public Spectrum getSpectrumInstance(String filename, ISmallBodyModel smallBodyModel) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public double[] getRGBMaxVals()
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int[] getRGBDefaultIndices()
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String[] getDataTypeNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
