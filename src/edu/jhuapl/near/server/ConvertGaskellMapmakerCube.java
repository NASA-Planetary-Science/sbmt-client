package edu.jhuapl.near.server;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.near.model.DEMModel;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class ConvertGaskellMapmakerCube
{
    private enum OutputType {
        OBJ, PLT, VTK
    }

    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: ConvertGaskellMapmakerCube -obj|-plt <cube-file> <output-file>");
            System.exit(0);
        }

        String outputType = args[0];
        String cubeFile = args[1];
        String outputFile = args[2];

        OutputType outputTypeEnum = null;

        if (outputType.equals("-obj") || outputType.equals("--obj"))
        {
            outputTypeEnum = OutputType.OBJ;
        }
        else if (outputType.equals("-plt") || outputType.equals("--plt"))
        {
            outputTypeEnum = OutputType.PLT;
        }
        else if (outputType.equals("-vtk") || outputType.equals("--vtk"))
        {
            outputTypeEnum = OutputType.VTK;
        }
        else
        {
            System.out.println("Error. Unsupported file type. Valid options are -obj for OBJ format, -plt for Gaskell plate format, or -vtk for VTK format.");
            System.exit(1);
        }

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
            if (outputTypeEnum == OutputType.OBJ)
            {
                dem.saveAsOBJ(new File(outputFile));
            }
            else if (outputTypeEnum == OutputType.PLT)
            {
                dem.saveAsPLT(new File(outputFile));
            }
            else if (outputTypeEnum == OutputType.VTK)
            {
                dem.saveAsVTK(new File(outputFile));
            }
        }
        catch (IOException e)
        {
            System.out.println("An error occurred writing out the file.");
            System.exit(1);
        }
    }

}
