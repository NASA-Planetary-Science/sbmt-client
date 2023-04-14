package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.io.ImageGalleryOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

public class ImageGalleryPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{

	private ImageGalleryPipeline(IImagingInstrument instrument, List<G1> images) throws IOException, Exception
	{
		Just.of(Pair.of(instrument, images))
		  .operate(new ImageGalleryOperator<>())
		  .run();
	}

	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> ImageGalleryPipeline<G1> of(IImagingInstrument instrument, List<G1> images) throws IOException, Exception
	{
		return new ImageGalleryPipeline<G1>(instrument, images);
	}

}
