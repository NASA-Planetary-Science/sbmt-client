package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.search;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.search.CreateImageFromSearchResultOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.search.ImageSearchOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class ImageSearchPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	List<G1> images = Lists.newArrayList();

	public ImageSearchPipeline(SmallBodyViewConfig viewConfig, ModelManager modelManager, ImageSearchParametersModel searchParamatersModel) throws Exception
	{
		Just.of(searchParamatersModel)
			.operate(new ImageSearchOperator(viewConfig, modelManager))
			.operate(new CreateImageFromSearchResultOperator<G1>())
			.subscribe(Sink.of(images))
			.run();
	}

	public List<G1> getImages()
	{
		return images;
	}
}