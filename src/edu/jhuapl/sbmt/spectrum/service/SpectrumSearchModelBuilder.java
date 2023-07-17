package edu.jhuapl.sbmt.spectrum.service;

import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumSearchModel;

@FunctionalInterface
public interface SpectrumSearchModelBuilder
{
	ISpectrumSearchModel buildSearchModel(double diagonalLength);
}
