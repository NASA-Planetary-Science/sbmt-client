package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;


public interface ISpectrumSearchModel
{
    public void setSpectrumRawResults(List<BasicSpectrum> spectrumRawResults);

//    public String createSpectrumName(int index);

    public void populateSpectrumMetadata(String line);

}
