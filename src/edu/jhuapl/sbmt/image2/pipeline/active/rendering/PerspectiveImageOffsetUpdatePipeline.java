package edu.jhuapl.sbmt.image2.pipeline.active.rendering;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

public class PerspectiveImageOffsetUpdatePipeline
{
	PerspectiveImageOffsetUpdatePipeline(PerspectiveImageCollection collection, PerspectiveImage image, double offset)
	{
		image.setOffset(offset);
		collection.updateImage(image);
	}

	public static void of(PerspectiveImageCollection collection, PerspectiveImage image, double offset)
	{
		new PerspectiveImageOffsetUpdatePipeline(collection, image, offset);
	}
}
