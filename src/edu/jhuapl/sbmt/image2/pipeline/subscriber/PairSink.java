package edu.jhuapl.sbmt.image2.pipeline.subscriber;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;

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
	public void setPublisher(IPipelinePublisher<Pair<L, R>> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public void run() throws IOException, Exception
	{
		publisher.run();
	}

}
