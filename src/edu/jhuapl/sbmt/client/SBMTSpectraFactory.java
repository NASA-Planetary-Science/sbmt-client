package edu.jhuapl.sbmt.client;

import java.io.IOException;

import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESQuery;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrum;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrumMath;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSQuery;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrumMath;
import edu.jhuapl.sbmt.model.eros.NIS;
import edu.jhuapl.sbmt.model.eros.NISSpectrum;
import edu.jhuapl.sbmt.model.eros.NISSpectrumMath;
import edu.jhuapl.sbmt.model.eros.NisQuery;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3Query;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SpectrumMath;
import edu.jhuapl.sbmt.model.ryugu.nirs3.atRyugu.NIRS3Spectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumBuilder;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.spectrum.rendering.AdvancedSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class SBMTSpectraFactory
{
	//TODO:  This should really be split out and have elements in the individual instrument packages, not in a centralized place like this
	public static void initializeModels(ISmallBodyModel smallBodyModel)
	{
		SpectraTypeFactory.registerSpectraType("OTES", OTESQuery.getInstance(), OTESSpectrumMath.getInstance(), "cm^-1", new OTES().getBandCenters());
		SpectraTypeFactory.registerSpectraType("OVIRS", OVIRSQuery.getInstance(), OVIRSSpectrumMath.getInstance(), "um", new OVIRS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIS", NisQuery.getInstance(), NISSpectrumMath.getSpectrumMath(), "cm^-1", new NIS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIRS3", NIRS3Query.getInstance(), NIRS3SpectrumMath.getInstance(), "cm^-1", new NIRS3().getBandCenters());

		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> nisSpectra = new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
		{

			@Override
			public BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel,
					BasicSpectrumInstrument instrument) throws IOException
			{
				NISSpectrum spectrum = new NISSpectrum(path, smallBodyModel, instrument);
				return spectrum;
			}

			@Override
			public IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument) throws IOException
			{
				NISSpectrum spectrum = new NISSpectrum(path, smallBodyModel, instrument);
				return new BasicSpectrumRenderer(spectrum, smallBodyModel, false);
			}


		};
//		nisSpectra.setSmallBodyModel(smallBodyModel);
		SbmtSpectrumModelFactory.registerModel("NIS", nisSpectra, smallBodyModel);

		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> otesSpectra = new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
		{

			@Override
			public BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel,
					BasicSpectrumInstrument instrument) throws IOException
			{
				OTESSpectrum spectrum = new OTESSpectrum(path, (SpectrumInstrumentMetadataIO)smallBodyModel.getSmallBodyConfig().getHierarchicalSpectraSearchSpecification(), smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
				return spectrum;
			}

			@Override
			public IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument) throws IOException
			{
				OTESSpectrum spectrum = new OTESSpectrum(path, (SpectrumInstrumentMetadataIO)smallBodyModel.getSmallBodyConfig().getHierarchicalSpectraSearchSpecification(), smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
				return new AdvancedSpectrumRenderer(spectrum, smallBodyModel, false);
			}
		};
		SbmtSpectrumModelFactory.registerModel("OTES", otesSpectra, smallBodyModel);

		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> ovirsSpectra = new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
		{

			@Override
			public BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel,
					BasicSpectrumInstrument instrument) throws IOException
			{
				OVIRSSpectrum spectrum = new OVIRSSpectrum(path, smallBodyModel.getSmallBodyConfig().getHierarchicalSpectraSearchSpecification(), smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
				return spectrum;
			}

			@Override
			public IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument) throws IOException
			{
				OVIRSSpectrum spectrum = new OVIRSSpectrum(path, smallBodyModel.getSmallBodyConfig().getHierarchicalSpectraSearchSpecification(), smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
				return new AdvancedSpectrumRenderer(spectrum, smallBodyModel, false);
			}
		};
		SbmtSpectrumModelFactory.registerModel("OVIRS", ovirsSpectra, smallBodyModel);

		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> nirs3Spectra = new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
		{

			@Override
			public BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel,
					BasicSpectrumInstrument instrument) throws IOException
			{
				NIRS3Spectrum spectrum = new NIRS3Spectrum(path, smallBodyModel, instrument);
				return spectrum;
			}

			@Override
			public IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument) throws IOException
			{
				NIRS3Spectrum spectrum = new NIRS3Spectrum(path, smallBodyModel, instrument);
				return new AdvancedSpectrumRenderer(spectrum, smallBodyModel, false);
			}
		};
		SbmtSpectrumModelFactory.registerModel("NIRS3", nirs3Spectra, smallBodyModel);
	}
}
