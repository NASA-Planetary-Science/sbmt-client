package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkConeSource;
import vtk.vtkDataSetMapper;
import vtk.vtkFrustumSource;
import vtk.vtkGraphicsFactory;
import vtk.vtkNamedColors;
import vtk.vtkNativeLibrary;
import vtk.vtkPNGWriter;
import vtk.vtkPlanes;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkRenderWindow;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkSelectVisiblePoints;
import vtk.vtkShrinkFilter;
import vtk.vtkWindowToImageFilter;

import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.RenderView;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipelineComponents.VTKDebug;
import edu.jhuapl.sbmt.model.SbmtModelFactory;

public class FrustumVisibleFacesTest
{
	private vtkSelectVisiblePoints visiblePoints;
//	private RenderView renderView;
	private RenderPanel renderView;
	private SmallBodyModel smallBodyModel;

	public void OffScreenRendering()
	{
		vtkGraphicsFactory factory = new vtkGraphicsFactory();
		factory.SetOffScreenOnlyMode(1);
		factory.SetUseMesaClasses(1);
		vtkConeSource coneSource = new vtkConeSource();

		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputConnection(coneSource.GetOutputPort());
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
//		vtkRenderer renderer =  new vtkRenderer();
//		renderer.AddActor(actor);
//		vtkRenderWindow renderWindow = new vtkRenderWindow();
//		renderWindow.AddRenderer(renderer);
		RenderView renderView = new RenderView();
		vtkRenderWindow renderWindow = renderView.getRenderPanel().getRenderWindow();
		renderView.getRenderPanel().getRenderer().AddActor(actor);
		renderWindow.SetOffScreenRendering(1);
		renderWindow.Render();
//		SwingUtilities.invokeLater(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				renderWindow.Render();
//			}
//		});

		vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
		windowToImage.SetInput(renderWindow);

		vtkPNGWriter writer = new vtkPNGWriter();
		writer.SetFileName("/Users/steelrj1/Desktop/offRenderTest.png");
		writer.SetInputConnection(windowToImage.GetOutputPort());
		writer.Write();
	}


	public void VisibleFacets()
	{
		boolean aplVersion = true;
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
//        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/multi-mission/test");
        String rootURL = "http://sbmt.jhuapl.edu/sbmt/prod/";

        Configuration.setAPLVersion(aplVersion);
        Configuration.setRootURL(rootURL);

        SbmtMultiMissionTool.configureMission();

        // authentication
        Configuration.authenticate();

        // initialize view config
        SmallBodyViewConfig.initialize();

        // VTK
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadHeadlessVtkLibraries();

        ShapeModelBody body = ShapeModelBody.RQ36;
        ShapeModelType type = ShapeModelType.provide("OLA-v21");
        SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(body, type);
//        ImagingInstrument instrument = config.imagingInstruments[imagerIndex];
//        System.out.println("PerspectiveImagePreRenderer: main: input is " + inputDirectory);
//        File input = new File(inputDirectory);
//        File[] fileList;
//        if (input.isDirectory())
//        {
//            fileList = new File(inputDirectory).listFiles(new FilenameFilter()
//            {
//                @Override
//                public boolean accept(File dir, String name)
//                {
//                    return FilenameUtils.getExtension(name).contains("fit");
//                }
//            });
//        }
//        else
//        {
//            fileList = new File[] {input};
//        }
//        Arrays.sort(fileList);
        ArrayList<File> imagesWithPointing = new ArrayList<File>();
       smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
//        System.out.println("FrustumVisibleFacesTest: main: smallbody model " + smallBodyModel.getNumberResolutionLevels());
//        vtkActor actor = smallBodyModel.getSmallBodyActor();
        List<vtkProp> props = smallBodyModel.getProps();

        vtkNamedColors Color = new vtkNamedColors();
		// For Actor Color
		double ActorColor[] = new double[4];
		// Renderer Background Color
		double BgColor[] = new double[4];
		// BackFace color
		double BackColor[] = new double[4];

		// Change Color Name to Use your own Color for Change Actor Color
		Color.GetColor("GreenYellow", ActorColor);
		// Change Color Name to Use your own Color for Renderer Background
		Color.GetColor("RoyalBlue", BgColor);
		// Change Color Name to Use your own Color for BackFace Color
		Color.GetColor("PeachPuff", BackColor);

		// A virtual camera for 3D rendering
		vtkCamera Camera = new vtkCamera();
//		Camera.SetPosition(22.058609008789062, 2.1035773754119873, 0.057784534990787506);
//		Camera.SetFocalPoint(0, 0, 0);
//		Camera.SetViewAngle(5);
		Camera.SetPosition(7, 7, 7);
		Camera.SetFocalPoint(0, 0, 0);
		Camera.SetViewAngle(30);
//    Camera.SetClippingRange(0.1,0.4);
		double PlanesArray[] = new double[24];

		Camera.GetFrustumPlanes(1.0, PlanesArray);

		vtkPlanes Planes = new vtkPlanes();
		Planes.SetFrustumPlanes(PlanesArray);

		// To create a frustum defined by a set of planes.
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

    renderView = new RenderPanel();

//		renderView = new RenderView();
		vtkRenderWindow renderWindow = renderView.getRenderWindow();
//		renderWindow.SetMultiSamples(0);
//		renderWindow.SetOffScreenRendering(1);

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				int w = 600;
				int h = 600;
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				frame.getContentPane().add(renderView.getComponent());
				frame.setVisible(true);

				frame.setSize(w, h);
				renderView.setSize(w, h);


			}
		});

		for (vtkProp aProp : props)
			renderView.getRenderer().AddActor(aProp);
		renderView.getRenderer().GetActiveCamera().SetPosition(new double[]
				{ 7, 7, 7 });
		renderView.getRenderer().GetActiveCamera().SetFocalPoint(new double[]
				{ 1, 0, 0 });
		renderView.getRenderer().ResetCamera();
		visiblePoints = new vtkSelectVisiblePoints();
		visiblePoints.SetTolerance(1e-4);
		visiblePoints.SetInputData(smallBodyModel.getSmallBodyPolyData());
		visiblePoints.SetRenderer(renderView.getRenderer());

