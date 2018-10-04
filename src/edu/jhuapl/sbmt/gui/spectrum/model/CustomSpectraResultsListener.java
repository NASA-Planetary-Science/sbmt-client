package edu.jhuapl.sbmt.gui.spectrum.model;

import java.util.List;

import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog.SpectrumInfo;

public interface CustomSpectraResultsListener
{
    public void resultsChanged(List<SpectrumInfo> results);
}