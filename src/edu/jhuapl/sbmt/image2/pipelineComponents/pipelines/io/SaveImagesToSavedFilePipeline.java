package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io;

import java.util.List;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.search.SaveImageListOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

public class SaveImagesToSavedFilePipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	List<IPerspectiveImage> images = Lists.newArrayList();

	public SaveImagesToSavedFilePipeline(ImmutableSet<G1> images) throws Exception
	{
		Just.of(images)
			.operate(new SaveImageListOperator<G1>())
			.run();
	}

	public List<IPerspectiveImage> getImages()
	{
		return images;
	}
}
