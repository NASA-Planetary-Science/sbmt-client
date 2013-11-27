package edu.jhuapl.near.server;

import java.io.IOException;

import vtk.vtkPolyData;

import edu.jhuapl.near.model.DEMModel;
import edu.jhuapl.near.util.NativeLibraryLoader;
import edu.jhuapl.near.util.PolyDataUtil;

public class ConvertGaskellMapmakerCube
{
    private enum OutputType {
        OBJ, PLT, VTK
    }

    private static void usage()
    {
        System.out.println("Usage: ConvertGaskellMapmakerCube -obj|-plt|-vtk [-boundary|-decimate] <cube-file> <output-file>");
        System.exit(0);
    }

    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            usage();
        }


        OutputType outputTypeEnum = null;
        boolean boundaryOnly = false;
        boolean decimate = false;

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

        String cubeFile = args[i++];
        String outputFile = args[i];

        NativeLibraryLoader.loadVtkLibraries();

        String labelFile = cubeFile.substring(0, cubeFile.length()-3) + "lbl";
        DEMModel dem = null;
        try
        {
            dem = new DEMModel(cubeFile, labelFile);
        }
        catch (IOException e)
        {
            System.out.println("An error occurred loading the cube file. Check that both the cube and label files exist and are in the correct format.");
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
                    PolyDataUtil.decimatePolyData(polydata, 0.99);
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
