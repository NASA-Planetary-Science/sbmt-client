package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

public interface InstrumentMetadataIO<S extends SearchSpec>
{

    InstrumentMetadata<S> getInstrumentMetadata(
            String instrumentName);

    void readHierarchyForInstrument(String instrumentName);

//    void loadMetadata() throws FileNotFoundException;

}
