package edu.jhuapl.sbmt.image2.pipeline.publisher;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image2.pipeline.IPipelineComponent;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.IPipelineSubscriber;

public interface IPipelinePublisher<OutputType extends Object> extends IPipelineComponent
{
	public void publish() throws IOException, Exception;

	public IPipelinePublisher<OutputType> subscribe(IPipelineSubscriber<OutputType> subscriber);

	public <T extends Object> IPipelineOperator<OutputType, T> operate(IPipelineOperator<OutputType, T> operator);

	public List<OutputType> getOutputs();
}
