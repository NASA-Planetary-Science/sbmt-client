package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import java.io.IOException;

import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.query.IQueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

public interface ISpectralInstrument
{
    public Double[] getBandCenters();
    public String getBandCenterUnit();
    public String getDisplayName();
    public IQueryBase getQueryBase();
    public SpectrumMath getSpectrumMath();
    public Spectrum getSpectrumInstance(String filename, ISmallBodyModel smallBodyModel) throws IOException;
    public double[] getRGBMaxVals();
    public int[] getRGBDefaultIndices();
    public String [] getDataTypeNames();
}
