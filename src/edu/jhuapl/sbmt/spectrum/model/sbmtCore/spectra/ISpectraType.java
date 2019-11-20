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
    /**
     * Returns the QueryBase object for this spectrum type
     * @return
     */
    public QueryBase getQueryBase();

    /**
     * Returns the SpectrumMatch object for this spectrum type
     * @return
     */
    public SpectrumMath getSpectrumMath();

    /**
     * Returns the display name for this spectrum type
     * @return
     */
    public String getDisplayName();

    public void setDisplayName(String displayName);

    /**
     * Returns the band centers for this spectrum type
     * @return
     */
    public Double[] getBandCenters();

    /**
     * Returns the band center unit of this spectrum type
     * @return
     */
    public String getBandCenterUnit();
}
