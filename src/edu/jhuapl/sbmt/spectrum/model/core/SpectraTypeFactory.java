package edu.jhuapl.sbmt.spectrum.model.core;

import java.util.HashMap;

import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.math.SpectrumMath;

public class SpectraTypeFactory
{
	static HashMap<String, SpectraType> registeredModels = new HashMap<String, SpectraType>();


	public static void registerSpectraType(String displayName, QueryBase queryBase, SpectrumMath spectrumMath, String bandCenterUnit, Double[] bandCenters)
	{
		registeredModels.put(displayName, new SpectraType(displayName, queryBase, spectrumMath, bandCenterUnit, bandCenters));
	}

	public static SpectraType[] values()
	{
		SpectraType[] types = new SpectraType[registeredModels.keySet().size()];
		registeredModels.values().toArray(types);
		return types;
	}

	public static SpectraType findSpectraTypeForDisplayName(String displayName)
    {
		return registeredModels.get(displayName);
    }
}
