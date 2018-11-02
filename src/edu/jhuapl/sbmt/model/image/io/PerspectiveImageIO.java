package edu.jhuapl.sbmt.model.image.io;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.model.image.PerspectiveImage;

import nom.tam.fits.FitsException;

public class PerspectiveImageIO
{
    ENVIFileFormatIO enviIO;
    PNGFileFormatIO pngIO;
    FitsFileFormatIO fitsIO;

    public PerspectiveImageIO(PerspectiveImage image)
    {
        this.enviIO = new ENVIFileFormatIO(image);
        this.pngIO = new PNGFileFormatIO(image);
        this.fitsIO = new FitsFileFormatIO(image);
    }

    public void loadFromFile(String filename) throws FitsException, IOException
    {
        PerspectiveImageIOSupportedFiletypes type = PerspectiveImageIOSupportedFiletypes.getTypeForExtension(FilenameUtils.getExtension(filename));
        switch (type)
        {
        case PNG:
            pngIO.loadPngFile(filename);
            break;
        case ENVI:
            enviIO.loadEnviFile(filename);
            enviIO.loadNumSlices(filename);
        case FITS:
            fitsIO.loadFitsFiles(new String[] { filename }, false);
            fitsIO.loadNumSlices(filename);
        default:
            break;
        }
    }

    public void loadFromFiles(String[] filenames, boolean transposeData) throws FitsException, IOException
    {
        fitsIO.loadFitsFiles(filenames, transposeData);
        fitsIO.loadNumSlices(filenames[0]);
    }

}
