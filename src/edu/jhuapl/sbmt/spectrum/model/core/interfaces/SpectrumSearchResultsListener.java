package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.query.v2.FetchedResults;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Listener to watch for updates in the search results for spectra
 * @author steelrj1
 *
 */
public interface SpectrumSearchResultsListener<S extends BasicSpectrum>
{
    /**
     * Signifies that the results to be shown are the incoming <pre>results</pre> object
     * @param results
     */
    public void resultsChanged(FetchedResults results);

    /**
     * Signifies the count of the of the results shown has changed
     * @param count
     */
    public void resultsCountChanged(int count);

    /**
     * Signifies that all results were removed
     */
    public void resultsRemoved();
}
