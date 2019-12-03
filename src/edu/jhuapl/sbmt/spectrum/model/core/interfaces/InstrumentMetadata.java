package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.util.ArrayList;
import java.util.List;


/**
 * Interface to define instrument specific metadata that is useful in formulating a search
 * @author steelrj1
 *
 * @param <S>
 */
public interface InstrumentMetadata<S extends SearchSpec>
{

    /**
     * Sets the Search specifications (metadata) for this instrument
     * @param specs
     */
    void setSpecs(ArrayList<S> specs);

    /**
     * Returns the list of search specifications
     * @return
     */
    List<S> getSpecs();

    /**
     * Adds a list of search specifications to the current list
     * @param specs
     */
    void addSearchSpecs(List<S> specs);

    /**
     * Adds a search specification to the list
     * @param spec
     */
    void addSearchSpec(S spec);

    /**
     * Gets the instrument name
     * @return
     */
    String getInstrumentName();

    /**
     * Sets the instrument name
     * @param instrumentName
     */
    void setInstrumentName(String instrumentName);

    /**
     * Returns the query type
     * @return
     */
    String getQueryType();

    /**
     * Sets the query type for the search
     * @param queryType
     */
    void setQueryType(String queryType);

    /**
     * Returns a string representation of the metadata
     * @return
     */
    String toString();

}
