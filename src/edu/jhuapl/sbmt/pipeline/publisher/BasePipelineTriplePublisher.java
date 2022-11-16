package edu.jhuapl.sbmt.pipeline.publisher;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class BasePipelineTriplePublisher<S extends Object, T extends Object, U extends Object> implements IPipelinePublisher<Triple<S, T, U>>
{

	protected List<Triple<S, T, U>> outputs;
	protected IPipelineSubscriber<Triple<S, T, U>> subscriber;

	public BasePipelineTriplePublisher()
	{
		outputs = Lists.newArrayList();
	}

	@Override
	public BasePipelineTriplePublisher<S, T,U> run() throws IOException, Exception
	{
		publish();
		return this;
	}

//	@Override
//	public BasePipelineTriplePublisher<S, T,U> run(Runnable completion) throws IOException, Exception
//	{
//		publish();
//		completion.run();
//		return this;
//	}

	@Override
	public void publish() throws IOException, Exception
	{
		subscriber.receive(outputs);
	}

	@Override
	public List<Triple<S, T, U>> getOutputs()
	{
		return outputs;
	}

	@Override
	public Triple<S, T, U> getOutput()
	{
		return outputs.get(0);
	}

	@Override
	public IPipelinePublisher<Triple<S, T, U>> subscribe(IPipelineSubscriber<Triple<S, T, U>> subscriber)
	{
		this.subscriber = subscriber;
		this.subscriber.setPublisher(this);
		return this;
	}

	@Override
	public <O extends Object> IPipelineOperator<Triple<S, T, U>, O> operate(IPipelineOperator<Triple<S, T, U>, O> operator)
	{
		this.subscriber = operator;
		this.subscriber.setPublisher(this);
		return operator;
	}

//	@Override
//	public Triple<S, T, U> drip()
//	{
//		return getOutput();
//	}
//
//	@Override
//	public List<Triple<S, T, U>> flow()
//	{
//		return getOutputs();
//	}
}