package edu.jhuapl.sbmt.tools;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.util.MapmakerNativeWrapper;

public class ConvertCUBToFITS
{
    private static void convertCubeToFits(int halfSize, String cubfile, String fitsfile)
    {
        try
        {
            FileInputStream fs = new FileInputStream(cubfile);
            BufferedInputStream bs = new BufferedInputStream(fs);
            DataInputStream in = new DataInputStream(bs);

            int liveSize = 2 * halfSize + 1;
            int startPixel = (MapmakerNativeWrapper.MAX_HEIGHT - liveSize) / 2;

            float[] indata = new float[MapmakerNativeWrapper.MAX_WIDTH*MapmakerNativeWrapper.MAX_HEIGHT*MapmakerNativeWrapper.MAX_PLANES];
            for (int i=0;i<indata.length; ++i)
            {
                indata[i] = FileUtil.readFloatAndSwap(in);
            }

            float[][][] outdata = new float[MapmakerNativeWrapper.MAX_PLANES][liveSize][liveSize];

            int endPixel = startPixel + liveSize - 1;
            for (int p=0; p<MapmakerNativeWrapper.MAX_PLANES; ++p)
                for (int m=0; m<MapmakerNativeWrapper.MAX_HEIGHT; ++m)
                    for (int n=0; n<MapmakerNativeWrapper.MAX_WIDTH; ++n)
                    {
                        if (m >= startPixel && m <= endPixel && n >= startPixel && n <= endPixel)
                        {
                            outdata[p][m-startPixel][n-startPixel] = indata[MapmakerNativeWrapper.index(n,m,p)];
                        }
                    }

            in.close();


            Fits f = new Fits();
            BasicHDU hdu = FitsFactory.HDUFactory(outdata);

            hdu.getHeader().addValue("PLANE1", "Elevation Relative to Gravity (kilometers)", null);
            hdu.getHeader().addValue("PLANE2", "Elevation Relative to Normal Plane (kilometers)", null);
            hdu.getHeader().addValue("PLANE3", "Slope (radians)", null);
            hdu.getHeader().addValue("PLANE4", "X coordinate of maplet vertices (kilometers)", null);
            hdu.getHeader().addValue("PLANE5", "Y coordinate of maplet vertices (kilometers)", null);
            hdu.getHeader().addValue("PLANE6", "Z coordinate of maplet vertices (kilometers)", null);
            hdu.getHeader().addValue("HALFSIZE", halfSize, "Half Size (pixels)");
            hdu.getHeader().addValue("SCALE", "UNKNOWN", "Horizontal Scale (meters per pixel)");
            hdu.getHeader().addValue("LATITUDE", "UNKNOWN", "Latitude of Maplet Center (degrees)");
            hdu.getHeader().addValue("LONGTUDE", "UNKNOWN", "Longitude of Maplet Center (degrees)");

            f.addHDU(hdu);
            BufferedFile bf = new BufferedFile(fitsfile, "rw");
            f.write(bf);
            bf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (FitsException e)
        {
            e.printStackTrace();
        }
    }

    private static void usage()
    {
        String usage = "This program takes a CUB file as output by mapmaker and converts it to FITS formt.\n\n"
                + "Usage: ConvertCUBToFITS <half-size> <cub-file> <fits-file>\n";
        System.out.println(usage);

        System.exit(0);
    }

    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");

        if (args.length != 3)
            usage();

        int halfSize = Integer.parseInt(args[0]);
        String cubfile = args[1];
        String fitsfile = args[2];

        convertCubeToFits(halfSize, cubfile, fitsfile);
    }

}
