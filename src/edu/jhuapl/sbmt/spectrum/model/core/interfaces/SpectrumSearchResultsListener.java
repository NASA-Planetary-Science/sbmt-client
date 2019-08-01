package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

public interface SpectrumSearchResultsListener
{
    public void resultsChanged(List<BasicSpectrum> results);

    public void resultsCountChanged(int count);

    public void resultsRemoved();
}
