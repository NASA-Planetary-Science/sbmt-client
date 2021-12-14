package edu.jhuapl.sbmt.image2.modules.rendering.layer;

import java.io.IOException;

import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class LayerLinearInterpolaterOperator extends BasePipelineOperator<Layer, Layer>
{
    protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();
    private int width, height;

	public LayerLinearInterpolaterOperator(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs.add((DoubleTransformFactory.linearInterpolate(width, height).apply(inputs.get(0))));
	}

}
