package edu.jhuapl.sbmt.image2.modules.pointing.offset;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.PairSink;

public class PointedImageEditingPipeline
{
	Pair<SpacecraftPointingState, SpacecraftPointingDelta>[] finalAdjustment = new Pair[1];

	public PointedImageEditingPipeline(SpacecraftPointingState pointingState, SpacecraftPointingDelta pointingDelta) throws Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputs =
				Pair.of(pointingState, pointingDelta);

//		System.out.println("PointedImageEditingPipeline: PointedImageEditingPipeline: original state " + pointingState);

		Just.of(inputs)
			.operate(new PointedImageRotateTargetPixelDirToLocalOriginOperator())
			.operate(new PointedImageTranslateInImagePlaneOperator())
			.operate(new PointedImageRotateAboutTargetOperator())
			.operate(new PointedImageZoomOperator())
			.subscribe(PairSink.of(finalAdjustment))
			.run();
	}

	public Pair<SpacecraftPointingState, SpacecraftPointingDelta> getFinalState()
	{
		return finalAdjustment[0];
	}
}
