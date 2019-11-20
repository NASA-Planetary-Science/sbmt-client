package edu.jhuapl.sbmt.spectrum.model.core;

import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectraType;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

/**
 * Represents a set of information related to a given spectrum instrument defined by <pre>displayName</pre>
 * @author steelrj1
 *
 */
public class SpectraType implements ISpectraType
{
    private QueryBase queryBase;
    private SpectrumMath spectrumMath;
    private String displayName;
    private Double[] bandCenters;
    private String bandCenterUnit;

    public SpectraType(String displayName, QueryBase queryBase, SpectrumMath spectrumMath, String bandCenterUnit, Double[] bandCenters)
    {
        this.displayName = displayName;
        this.queryBase = queryBase;
        this.spectrumMath = spectrumMath;
        this.bandCenterUnit = bandCenterUnit;
        this.bandCenters = bandCenters;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
    	this.displayName = displayName;
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
	public String toString()
	{
		return displayName;
	}
}
