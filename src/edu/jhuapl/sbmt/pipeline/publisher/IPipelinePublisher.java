package edu.jhuapl.sbmt.pipeline.publisher;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.pipeline.IPipelineComponent;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public interface IPipelinePublisher<OutputType extends Object> extends IPipelineComponent //, IPipelineSpigot<OutputType>
{
	public void publish() throws IOException, Exception;

	public IPipelinePublisher<OutputType> subscribe(IPipelineSubscriber<OutputType> subscriber);

	public <T extends Object> IPipelineOperator<OutputType, T> operate(IPipelineOperator<OutputType, T> operator);

	public List<OutputType> getOutputs();

	public OutputType getOutput();
}
