package edu.jhuapl.sbmt.pipeline.subscriber;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;

public abstract class BasePipelineSubscriber<O extends Object> implements IPipelineSubscriber<O>
{
	protected IPipelinePublisher<O> publisher;
	private List<O> outputs;

	public BasePipelineSubscriber()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public BasePipelineSubscriber<O> run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

//	@Override
//	public BasePipelineSubscriber<O> run(IPipelineSpigot completion) throws IOException, Exception
//	{
//		publisher.run();
//		completion.flowData(outputs);
//		return this;
//	}

	@Override
	public void receive(List<O> items) throws IOException, Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void receive(O item) throws IOException, Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPublisher(IPipelinePublisher<O> publisher)
	{
		this.publisher = publisher;
	}

}
