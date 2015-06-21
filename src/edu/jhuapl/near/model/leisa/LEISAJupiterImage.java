package edu.jhuapl.near.model.leisa;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.ImageDataUtil;

public class LEISAJupiterImage extends PerspectiveImage
{
    public static final int INITIAL_BAND = 127;

    private double[][] spectrumWavelengths = null; // { 0.0, 100.0, 200.0, 300.0 };
    private double[][] spectrumValues = null;      // { 0.0, 10.0, 14.1, 15.0 };
    private double[][] spectrumRegion = null;      // { 0.0 };

    public ImageKey getKey()
    {
        ImageKey key = super.getKey();
        key.slice = getCurrentSlice();
        key.band = getCurrentBand();
        return key;
    }

    public LEISAJupiterImage(ImageKey key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException,
            IOException
    {
        super(key, smallBodyModel, loadPointingOnly, INITIAL_BAND);

        // initialize the spectrum wavelengths, eventually this should be read from the fits image
        spectrumWavelengths = new double[2][];
        spectrumWavelengths[0] = new double[256];
        spectrumWavelengths[1] = new double[256];

        // initialize the spectrum values

        spectrumValues = new double[2][];
        spectrumValues[0] = new double[256];
        spectrumValues[1] = new double[256];

        for (int i=0; i<256; i++)
        {
            spectrumWavelengths[0][i] = i;
            spectrumValues[0][i] = i;
        }

        for (int i=0; i<256; i++)
        {
            spectrumWavelengths[1][i] = i;
            spectrumValues[1][i] = i + 10.0;
        }

//        for (int i=0; i<128; i++)
//        {
//            spectrumWavelengths[0][i] = 1.25 + i * (2.5 - 1.25) / 127.0;
//            spectrumValues[0][i] = i;
//        }
//
//        for (int i=0; i<128; i++)
//        {
//            spectrumWavelengths[1][i] = 2.1 + i * (2.25 - 2.1) / 127.0;
//            spectrumValues[1][i] = i * 1.1;
//        }

        double centerI = (this.getImageHeight() - 1) / 2.0;
        double centerJ = (this.getImageWidth() - 1) / 2.0;
        double[][] region = { { centerI, centerJ } };
        this.setSpectrumRegion(region);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis and y axis. For some reason we need to do
        // this so the image is displayed properly.
//        ImageDataUtil.flipImageYAxis(rawImage);
        ImageDataUtil.flipImageXAxis(rawImage);
    }

    public int getDefaultSlice() { return INITIAL_BAND; }

    public boolean shiftBands() { return true; }

    protected int getNumberBands()
    {
        return 256;
    }

    public int getNumberSpectra() { return 2; }

    public double[] getSpectrumWavelengths(int spectrum) { return spectrumWavelengths[spectrum]; }

    public double[] getSpectrumValues(int spectrum) { return spectrumValues[spectrum]; }

    public String getSpectrumUnits() { return "micrometers"; }


    @Override
    public void setSpectrumRegion(double[][] spectrumRegion)
    {
        System.out.println("Setting spectrum region: " + spectrumRegion[0][0] + ", " + spectrumRegion[0][1]);
        this.spectrumRegion = spectrumRegion;

        // calculate the spectrum values
        vtkImageData image = this.getRawImage();

        if (image != null)
        {
            int x = (int)Math.round(spectrumRegion[0][0]);
            int y = (int)Math.round(spectrumRegion[0][1]);
            float[] pixelColumn = ImageDataUtil.vtkImageDataToArray1D(image, x, y);

            for (int i=0; i<256; i++)
            {
                spectrumValues[0][i] = 1.0e-12 * (double)pixelColumn[i];
            }

            for (int i=0; i<256; i++)
            {
                spectrumValues[1][i] = 2.0e-12 * (double)pixelColumn[i];
            }
//            for (int i=0; i<128; i++)
//            {
//                spectrumValues[0][i] = 1.0e-12 * (double)pixelColumn[i];
//            }
//
//            for (int i=0; i<128; i++)
//            {
//                spectrumValues[1][i] = 1.0e-12 * (double)pixelColumn[i + 128];
//            }
        }
    }

    @Override
    public double[][] getSpectrumRegion()
    {
        return this.spectrumRegion;
    }

    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }

    @Override
    protected String initializeFitFileFullPath()
    {
        ImageKey key = getKey();
        return FileCache.getFileFromServer(key.name + ".fit").getAbsolutePath();
    }

    protected double getFocalLength() { return 657.5; }    // in mm

    protected double getPixelWidth() { return 0.013; }    // in mm

    protected double getPixelHeight() { return 0.013; }   // in mm

    @Override
    protected String initializeLabelFileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/labelfiles/"
        + keyFile.getName().split("\\.")[0] + ".lbl";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    @Override
    protected String initializeInfoFileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/infofiles/"
        + keyFile.getName() + ".INFO";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    @Override
    protected String initializeSumfileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/sumfiles/"
        + keyFile.getName().split("\\.")[0] + ".SUM";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    protected vtkImageData createRawImage(int height, int width, int depth, float[][] array2D, float[][][] array3D)
    {
        return createRawImage(height, width, depth, false, array2D, array3D);
    }

}
