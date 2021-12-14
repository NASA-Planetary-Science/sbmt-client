package edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.image2.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class RenderableCylindricalImageFootprintOperator extends BasePipelineOperator<RenderableCylindricalImage, vtkPolyData>
{
	private final List<SmallBodyModel> smallBodyModels;

	public RenderableCylindricalImageFootprintOperator(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		RenderableCylindricalImage renderableImage = inputs.get(0);
		CylindricalBounds bounds = renderableImage.getBounds();
		double lllat = bounds.getMinLatitude();
        double lllon = bounds.getMinLongitude();
        double urlat = bounds.getMaxLatitude();
        double urlon = bounds.getMaxLongitude();
		for (SmallBodyModel smallBodyModel : smallBodyModels)
		{
			BasePipelineOperator<vtkPolyData, vtkPolyData> latitudeClipOperator = null;
			if (lllat != -90.0 || lllon != 0.0 || urlat != 90.0 || urlon != 360.0)
	        {
	            if (smallBodyModel.isEllipsoid())
	            	latitudeClipOperator
	            		= new EllipsoidCylindicalClipOperator(smallBodyModel, lllat, urlat);
	            else
	            	latitudeClipOperator
	            		= new GeneralShapeCylindicalClipOperator(lllat, urlat);
	        }
			else
			{
				latitudeClipOperator = new PassthroughOperator<vtkPolyData>();
			}

			BasePipelineOperator<vtkPolyData, vtkPolyData> longitudeClipOperator
				= new PartialCylindricalClipOperator(lllat, lllon, urlat, urlon);

			List<vtkPolyData> polyDatas = Lists.newArrayList();
			Just.of(smallBodyModel.getSmallBodyPolyData())
				.operate(latitudeClipOperator)
				.operate(longitudeClipOperator)
				.subscribe(Sink.of(polyDatas))
				.run();

	        // Need to clear out scalar data since if coloring data is being shown,
	        // then the color might mix-in with the image.
	        polyDatas.get(0).GetCellData().SetScalars(null);
	        polyDatas.get(0).GetPointData().SetScalars(null);

	        vtkPolyData shiftedFootprint = new vtkPolyData();
	        shiftedFootprint.DeepCopy(polyDatas.get(0));
	        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, renderableImage.getOffset());
	        outputs.add(shiftedFootprint);
		}
	}
}
