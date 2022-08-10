package edu.jhuapl.sbmt.image2.pipeline;

import java.io.IOException;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipeline.io.export.SaveImageStateToDiskOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

public class SaveImageStateToDiskPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	private SaveImageStateToDiskPipeline(G1 image) throws IOException, Exception
	{
		Just.of(image)
			.subscribe(new SaveImageStateToDiskOperator<G1>())
			.run();
	}

	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> void of(G1 image) throws IOException, Exception
	{
		SaveImageStateToDiskPipeline<G1> pipeline = new SaveImageStateToDiskPipeline<G1>(image);
	}
}
