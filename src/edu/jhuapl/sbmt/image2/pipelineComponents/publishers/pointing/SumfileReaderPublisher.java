package edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.core.image.SumFileReader;
import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;

public class SumfileReaderPublisher extends BasePipelinePublisher<PointingFileReader>
{

	public SumfileReaderPublisher(String filename) throws NumberFormatException, IOException
	{
		if (new File(filename).exists() == false) filename = FileCache.getFileFromServer(filename).getAbsolutePath();
		SumFileReader reader = new SumFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
