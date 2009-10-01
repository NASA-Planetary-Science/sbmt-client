package edu.jhuapl.near;

import javax.swing.*;
import java.awt.*;

public class ErosLineamentViewer extends JFrame 
{
	private JSplitPane splitPane;
	private ImageGLWidget imageViewer;
	private ControlPanel controlPanel;
	private StatusBar statusBar = new StatusBar();
	private FileMenu fileMenu;
	
    private LineamentModel lineamentModel = new LineamentModel();
    
	public ErosLineamentViewer()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		imageViewer = new ImageGLWidget(lineamentModel, statusBar);

        controlPanel = new ControlPanel(imageViewer);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, imageViewer);
		splitPane.setOneTouchExpandable(true);

        imageViewer.setMinimumSize(new Dimension(100, 100));
        imageViewer.setPreferredSize(new Dimension(700, 700));
        controlPanel.setMinimumSize(new Dimension(300, 100));
        controlPanel.setPreferredSize(new Dimension(300, 700));

		createMenus(imageViewer);
        createToolBars();
        createStatusBar();

		this.add(splitPane, BorderLayout.CENTER);

//        // Center the application on the screen.
//        Dimension prefSize = this.getPreferredSize();
//        Dimension parentSize;
//        java.awt.Point parentLocation = new java.awt.Point(0, 0);
//        parentSize = Toolkit.getDefaultToolkit().getScreenSize();
//        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
//        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
//        this.setLocation(x, y);
//        this.setResizable(true);
	}
	
    private void createMenus(ImageGLWidget imageViewer)
    {
    	JMenuBar menuBar = new JMenuBar();

    	fileMenu = new FileMenu(imageViewer);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    private void createToolBars()
    {
    }

    private void createStatusBar()
    {
    	this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
    }

//    public void about()
//    {
//        QMessageBox.about(this,
//                         tr("About Near Lineaments Viewer"),
//                         tr("(C) 2009 The Johns Hopkins University Applied Physics Laboratory"));
//    }

    static
    {
    	// On windows there are problems finding the dependent libraries,
    	// so load them all manually. Note they must be loaded in the
    	// following order
    	
    	String name = System.getProperty("os.name");
    	if (name.toLowerCase().startsWith("windows"))
    	{
    		System.loadLibrary("jawt");
    		System.loadLibrary("vtkzlib");
    		System.loadLibrary("vtkNetCDF");
    		System.loadLibrary("vtksys");
    		System.loadLibrary("vtkalglib");
    		System.loadLibrary("vtkexoIIc");
    		System.loadLibrary("vtkexpat");
    		System.loadLibrary("vtkfreetype");
    		System.loadLibrary("vtkftgl");
    		System.loadLibrary("vtkjpeg");
    		System.loadLibrary("vtklibxml2");
    		System.loadLibrary("vtkmetaio");
    		System.loadLibrary("vtkpng");
    		System.loadLibrary("vtkproj4");
    		System.loadLibrary("vtktiff");
    		System.loadLibrary("vtkverdict");
    		System.loadLibrary("vtkCommon");
    		System.loadLibrary("vtkCommonJava");
    		System.loadLibrary("vtkDICOMParser");
    		System.loadLibrary("vtkFiltering");
    		System.loadLibrary("vtkFilteringJava");
    		System.loadLibrary("vtkGraphics");
    		System.loadLibrary("vtkGraphicsJava");
    		System.loadLibrary("vtkGenericFiltering");
    		System.loadLibrary("vtkGenericFilteringJava");
    		System.loadLibrary("vtkIO");
    		System.loadLibrary("vtkIOJava");
    		System.loadLibrary("vtkImaging");
    		System.loadLibrary("vtkImagingJava");
    		System.loadLibrary("vtkRendering");
    		System.loadLibrary("vtkRenderingJava");
    		System.loadLibrary("vtkHybrid");
    		System.loadLibrary("vtkHybridJava");
    		System.loadLibrary("vtkWidgets");
    		System.loadLibrary("vtkWidgetsJava");
    		System.loadLibrary("vtkInfovis");
    		System.loadLibrary("vtkInfovisJava");
    		System.loadLibrary("vtkViews");
    		System.loadLibrary("vtkViewsJava");
    		System.loadLibrary("vtkGeovis");
    		System.loadLibrary("vtkGeovisJava");
    		System.loadLibrary("vtkVolumeRendering");
    		System.loadLibrary("vtkVolumeRenderingJava");
    	}
    	else if (name.toLowerCase().startsWith("linux"))
    	{
    		System.loadLibrary("vtkCommonJava");
    		System.loadLibrary("vtkFilteringJava");
    		System.loadLibrary("vtkGraphicsJava");
    		System.loadLibrary("vtkGenericFilteringJava");
    		System.loadLibrary("vtkIOJava");
    		System.loadLibrary("vtkImagingJava");
    		System.loadLibrary("vtkRenderingJava");
    		System.loadLibrary("vtkHybridJava");
    		System.loadLibrary("vtkWidgetsJava");
    		System.loadLibrary("vtkInfovisJava");
    		System.loadLibrary("vtkViewsJava");
    		System.loadLibrary("vtkGeovisJava");
    		System.loadLibrary("vtkVolumeRenderingJava");
    	}
    }

    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (Exception e) {}

        try
        {
        	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        	
            final ErosLineamentViewer frame = new ErosLineamentViewer();
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
            	public void run()
            	{
                    frame.setTitle("Eros Lineament Viewer");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
            	}
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
