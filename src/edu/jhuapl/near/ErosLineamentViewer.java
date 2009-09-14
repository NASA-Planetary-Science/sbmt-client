package edu.jhuapl.near;

//import com.trolltech.qt.core.Qt;
//import com.trolltech.qt.gui.*;
//import com.trolltech.qt.gui.QDockWidget.DockWidgetFeature;

import javax.swing.*;

import java.awt.*;

public class ErosLineamentViewer extends JFrame 
{
	private JSplitPane splitPane = null;
	private ImageGLWidget imageViewer;
	private ControlPanel controlPanel;
//	private QMenu fileMenu;
//    //private QMenu editMenu;
//    private QMenu helpMenu;
//
//    //private QToolBar fileToolBar;
//    //private QToolBar editToolBar;
//
//    private QAction exitAct;
//    private QAction aboutAct;
    //private String rsrcPath = "classpath:com/trolltech/images";

    private LineamentModel lineamentModel = new LineamentModel();
    private Dimension canvasSize = new Dimension(800, 600);
    
    
	public ErosLineamentViewer()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		imageViewer = new ImageGLWidget(lineamentModel);
		controlPanel = new ControlPanel(imageViewer);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, imageViewer);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		splitPane.setPreferredSize(new Dimension(400, 200));
		//splitPane.setPreferredSize(canvasSize);

		setPreferredSize(canvasSize);

        Dimension minimumSize = new Dimension(100, 50);
        imageViewer.setMinimumSize(minimumSize);
        controlPanel.setMinimumSize(minimumSize);

		this.getContentPane().add(splitPane);
		
//		QMenuBar menuBar = new QMenuBar();
//		setMenuBar(menuBar);

//        setWindowTitle(tr("Near Lineaments Viewer"));
        //setWindowIcon(new QIcon("classpath:com/trolltech/images/qt-logo.png"));
        
//        imageViewer = new ImageViewer(lineamentModel, this);
//        setCentralWidget(imageViewer);

        try {
            createActions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        createMenus();
        createToolBars();
        createStatusBar();
        createDockWindows();

        
        this.pack();
        this.setVisible(true);

        // Center the application on the screen.
        Dimension prefSize = this.getPreferredSize();
        Dimension parentSize;
        java.awt.Point parentLocation = new java.awt.Point(0, 0);
        parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
        this.setLocation(x, y);
        this.setResizable(true);
	}
	
    private void createActions()
    {
//        exitAct = new QAction(tr("E&xit"), this);
//        exitAct.setShortcut(tr("Ctrl+Q"));
//        exitAct.setStatusTip(tr("Exit the application"));
//        exitAct.triggered.connect(this, "close()");
//
//        aboutAct = new QAction(tr("&About"), this);
//        aboutAct.setStatusTip(tr("Show the application's About box"));
//        aboutAct.triggered.connect(this, "about()");
    }

    private void createMenus()
    {
//        fileMenu = menuBar().addMenu(tr("&File"));
//        //fileMenu.addSeparator();
//        fileMenu.addAction(exitAct);
//
//        //editMenu = menuBar().addMenu(tr("&Edit"));
//
//        menuBar().addSeparator();
//
//        helpMenu = menuBar().addMenu(tr("&Help"));
//        helpMenu.addAction(aboutAct);
//        //helpMenu.addSeparator();
    }

    private void createToolBars()
    {
        //fileToolBar = addToolBar(tr("File"));

        //editToolBar = addToolBar(tr("Edit"));
    }

    private void createStatusBar()
    {
//        statusBar().showMessage(tr("Ready"));
    }

    private void createDockWindows()
    {
//    	QDockWidget dock = new QDockWidget(tr("Image Browser"), this);
//    	dock.setFeatures(DockWidgetFeature.DockWidgetMovable, DockWidgetFeature.DockWidgetFloatable);
//    	imageBrowser = new ImageBrowser(this);
//    	imageBrowser.fitFileDoubleClicked.connect(this.imageViewer, "addNewTab(String)");
//    	dock.setWidget(imageBrowser);
//    	addDockWidget(Qt.DockWidgetArea.LeftDockWidgetArea, dock);
    }
    
    public void about()
    {
//        QMessageBox.about(this,
//                         tr("About Near Lineaments Viewer"),
//                         tr("(C) 2009 The Johns Hopkins University Applied Physics Laboratory"));
    }

    
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (Exception e) {}

        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        //MainWindow.start("JIEDDO Analysis Tool", AppFrame.class);

        String appName = "Eros Lineament Viewer";
        //Class<AppFrame> appFrameClass)

//        if (Configuration.isMacOS() && appName != null)
//        {
//            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
//        }

        try
        {
        	//final AppFrame frame = (AppFrame) appFrameClass.newInstance();
            final ErosLineamentViewer frame = new ErosLineamentViewer();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
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
