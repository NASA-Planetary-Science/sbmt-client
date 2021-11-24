package edu.jhuapl.sbmt.image2.modules.rendering;

import java.io.IOException;

import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class LayerXFlipOperator extends BasePipelineOperator<Layer, Layer>
{
    protected final LayerTransformFactory TransformFactory = new LayerTransformFactory();


	public LayerXFlipOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		Layer rotatedLayer = TransformFactory.flipAboutX().apply(inputs.get(0));
		outputs.add(rotatedLayer);
	}

}
