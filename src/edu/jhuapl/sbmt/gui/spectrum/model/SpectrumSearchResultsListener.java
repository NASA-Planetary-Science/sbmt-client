package edu.jhuapl.sbmt.gui.spectrum.model;

import java.util.List;

public interface SpectrumSearchResultsListener
{
    public void resultsChanged(List<List<String>> results);

    public void resultsCountChanged(int count);
}
