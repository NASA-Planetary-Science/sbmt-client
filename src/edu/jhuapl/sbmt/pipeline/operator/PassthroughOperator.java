package edu.jhuapl.sbmt.pipeline.operator;

import java.io.IOException;

public class PassthroughOperator<T1> extends BasePipelineOperator<T1, T1>
{
	public PassthroughOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		for (T1 layer : inputs)
			outputs.add(layer);
	}
}
