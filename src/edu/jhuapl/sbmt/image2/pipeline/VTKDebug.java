package edu.jhuapl.sbmt.image2.pipeline;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.sbmt.image2.pipeline.preview.VtkImagePreview;
import edu.jhuapl.sbmt.image2.pipeline.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

public class VTKDebug
{
	public static void writePolyDataToFile(vtkPolyData polyData, String filename)
	{
        vtkPolyDataWriter imageWriter = new vtkPolyDataWriter();
        imageWriter.SetInputData(polyData);
        imageWriter.SetFileName(filename);
        imageWriter.SetFileTypeToBinary();
        imageWriter.Write();
	}

	public static void previewLayer(Layer layer, String title) throws Exception
	{
		Pair<Layer, HashMap<String, String>> inputs = Pair.of(layer, new HashMap<String, String>());
		Just.of(inputs)
			.subscribe(new VtkLayerPreview(title, new Runnable()
			{

				@Override
				public void run()
				{
					// TODO Auto-generated method stub

				}
			}))
			.run();
	}

	public static void previewVtkImageData(vtkImageData imageData, String title) throws Exception
	{
		Just.of(imageData)
			.subscribe(new VtkImagePreview(title))
			.run();
	}
}
