package edu.jhuapl.sbmt.tools;

import java.io.File;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.PolyDataUtil;

/**
 * This program converts a shape model in lat, lon, radius format
 * (some of the Thomas and Stooke shape models are distributed this
 * way) into PLT format. If the --east or -east option is provided,
 * it is assumed to be in east longitude. Otherwise west longitude
 * is assumed.
 */
public class ConvertLatLonShapeModelToPLT
{
    public static void main(String[] args) throws Exception
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        boolean westLongitude = true;
        int i = 0;
        for(; i<args.length; ++i)
        {
            if ("--east".equals(args[i]) || "-east".equals(args[i]))
            {
                westLongitude = false;
            }
            else
            {
                break;
            }
        }

        if (args.length - i != 2)
        {
            System.out.println("Usage: convertLatLonShapeModelToPLT [--east] infilename outfilename");
            System.exit(1);
        }

        String infile = args[i];
        File outfile = new File(args[i+1]);

        vtkPolyData polydata = PolyDataUtil.loadLLRShapeModel(infile, westLongitude);

        PolyDataUtil.saveShapeModelAsOBJ(polydata, outfile);
    }
}
