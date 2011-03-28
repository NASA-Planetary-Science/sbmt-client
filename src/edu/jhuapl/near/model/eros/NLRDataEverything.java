package edu.jhuapl.near.model.eros;

import java.io.File;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkProp;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.util.FileCache;

public class NLRDataEverything extends Model
{
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();

    public NLRDataEverything()
    {
        setVisible(false);
    }

    private void initialize()
    {
        System.out.println("initializing");
        File file = FileCache.getFileFromServer("/NLR/nlrdata.vtk.gz");

        if (file == null)
        {
            System.out.println(file + " could not be loaded");
            return;
        }

        vtkPolyDataReader nlrReader = new vtkPolyDataReader();
        nlrReader.SetFileName(file.getAbsolutePath());
        nlrReader.Update();

        vtkPolyDataMapper pointsMapper = new vtkPolyDataMapper();
        pointsMapper.SetInput(nlrReader.GetOutput());

        vtkActor actor = new vtkActor();
        actor.SetMapper(pointsMapper);
        actor.GetProperty().SetColor(0.0, 0.0, 1.0);
        actor.GetProperty().SetPointSize(1.0);

        actors.add(actor);
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        return "NLR data";
    }

    public ArrayList<vtkProp> getProps()
    {
        if (actors.isEmpty())
            initialize();

        return actors;
    }
}

