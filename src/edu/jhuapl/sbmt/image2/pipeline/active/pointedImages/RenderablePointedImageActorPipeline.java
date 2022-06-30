package edu.jhuapl.sbmt.image2.pipeline.active.pointedImages;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInPNGHeaderReader;
import edu.jhuapl.sbmt.image2.modules.io.builtIn.BuiltInPNGReader;
import edu.jhuapl.sbmt.image2.modules.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.image2.modules.rendering.PointedImageRenderables;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerRotationOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerXFlipOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerYFlipOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.ScenePointedImageBuilderOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.PointingFileReader;

public class RenderablePointedImageActorPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements RenderableImageActorPipeline
{
	private List<vtkActor> renderableImageActors = Lists.newArrayList();
	private Pair<List<vtkActor>, List<PointedImageRenderables>>[] sceneOutputs = new Pair[1];
	private G1 image;
	private List<SmallBodyModel> smallBodyModels;
	private String filename, pointingFile;
	private Optional<String> modifiedPointingFile;
	private String flip;
	private double rotation;
	private List<Layer> updatedLayers = Lists.newArrayList();
	private IPipelinePublisher<HashMap<String, String>> metadataReader;
	private IPipelinePublisher<PointingFileReader> pointingPublisher = null;
	private IPipelinePublisher<PointingFileReader> modifiedPointingPublisher = null;
	List<RenderablePointedImage> renderableImages = Lists.newArrayList();

