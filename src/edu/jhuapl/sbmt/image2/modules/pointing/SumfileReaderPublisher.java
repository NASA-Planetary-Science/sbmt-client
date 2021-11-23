package edu.jhuapl.sbmt.image2.modules.pointing;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image2.pipeline.publisher.BasePipelinePublisher;

import altwg.util.SumFileSingleSlice;

public class SumfileReaderPublisher extends BasePipelinePublisher<SumFileSingleSlice>
{

	public SumfileReaderPublisher(String filename) throws NumberFormatException, IOException
	{
		SumFileSingleSlice reader = new SumFileSingleSlice(filename);
		reader.loadSumfile();
		this.outputs = List.of(reader);
	}
}
