package edu.jhuapl.sbmt.image2.pipeline.rendering;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

public class PerspectiveImageOffsetUpdatePipeline
{
	PerspectiveImageOffsetUpdatePipeline(PerspectiveImageCollection collection, IPerspectiveImage image, double offset)
	{
		image.setOffset(offset);
		collection.updateImage(image);
	}

	public static void of(PerspectiveImageCollection collection, IPerspectiveImage image, double offset)
	{
		new PerspectiveImageOffsetUpdatePipeline(collection, image, offset);
	}
}
