package edu.jhuapl.near.tomo;

import vtk.vtkAppendPolyData;
import vtk.vtkParametricFunctionSource;
import vtk.vtkParametricTorus;
import vtk.vtkPlaneSource;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;

public class TomoTest
{
    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();

        vtkParametricTorus func=new vtkParametricTorus();
        vtkParametricFunctionSource surfaceSource=new vtkParametricFunctionSource();
        surfaceSource.SetParametricFunction(func);
        surfaceSource.Update();
        vtkPolyData surfacePolyData=surfaceSource.GetOutput();

        vtkPlaneSource planeSource=new vtkPlaneSource();
        planeSource.SetCenter(new double[]{0,0,1});
        planeSource.SetNormal(new double[]{0,0.25,0.25});
        planeSource.Update();
        vtkPolyData planePolyData=planeSource.GetOutput();

        vtkAppendPolyData appendFilter=new vtkAppendPolyData();
        appendFilter.AddInputData(surfacePolyData);
        appendFilter.AddInputData(planePolyData);
        appendFilter.Update();

        vtkPolyDataWriter writer=new vtkPolyDataWriter();
        writer.SetInputData(appendFilter.GetOutput());
        writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
        writer.SetFileTypeToBinary();
        writer.Write();
    }
}
