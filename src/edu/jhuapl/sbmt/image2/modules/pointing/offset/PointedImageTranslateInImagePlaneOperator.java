package edu.jhuapl.sbmt.image2.modules.pointing.offset;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class PointedImageTranslateInImagePlaneOperator extends BasePipelineOperator<Pair<SpacecraftPointingState, SpacecraftPointingDelta>, Pair<SpacecraftPointingState, SpacecraftPointingDelta>>
{


	@Override
	public void processData() throws IOException, Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputPair = inputs.get(0);
		SpacecraftPointingState origState = inputPair.getLeft();
		SpacecraftPointingState scState = new SpacecraftPointingState(origState);
		SpacecraftPointingDelta scDelta = inputPair.getRight();
		double sampleDelta = scDelta.sampleOffset;
		double lineDelta = scDelta.lineOffset;
		double[] sampleAxis = new double[] { 0.0, 0.0, 0.0 };
    	MathUtil.vsub(origState.frustum1, origState.frustum2, sampleAxis);
    	MathUtil.unorm(sampleAxis, sampleAxis);
    	double[] lineAxis = new double[] { 0.0, 0.0, 0.0 };
    	MathUtil.vsub(origState.frustum1, origState.frustum3, lineAxis);
    	MathUtil.unorm(lineAxis, lineAxis);
    	MathUtil.vscl(sampleDelta, sampleAxis, sampleAxis);
    	MathUtil.vadd(origState.spacecraftPosition, sampleAxis, scState.spacecraftPosition);
    	MathUtil.vscl(lineDelta, lineAxis, lineAxis);
    	MathUtil.vadd(scState.spacecraftPosition, lineAxis, scState.spacecraftPosition);

		outputs.add(Pair.of(scState, scDelta));
	}
}
