package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.core.pointing.PointingSource;


/**
 * Interface for search specifications for instruments.  Consists of getters to read data
 * @author steelrj1
 *
 */
public interface SearchSpec
{

    /**
     * Returns the name of this data type (usually shorter for display in a UI)
     * @return
     */
    String getDataName();

    /**
     * Returns the root location of the data on the server
     * @return
     */
    String getDataRootLocation();

    /**
     * Returns the path to the data file on the server
     * @return
     */
    String getDataPath();

    /**
     * Returns the filename of the list of items being searched
     * @return
     */
    String getDataListFilename();

    /**
     * Returns the Pointing source
     * @return
     */
    PointingSource getSource();

    /**
     * Returns the x axis units string
     * @return
     */
    String getxAxisUnits();

    /**
     * Returns the y axis units string
     * @return
     */
    String getyAxisUnits();

    /**
     * Returns the full length description for this data type
     * @return
     */
    String getDataDescription();

}
