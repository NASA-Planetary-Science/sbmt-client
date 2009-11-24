package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;

public class NLRDataPerDay extends Model
{
	private vtkPolyData polydata;
    private ArrayList<vtkActor> actors = new ArrayList<vtkActor>();
	private double startPercentage = 0.0;
	private double stopPercentage = 1.0;
	
	public NLRDataPerDay(String path) throws IOException
	{
		File file = FileCache.getFileFromServer(path);

		if (file == null)
			throw new IOException(path + " could not be loaded");

		ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
		
		polydata = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray vert = new vtkCellArray();
		polydata.SetPoints( points );
		polydata.SetVerts( vert );
        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);

		for (int i=2; i<lines.size(); ++i)
		{
            String [] vals = lines.get(i).trim().split("\\s+");

            int id = points.InsertNextPoint(
            			Double.parseDouble(vals[14])/1000.0,
            			Double.parseDouble(vals[15])/1000.0,
            			Double.parseDouble(vals[16])/1000.0);
        	idList.SetId(0, id);
		    vert.InsertNextCell(idList);
		}

        vtkPolyDataMapper pointsMapper = new vtkPolyDataMapper();
        pointsMapper.SetInput(polydata);
        pointsMapper.SetResolveCoincidentTopologyToPolygonOffset();
        pointsMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, -1.0);

        vtkActor actor = new vtkActor();
        actor.SetMapper(pointsMapper);
        actor.GetProperty().SetColor(0.0, 0.0, 1.0);
        //actor.GetProperty().SetPointSize(2.0);
        
        actors.add(actor);
	}

	void setPercentageShown(double startPercent, double stopPercent)
	{
		startPercentage = startPercent;
		stopPercentage = stopPercent;
	}

	public ArrayList<vtkActor> getActors() 
	{
		return actors;
	}
}

