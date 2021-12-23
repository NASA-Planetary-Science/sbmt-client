package edu.jhuapl.sbmt.image2.pipeline.active.offlimb;

import java.io.IOException;

import vtk.vtkActor;
import vtk.vtkPolyData;
import vtk.vtkTexture;

import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class OfflimbActorOperator extends BasePipelineOperator
{
	private vtkPolyData offLimbPlane=null;
	private vtkActor offLimbActor;
    private vtkTexture offLimbTexture;
    private vtkPolyData offLimbBoundary=null;
    private vtkActor offLimbBoundaryActor;

	@Override
	public void processData() throws IOException, Exception
	{
//		//clean up the polydata to merge overlapping sub pixels
//    	vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
//        cleanFilter.SetInputData(imagePolyData);
//        cleanFilter.Update();
//        imagePolyData = cleanFilter.GetOutput();
//
//    	// keep a reference to a copy of the polydata
//        offLimbPlane=new vtkPolyData();
//        offLimbPlane.DeepCopy(imagePolyData);
//        PolyDataUtil.generateTextureCoordinates(img.getFrustum(), img.getImageWidth(), img.getImageHeight(), offLimbPlane); // generate (u,v) coords; individual components lie on the interval [0 1]; https://en.wikipedia.org/wiki/UV_mapping
//
//        // now, if there is an "active" image, cf. PerspectiveImage class", then map it to the off-limb polydata
//        if (img.getDisplayedImage()!=null)
//        {
//            if (offLimbTexture==null)
//            {
//                // create the texture first
//                offLimbTexture = img.getOffLimbTexture();
//                offLimbTexture.InterpolateOn();
//                offLimbTexture.RepeatOff();
//                offLimbTexture.EdgeClampOn();
//                offLimbTexture.Modified();
//            }
////            img.setDisplayedImageRange(img.getDisplayedRange()); // match off-limb image intensity range to that of the on-body footprint; the "img" method call also takes care of syncing the off-limb vtkTexture object with the displayed raw image, above and beyond what the parent class has to do for the on-body geometry
//
//            // setup off-limb mapper and actor
//            vtkPolyDataMapper offLimbMapper=new vtkPolyDataMapper();
//            offLimbMapper.SetInputData(offLimbPlane);
//            if (offLimbActor==null)
//                offLimbActor=new vtkActor();
//            offLimbActor.SetMapper(offLimbMapper);
//            offLimbActor.SetTexture(offLimbTexture);
//            offLimbActor.Modified();
//
//            // generate off-limb edge geometry, with mapper and actor
//            vtkFeatureEdges edgeFilter=new vtkFeatureEdges();
//            edgeFilter.SetInputData(offLimbPlane);
//            edgeFilter.BoundaryEdgesOn();
//            edgeFilter.ManifoldEdgesOff();
//            edgeFilter.NonManifoldEdgesOff();
//            edgeFilter.FeatureEdgesOff();
//            edgeFilter.Update();
//            offLimbBoundary=new vtkPolyData();
//            offLimbBoundary.DeepCopy(edgeFilter.GetOutput());
//            vtkPolyDataMapper boundaryMapper=new vtkPolyDataMapper();
//            boundaryMapper.SetInputData(offLimbBoundary);
//            boundaryMapper.ScalarVisibilityOff();
//
//            if (offLimbBoundaryActor==null)
//                offLimbBoundaryActor=new vtkActor();
//            offLimbBoundaryActor.SetMapper(boundaryMapper);
//
//
//            // get color from default boundary color of image
//            // set boundary color to this color
//            Color color = img.getOfflimbBoundaryColor();
//            offLimbBoundaryActor.GetProperty().SetColor(new double[] {color.getRed()/255, color.getGreen()/255, color.getBlue()/255});
//            offLimbBoundaryActor.GetProperty().SetLineWidth(1);
//            offLimbBoundaryActor.Modified();
//        }
	}
}
