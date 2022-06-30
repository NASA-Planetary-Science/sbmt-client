package edu.jhuapl.sbmt.image2.modules.search;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;

public class LoadImagesFromSavedFilePipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	List<G1> images = Lists.newArrayList();

	public LoadImagesFromSavedFilePipeline(SmallBodyViewConfig viewConfig, String filename, IImagingInstrument instrument) throws Exception
	{
		Just.of(Pair.of(filename, instrument))
			.operate(new LoadImageListOperator())
			.operate(new CreateImageFromSavedListOperator<G1>(viewConfig))
			.subscribe(Sink.of(images))
			.run();
	}

	public List<G1> getImages()
	{
		return images;
	}
}
