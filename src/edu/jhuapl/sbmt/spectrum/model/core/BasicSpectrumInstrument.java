package edu.jhuapl.sbmt.spectrum.model.core;

import java.io.IOException;

import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

public class BasicSpectrumInstrument implements ISpectralInstrument
{
	protected String bandCenterUnit;
    protected String displayName;
    protected QueryBase queryBase;
    protected SpectrumMath spectrumMath;
    protected Double[] bandCenters;

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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicSpectrumInstrument other = (BasicSpectrumInstrument) obj;
		if (displayName == null)
		{
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		return true;
	}

}
