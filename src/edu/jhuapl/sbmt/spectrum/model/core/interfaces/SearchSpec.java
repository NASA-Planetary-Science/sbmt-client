package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.io.BufferedWriter;
import java.io.IOException;

import edu.jhuapl.sbmt.model.image.ImageSource;


public interface SearchSpec
{

    String getDataName();

    String getDataRootLocation();

    String getDataPath();

    String getDataListFilename();

    ImageSource getSource();

    String getxAxisUnits();

    String getyAxisUnits();

    String getDataDescription();

    void toFile(BufferedWriter writer) throws IOException;

    public void fromFile(String csvLine);
}
