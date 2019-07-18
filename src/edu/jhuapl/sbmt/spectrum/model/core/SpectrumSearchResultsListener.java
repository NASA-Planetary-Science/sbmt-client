package edu.jhuapl.sbmt.spectrum.model.core;

import java.util.List;

public interface SpectrumSearchResultsListener
{
    public void resultsChanged(List<List<String>> results);

    public void resultsCountChanged(int count);
}
