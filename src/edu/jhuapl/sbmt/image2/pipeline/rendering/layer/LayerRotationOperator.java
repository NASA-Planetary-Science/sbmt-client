package edu.jhuapl.sbmt.image2.pipeline.rendering.layer;

import java.io.IOException;
import java.util.function.Function;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LayerRotationOperator extends BasePipelineOperator<Layer, Layer>
{
    protected final LayerTransformFactory TransformFactory = new LayerTransformFactory();
    private double rotation;

	public LayerRotationOperator(double rotation)
	{
		this.rotation = rotation;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		Function<Layer, Layer> rotationType = null;
		if (rotation == 0.0)
		{
			outputs.add(inputs.get(0));
			return;
		}
		else if (rotation == 90.0)
		{
			rotationType = TransformFactory.rotateCCW();
		}
		else if (rotation == 180.0)
		{
			rotationType = TransformFactory.rotateHalfway();
		}
		else if (rotation == 270.0)
		{
			rotationType = TransformFactory.rotateCW();
		}

		Layer rotatedLayer = rotationType.apply(inputs.get(0));
		outputs.add(rotatedLayer);
	}

}
