package edu.jhuapl.near.server;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.near.util.Mapmaker;

/**
 * Program to runs the Gaskell's mapmaker program
 */
public class RunMapmaker
{

    public static void main(String[] args) throws IOException, InterruptedException
    {
        System.setProperty("java.awt.headless", "true");

        if (args.length != 7)
        {
            System.out.println("Usage: RunMapmaker <mapmaker-root-dir> <name> <half-size> <scale> <latitude> <longitude> <output-dir>");
            System.exit(0);
        }

        String mapmakerRootDir = args[0];
        String name = args[1];
        int halfSize = Integer.parseInt(args[2]);
        double scale = Double.parseDouble(args[3]);
        double lat = Double.parseDouble(args[4]);
        double lon = Double.parseDouble(args[5]);
        File outputFolder = new File(args[6]);

        Mapmaker mapmaker = new Mapmaker(mapmakerRootDir);
        mapmaker.setName(name);
        mapmaker.setLatitude(lat);
        mapmaker.setLongitude(lon);
        mapmaker.setHalfSize(halfSize);
        mapmaker.setPixelSize(scale);
        mapmaker.setOutputFolder(outputFolder);

        Process mapmakerProcess = mapmaker.runMapmaker();
        mapmakerProcess.waitFor();

        mapmaker.convertCubeToFitsAndSaveInOutputFolder();
    }

}
