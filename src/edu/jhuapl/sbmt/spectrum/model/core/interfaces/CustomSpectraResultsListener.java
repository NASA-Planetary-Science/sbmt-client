package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;

public interface CustomSpectraResultsListener
{
	/**
	 * Signifies an initial load of results to display
	 * @param results
	 */
	public void resultsLoaded(List<CustomSpectrumKeyInterface> results);

    /**
     * Signifies a change in results to display in the custom spectra table
     * @param results
     */
    public void resultsChanged(List<CustomSpectrumKeyInterface> results);

//    /**
//     * Signifies a change in results to display in the custom spectra table
//     * @param results
//     */
//    public void resultsDeleted(List<CustomSpectrumKeyInterface> results);

    /**
     * Signfies a change in the count of results to display in the custom spectra table
     * @param count
     */
    public void resultsCountChanged(int count);
}