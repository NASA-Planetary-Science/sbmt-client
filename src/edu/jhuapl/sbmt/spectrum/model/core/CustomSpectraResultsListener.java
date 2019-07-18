package edu.jhuapl.sbmt.spectrum.model.core;

import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKeyInterface;

public interface CustomSpectraResultsListener
{
    public void resultsChanged(List<CustomSpectrumKeyInterface> results);

    public void resultsCountChanged(int count);
}