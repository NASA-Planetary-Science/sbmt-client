package edu.jhuapl.sbmt.image2.pipeline.rendering.layer;

import java.io.IOException;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LayerYFlipOperator extends BasePipelineOperator<Layer, Layer>
{
    protected final LayerTransformFactory TransformFactory = new LayerTransformFactory();


	public LayerYFlipOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		Layer rotatedLayer = TransformFactory.flipAboutY().apply(inputs.get(0));
		outputs.add(rotatedLayer);
	}

}
