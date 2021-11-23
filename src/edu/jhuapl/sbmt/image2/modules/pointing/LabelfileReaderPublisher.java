package edu.jhuapl.sbmt.image2.modules.pointing;

import java.util.List;

import edu.jhuapl.sbmt.image2.pipeline.publisher.BasePipelinePublisher;
import edu.jhuapl.sbmt.pointing.LabelFileReader;

public class LabelfileReaderPublisher extends BasePipelinePublisher<LabelFileReader>
{

	public LabelfileReaderPublisher(String filename)
	{
		LabelFileReader reader = new LabelFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
