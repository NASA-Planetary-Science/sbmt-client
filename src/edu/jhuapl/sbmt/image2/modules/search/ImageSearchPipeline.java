package edu.jhuapl.sbmt.image2.modules.search;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class ImageSearchPipeline
{
	List<IPerspectiveImage> images = Lists.newArrayList();

	public ImageSearchPipeline(SmallBodyViewConfig viewConfig, ModelManager modelManager, ImageSearchParametersModel searchParamatersModel) throws Exception
	{
		Just.of(searchParamatersModel)
			.operate(new ImageSearchOperator(viewConfig, modelManager))
			.operate(new CreateImageFromSearchResultOperator(viewConfig))
			.subscribe(Sink.of(images))
			.run();
	}

	public List<IPerspectiveImage> getImages()
	{
		return images;
	}
}
