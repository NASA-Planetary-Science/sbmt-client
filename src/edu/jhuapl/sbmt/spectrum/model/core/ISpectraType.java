package edu.jhuapl.sbmt.spectrum.model.core;

import edu.jhuapl.sbmt.query.QueryBase;

public interface ISpectraType
{
    public QueryBase getQueryBase();

    public SpectrumMath getSpectrumMath();

    public String getDisplayName();

    public Double[] getBandCenters();

    public String getBandCenterUnit();
}
