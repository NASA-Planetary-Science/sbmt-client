package edu.jhuapl.sbmt.image2.pipeline.operator;

import java.io.IOException;

import edu.jhuapl.sbmt.image2.api.Layer;

public class LayerPassthroughOperator extends BasePipelineOperator<Layer, Layer>
{
	public LayerPassthroughOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs.add(inputs.get(0));
	}
}
