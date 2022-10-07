package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.common.client.ISmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.search.SearchResultsToPointingFilesOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.preview.RenderableImagePipeline;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.TripleSink;

public class FilenameToRenderableImagePipeline
{
	private List<RenderablePointedImage> images;
	private List<String> pointingFilenames;

	public FilenameToRenderableImagePipeline(String filename, ImageSource imageSource, ISmallBodyViewConfig config, ImagingInstrument selectedInstrument) throws IOException, Exception
	{
		Triple<List<List<String>>, ImagingInstrument, List<String>>[] tripleSink = new Triple[1];
        List<List<String>> fileInputs = List.of(List.of(filename, "", imageSource.toString()));
        IPipelineOperator<Pair<List<List<String>>, ImagingInstrument>, Triple<List<List<String>>, ImagingInstrument, List<String>>> searchToPointingFilesOperator
        		= new SearchResultsToPointingFilesOperator(config);
        Just.of(Pair.of(fileInputs, selectedInstrument))
			.operate(searchToPointingFilesOperator)
			.subscribe(TripleSink.of(tripleSink))
			.run();

        pointingFilenames = tripleSink[0].getRight();

    	RenderableImagePipeline pipeline = new RenderableImagePipeline(filename, pointingFilenames.get(0), selectedInstrument, imageSource);
    	images = pipeline.getOutput();
	}

	public static FilenameToRenderableImagePipeline of(String filename, ImageSource imageSource, ISmallBodyViewConfig config, ImagingInstrument selectedInstrument) throws IOException, Exception
	{
		return new FilenameToRenderableImagePipeline(filename, imageSource, config, selectedInstrument);
	}

	public List<RenderablePointedImage> getImages()
	{
		return images;
	}

	public List<String> getPointingFilenames()
	{
		return pointingFilenames;
	}
}
