package edu.jhuapl.sbmt.image2.modules.rendering;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkFeatureEdges;
import vtk.vtkFloatArray;
import vtk.vtkIdList;
import vtk.vtkImageData;
import vtk.vtkPointData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.modules.rendering.vtk.VTKImagePolyDataRenderer;
import edu.jhuapl.sbmt.image2.modules.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image2.modules.rendering.vtk.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.offlimb.OfflimbActorOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.offlimb.OfflimbPlaneGenerator;
import edu.jhuapl.sbmt.image2.pipeline.active.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class PointedImageRenderables
{
	private List<vtkActor> footprintActors = Lists.newArrayList();
	private List<vtkActor> modifiedFootprintActors = Lists.newArrayList();
	private List<vtkPolyData> footprintPolyData = Lists.newArrayList();
	private vtkActor frustumActor;
	private vtkActor modifiedFrustumActor;
	private vtkActor offLimbActor;
	private vtkActor offLimbBoundaryActor;
	private List<vtkActor> boundaryActors = Lists.newArrayList();
	private List<vtkActor> modifiedBoundaryActors = Lists.newArrayList();
	private List<SmallBodyModel> smallBodyModels;
	public double maxFrustumDepth;
	public double minFrustumDepth;
	private boolean offLimbVisibility = false;
    private boolean offLimbBoundaryVisibility = false;
    private double offLimbFootprintDepth;

	public PointedImageRenderables(RenderablePointedImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		this.smallBodyModels = smallBodyModels;
		footprintActors = processFootprints(image, image.getPointing());
		frustumActor = processFrustum(image, image.getPointing());
		boundaryActors = processBoundaries(image, image.getPointing());
		Pair<vtkActor, vtkActor> offLimbActors = processOfflimb(image);
		offLimbActor = offLimbActors.getLeft();
		offLimbBoundaryActor = offLimbActors.getRight();

		image.getModifiedPointing().ifPresent(pointing -> {
			try
			{
				modifiedFootprintActors = processFootprints(image, pointing);
				modifiedFrustumActor = processFrustum(image, pointing);
				modifiedBoundaryActors = processBoundaries(image, pointing);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}

	private List<vtkActor> processFootprints(RenderablePointedImage renderableImage, PointingFileReader pointing) throws IOException, Exception
	{
		List<vtkActor> footprintActors = Lists.newArrayList();
		RenderablePointedImageFootprintGeneratorPipeline pipeline =
				new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		List<vtkPolyData> footprints = pipeline.getFootprintPolyData();

//		PointingFileReader infoReader = renderableImage.getPointing();

		double[] spacecraftPositionAdjusted = pointing.getSpacecraftPosition();
//		System.out.println("PointedImageRenderables: processFootprints: sc pos " + new Vector3D(spacecraftPositionAdjusted));
    	double[] frustum1Adjusted = pointing.getFrustum1();
    	double[] frustum2Adjusted = pointing.getFrustum2();
    	double[] frustum3Adjusted = pointing.getFrustum3();
    	double[] frustum4Adjusted = pointing.getFrustum4();
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

        int i=0;
    	for (SmallBodyModel smallBody : smallBodyModels)
    	{
    		vtkPolyData footprint = footprints.get(i++);
    		vtkFloatArray textureCoords = new vtkFloatArray();

	        vtkPointData pointData = footprint.GetPointData();
	        pointData.SetTCoords(textureCoords);
	        PolyDataUtil.generateTextureCoordinates(frustum, renderableImage.getImageWidth(), renderableImage.getImageHeight(), footprint);
	        pointData.Delete();
	        footprintPolyData.add(footprint);
	        List<vtkActor> actors = Lists.newArrayList();
	        Just.of(Pair.of(imageData.get(0), footprint))
	        	.operate(new VTKImagePolyDataRenderer(renderableImage.isLinearInterpolation()))
	        	.subscribe(Sink.of(actors))
	        	.run();
	        footprintActors.addAll(actors);
    	}
    	return footprintActors;
	}

	private vtkActor processFrustum(RenderablePointedImage renderableImage, PointingFileReader pointing)
	{
		vtkActor frustumActor;
//		PointingFileReader infoReader = renderableImage.getPointing();
		double diagonalLength = smallBodyModels.get(0).getBoundingBoxDiagonalLength();
		double[] scPos = pointing.getSpacecraftPosition();
		offLimbFootprintDepth = new Vector3D(scPos).getNorm();
    	double[] frus1 = pointing.getFrustum1();
    	double[] frus2 = pointing.getFrustum2();
    	double[] frus3 = pointing.getFrustum3();
    	double[] frus4 = pointing.getFrustum4();
    	Frustum frustum = new Frustum(scPos,
						    			frus1,
						    			frus2,
						    			frus3,
						    			frus4);

		vtkPolyData frustumPolyData = new vtkPolyData();
		frustumActor = new vtkActor();
		vtkPoints points = new vtkPoints();
		vtkCellArray lines = new vtkCellArray();

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(2);

		double maxFrustumRayLength = MathUtil.vnorm(scPos) + diagonalLength;
		double[] origin = scPos;

		double[] UL = { origin[0] + frus1[0] * maxFrustumRayLength,
				origin[1] + frus1[1] * maxFrustumRayLength,
				origin[2] + frus1[2] * maxFrustumRayLength };
		double[] UR = { origin[0] + frus2[0] * maxFrustumRayLength,
				origin[1] + frus2[1] * maxFrustumRayLength,
				origin[2] + frus2[2] * maxFrustumRayLength };
		double[] LL = { origin[0] + frus3[0] * maxFrustumRayLength,
				origin[1] + frus3[1] * maxFrustumRayLength,
				origin[2] + frus3[2] * maxFrustumRayLength };
		double[] LR = { origin[0] + frus4[0] * maxFrustumRayLength,
				origin[1] + frus4[1] * maxFrustumRayLength,
				origin[2] + frus4[2] * maxFrustumRayLength };

		double minFrustumRayLength = MathUtil.vnorm(scPos) - diagonalLength;
		maxFrustumDepth = maxFrustumRayLength; // a reasonable
																// approximation
																// for a max
																// bound on the
																// frustum depth
		minFrustumDepth = minFrustumRayLength; // a reasonable
																// approximation
																// for a min
																// bound on the
																// frustum depth

		points.InsertNextPoint(scPos);
		points.InsertNextPoint(UL);
		points.InsertNextPoint(UR);
		points.InsertNextPoint(LL);
		points.InsertNextPoint(LR);

		idList.SetId(0, 0);
		idList.SetId(1, 1);
		lines.InsertNextCell(idList);
		idList.SetId(0, 0);
		idList.SetId(1, 2);
		lines.InsertNextCell(idList);
		idList.SetId(0, 0);
		idList.SetId(1, 3);
		lines.InsertNextCell(idList);
		idList.SetId(0, 0);
		idList.SetId(1, 4);
		lines.InsertNextCell(idList);

		frustumPolyData.SetPoints(points);
		frustumPolyData.SetLines(lines);

		vtkPolyDataMapper frusMapper = new vtkPolyDataMapper();
		frusMapper.SetInputData(frustumPolyData);

		frustumActor.SetMapper(frusMapper);
		return frustumActor;
	}

	private Pair<vtkActor, vtkActor> processOfflimb(RenderablePointedImage renderableImage) throws IOException, Exception
	{
		vtkActor offLimbActor;
		vtkActor offLimbBoundaryActor;
		offLimbFootprintDepth = renderableImage.getOfflimbDepth();
		List<vtkActor> actors = Lists.newArrayList();
		Just.of(renderableImage)
			.operate(new OfflimbPlaneGenerator(offLimbFootprintDepth, smallBodyModels.get(0)))
			.operate(new OfflimbActorOperator())
			.subscribe(Sink.of(actors))
			.run();

		offLimbActor = actors.get(0);
		offLimbBoundaryActor = actors.get(1);

		offLimbActor.SetVisibility(offLimbVisibility ? 1 : 0);
		offLimbBoundaryActor.SetVisibility(offLimbBoundaryVisibility ? 1 : 0);
		return Pair.of(offLimbActor, offLimbBoundaryActor);
	}

	private List<vtkActor> processBoundaries(RenderablePointedImage renderableImage, PointingFileReader pointing) throws IOException, Exception
	{
		List<vtkActor> boundaryActors = Lists.newArrayList();
//		PointingFileReader infoReader = renderableImage.getPointing();
		vtkPolyData boundary;
		vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
		vtkActor boundaryActor = new vtkActor();
		double[] spacecraftPositionAdjusted = pointing.getSpacecraftPosition();
    	double[] frustum1Adjusted = pointing.getFrustum1();
    	double[] frustum2Adjusted = pointing.getFrustum2();
    	double[] frustum3Adjusted = pointing.getFrustum3();
    	double[] frustum4Adjusted = pointing.getFrustum4();
    	Frustum frustum = new Frustum(spacecraftPositionAdjusted,
						    			frustum1Adjusted,
						    			frustum3Adjusted,
						    			frustum4Adjusted,
						    			frustum2Adjusted);

    	for (vtkPolyData footprint : footprintPolyData)
    	{
			vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
			edgeExtracter.SetInputData(footprint);
			edgeExtracter.BoundaryEdgesOn();
			edgeExtracter.FeatureEdgesOff();
			edgeExtracter.NonManifoldEdgesOff();
			edgeExtracter.ManifoldEdgesOff();
			edgeExtracter.ColoringOff();
			edgeExtracter.Update();


			for (SmallBodyModel smallBody : smallBodyModels)
	    	{
				boundary = new vtkPolyData();
				vtkPolyData tmp = smallBody.computeFrustumIntersection(spacecraftPositionAdjusted,
							frustum1Adjusted,
							frustum3Adjusted,
							frustum4Adjusted,
							frustum2Adjusted);

//				if (tmp == null)
//				{
//					if (boundaryMapper != null)
//					{
//				        boundaryMapper.SetInputData(boundary);
//				        boundaryMapper.Update();
//				        boundaryActor.SetMapper(boundaryMapper);
//					}
//					boundaries.add(boundaryActor);
//					return;
//				}

				vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
				boundary.DeepCopy(edgeExtracterOutput);
				if (boundaryMapper != null)
				{
			        boundaryMapper.SetInputData(boundary);
			        boundaryMapper.Update();
			        boundaryActor.SetMapper(boundaryMapper);
			        boundaryActors.add(boundaryActor);
			        return boundaryActors;
				}
	    	}
    	}
    	return boundaryActors;

	}

	public List<vtkActor> getFootprints()
	{
		return footprintActors;
	}

	public vtkActor getFrustum()
	{
		return frustumActor;
	}

	public vtkActor getOffLimb()
	{
		return offLimbActor;
	}

	public vtkActor getOffLimbBoundary()
	{
		return offLimbBoundaryActor;
	}

	public List<vtkActor> getBoundaries()
	{
		return boundaryActors;
	}

	/**
	 * @return the modifiedFootprintActors
	 */
	public List<vtkActor> getModifiedFootprintActors()
	{
		return modifiedFootprintActors;
	}

	/**
	 * @return the modifiedFrustumActor
	 */
	public vtkActor getModifiedFrustumActor()
	{
		return modifiedFrustumActor;
	}

	/**
	 * @return the modifiedBoundaryActors
	 */
	public List<vtkActor> getModifiedBoundaryActors()
	{
		return modifiedBoundaryActors;
	}
}
