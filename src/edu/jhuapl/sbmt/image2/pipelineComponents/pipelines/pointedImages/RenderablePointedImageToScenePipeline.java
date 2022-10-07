package edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.pointedImages;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.ScenePointedImageBuilderOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.ImageToScenePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class RenderablePointedImageToScenePipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends ImageToScenePipeline
{
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

	public RenderablePointedImageToScenePipeline(G1 image, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		sceneOutputs = new Pair[1];
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
		rotation = image.getRotation();
	}

	private void generateImageLayer() throws InvalidGDALFileTypeException, Exception
	{
		IPerspectiveImageToLayerAndMetadataPipeline inputPipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(image);
		updatedLayers = inputPipeline.getLayers();
		metadataReader = Just.of(inputPipeline.getMetadata());
	}

	private IPipelinePublisher<PointingFileReader> generatePointing(String pointingFile) throws IOException
	{
		IPipelinePublisher<PointingFileReader> pointingPublisher = null;
		if (image.getPointingSourceType() == ImageSource.SPICE || image.getPointingSourceType() == ImageSource.CORRECTED_SPICE || pointingFile.endsWith(".adjusted"))
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
			renderableImage.setImageSource(image.getPointingSourceType());
			renderableImage.setOfflimbShowing(image.isOfflimbShowing());
			renderableImage.setFilename(image.getFilename());
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
		List<RenderablePointedImage> allImages = Lists.newArrayList();
		for (int i=0; i<smallBodyModels.size(); i++) allImages.addAll(renderableImages);

		IPipelinePublisher<Pair<SmallBodyModel, RenderablePointedImage>> sceneObjects =
				Publishers.formPair(Just.of(smallBodyModels), Just.of(allImages));

		//*****************************************************************************************************
		//Pass them into the scene builder to perform intersection calculations, and send actors to List
		//*****************************************************************************************************
		IPipelineOperator<Pair<SmallBodyModel, RenderablePointedImage>, Pair<List<vtkActor>, List<ImageRenderable>>> sceneBuilder =
				new ScenePointedImageBuilderOperator();

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();
	}
}