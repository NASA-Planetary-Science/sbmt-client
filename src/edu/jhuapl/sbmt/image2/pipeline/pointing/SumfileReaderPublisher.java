package edu.jhuapl.sbmt.image2.pipeline.pointing;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.core.image.SumFileReader;
import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;

public class SumfileReaderPublisher extends BasePipelinePublisher<PointingFileReader>
{

	public SumfileReaderPublisher(String filename) throws NumberFormatException, IOException
	{
		SumFileReader reader = new SumFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
