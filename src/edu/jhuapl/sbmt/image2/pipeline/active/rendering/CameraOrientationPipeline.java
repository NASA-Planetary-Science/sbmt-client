package edu.jhuapl.sbmt.image2.pipeline.active.rendering;

import java.util.List;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.model.image.PointingFileReader;

public class CameraOrientationPipeline
{
	private double[] focalPoint = new double[3];
	double[] spacecraftPosition = new double[3];
	double[] boresightDirection = new double[3];
	double[] upVector = new double[3];

	public CameraOrientationPipeline(RenderablePointedImage renderableImage, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		PointingFileReader infoReader = renderableImage.getPointing();
		spacecraftPosition = infoReader.getSpacecraftPosition();
    	boresightDirection = infoReader.getBoresightDirection();
    	upVector = infoReader.getUpVector();

		// Normalize the direction vector
		double[] direction = new double[3];
		MathUtil.unorm(boresightDirection, direction);

		int cellId = smallBodyModels.get(0).computeRayIntersection(spacecraftPosition, direction, focalPoint);

		if (cellId < 0)
		{
			RenderablePointedImageFootprintGeneratorPipeline pipeline =
					new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
			BoundingBox bb = new BoundingBox(pipeline.getFootprintPolyData().get(0).GetBounds());
			double[] centerPoint = bb.getCenterPoint();
			// double[] centerPoint = footprint[currentSlice].GetPoint(0);
			double distanceToCenter = MathUtil.distanceBetween(spacecraftPosition, centerPoint);

			focalPoint[0] = spacecraftPosition[0] + distanceToCenter * direction[0];
			focalPoint[1] = spacecraftPosition[1] + distanceToCenter * direction[1];
			focalPoint[2] = spacecraftPosition[2] + distanceToCenter * direction[2];
		}

	}

	public static CameraOrientationPipeline of(RenderablePointedImage renderableImage, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		return new CameraOrientationPipeline(renderableImage, smallBodyModels);
	}

	public double[] getCameraOrientation()
	{
		return focalPoint;
	}

	public double[] getFocalPoint()
	{
		return focalPoint;
	}

	public double[] getSpacecraftPosition()
	{
		return spacecraftPosition;
	}

	public double[] getBoresightDirection()
	{
		return boresightDirection;
	}

	public double[] getUpVector()
	{
		return upVector;
	}

}
