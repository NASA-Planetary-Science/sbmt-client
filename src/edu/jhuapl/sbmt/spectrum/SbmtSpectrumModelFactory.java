package edu.jhuapl.sbmt.spectrum;

import java.io.IOException;
import java.util.HashMap;

import edu.jhuapl.sbmt.core.body.ISmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumBuilder;

public class SbmtSpectrumModelFactory
{

	static HashMap<String, SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>> registeredModels
		= new HashMap<String, SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>>();
	static HashMap<String, ISmallBodyModel> registeredSmallBodyModels
	= new HashMap<String, ISmallBodyModel>();


	static public <S extends BasicSpectrum> void registerModel(String uniqueName, SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder, ISmallBodyModel smallBodyModel)
	{
		registeredModels.put(uniqueName, builder);
		registeredSmallBodyModels.put(uniqueName, smallBodyModel);
	}

	static public BasicSpectrum createSpectrum(
			String path,
			BasicSpectrumInstrument instrument) throws IOException
    {
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrum(path, registeredSmallBodyModels.get(instrument.getDisplayName()), instrument);

    }

	static public BasicSpectrum createSpectrum(
			String path,
			BasicSpectrumInstrument instrument, String timeString) throws IOException
    {
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrum(path, registeredSmallBodyModels.get(instrument.getDisplayName()), instrument, timeString);

    }

	static public IBasicSpectrumRenderer createSpectrumRenderer(
			String path,
			BasicSpectrumInstrument instrument,
			boolean headless) throws IOException
    {
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrumRenderer(path, registeredSmallBodyModels.get(instrument.getDisplayName()), instrument, headless);

    }

	static public IBasicSpectrumRenderer createSpectrumRenderer(BasicSpectrum spec, BasicSpectrumInstrument instrument, boolean headless) throws IOException
    {
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> builder = registeredModels.get(instrument.getDisplayName());
    	return builder.buildSpectrumRenderer(spec, registeredSmallBodyModels.get(instrument.getDisplayName()), headless);

    }

}
