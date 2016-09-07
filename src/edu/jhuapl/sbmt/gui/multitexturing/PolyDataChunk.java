package edu.jhuapl.sbmt.gui.multitexturing;

import vtk.vtkImageReader2Factory;
import vtk.vtkPolyData;
import vtk.vtkSphereSource;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;

public class PolyDataChunk extends vtkPolyData
{

    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();
        vtkSphereSource source=new vtkSphereSource();
        source.Update();
        vtkPolyData polyData=new vtkPolyData();
        polyData.DeepCopy(source.GetOutput());

        vtkImageReader2Factory factory=new vtkImageReader2Factory();
        //factory.CreateImageReader2(")

    }

}
