package edu.jhuapl.sbmt.pipeline.publisher;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class BasePipelinePairPublisher<S extends Object, T extends Object> implements IPipelinePublisher<Pair<S, T>>
{

	protected List<Pair<S, T>> outputs;
	protected IPipelineSubscriber<Pair<S, T>> subscriber;

	public BasePipelinePairPublisher()
	{
		outputs = Lists.newArrayList();
	}

	@Override
	public BasePipelinePairPublisher<S, T> run() throws IOException, Exception
	{
		publish();
		return this;
	}

//	@Override
//	public BasePipelinePairPublisher<S, T> run(Runnable completion) throws IOException, Exception
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
	public List<Pair<S, T>> getOutputs()
	{
		return outputs;
	}

	@Override
	public Pair<S, T> getOutput()
	{
		return outputs.get(0);
	}

	@Override
	public IPipelinePublisher<Pair<S, T>> subscribe(IPipelineSubscriber<Pair<S, T>> subscriber)
	{
		this.subscriber = subscriber;
		this.subscriber.setPublisher(this);
		return this;
	}

	@Override
	public <O extends Object> IPipelineOperator<Pair<S, T>, O> operate(IPipelineOperator<Pair<S, T>, O> operator)
	{
		this.subscriber = operator;
		this.subscriber.setPublisher(this);
		return operator;
	}

//	@Override
//	public Pair<S, T> drip()
//	{
//		return getOutput();
//	}
//
//	@Override
//	public List<Pair<S, T>> flow()
//	{
//		return getOutputs();
//	}
}