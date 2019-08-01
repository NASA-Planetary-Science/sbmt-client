package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;

public interface CustomSpectraResultsListener
{
    public void resultsChanged(List<CustomSpectrumKeyInterface> results);

    public void resultsCountChanged(int count);
}