package edu.jhuapl.sbmt.pipeline.subscriber;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;

public class PairSink<L extends Object, R extends Object, O extends Object> implements IPipelineSubscriber<Pair<L, R>>
{
	private IPipelinePublisher<Pair<L, R>> publisher;
	private Pair<L, R>[] outputs;

	public static <L extends Object, R extends Object, O extends Object> PairSink<L, R, O> of(Pair<L, R>[] outputs)
	{
		return new PairSink<L, R, O>(outputs);
	}

	public PairSink(Pair<L, R>[] outputs)
	{
		this.outputs = outputs;
	}

	@Override
	public void receive(List<Pair<L, R>> items)
	{
		this.outputs[0] = Pair.of(items.get(0).getLeft(), items.get(0).getRight());
	}

	@Override
	public void receive(Pair<L, R> item)
	{
		receive(List.of(item));
	}

	@Override
	public void setPublisher(IPipelinePublisher<Pair<L, R>> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public PairSink<L, R, O> run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

//	@Override
//	public PairSink<L, R, O> run(Runnable completion) throws IOException, Exception
//	{
//		publisher.run();
//		completion.run();
//		return this;
//	}

//	@Override
//	public Pair<L, R> drip()
//	{
//		return outputs[0];
//	}
//
//	@Override
//	public List<Pair<L, R>> flow()
//	{
//		return List.of(outputs);
//	}

}