//		renderView.addPropertyChangeListener(new PropertyChangeListener()
//		{
//
//			@Override
//			public void propertyChange(PropertyChangeEvent arg0)
//			{
//				System.out.println(
//						"FrustumVisibleFacesTest.VisibleFacets().new PropertyChangeListener() {...}: propertyChange: firing");

//				visiblePoints.SetTolerance(1e-4);
////				SwingUtilities.invokeLater(new Runnable()
////				{
////					@Override
////					public void run()
////					{
////						System.out.println("FrustumVisibleFacesTest.main(...).new Runnable() {...}: run: sbpd " + smallBodyModel.getSmallBodyPolyData().GetNumberOfPoints());
////
////						System.out.println(
////								"FrustumVisibleFacesTest.VisibleFacets().new PropertyChangeListener() {...}.propertyChange(...).new Runnable() {...}: run: running prop change main thread");
////						visiblePoints.Update();
////						renderView.getRenderPanel().Render();
////						visiblePoints.Update();
////						vtkPolyData outputData = visiblePoints.GetOutput();
////						System.out.println("FrustumVisibleFacesTest.main(...).new Runnable() {...}: run: output size " + outputData.GetNumberOfPoints());
////						VTKDebug.writePolyDataToFile(outputData, "/Users/steelrj1/Desktop/visiblePoints.vtk");
////					}
////				});
//			}
//		});

		vtkRenderWindowInteractor vInteractor = renderView.getRenderWindowInteractor();
		vInteractor.AddObserver("RenderEvent", this, "selectVisible");

//		vtkPointSource pointSource = new vtkPointSource();
//		pointSource.SetRadius(2.0);
//		pointSource.SetNumberOfPoints(200);
//		pointSource.Update();
//		vtkPolyDataMapper pointMapper = new vtkPolyDataMapper();
//		pointMapper.SetInputConnection(pointSource.GetOutputPort());
//		vtkActor pointActor = new vtkActor();
//		pointActor.SetMapper(pointMapper);

//		renderView.getRenderPanel().getRenderer().AddActor(pointActor);

//		renderWindow.Render();


//		renderView.registerProp(Actor);
//		renderView.registerProp(props);
//		renderView.getRenderPanel().getRenderer().AddActor(Actor);

//		vtkPointSource pointSource = new vtkPointSource();
//		pointSource.SetRadius(2.0);
//		pointSource.SetNumberOfPoints(200);
//		pointSource.Update();
//		vtkPolyDataMapper pointMapper = new vtkPolyDataMapper();
//		pointMapper.SetInputConnection(pointSource.GetOutputPort());
//		vtkActor pointActor = new vtkActor();
//		pointActor.SetMapper(pointMapper);
//
//		renderView.getRenderPanel().getRenderer().AddActor(pointActor);
//		for (vtkProp aProp : props)
//			renderView.getRenderPanel().getRenderer().AddActor(aProp);
//		renderView.getRenderPanel().getRenderer().GetActiveCamera().SetPosition(new double[]
//				{ 7, 7, 7 });
//		renderView.getRenderPanel().getRenderer().GetActiveCamera().SetFocalPoint(new double[]
//				{ 1, 0, 0 });
//
//		System.out.println("FrustumVisibleFacesTest.main(...).new Runnable() {...}: run: sbpd " + smallBodyModel.getSmallBodyPolyData().GetNumberOfPoints());
////		renderWindow.Render();
//		vtkSelectVisiblePoints visiblePoints = new vtkSelectVisiblePoints();
//		visiblePoints.SetInputData(smallBodyModel.getSmallBodyPolyData());
////		visiblePoints.SetInputConnection(pointSource.GetOutputPort());
//		visiblePoints.SetRenderer(renderView.getRenderPanel().getRenderer());
//		visiblePoints.Update();
//		vtkPolyData outputData = visiblePoints.GetOutput();
//		System.out.println("FrustumVisibleFacesTest.main(...).new Runnable() {...}: run: output size " + outputData.GetNumberOfPoints());
//		VTKDebug.writePolyDataToFile(outputData, "/Users/steelrj1/Desktop/visiblePoints.vtk");
	}


	private void selectVisible()
	{
		System.out.println("FrustumVisibleFacesTest: selectVisible: selecting visible");
		visiblePoints = new vtkSelectVisiblePoints();
		visiblePoints.SetTolerance(1e-4);
		visiblePoints.SetInputData(smallBodyModel.getSmallBodyPolyData());
		visiblePoints.SetRenderer(renderView.getRenderer());
		visiblePoints.Update();
		renderView.Render();
		visiblePoints.Update();



		vtkPolyData outputData = visiblePoints.GetOutput();
		System.out.println("FrustumVisibleFacesTest.main(...).new Runnable() {...}: run: output size " + outputData.GetNumberOfPoints());
		VTKDebug.writePolyDataToFile(outputData, "/Users/steelrj1/Desktop/visiblePoints.vtk");
	}

	// -----------------------------------------------------------------
	// properly loaded
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
		FrustumVisibleFacesTest test = new  FrustumVisibleFacesTest();
		test.VisibleFacets();
//        FrustumVisibleFacesTest.OffScreenRendering();

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