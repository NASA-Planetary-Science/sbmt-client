package edu.jhuapl.sbmt.image2.pipeline.active;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.CylindricalBounds;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.RenderableCylindricalImageGenerator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class CylindricalImageToRenderableImagePipeline
{
	List<RenderableCylindricalImage> renderableImages = Lists.newArrayList();
	List<HashMap<String, String>> metadata = Lists.newArrayList();

	public CylindricalImageToRenderableImagePipeline(List<IPerspectiveImage> images) throws Exception
	{
		for (IPerspectiveImage image : images)
		{
			String filename = image.getFilename();
			String pointingFile = image.getPointingSource();

			if (!new File(filename).exists())
			{
				filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
				pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
			}

			IPipelinePublisher<Layer> reader = new BuiltInFitsReader(filename, new double[] {});
			IPipelineOperator<Layer, Layer> linearInterpolator = null;
			if (image.getLinearInterpolatorDims() == null)
				linearInterpolator = new PassthroughOperator<>();
			else
				linearInterpolator = new LayerLinearInterpolaterOperator(image.getLinearInterpolatorDims()[0], image.getLinearInterpolatorDims()[1]);

			List<Layer> updatedLayers = Lists.newArrayList();
			reader
				.operate(linearInterpolator)
				.subscribe(Sink.of(updatedLayers)).run();
			//generate image pointing (in: filename, out: ImagePointing)
//			IPipelinePublisher<PointingFileReader> pointingPublisher = null;
//			if (image.getPointingSourceType() == ImageSource.SPICE)
//				pointingPublisher = new InfofileReaderPublisher(pointingFile);
//			else
//				pointingPublisher = new SumfileReaderPublisher(pointingFile);

			//generate metadata (in: filename, out: ImageMetadata)
			IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(filename);

			metadataReader.subscribe(Sink.of(metadata)).run();

			//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
			IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
			IPipelinePublisher<CylindricalBounds> boundsPublisher = Just.of(image.getBounds());
			IPipelinePublisher<Triple<Layer, HashMap<String, String>, CylindricalBounds>> imageComponents = Publishers.formTriple(layerPublisher, metadataReader, boundsPublisher);

			IPipelineOperator<Triple<Layer, HashMap<String, String>, CylindricalBounds>, RenderableCylindricalImage> renderableImageGenerator = new RenderableCylindricalImageGenerator();


			//***************************************************************************************
			//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
			//***************************************************************************************
			List<RenderableCylindricalImage> renderableImage = Lists.newArrayList();
			imageComponents
				.operate(renderableImageGenerator)
				.subscribe(Sink.of(renderableImage)).run();
			renderableImages.addAll(renderableImage);

//			System.out.println(
//					"PerspectiveImageToRenderableImagePipeline: PerspectiveImageToRenderableImagePipeline: image off depth " + image.getOfflimbDepth());
//			System.out.println(
//					"PerspectiveImageToRenderableImagePipeline: PerspectiveImageToRenderableImagePipeline: image min frus " + image.getMinFrustumLength());
//			System.out.println(
//					"PerspectiveImageToRenderableImagePipeline: PerspectiveImageToRenderableImagePipeline: image max frus " + image.getMaxFrustumLength());

//			for (RenderablePointedImage renderableImg : renderableImages)
//			{
//				renderableImg.setMasking(new LayerMasking(image.getMaskValues()));
//				renderableImg.setOffset(image.getOffset());
//				renderableImg.setDefaultOffset(image.getDefaultOffset());
//				renderableImg.setIntensityRange(image.getIntensityRange());
//				renderableImg.setOfflimbIntensityRange(image.getOfflimbIntensityRange());
////				double diagonalLength = smallBodyModel.get(0).getBoundingBoxDiagonalLength();
////				System.out.println("RenderablePointedImageActorPipeline: RenderablePointedImageActorPipeline: diag length " + diagonalLength);
////				double[] scPos = renderableImages.get(0).getPointing().getSpacecraftPosition();
//				renderableImg.setMinFrustumLength(image.getMinFrustumLength());
//				renderableImg.setMaxFrustumLength(image.getMaxFrustumLength());
//				renderableImg.setOfflimbDepth(image.getOfflimbDepth());
//				renderableImg.setLinearInterpolation(image.getInterpolateState());
//			}
		}
	}

	public List<RenderableCylindricalImage> getRenderableImages()
	{
		return renderableImages;
	}

	public List<HashMap<String, String>> getMetadata()
	{
		return metadata;
	}
}
