package edu.jhuapl.near.server;

import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

import edu.jhuapl.near.util.NativeLibraryLoader;
import edu.jhuapl.near.util.PolyDataUtil;

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
        String outfile = args[i+1];

        vtkPolyData polydata = PolyDataUtil.loadLLRShapeModel(infile, westLongitude);

        // put it through a normal vector filter with auto orient on. This
        // will fix shape models where the point ids in the plates are not
        // oriented correctly.
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetInput(polydata);
        normalsFilter.SetComputeCellNormals(0);
        normalsFilter.SetComputePointNormals(1);
        normalsFilter.SplittingOff();
        normalsFilter.AutoOrientNormalsOn();
        normalsFilter.Update();
        vtkPolyData normalsOutput = normalsFilter.GetOutput();
        polydata.ShallowCopy(normalsOutput);

        PolyDataUtil.saveShapeModelAsPLT(polydata, outfile);
    }
}
