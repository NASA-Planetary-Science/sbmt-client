package edu.jhuapl.sbmt.image2.pipeline.active.offlimb;

import java.io.File;
import java.io.IOException;

import vtk.vtkPolyData;
import vtk.vtkPolyDataReader;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class OfflimbLoadFromFileOperator extends BasePipelineOperator<String, vtkPolyData>
{
	@Override
	public void processData() throws IOException, Exception
	{
		String offLimbImageDataFileName = inputs.get(0) + "_offLimbImageData.vtk.gz";
		File file;
		try
		{
			file = FileCache.getFileFromServer(offLimbImageDataFileName);
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
		    file = null;
		}

		if (file != null)
		{
		    vtkPolyDataReader reader = new vtkPolyDataReader();
		    reader.SetFileName(file.getAbsolutePath());
		    reader.Update();
		    vtkPolyData offLimbImageData = reader.GetOutput();
		    outputs.add(offLimbImageData);
		}
	}
}
