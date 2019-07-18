package edu.jhuapl.sbmt.spectrum.model.core;

import java.io.IOException;

import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.query.IQueryBase;

import crucible.crust.metadata.api.Metadata;

public interface ISpectralInstrument //extends StorableAsMetadata<ISpectralInstrument>
{
	public Metadata store();
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
