package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

/**
 * Defines IO interfaces for the instrument metadata
 * @author steelrj1
 *
 * @param <S>
 */
public interface InstrumentMetadataIO<S extends SearchSpec>
{
    /**
     * Returns the instrument metadata for the given <pre>instrumentName</pre>
     * @param instrumentName
     * @return
     */
    InstrumentMetadata<S> getInstrumentMetadata(String instrumentName);

    /**
     * Reads the metadata heirarchy for the given <pre>instrumentName</pre>
     * @param instrumentName
     */
    void readHierarchyForInstrument(String instrumentName);

}
