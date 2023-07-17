package edu.jhuapl.sbmt.spectrum.service;

import java.util.HashMap;

import edu.jhuapl.sbmt.core.body.ISmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumSearchModel;

public class SBMTSpectraFactory
{
	static HashMap<String, SpectrumSearchModelBuilder> registeredModels = new HashMap<String, SpectrumSearchModelBuilder>();

	static public void registerModel(String uniqueName, SpectrumSearchModelBuilder builder)
	{
		registeredModels.put(uniqueName, builder);
	}
	
//	static public ISpectrumSearchModel getModelForName(String key)
//	{
//		return registeredModels.get(key);
//	}
	
	static public ISpectrumSearchModel getModelFor(String key, double bodyDiagonalLength)
	{
		SpectrumSearchModelBuilder builder = registeredModels.get(key);
		ISpectrumSearchModel model = builder.buildSearchModel(bodyDiagonalLength);
		return model;
		
	}

	// TODO: This should really be split out and have elements in the individual
	// instrument packages, not in a centralized place like this
	public static void initializeModels(ISmallBodyModel smallBodyModel)
	{

//		SpectraTypeFactory.registerSpectraType("OTES", OTESQuery.getInstance(), OTESSpectrumMath.getInstance(), "cm^-1", new OTES().getBandCenters());
//		SpectraTypeFactory.registerSpectraType("OVIRS", OVIRSQuery.getInstance(), OVIRSSpectrumMath.getInstance(), "um", new OVIRS().getBandCenters());
//		SpectraTypeFactory.registerSpectraType("NIS", NisQuery.getInstance(), NISSpectrumMath.getSpectrumMath(), "cm^-1", new NIS().getBandCenters());
//		SpectraTypeFactory.registerSpectraType("NIRS3", NIRS3Query.getInstance(), NIRS3SpectrumMath.getInstance(), "cm^-1", new NIRS3().getBandCenters());
//		SpectraTypeFactory.registerSpectraType("MEGANE", MEGANEQuery.getInstance(), MEGANESpectrumMath.getInstance(), "cm^-1", new MEGANE().getBandCenters());
	}
}
