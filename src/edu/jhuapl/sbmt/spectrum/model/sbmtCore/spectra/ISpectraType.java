package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

/**
 * Interface for spectral instrument types
 * @author steelrj1
 *
 */
public interface ISpectraType
{
    public QueryBase getQueryBase();

    public SpectrumMath getSpectrumMath();

    public String getDisplayName();

    public Double[] getBandCenters();

    public String getBandCenterUnit();
}
