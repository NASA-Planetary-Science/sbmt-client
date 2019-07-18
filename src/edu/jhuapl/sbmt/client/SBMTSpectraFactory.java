package edu.jhuapl.sbmt.client;

import java.io.IOException;

import edu.jhuapl.sbmt.model.bennu.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.otes.OTESQuery;
import edu.jhuapl.sbmt.model.bennu.otes.OTESSpectrum;
import edu.jhuapl.sbmt.model.bennu.otes.OTESSpectrumMath;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSQuery;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSSpectrumMath;
import edu.jhuapl.sbmt.model.eros.NIS;
import edu.jhuapl.sbmt.model.eros.NISSpectrum;
import edu.jhuapl.sbmt.model.eros.NISSpectrumMath;
import edu.jhuapl.sbmt.model.eros.NisQuery;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3Query;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SpectrumMath;
import edu.jhuapl.sbmt.model.ryugu.nirs3.atRyugu.NIRS3Spectrum;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumBuilder;

public class SBMTSpectraFactory
{	
	public static void initializeModels()
	{
		SpectraTypeFactory.registerSpectraType("OTES", OTESQuery.getInstance(), OTESSpectrumMath.getInstance(), "cm^-1", new OTES().getBandCenters());
		SpectraTypeFactory.registerSpectraType("OVIRS", OVIRSQuery.getInstance(), OVIRSSpectrumMath.getInstance(), "um", new OVIRS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIS", NisQuery.getInstance(), NISSpectrumMath.getSpectrumMath(), "cm^-1", new NIS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIRS3", NIRS3Query.getInstance(), NIRS3SpectrumMath.getInstance(), "cm^-1", new NIRS3().getBandCenters());
		
		SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> nisSpectra = new SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>()
		{

			@Override
			public Spectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException
			{
				return new NISSpectrum(path, smallBodyModel, instrument);
			}
		};
		SbmtSpectrumModelFactory.registerModel("NIS", nisSpectra);
		
		SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> otesSpectra = new SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>()
		{

			@Override
			public Spectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException
			{
				return new OTESSpectrum(path, smallBodyModel, instrument);
			}
		};
		SbmtSpectrumModelFactory.registerModel("OTES", otesSpectra);
		
		SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> ovirsSpectra = new SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>()
		{

			@Override
			public Spectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException
			{
				return new OVIRSSpectrum(path, smallBodyModel, instrument);
			}
		};
		SbmtSpectrumModelFactory.registerModel("OVIRS", ovirsSpectra);
		
		SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> nirs3Spectra = new SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>()
		{

			@Override
			public Spectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException
			{
				return new NIRS3Spectrum(path, smallBodyModel, instrument);
			}
		};
		SbmtSpectrumModelFactory.registerModel("NIRS3", nirs3Spectra);
	}
}
