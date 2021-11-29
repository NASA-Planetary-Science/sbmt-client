package edu.jhuapl.sbmt.image2.modules.rendering;

import java.io.IOException;
import java.util.List;

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
import vtk.vtkProperty;
import vtk.vtkTexture;

import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.active.RenderableImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class ImageRenderables
{
	private List<vtkActor> footprintActors = Lists.newArrayList();
	private List<vtkPolyData> footprintPolyData = Lists.newArrayList();
	private vtkActor frustumActor;
	private vtkActor offLimbActor;
	private vtkActor offLimbBoundaryActor;
	private List<vtkActor> boundaryActors = Lists.newArrayList();
	private List<SmallBodyModel> smallBodyModels;
	public double maxFrustumDepth;
	public double minFrustumDepth;
	private boolean offLimbVisibility;
    private boolean offLimbBoundaryVisibility;

	public ImageRenderables(RenderableImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		this.smallBodyModels = smallBodyModels;
		processFootprints(image);
		processFrustum(image);
		processOfflimb(image);
		processBoundaries(image);
	}

	private void processFootprints(RenderableImage renderableImage) throws IOException, Exception
	{
		RenderableImageFootprintGeneratorPipeline pipeline =
				new RenderableImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		List<vtkPolyData> footprints = pipeline.getFootprintPolyData();

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


        VtkImageRendererOperator imageRenderer = new VtkImageRendererOperator();
        List<vtkImageData> imageData = Lists.newArrayList();
        Just.of(renderableImage.getLayer())
        	.operate(imageRenderer)
        	.operate(new VtkImageContrastOperator(null))
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

			vtkTexture imageTexture = new vtkTexture();
	        imageTexture.InterpolateOn();
	        imageTexture.RepeatOff();
	        imageTexture.EdgeClampOn();
	        imageTexture.SetInputData(imageData.get(0));

			vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			mapper.SetInputData(footprint);
			footprintPolyData.add(footprint);

			vtkActor actor = new vtkActor();
			actor.SetMapper(mapper);
			actor.SetTexture(imageTexture);
	        vtkProperty footprintProperty = actor.GetProperty();
	        footprintProperty.LightingOff();
	        footprintActors.add(actor);
    	}
	}

	private void processFrustum(RenderableImage renderableImage)
	{
		InfoFileReader infoReader = renderableImage.getPointing();
		double diagonalLength = smallBodyModels.get(0).getBoundingBoxDiagonalLength();
		double[] scPos = infoReader.getSpacecraftPosition();

    	double[] frus1 = infoReader.getFrustum1();
    	double[] frus2 = infoReader.getFrustum2();
    	double[] frus3 = infoReader.getFrustum3();
    	double[] frus4 = infoReader.getFrustum4();
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

	}

	private void processOfflimb(RenderableImage renderableImage)
	{
//		OffLimbPlaneCalculator calculator = new OffLimbPlaneCalculator();
//		vtkPolyData offLimbPlane = null;
//	    vtkTexture offLimbTexture;
//	    vtkPolyData offLimbBoundary = null;
//	    double offLimbFootprintDepth;
//		double[] spacecraftPosition = new double[3];
//		double[] focalPoint = new double[3];
//		double[] upVector = new double[3];
//		CameraOrientationPipeline cameraOrientationPipeline =
//				new CameraOrientationPipeline(null, smallBodyModels);
//		focalPoint = cameraOrientationPipeline.getCameraOrientation();
//
////		image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//		//TODO not sure what this was supposed to do here
//		offLimbFootprintDepth = new Vector3D(spacecraftPosition).getNorm();
//		calculator.loadOffLimbPlane(renderableImage, offLimbFootprintDepth);
//		offLimbActor = calculator.getOffLimbActor();
//		offLimbBoundaryActor = calculator.getOffLimbBoundaryActor();
//		offLimbTexture = calculator.getOffLimbTexture();
//		// set initial visibilities
//		if (offLimbActor != null) {
//			offLimbActor.SetVisibility(offLimbVisibility ? 1 : 0);
//			offLimbBoundaryActor.SetVisibility(offLimbBoundaryVisibility ? 1 : 0);
//		}
//
//
//	    if (offLimbTexture == null)
//        { // if offlimbtexture is null, initialize it.
//            vtkImageData imageData = new vtkImageData();
//            imageData.DeepCopy(renderableImage.getDisplayedImage());
//            offLimbTexture = new vtkTexture();
//            offLimbTexture.SetInputData(imageData);
//            offLimbTexture.Modified();
//        }
	}

	private void processBoundaries(RenderableImage renderableImage) throws IOException, Exception
	{
		InfoFileReader infoReader = renderableImage.getPointing();
		vtkPolyData boundary;
		vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
		vtkActor boundaryActor = new vtkActor();
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
					return;
				}
	    	}
    	}


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
}
