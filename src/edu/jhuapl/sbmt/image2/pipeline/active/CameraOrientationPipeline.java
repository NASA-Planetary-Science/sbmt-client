package edu.jhuapl.sbmt.image2.pipeline.active;

import java.util.List;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.modules.rendering.RenderableImage;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class CameraOrientationPipeline
{
	private double[] focalPoint = new double[3];

	public CameraOrientationPipeline(RenderableImage renderableImage, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		InfoFileReader infoReader = renderableImage.getPointing();
		double[] spacecraftPosition = infoReader.getSpacecraftPosition();
    	double[] boresightDirection = infoReader.getBoresightDirection();


		// Normalize the direction vector
		double[] direction = new double[3];
		MathUtil.unorm(boresightDirection, direction);

		int cellId = smallBodyModels.get(0).computeRayIntersection(spacecraftPosition, direction, focalPoint);

		if (cellId < 0)
		{
			RenderableImageFootprintGeneratorPipeline pipeline =
					new RenderableImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
			BoundingBox bb = new BoundingBox(pipeline.getFootprintPolyData().get(0).GetBounds());
			double[] centerPoint = bb.getCenterPoint();
			// double[] centerPoint = footprint[currentSlice].GetPoint(0);
			double distanceToCenter = MathUtil.distanceBetween(spacecraftPosition, centerPoint);

			focalPoint[0] = spacecraftPosition[0] + distanceToCenter * direction[0];
			focalPoint[1] = spacecraftPosition[1] + distanceToCenter * direction[1];
			focalPoint[2] = spacecraftPosition[2] + distanceToCenter * direction[2];
		}

	}

	public double[] getCameraOrientation()
	{
		return focalPoint;
	}

}
