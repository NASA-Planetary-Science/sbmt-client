package edu.jhuapl.sbmt.client;

import java.io.IOException;
import java.util.HashMap;

import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumBuilder;

public class SbmtSpectrumModelFactory
{

	static HashMap<String, SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>> registeredModels
		= new HashMap<String, SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>>();


	static public void registerModel(String uniqueName, SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> builder)
	{
		registeredModels.put(uniqueName, builder);
	}

	static public Spectrum createSpectrum(
			String path,
            ISmallBodyModel smallBodyModel,
            ISpectralInstrument instrument) throws IOException
    {
		SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrum(path, smallBodyModel, instrument);

    }

}
