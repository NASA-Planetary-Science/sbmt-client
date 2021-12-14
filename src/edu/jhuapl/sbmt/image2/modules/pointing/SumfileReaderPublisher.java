package edu.jhuapl.sbmt.image2.modules.pointing;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image2.pipeline.publisher.BasePipelinePublisher;
import edu.jhuapl.sbmt.model.image.PointingFileReader;
import edu.jhuapl.sbmt.model.image.SumFileReader;

public class SumfileReaderPublisher extends BasePipelinePublisher<PointingFileReader>
{

	public SumfileReaderPublisher(String filename) throws NumberFormatException, IOException
	{
		SumFileReader reader = new SumFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
