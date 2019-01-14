package edu.jhuapl.sbmt.gui.spectrum.model;

import java.util.List;


public interface ISpectrumSearchModel
{
    public void setSpectrumRawResults(List<List<String>> spectrumRawResults);

    public String createSpectrumName(int index);

    public void populateSpectrumMetadata(String line);

}
