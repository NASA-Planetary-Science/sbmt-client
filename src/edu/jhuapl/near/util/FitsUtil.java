package edu.jhuapl.near.util;

import java.io.IOException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;

/**
 * Various static routines for working with FITS files.
 */
public class FitsUtil {

    public static void saveToFits(double[][][] data, String outfile) throws FitsException, IOException {
        if (data.length < 6) {
            throw new IOException("FITS data must have at least 6 planes.");
        }

        float[][][] dataF = double2float(data);

        Fits f = new Fits();
        BasicHDU hdu = FitsFactory.HDUFactory(dataF);
        f.addHDU(hdu);
        BufferedFile bf = new BufferedFile(outfile, "rw");
        f.write(bf);
        bf.close();
    }

    public static double[][][] loadFits(String filename, int[] axes) throws FitsException, IOException {
        Fits f = new Fits(filename);
        BasicHDU hdu = f.getHDU(0);
        int[] axes2 = hdu.getAxes();
        if (axes2.length != 3) {
            throw new IOException("FITS file has incorrect dimensions");
        }

        axes[0] = axes2[0];
        axes[1] = axes2[1];
        axes[2] = axes2[2];

        Object data = hdu.getData().getData();
        f.getStream().close();

        if (data instanceof float[][][]) {
            return float2double((float[][][]) data);
        }
        else {// assume double[][][]
            return (double[][][]) data;
        }
    }

    public static double[][][] xyz2llrxyz(double[][][] indata) {
        int numPlanes = indata.length;
        if (numPlanes != 3) {
            System.out.println("Error: cube must contain exactly 3 planes");
            return null;
        }
        int numRows = indata[0].length;
        int numCols = indata[0][0].length;
        double[][][] outdata = new double[6][numRows][numCols];
        double[] pt = new double[3];
        for (int i = 0; i < numRows; ++i)
            for (int j = 0; j < numCols; ++j) {
                pt[0] = indata[0][i][j];
                pt[1] = indata[1][i][j];
                pt[2] = indata[2][i][j];
                LatLon llr = MathUtil.reclat(pt).toDegrees();
                outdata[0][i][j] = llr.lat;
                outdata[1][i][j] = llr.lon;
                outdata[2][i][j] = llr.rad;
                outdata[3][i][j] = pt[0];
                outdata[4][i][j] = pt[1];
                outdata[5][i][j] = pt[2];
            }
        return outdata;
    }

    public static double[][][] llr2llrxyz(double[][][] indata) {
        int numPlanes = indata.length;
        if (numPlanes != 3) {
            System.out.println("Error: cube must contain exactly 3 planes");
            return null;
        }
        int numRows = indata[0].length;
        int numCols = indata[0][0].length;
        double[][][] outdata = new double[6][numRows][numCols];
        for (int i = 0; i < numRows; ++i)
            for (int j = 0; j < numCols; ++j) {
                double lat = indata[0][i][j];
                double lon = indata[1][i][j];
                double rad = indata[2][i][j];
                LatLon llr = new LatLon(lat, lon, rad).toRadians();
                double[] pt = MathUtil.latrec(llr);
                outdata[0][i][j] = indata[0][i][j];
                outdata[1][i][j] = indata[1][i][j];
                outdata[2][i][j] = indata[2][i][j];
                outdata[3][i][j] = pt[0];
                outdata[4][i][j] = pt[1];
                outdata[5][i][j] = pt[2];
            }
        return outdata;
    }

    public static float[][][] double2float(double[][][] indata) {
        int numPlanes = indata.length;
        int numRows = indata[0].length;
        int numCols = indata[0][0].length;
        float[][][] outdata = new float[numPlanes][numRows][numCols];
        for (int k = 0; k < numPlanes; ++k)
            for (int i = 0; i < numRows; ++i)
                for (int j = 0; j < numCols; ++j) {
                    outdata[k][i][j] = (float) indata[k][i][j];
                }
        return outdata;
    }

    public static double[][][] float2double(float[][][] indata) {
        int numPlanes = indata.length;
        int numRows = indata[0].length;
        int numCols = indata[0][0].length;
        double[][][] outdata = new double[numPlanes][numRows][numCols];
        for (int k = 0; k < numPlanes; ++k)
            for (int i = 0; i < numRows; ++i)
                for (int j = 0; j < numCols; ++j) {
                    outdata[k][i][j] = indata[k][i][j];
                }
        return outdata;
    }

}
