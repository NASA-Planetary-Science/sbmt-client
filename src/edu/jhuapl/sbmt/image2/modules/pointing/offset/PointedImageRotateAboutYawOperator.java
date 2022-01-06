package edu.jhuapl.sbmt.image2.modules.pointing.offset;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class PointedImageRotateAboutYawOperator extends BasePipelineOperator<Pair<SpacecraftPointingState, SpacecraftPointingDelta>, Pair<SpacecraftPointingState, SpacecraftPointingDelta>>
{


	@Override
	public void processData() throws IOException, Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputPair = inputs.get(0);
		SpacecraftPointingState origState = inputPair.getLeft();
		SpacecraftPointingState scState = new SpacecraftPointingState(origState);
		SpacecraftPointingDelta scDelta = inputPair.getRight();
		double angleDegrees = scDelta.rotationOffset;

		double[] vout = new double[] { 0.0, 0.0, 0.0 };
    	MathUtil.vsub(origState.frustum1, origState.frustum3, vout);
    	MathUtil.unorm(vout, vout);
    	Rotation rotation = new Rotation(new Vector3D(vout), Math.toRadians(angleDegrees), RotationConvention.VECTOR_OPERATOR);
    	MathUtil.rotateVector(origState.frustum1, rotation, scState.frustum1);
        MathUtil.rotateVector(origState.frustum2, rotation, scState.frustum2);
        MathUtil.rotateVector(origState.frustum3, rotation, scState.frustum3);
        MathUtil.rotateVector(origState.frustum4, rotation, scState.frustum4);
        MathUtil.rotateVector(origState.boresightDirection, rotation, scState.boresightDirection);

		outputs.add(Pair.of(scState, scDelta));
	}
}
