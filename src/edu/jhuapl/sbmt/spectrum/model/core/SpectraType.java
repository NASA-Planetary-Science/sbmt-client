package edu.jhuapl.sbmt.spectrum.model.core;

import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectraType;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

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
}