	public RenderablePointedImageActorPipeline(G1 image, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		this.image = image;
		this.smallBodyModels = smallBodyModels;

		loadFiles();
		generateImageLayer();
		pointingPublisher = generatePointing(pointingFile);
		modifiedPointingFile.ifPresent(file -> {
			try
			{
				modifiedPointingPublisher = generatePointing(file);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		generateRenderableImages();
		buildScene();
	}

	private void loadFiles()
	{
		filename = image.getFilename();
		pointingFile = image.getPointingSource();
		modifiedPointingFile = Optional.ofNullable(null);
		if (image.getModifiedPointingSource().isPresent()) modifiedPointingFile = image.getModifiedPointingSource();
		System.out.println("RenderablePointedImageActorPipeline: loadFiles: image pointing source " + image.getPointingSource());
		if (!new File(filename).exists())
		{
			filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
			pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
//			if (image.getModifiedPointingSource().isPresent())
//			{
//				modifiedPointingFile = Optional.of(FileCache.getFileFromServer(image.getModifiedPointingSource().get()).getAbsolutePath());
//			}
		}

		flip = image.getFlip();
		System.out.println("RenderablePointedImageActorPipeline: loadFiles: flip is " + flip + " for " + filename);
		rotation = image.getRotation();
	}

	private void generateImageLayer() throws Exception
	{
		//***********************
		//generate image layer
		//***********************
		//TODO: eventually replace this with a GDAL call to read in the data
		IPipelinePublisher<Layer> reader = null;
		if (FilenameUtils.getExtension(filename).toLowerCase().equals("fit") || FilenameUtils.getExtension(filename).toLowerCase().equals("fits"))
		{
			reader = new BuiltInFitsReader(filename, image.getFillValues());
			int[] interpolationDims = new int[] { reader.getOutputs().get(0).iSize(), reader.getOutputs().get(0).jSize()};
			if (image.getLinearInterpolatorDims() != null) interpolationDims = image.getLinearInterpolatorDims();
			IPipelineOperator<Layer, Layer> linearInterpolator = null;
			if (image.getLinearInterpolatorDims() == null)
				linearInterpolator = new PassthroughOperator<>();
			else
				linearInterpolator = new LayerLinearInterpolaterOperator(interpolationDims[0], interpolationDims[1]);
			LayerRotationOperator rotationOperator = new LayerRotationOperator(rotation);

			BasePipelineOperator<Layer, Layer> flipOperator = new PassthroughOperator<Layer>();
			if (flip.equals("X"))
				flipOperator = new LayerXFlipOperator();
			else if (flip.equals("Y"))
				flipOperator = new LayerYFlipOperator();

			reader
				.operate(linearInterpolator)
				.operate(rotationOperator)
				.operate(flipOperator)
				.subscribe(Sink.of(updatedLayers)).run();

			//generate metadata (in: filename, out: ImageMetadata)
			metadataReader = new BuiltInFitsHeaderReader(filename);
		}
		else if (FilenameUtils.getExtension(filename).toLowerCase().equals("png"))
		{
			reader = new BuiltInPNGReader(filename);
			metadataReader = new BuiltInPNGHeaderReader(filename);
			reader
				.subscribe(Sink.of(updatedLayers)).run();
		}

	}

	private IPipelinePublisher<PointingFileReader> generatePointing(String pointingFile) throws IOException
	{
		System.out.println("RenderablePointedImageActorPipeline: generatePointing: pointing file " + pointingFile);
		IPipelinePublisher<PointingFileReader> pointingPublisher = null;
		if (image.getPointingSourceType() == ImageSource.SPICE || pointingFile.endsWith(".adjusted"))
			pointingPublisher = new InfofileReaderPublisher(pointingFile);
		else
			pointingPublisher = new SumfileReaderPublisher(pointingFile);
		return pointingPublisher;
	}

	private void generateRenderableImages() throws IOException, Exception
	{
		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents =
				Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator =
				new RenderablePointedImageGenerator();


		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************

		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages)).run();

		image.setDefaultOffset(3.0 * smallBodyModels.get(0).getMinShiftAmount());
		if (image.getOffset() == -1) image.setOffset(image.getDefaultOffset());

		double diagonalLength = smallBodyModels.get(0).getBoundingBoxDiagonalLength();
		double[] scPos = renderableImages.get(0).getPointing().getSpacecraftPosition();

		for (RenderablePointedImage renderableImage : renderableImages)
		{
			if (modifiedPointingPublisher != null) renderableImage.setModifiedPointing(Optional.of(modifiedPointingPublisher.getOutputs().get(0)));
			renderableImage.setMasking(new LayerMasking(image.getMaskValues()));
			renderableImage.setOffset(image.getOffset());
			renderableImage.setDefaultOffset(image.getDefaultOffset());
			renderableImage.setIntensityRange(image.getIntensityRange());
			renderableImage.setOfflimbIntensityRange(image.getOfflimbIntensityRange());
			renderableImage.setMinFrustumLength(MathUtil.vnorm(scPos) - diagonalLength);
			renderableImage.setMaxFrustumLength(MathUtil.vnorm(scPos) + diagonalLength);
			image.setMinFrustumLength(MathUtil.vnorm(scPos) - diagonalLength);
			image.setMaxFrustumLength(MathUtil.vnorm(scPos) + diagonalLength);
			if (image.getOfflimbDepth() == 0)
				image.setOfflimbDepth(MathUtil.vnorm(scPos));
			renderableImage.setOfflimbDepth(image.getOfflimbDepth());
			renderableImage.setLinearInterpolation(image.getInterpolateState());
		}
	}

	private void buildScene() throws IOException, Exception
	{
		//*************************
		//zip the sources together
		//*************************
		IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderablePointedImage>>> sceneObjects =
				Publishers.formPair(Just.of(smallBodyModels), Just.of(renderableImages));

		//*****************************************************************************************************
		//Pass them into the scene builder to perform intersection calculations, and send actors to List
		//*****************************************************************************************************
		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderablePointedImage>>, Pair<List<vtkActor>, List<PointedImageRenderables>>> sceneBuilder =
				new ScenePointedImageBuilderOperator();

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();

	}


	@Override
	public List<vtkActor> getRenderableImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageActors.addAll(renderable.getFootprints());
		}
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageActors.addAll(renderable.getModifiedFootprintActors());
		}
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableImageBoundaryActors()
	{
		List<vtkActor> imageBoundaryActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageBoundaryActors.addAll(renderable.getBoundaries());
		}
		return imageBoundaryActors;
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageBoundaryActors()
	{
		List<vtkActor> imageBoundaryActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			imageBoundaryActors.addAll(renderable.getModifiedBoundaryActors());
		}
		return imageBoundaryActors;
	}

	public List<vtkActor> getRenderableImageFrustumActors()
	{
		List<vtkActor> frustumActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			frustumActors.add(renderable.getFrustum());
		}
		return frustumActors;
	}

	public List<vtkActor> getRenderableModifiedImageFrustumActors()
	{
		List<vtkActor> frustumActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			frustumActors.add(renderable.getModifiedFrustumActor());
		}
		return frustumActors;
	}

	public List<vtkActor> getRenderableOfflimbImageActors()
	{
		List<vtkActor> offLimbActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			if (renderable.getOffLimb() == null) continue;
			offLimbActors.add(renderable.getOffLimb());
//			offLimbActors.add(renderable.getOffLimbBoundary());
		}
		return offLimbActors;
	}

	@Override
	public List<vtkActor> getSmallBodyActors()
	{
		return sceneOutputs[0].getLeft();
	}


	@Override
	public List<vtkActor> getRenderableOffLimbBoundaryActors()
	{
		List<vtkActor> offLimbActors = Lists.newArrayList();
		for (PointedImageRenderables renderable : sceneOutputs[0].getRight())
		{
			if (renderable.getOffLimb() == null) continue;
//			offLimbActors.add(renderable.getOffLimb());
			offLimbActors.add(renderable.getOffLimbBoundary());
		}
		return offLimbActors;
	}
}
