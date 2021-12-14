package edu.jhuapl.sbmt.image2.pipeline.operator;

import java.io.IOException;

public class PassthroughOperator<T1> extends BasePipelineOperator<T1, T1>
{
	public PassthroughOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs.add(inputs.get(0));
	}
}
