package edu.jhuapl.sbmt.client;

import java.io.IOException;
import java.util.HashMap;

import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumBuilder;
import edu.jhuapl.sbmt.spectrum.model.rendering.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;

public class SbmtSpectrumModelFactory
{

	static HashMap<String, SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>> registeredModels
		= new HashMap<String, SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>>();
	static HashMap<String, ISmallBodyModel> registeredSmallBodyModels
	= new HashMap<String, ISmallBodyModel>();


	static public void registerModel(String uniqueName, SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> builder, ISmallBodyModel smallBodyModel)
	{
		registeredModels.put(uniqueName, builder);
		registeredSmallBodyModels.put(uniqueName, smallBodyModel);
	}

	static public IBasicSpectrumRenderer createSpectrumRenderer(
			String path,
            ISpectralInstrument instrument) throws IOException
    {
		SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrum(path, registeredSmallBodyModels.get(instrument.getDisplayName()), instrument);

    }

}
