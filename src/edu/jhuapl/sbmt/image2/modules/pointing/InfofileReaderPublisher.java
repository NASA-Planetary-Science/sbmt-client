package edu.jhuapl.sbmt.image2.modules.pointing;

import java.util.List;

import edu.jhuapl.sbmt.core.image.InfoFileReader;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.pipeline.publisher.BasePipelinePublisher;

public class InfofileReaderPublisher extends BasePipelinePublisher<PointingFileReader>
{

	public InfofileReaderPublisher(String filename)
	{
		InfoFileReader reader = new InfoFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
