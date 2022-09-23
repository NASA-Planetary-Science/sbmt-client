package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io;

import java.io.IOException;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.io.LoadCustomImageListFromFileOperator;

public class LoadFileToCustomImageListPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{

	private LoadFileToCustomImageListPipeline() throws IOException, Exception
	{
		new LoadCustomImageListFromFileOperator<G1>()
			.run();
	}

	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> LoadFileToCustomImageListPipeline<G1> of() throws IOException, Exception
	{
		return new LoadFileToCustomImageListPipeline<G1>();
	}

}
