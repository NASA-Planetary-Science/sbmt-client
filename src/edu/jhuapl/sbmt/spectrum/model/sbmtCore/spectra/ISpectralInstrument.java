package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import java.io.IOException;

import edu.jhuapl.sbmt.common.client.ISmallBodyModel;
import edu.jhuapl.sbmt.query.IQueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

/**
 * Interface for spectral instruments
 * @author steelrj1
 *
 */
public interface ISpectralInstrument
{
    /**
     * Returns the band centers for this spectrum
     * @return
     */
    public Double[] getBandCenters();

    /**
     * Returns the band center unit for the spectrum
     * @return
     */
    public String getBandCenterUnit();

    /**
     * Returns the display name for the spectrum
     * @return
     */
    public String getDisplayName();

    /**
     * Returns the query base object for this spectrum
     * @return
     */
    public IQueryBase getQueryBase();

    /**
     * Returns the spectrum math object for the spectrum
     * @return
     */
    public SpectrumMath getSpectrumMath();

    /**
     * Returns the spectrum instance for this instrument
     * @param filename
     * @param smallBodyModel
     * @return
     * @throws IOException
     */
    public Spectrum getSpectrumInstance(String filename, ISmallBodyModel smallBodyModel) throws IOException;

    /**
     * Returns the RGB Max values for this spectrum type
     * @return
     */
    public double[] getRGBMaxVals();

    /**
     * Returns the RGB default indices for this spectrum type
     * @return
     */
    public int[] getRGBDefaultIndices();

    /**
     * Returns the data type names for this spectrum type
     * @return
     */
    public String [] getDataTypeNames();
}
