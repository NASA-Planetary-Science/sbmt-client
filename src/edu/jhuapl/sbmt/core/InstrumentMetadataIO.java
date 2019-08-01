package edu.jhuapl.sbmt.core;

import java.io.FileNotFoundException;

import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;

public interface InstrumentMetadataIO<S extends SearchSpec>
{

    InstrumentMetadata<S> getInstrumentMetadata(
            String instrumentName);

    void readHierarchyForInstrument(String instrumentName);

    void loadMetadata() throws FileNotFoundException;

}
