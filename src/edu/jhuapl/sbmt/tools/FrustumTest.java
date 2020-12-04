package edu.jhuapl.sbmt.tools;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkDataSetMapper;
import vtk.vtkFrustumSource;
import vtk.vtkNamedColors;
import vtk.vtkNativeLibrary;
import vtk.vtkPlanes;
import vtk.vtkProperty;
import vtk.vtkShrinkFilter;

import edu.jhuapl.saavtk.gui.render.RenderView;

public class FrustumTest
{

  //-----------------------------------------------------------------
  //Load VTK library and print which library was not properly loaded
  static
  {
    if (!vtkNativeLibrary.LoadAllNativeLibraries())
    {
      for (vtkNativeLibrary lib : vtkNativeLibrary.values())
      {
        if (!lib.IsLoaded())
        {
          System.out.println(lib.GetLibraryName() + " not loaded");
        }
      }
    }
    vtkNativeLibrary.DisableOutputWindow(null);
  }
  // -----------------------------------------------------------------

  public static void main(String s[])
  {

    vtkNamedColors Color = new vtkNamedColors();
    //For Actor Color
    double ActorColor[] = new double[4];
    //Renderer Background Color
    double BgColor[] = new double[4];
    //BackFace color
    double BackColor[] = new double[4];

    //Change Color Name to Use your own Color for Change Actor Color
    Color.GetColor("GreenYellow",ActorColor);
    //Change Color Name to Use your own Color for Renderer Background
    Color.GetColor("RoyalBlue",BgColor);
    //Change Color Name to Use your own Color for BackFace Color
    Color.GetColor("PeachPuff",BackColor);

    // A virtual camera for 3D rendering
    vtkCamera Camera = new vtkCamera();
    Camera.SetPosition(22.058609008789062,2.1035773754119873,0.057784534990787506);
    Camera.SetFocalPoint(0, 0, 0);
    Camera.SetViewAngle(5);
//    Camera.SetClippingRange(0.1,0.4);
    double PlanesArray[] = new double[24];

    Camera.GetFrustumPlanes(1.0, PlanesArray);

    for (int i=0; i<24; i++)
    {
    	System.out.println("FrustumTest: main: plane array entry " + PlanesArray[i]);
    }

    vtkPlanes Planes = new vtkPlanes();
    Planes.SetFrustumPlanes(PlanesArray);


//    vtkPoints planePoints = new vtkPoints();
//	planePoints.SetNumberOfPoints(6);
////	planePoints.InsertPoint(0, 21.065134048461914,1.9913076162338257*3,0.03772789239883423*3);
////	planePoints.InsertPoint(1, 21.06504249572754,1.991299033164978*3,0.07262726873159409*3);
////	planePoints.InsertPoint(2, 21.061729431152344,2.02604079246521*3,0.07262726873159409*3);
////	planePoints.InsertPoint(3, 21.06182098388672,2.0260496139526367*3,0.03772789239883423*3);
//	planePoints.InsertPoint(0, 0.04442835971713066,-0.3841806948184967,-0.3866458237171173);
//	planePoints.InsertPoint(1, 0.04242081567645073,-0.38437214493751526,0.38668113946914673);
//	planePoints.InsertPoint(2, -0.030993176624178886,0.3854648768901825,0.38668113946914673);
//	planePoints.InsertPoint(3, -0.028985634446144104,0.38565632700920105,-0.3866458237171173);
//	planePoints.InsertPoint(4, 22.058609008789062,2.1035773754119873,0.057784534990787506);
////	planePoints.InsertPoint(4, 1, 1, 1);
//	planePoints.InsertPoint(5, new double[] {0, 0, 0});
//
//
//	vtkDoubleArray planeNormals = new vtkDoubleArray();
//	planeNormals.SetNumberOfComponents(3);
//	planeNormals.SetNumberOfTuples(6);
//	planeNormals.InsertTuple3(0, 0.003918317457502467,-0.0346734899180673,1.5880955534786079E-6);
//	planeNormals.InsertTuple3(1, 5.156645731739457E-4,4.91753478363121E-5,0.03489033948174293);
//	planeNormals.InsertTuple3(2, -0.002705847305554755,0.03478911475095572,1.5880768939047218E-6);
//	planeNormals.InsertTuple3(3, 6.968055787737661E-4,6.64494850521058E-5,-0.034887163309295546);
////	planeNormals.InsertTuple3(4, 1, 1, 1);
////	planeNormals.InsertTuple3(5, -1, -1, -1);
//	Vector3D posSC = new Vector3D(22.058609008789062,2.1035773754119873,0.057784534990787506).normalize();
//	Vector3D negSC = new Vector3D(-22.058609008789062,-2.1035773754119873,-0.057784534990787506).normalize();
//	planeNormals.InsertTuple3(4, posSC.getX(), posSC.getY(), posSC.getZ());
//	planeNormals.InsertTuple3(5, negSC.getX(), negSC.getY(), negSC.getZ());
//
//	vtkPlanes planes = new vtkPlanes();
//	planes.SetPoints(planePoints);
//	planes.SetNormals(planeNormals);

    //To create a frustum defined by a set of planes.
    vtkFrustumSource FrustumSource = new vtkFrustumSource();
    FrustumSource.ShowLinesOff();
    FrustumSource.SetPlanes(Planes);

    //shrink cells composing an arbitrary data set
    vtkShrinkFilter shrink = new vtkShrinkFilter();
    shrink.SetInputConnection(FrustumSource.GetOutputPort());
    shrink.SetShrinkFactor(.9);

    //Create a Mapper and Actor
    vtkDataSetMapper Mapper = new vtkDataSetMapper();
    Mapper.SetInputConnection(FrustumSource.GetOutputPort());
//    Mapper.SetInputConnection(shrink.GetOutputPort());

    vtkProperty Back = new vtkProperty();
    Back.SetColor(BackColor);

    vtkActor Actor = new vtkActor();
    Actor.SetMapper(Mapper);
    Actor.GetProperty().EdgeVisibilityOn();
    Actor.GetProperty().SetColor(ActorColor);
    Actor.SetBackfaceProperty(Back);

//    NativeLibraryLoader.loadAllVtkLibraries();
	RenderView renderView = new RenderView();
	SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run()
		{
			int w = 600;
			int h = 600;
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			frame.getContentPane().add(renderView);
			frame.setVisible(true);

			frame.setSize(w, h);
			renderView.setSize(w, h);
		}
	});

	renderView.registerProp(Actor);
	renderView.getRenderPanel().getRenderer().AddActor(Actor);
	renderView.getRenderPanel().getRenderer().GetActiveCamera().SetPosition(new double[] { 100, 100, 100 });
	renderView.getRenderPanel().getRenderer().GetActiveCamera().SetFocalPoint(new double[] { 0, 0, 0 } );
  }


//    //Create the renderer, render window and interactor.
//    vtkRenderer ren = new vtkRenderer();
//    vtkRenderWindow renWin = new vtkRenderWindow();
//    renWin.AddRenderer(ren);
//    vtkRenderWindowInteractor iren = new vtkRenderWindowInteractor();
//    iren.SetRenderWindow(renWin);
//
//    // Visualise the arrow
//    ren.AddActor(Actor);
//    ren.SetBackground(BgColor);
//
////    renWin.SetSize(300, 300);
//    renWin.Render();
////
////    iren.Initialize();
////    iren.Start();
//  }
}