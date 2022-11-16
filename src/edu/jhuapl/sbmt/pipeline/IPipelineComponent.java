package edu.jhuapl.sbmt.pipeline;

import java.io.IOException;

public interface IPipelineComponent
{
//	public IPipelineComponent run(IPipelineSpigot completion) throws IOException, Exception;

	public IPipelineComponent run() throws IOException, Exception;

//	public O drip();
//
//	public List<O> flow();
}
