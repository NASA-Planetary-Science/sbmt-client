package edu.jhuapl.near.util;

/**
 * Interface for loading, filling out, or parsing PDS4 xml labels.
 * @author espirrc1
 *
 */
public interface BackPlanesPDS4XML
{

    /**
     * Method for building BackPlanesXmlMeta from a PDS3 label file.
     * Use this method to use an existing PDS3 label as the source metadata on which to
     * describe a new PDS4 product.
     * @param pds3Fname
     * @return
     */
    public BackPlanesXmlMeta pds3ToXmlMeta(String pds3Fname, String outXmlFname);

    /**
     * Method for building BackPlanesXmlMeta from a PDS4 XML file.
     * Use this method to use an existing PDS4 label as the source metadata on which to
     * describe a new PDS4 product.
     * @param pds4Fname
     * @return
     */
    public BackPlanesXmlMeta pds4ToXmlMeta(String pds4Fname, String outXmlFname);

    /**
     * Generate XML document from XmlMetadata
     * @param metaData - metadata to be used in populating XmlDoc
     * @param xmlTemplate - path to XML template file
     */
    public BackPlanesXml metaToXmlDoc(BackPlanesXmlMeta metaData, String xmlTemplate);

}
