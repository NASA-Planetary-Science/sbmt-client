package edu.jhuapl.sbmt.client;

public class SBMTModelBootstrap
{

	public static void initialize(ISmallBodyModel smallBodyModel)
	{
//		SBMTBodyModelFactory.initializeModels();
//		SBMTImageFactory.initializeModels();
		SBMTSpectraFactory.initializeModels(smallBodyModel);
	}

}
