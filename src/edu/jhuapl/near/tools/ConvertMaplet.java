package edu.jhuapl.near.tools;

import java.io.IOException;

import vtk.vtkPolyData;

import edu.jhuapl.near.model.DEM;
import edu.jhuapl.near.util.NativeLibraryLoader;
import edu.jhuapl.near.util.PolyDataUtil;

public class ConvertMaplet
{
    private enum OutputType {
        OBJ, PLT, VTK
    }

    private static void usage()
    {
        String usage = "This program takes a FITS file which was output from the Mapmaker program and converts\n"
                + "it to a shape model in either OBJ, PLT or VTK format. You can also optionally decimate\n"
                + "it (to 1% its original size) or save out only the boundary of the maplet.\n\n"
                + "Usage: ConvertMaplet -obj|-plt|-vtk [-boundary|-decimate] <maplet-file> <output-file>\n";
        System.out.println(usage);

        System.exit(0);
    }

    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");

        if (args.length < 3)
        {
            usage();
        }


        OutputType outputTypeEnum = null;
        boolean boundaryOnly = false;
        boolean decimate = false;
        double decimationPercentage = 0.99;

        int i = 0;
        for(; i<args.length; ++i)
        {
            if (args[i].equals("-obj") || args[i].equals("--obj"))
            {
                outputTypeEnum = OutputType.OBJ;
            }
            else if (args[i].equals("-plt") || args[i].equals("--plt"))
            {
                outputTypeEnum = OutputType.PLT;
            }
            else if (args[i].equals("-vtk") || args[i].equals("--vtk"))
            {
                outputTypeEnum = OutputType.VTK;
            }
            else if (args[i].equals("-boundary") || args[i].equals("--boundary"))
            {
                boundaryOnly = true;
            }
            else if (args[i].equals("-decimate") || args[i].equals("--decimate"))
            {
                decimate = true;
                decimationPercentage = Double.parseDouble(args[++i]);
            }
            else
            {
                break;
            }
        }

        // There must be numRequiredArgs arguments remaining after the options. Otherwise abort.
        int numberRequiredArgs = 2;
        if (args.length - i != numberRequiredArgs )
            usage();

        if ((outputTypeEnum == OutputType.PLT || outputTypeEnum == OutputType.OBJ) && boundaryOnly)
        {
            System.out.println("When saving boundry, only VTK format is supported");
            System.exit(1);
        }

        String mapletFile = args[i++];
        String outputFile = args[i];

        NativeLibraryLoader.loadVtkLibrariesHeadless();

        DEM dem = null;
        try
        {
            dem = new DEM(mapletFile);
        }
        catch (Exception e)
        {
            System.out.println("An error occurred loading the maplet file. Check that the maplet file exists and is in the correct format.");
            System.exit(1);
        }

        try
        {
            if (boundaryOnly)
            {
                vtkPolyData boundary = dem.getBoundary();
                PolyDataUtil.saveShapeModelAsVTK(boundary, outputFile);
            }
            else
            {
                vtkPolyData polydata = dem.getSmallBodyPolyData();

                if (decimate)
                {
                    PolyDataUtil.decimatePolyData(polydata, decimationPercentage);
                }

                if (outputTypeEnum == OutputType.OBJ)
                {
                    PolyDataUtil.saveShapeModelAsOBJ(polydata, outputFile);
                }
                else if (outputTypeEnum == OutputType.PLT)
                {
                    PolyDataUtil.saveShapeModelAsPLT(polydata, outputFile);
                }
                else if (outputTypeEnum == OutputType.VTK)
                {
                    PolyDataUtil.saveShapeModelAsVTK(polydata, outputFile);
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("An error occurred writing out the file.");
            System.exit(1);
        }
    }

}
