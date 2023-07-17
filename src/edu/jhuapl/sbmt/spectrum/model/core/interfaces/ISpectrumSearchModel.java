package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.query.v2.FetchedResults;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;


/**
 * Interface that defines a search model for spectra
 * @author steelrj1
 *
 */
public interface ISpectrumSearchModel<S extends BasicSpectrum>
{

    /**
     * Sets the list of spectrum results from a search
     * @param spectrumRawResults
     */
    public void setSpectrumRawResults(FetchedResults spectrumRawResults);
}
