package edu.jhuapl.sbmt.image2.modules.rendering;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkPointData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkTexture;

import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class RenderableImageFootprintOperator extends BasePipelineOperator<RenderableImage, vtkPolyData>
{
	List<SmallBodyModel> smallBodyModels;

	public RenderableImageFootprintOperator(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		RenderableImage renderableImage = inputs.get(0);
		InfoFileReader infoReader = renderableImage.getPointing();

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


        VtkImageRenderer imageRenderer = new VtkImageRenderer();
        List<vtkImageData> imageData = Lists.newArrayList();
        Just.of(renderableImage.getLayer())
        	.operate(imageRenderer)
        	.operate(new VtkImageContrastOperator(null))
        	.subscribe(Sink.of(imageData)).run();

    	for (SmallBodyModel smallBody : smallBodyModels)
    	{
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

			vtkTexture imageTexture = new vtkTexture();
	        imageTexture.InterpolateOn();
	        imageTexture.RepeatOff();
	        imageTexture.EdgeClampOn();
	        imageTexture.SetInputData(imageData.get(0));

			vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			mapper.SetInputData(footprint);
			outputs.add(footprint);

//			vtkActor actor = new vtkActor();
//			actor.SetMapper(mapper);
//			actor.SetTexture(imageTexture);
//	        vtkProperty footprintProperty = actor.GetProperty();
//	        footprintProperty.LightingOff();
//	        footprints.add(actor);
    	}
	}
}
