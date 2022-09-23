package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.CylindricalBounds;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerRotationOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerXFlipOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerYFlipOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInPNGHeaderReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.builtin.BuiltInPNGReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.gdal.GDALReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.ValidityCheckerDoubleFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class IPerspectiveImageToLayerAndMetadataPipeline
{
	static boolean useGDAL = true;
	private List<Layer> updatedLayers = Lists.newArrayList();
	private List<HashMap<String, String>> metadata = Lists.newArrayList();
	private IPipelinePublisher<HashMap<String, String>> metadataReader = null;

	private IPerspectiveImageToLayerAndMetadataPipeline(IPerspectiveImage image) throws InvalidGDALFileTypeException, IOException, Exception
	{
		IPipelinePublisher<Layer> reader = null;
		String fileName = image.getFilename();
		if (!new File(fileName).exists())
			fileName = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
		CylindricalBounds bounds = image.getBounds();

		if (FilenameUtils.getExtension(fileName).toLowerCase().equals("fit") || FilenameUtils.getExtension(fileName).toLowerCase().equals("fits"))
		{
			if (useGDAL)
				reader = new GDALReader(fileName, false, new ValidityCheckerDoubleFactory().allValid(), Double.NaN);
			else
				reader = new BuiltInFitsReader(fileName, new double[] {});
			metadataReader = new BuiltInFitsHeaderReader(fileName);
		}
		else if (FilenameUtils.getExtension(fileName).toLowerCase().equals("png"))
		{
			if (useGDAL)
				reader = new GDALReader(fileName, false, new ValidityCheckerDoubleFactory().allValid(), Double.NaN);
			else
				reader = new BuiltInPNGReader(fileName);
			metadataReader = new BuiltInPNGHeaderReader(fileName);
		}
		else
		{
			if (useGDAL)
				reader = new GDALReader(fileName, false, new ValidityCheckerDoubleFactory().allValid(), Double.NaN);
		}
		IPipelineOperator<Layer, Layer> linearInterpolator = null;
		if (image.getLinearInterpolatorDims() == null || (image.getLinearInterpolatorDims()[0] == 0 && image.getLinearInterpolatorDims()[1] == 0))
			linearInterpolator = new PassthroughOperator<>();
		else
			linearInterpolator = new LayerLinearInterpolaterOperator(image.getLinearInterpolatorDims()[0], image.getLinearInterpolatorDims()[1]);

		LayerRotationOperator rotationOperator = new LayerRotationOperator(image.getRotation());

		BasePipelineOperator<Layer, Layer> flipOperator = new PassthroughOperator<Layer>();
		if (image.getFlip().equals("X"))
			flipOperator = new LayerXFlipOperator();
		else if (image.getFlip().equals("Y"))
			flipOperator = new LayerYFlipOperator();
		reader
			.operate(linearInterpolator)
			.operate(flipOperator)
			.operate(rotationOperator)
			.subscribe(Sink.of(updatedLayers)).run();

	}

	public static IPerspectiveImageToLayerAndMetadataPipeline of(IPerspectiveImage image) throws InvalidGDALFileTypeException, IOException, Exception
	{
		return new IPerspectiveImageToLayerAndMetadataPipeline(image);
	}

	public List<Layer> getLayers()
	{
		return updatedLayers;
	}

	public List<HashMap<String, String>> getMetadata()
	{

		for (int i=0; i<updatedLayers.size(); i++)
		{
			if (metadataReader == null)
			{
				HashMap<String, String> meta = new HashMap<String, String>();
				metadata.add(meta);
			}
			else
				metadata.add(metadataReader.getOutput());
		}
		return metadata;
	}
}
