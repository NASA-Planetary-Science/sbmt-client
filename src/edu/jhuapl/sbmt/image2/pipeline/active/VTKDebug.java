package edu.jhuapl.sbmt.image2.pipeline.active;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.modules.preview.VtkImagePreview;
import edu.jhuapl.sbmt.image2.modules.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;

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

	public static void previewLayer(Layer layer) throws Exception
	{
		Pair<Layer, HashMap<String, String>> inputs = Pair.of(layer, new HashMap<String, String>());
		Just.of(inputs)
			.subscribe(new VtkLayerPreview())
			.run();
	}

	public static void previewVtkImageData(vtkImageData imageData) throws Exception
	{
		Just.of(imageData)
			.subscribe(new VtkImagePreview())
			.run();
	}
}
