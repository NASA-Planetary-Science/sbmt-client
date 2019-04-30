package edu.jhuapl.sbmt.gui.spectrum.model;

import java.util.List;

import edu.jhuapl.sbmt.model.spectrum.CustomSpectrumKeyInterface;

public interface CustomSpectraResultsListener
{
    public void resultsChanged(List<CustomSpectrumKeyInterface> results);

    public void resultsCountChanged(int count);
}