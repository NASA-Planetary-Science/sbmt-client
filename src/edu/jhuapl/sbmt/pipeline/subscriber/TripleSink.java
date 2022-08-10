package edu.jhuapl.sbmt.pipeline.subscriber;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;

public class TripleSink<L extends Object, M extends Object, R extends Object, O extends Object> implements IPipelineSubscriber<Triple<L, M, R>>
{
	private IPipelinePublisher<Triple<L, M, R>> publisher;
	private Triple<L, M, R>[] outputs;

	public static <L extends Object, M extends Object, R extends Object, O extends Object> TripleSink<L, M, R, O> of(Triple<L, M, R>[] outputs)
	{
		return new TripleSink<L, M, R, O>(outputs);
	}

	public TripleSink(Triple<L, M, R>[] outputs)
	{
		this.outputs = outputs;
	}

	@Override
	public void receive(List<Triple<L, M, R>> items)
	{
		this.outputs[0] = Triple.of(items.get(0).getLeft(), items.get(0).getMiddle(), items.get(0).getRight());
	}

	@Override
	public void receive(Triple<L, M, R> item)
	{
		receive(List.of(item));
	}

	@Override
	public void setPublisher(IPipelinePublisher<Triple<L, M, R>> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public TripleSink<L, M, R, O> run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}
}
