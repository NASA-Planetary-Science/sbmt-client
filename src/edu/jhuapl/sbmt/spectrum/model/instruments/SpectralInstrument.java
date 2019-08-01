package edu.jhuapl.sbmt.spectrum.model.instruments;

import java.io.IOException;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

import crucible.crust.metadata.api.MetadataManager;

public interface SpectralInstrument extends MetadataManager
{
    public double[] getBandCenters();
    public String getBandCenterUnit();
    public String getDisplayName();
    public QueryBase getQueryBase();
    public SpectrumMath getSpectrumMath();
    public Spectrum getSpectrumInstance(String filename, SmallBodyModel smallBodyModel) throws IOException;

}
