package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.util.List;

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
    public void setSpectrumRawResults(List<S> spectrumRawResults);
}
