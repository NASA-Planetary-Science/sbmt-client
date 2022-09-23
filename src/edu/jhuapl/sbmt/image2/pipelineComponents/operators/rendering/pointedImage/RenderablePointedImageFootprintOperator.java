package edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkPointData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkTexture;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.model.IRenderableImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.vtk.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.LoadPolydataFromCachePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.SavePolydataToCachePipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class RenderablePointedImageFootprintOperator extends BasePipelineOperator<IRenderableImage, Pair<List<vtkImageData>, List<vtkPolyData>>>
{
	List<SmallBodyModel> smallBodyModels;

	public RenderablePointedImageFootprintOperator(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		RenderablePointedImage renderableImage = (RenderablePointedImage)inputs.get(0);
		PointingFileReader infoReader = renderableImage.getPointing();
		double[] spacecraftPositionAdjusted = infoReader.getSpacecraftPosition();
    	double[] frustum1Adjusted = infoReader.getFrustum1();
    	double[] frustum2Adjusted = infoReader.getFrustum2();
    	double[] frustum3Adjusted = infoReader.getFrustum3();
    	double[] frustum4Adjusted = infoReader.getFrustum4();
    	Frustum frustum = new Frustum(spacecraftPositionAdjusted,
						    			frustum1Adjusted,
						    			frustum3Adjusted,
						    			frustum4Adjusted,
						    			frustum2Adjusted);


        VtkImageRendererOperator imageRenderer = new VtkImageRendererOperator();
        List<vtkImageData> imageData = Lists.newArrayList();
        Just.of(renderableImage.getLayer())
        	.operate(imageRenderer)
        	.operate(new VtkImageContrastOperator(renderableImage.getIntensityRange()))
        	.operate(new VtkImageVtkMaskingOperator(renderableImage.getMasking().getMask()))
        	.subscribe(Sink.of(imageData)).run();
        List<vtkPolyData> footprints = Lists.newArrayList();
    	for (SmallBodyModel smallBody : smallBodyModels)
    	{
    		String imageFilename = getPrerenderingFileNameBase(renderableImage, smallBody) + "_footprintImageData.vtk.gz";
    		vtkPolyData existingFootprint = LoadPolydataFromCachePipeline.of(imageFilename).orNull();
    		if (existingFootprint != null)
    		{
    			footprints.add(existingFootprint);
    			continue;
    		}

    		vtkFloatArray textureCoords = new vtkFloatArray();
    		vtkPolyData tmp = null;
    		vtkPolyData footprint = new vtkPolyData();
	        tmp = smallBody.computeFrustumIntersection(spacecraftPositionAdjusted,
	        															frustum1Adjusted,
	        															frustum3Adjusted,
	        															frustum4Adjusted,
	        															frustum2Adjusted);

	        if (tmp == null) return;

	        // Need to clear out scalar data since if coloring data is being shown,
	        // then the color might mix-in with the image.
	        tmp.GetCellData().SetScalars(null);
	        tmp.GetPointData().SetScalars(null);

	        footprint.DeepCopy(tmp);
	        vtkPointData pointData = footprint.GetPointData();
	        pointData.SetTCoords(textureCoords);
	        PolyDataUtil.generateTextureCoordinates(frustum, renderableImage.getImageWidth(), renderableImage.getImageHeight(), footprint);
	        pointData.Delete();
	        PolyDataUtil.shiftPolyDataInNormalDirection(footprint, renderableImage.getOffset());
			vtkTexture imageTexture = new vtkTexture();
	        imageTexture.InterpolateOn();
	        imageTexture.RepeatOff();
	        imageTexture.EdgeClampOn();
	        imageTexture.SetInputData(imageData.get(0));

			vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			mapper.SetInputData(footprint);
			System.out.println("RenderablePointedImageFootprintOperator: processData: writing to " + imageFilename);
			SavePolydataToCachePipeline.of(footprint, imageFilename);
			footprints.add(footprint);
    	}
    	outputs.add(Pair.of(imageData, footprints));
	}

    private String getPrerenderingFileNameBase(RenderablePointedImage renderableImage, SmallBodyModel smallBodyModel)
    {
        String imageName = renderableImage.getFilename();
        String topPath = FileCache.instance().getFile(imageName).getParent();
        String result = SafeURLPaths.instance().getString(topPath, "support",
        												  renderableImage.getImageSource().name(),
        												  FilenameUtils.getBaseName(imageName) + "_" + smallBodyModel.getModelResolution());

        return result;
    }
}
