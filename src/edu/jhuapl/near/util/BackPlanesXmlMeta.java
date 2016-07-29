package edu.jhuapl.near.util;

import java.util.List;

import edu.jhuapl.near.model.Image.ImageKey;

import nom.tam.fits.HeaderCard;

/**
 * Container class for storing the metadata that we'd like to put into the Backplanes PDS4 XML
 * files. Uses builder pattern to fill out the fields.
 *
 * @author espirrc1
 *
 */
public class BackPlanesXmlMeta
{

    /*
     * These fields are common to all XML labels
     */
    public final ImageKey imageKey;
    public final String logicalId;
    public final String startDateTime;
    public final String stopDateTime;
    public final String fileName;
    public final String creationDateTime;
    public final long fileSize;

    /*
     * These fields are common to XML describing FITS data (image or table). Set
     * to 0 for non-FITS tablular data.
     */
    public final int lines; // used to store number of lines in FITS table.
    public final int samples; // used to store number of columns in FITS table.
    public final long headerSize;
    public final List<HeaderCard> fitsHdrCards;



    private BackPlanesXmlMeta(BPMetaBuilder b) {
        this.imageKey = b.imageKey;
        this.logicalId = b.logicalId;
        this.startDateTime = b.startDateTime;
        this.stopDateTime = b.stopDateTime;
        this.fileName = b.fileName;
        this.creationDateTime = b.creationDateTime;
        this.fileSize = b.fileSize;
        this.lines = b.lines;
        this.samples = b.samples;
        this.headerSize = b.headerSize;
        this.fitsHdrCards = b.fitsHdrCards;
    }

    /**
     * populate metadata values with this builder.
     *
     * @author espirrc1
     *
     */
    public static class BPMetaBuilder {
        private ImageKey imageKey = null;
        private String logicalId = "";
        private String startDateTime = "";
        private String stopDateTime = "";
        private String fileName = "";
        private String creationDateTime = "";
        private long fileSize = 0;
        private long numRecords1 = 0;
        private long numRecords2 = 0;
        private int tableOffset = 0;
        private int lines = 0;
        private int samples = 0;
        private long headerSize = 0;
        private List<HeaderCard> fitsHdrCards = null;

        /**
         * Constructor. Specify the image key and XML output filename
         *
         *
         * @param productType
         */
        public BPMetaBuilder(String fileName) {

            this.fileName = fileName;

        }

        public BPMetaBuilder setKey(ImageKey key) {
            this.imageKey = key;
            return this;
        }

        public BPMetaBuilder startDate(String startDate) {
            this.startDateTime = startDate;
            return this;
        }

        public BPMetaBuilder stopDate(String stopDate) {
            this.stopDateTime = stopDate;
            return this;
        }

        public BPMetaBuilder creationDate(String createDate) {
            this.creationDateTime = createDate;
            return this;
        }

        /**
         * Build the XmlMetaData object.
         *
         * @return
         */
        public BackPlanesXmlMeta build() {

            // fill out the logical id: root base LID + filename
//            this.logicalId = productType.baseLid() + ":" + fileName;

            return new BackPlanesXmlMeta(this);

        }

    }

    /**
     * Convenience method for extracting the value from a "keyword = value" string.
     * Returns value with leading and trailing whitespace removed or empty string
     * if line does not match "keyword = value" format.
     * @param line
     */
    public static String valFromKeyVal(String line) {
        String[] temp;
        String temp2 = "";
        temp = line.split("=");
        if (temp.length == 2) {
            temp2 = temp[1].trim();
        }
        return temp2;
    }
}
