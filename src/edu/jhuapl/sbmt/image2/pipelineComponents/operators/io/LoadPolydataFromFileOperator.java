package edu.jhuapl.sbmt.image2.pipelineComponents.operators.io;

import java.io.File;
import java.io.IOException;

import vtk.vtkPolyData;
import vtk.vtkPolyDataReader;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LoadPolydataFromFileOperator extends BasePipelineOperator<String, vtkPolyData>
{
	@Override
	public void processData() throws IOException, Exception
	{
		if (inputs.get(0).split("cache/2/").length != 2) return;
		String imageDataFileName = inputs.get(0).split("cache/2/")[1];
		File file;
		try
		{
			file = new File(FileCache.instance().getFile(imageDataFileName).getAbsolutePath() + ".gz");
		}
		catch (UnauthorizedAccessException e)
		{
		    // Report this but continue.
		    e.printStackTrace();
		    file = null;
		}
		catch (Exception e)
		{
		    // Ignore this one.
			e.printStackTrace();
		    file = null;
		}
		if (file != null && file.exists())
		{
		    vtkPolyDataReader reader = new vtkPolyDataReader();
		    reader.SetFileName(file.getAbsolutePath());
		    reader.Update();
		    vtkPolyData imageData = reader.GetOutput();
		    outputs.add(imageData);
		}
	}
}
