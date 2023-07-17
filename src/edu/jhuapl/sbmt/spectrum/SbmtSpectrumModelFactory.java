package edu.jhuapl.sbmt.spectrum;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import edu.jhuapl.sbmt.core.body.ISmallBodyModel;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumBuilder;

public class SbmtSpectrumModelFactory
{

	static HashMap<String, SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>> registeredModels
		= new HashMap<String, SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>>();
//	static HashMap<String, SpectrumInstrumentConfig> registeredSpectrumConfigs
//		= new HashMap<String, SpectrumInstrumentConfig>();
//	static HashMap<String, Double> registeredBoundingBoxDiagonals
//		= new HashMap<String, Double>();


	static public <S extends BasicSpectrum> void registerModel(String uniqueName, SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder)
	{
		registeredModels.put(uniqueName, builder);
//		registeredSpectrumConfigs.put(uniqueName, specConfig);
//		registeredBoundingBoxDiagonals.put(uniqueName, boundingBoxDiagonalLength);
	}

	static public BasicSpectrum createSpectrum(
			List<String> result, ISmallBodyModel smallBodyModel,
			BasicSpectrumInstrument instrument, SpectrumInstrumentConfig spectrumConfig) throws IOException
    {
//		double boundingBoxDiagonalLength = registeredBoundingBoxDiagonals.get(instrument.getDisplayName());
//		SpectrumInstrumentConfig specConfig = registeredSpectrumConfigs.get(instrument.getDisplayName());
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrum(result, smallBodyModel, instrument, spectrumConfig);

    }

	static public BasicSpectrum createSpectrum(
			List<String> result, ISmallBodyModel smallBodyModel,
			BasicSpectrumInstrument instrument, String timeString, SpectrumInstrumentConfig spectrumConfig) throws IOException
    {
//		double boundingBoxDiagonalLength = registeredBoundingBoxDiagonals.get(instrument.getDisplayName());
//		SpectrumInstrumentConfig specConfig = registeredSpectrumConfigs.get(instrument.getDisplayName());
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrum(result, smallBodyModel, instrument, timeString, spectrumConfig);

    }

	static public <S extends BasicSpectrum> IBasicSpectrumRenderer<S> createSpectrumRenderer(
			String path, ISmallBodyModel smallBodyModel,
			BasicSpectrumInstrument instrument,
			boolean headless, SpectrumInstrumentConfig spectrumConfig) throws IOException
    {
//		double boundingBoxDiagonalLength = registeredBoundingBoxDiagonals.get(instrument.getDisplayName());
//		SpectrumInstrumentConfig specConfig = registeredSpectrumConfigs.get(instrument.getDisplayName());
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrumRenderer(path, smallBodyModel, instrument, headless, spectrumConfig);

    }

	static public <S extends BasicSpectrum> IBasicSpectrumRenderer<S> createSpectrumRenderer(
			BasicSpectrum spec, ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument, boolean headless) throws IOException
    {
//		double boundingBoxDiagonalLength = registeredBoundingBoxDiagonals.get(instrument.getDisplayName());
//		SpectrumInstrumentConfig specConfig = registeredSpectrumConfigs.get(instrument.getDisplayName());
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrumRenderer(spec, smallBodyModel, headless);

    }

}