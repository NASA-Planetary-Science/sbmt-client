package edu.jhuapl.near.tools;

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

        boolean deleteCub = false;

        int i = 0;
        for(; i<args.length; ++i)
        {
            if (args[i].equals("--delete-cub"))
            {
                deleteCub = true;
            }
            else
            {
                break;
            }
        }

        int numberRequiredArgs = 7;
        if (args.length - i != numberRequiredArgs )
        {
            System.out.println("Usage: RunMapmaker [--delete-cub] <mapmaker-root-dir> <name> <half-size> <scale> <latitude> <longitude> <output-dir>");
            System.exit(0);
        }

        String mapmakerRootDir = args[i++];
        String name = args[i++];
        int halfSize = Integer.parseInt(args[i++]);
        double scale = Double.parseDouble(args[i++]);
        double lat = Double.parseDouble(args[i++]);
        double lon = Double.parseDouble(args[i++]);
        File outputFolder = new File(args[i++]);

        Mapmaker mapmaker = new Mapmaker(mapmakerRootDir);
        mapmaker.setName(name);
        mapmaker.setLatitude(lat);
        mapmaker.setLongitude(lon);
        mapmaker.setHalfSize(halfSize);
        mapmaker.setPixelSize(scale);
        mapmaker.setOutputFolder(outputFolder);

        Process mapmakerProcess = mapmaker.runMapmaker();
        mapmakerProcess.waitFor();

        mapmaker.convertCubeToFitsAndSaveInOutputFolder(deleteCub);


    }

}
