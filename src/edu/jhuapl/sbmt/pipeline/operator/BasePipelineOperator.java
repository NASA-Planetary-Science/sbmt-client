package edu.jhuapl.sbmt.pipeline.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class BasePipelineOperator<InputType, OutputType> implements IPipelineOperator<InputType, OutputType>
{
	protected List<InputType> inputs;
    protected List<OutputType> outputs = new ArrayList<OutputType>();
	protected IPipelinePublisher<InputType> publisher;
	protected IPipelineSubscriber<OutputType> subscriber;


	@Override
	public void processData() throws IOException, Exception
	{

	}

	@Override
	public void publish() throws IOException, Exception
	{
		if (outputs.size() == 1)
			subscriber.receive(outputs.get(0));
		else
			subscriber.receive(outputs);
	}

	@Override
	public IPipelinePublisher<OutputType> subscribe(IPipelineSubscriber<OutputType> subscriber)
	{
		this.subscriber = subscriber;
		this.subscriber.setPublisher(this);
		return this;
	}

	@Override
	public <T extends Object> IPipelineOperator<OutputType, T> operate(IPipelineOperator<OutputType, T> operator)
	{
		this.subscriber = operator;
		this.subscriber.setPublisher(this);
		return operator;
	}

	@Override
	public void setPublisher(IPipelinePublisher<InputType> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public List<OutputType> getOutputs()
	{
		return outputs;
	}

	@Override
	public OutputType getOutput()
	{
		return outputs.get(0);
	}

	@Override
	public BasePipelineOperator<InputType, OutputType> run() throws IOException, Exception
	{
		publisher.run();
		if (subscriber == null) return this;
		publish();
		return this;
	}

	@Override
	public void receive(List<InputType> items) throws IOException, Exception
	{
		this.inputs = items;
		processData();
	}

	@Override
	public void receive(InputType item) throws IOException, Exception
	{
		this.inputs = List.of(item);
		processData();
	}
}
