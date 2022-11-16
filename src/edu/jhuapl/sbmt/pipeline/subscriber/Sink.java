package edu.jhuapl.sbmt.pipeline.subscriber;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;

public class Sink<O extends Object> implements IPipelineSubscriber<O>
{
	private IPipelinePublisher<O> publisher;
	private List<O> outputs;

	public static <O extends Object> Sink<O> of(List<O> outputs)
	{
		return new Sink<O>(outputs);
	}

	public static <O extends Object> Sink<O> of(O output)
	{
		return new Sink<O>(output);
	}

	public Sink(List<O> outputs)
	{
		this.outputs = outputs;
	}

	public Sink(O output)
	{
		this.outputs = Lists.newArrayList();
		this.outputs.add(output);
	}

	@Override
	public void receive(List<O> items)
	{
		this.outputs.addAll(items);
	}

	@Override
	public void receive(O item)
	{
		receive(List.of(item));
	}

//	@Override
//	public O drip()
//	{
//		return outputs.get(0);
//	}
//
//	@Override
//	public List<O> flow()
//	{
//		return outputs;
//	}

	@Override
	public void setPublisher(IPipelinePublisher<O> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public Sink<O> run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

//	@Override
//	public Sink<O> run(IPipelineSpigot completion) throws IOException, Exception
//	{
//		publisher.run();
//		completion.flowData(outputs);
//		return this;
//	}

}
