package edu.jhuapl.near;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QDockWidget.DockWidgetFeature;

public class MainWindow extends QMainWindow 
{
	private ImageViewer imageViewer;
	private ImageBrowser imageBrowser;
	private QMenu fileMenu;
    //private QMenu editMenu;
    private QMenu helpMenu;

    //private QToolBar fileToolBar;
    //private QToolBar editToolBar;

    private QAction exitAct;
    private QAction aboutAct;
    //private String rsrcPath = "classpath:com/trolltech/images";

    private LineamentModel lineamentModel = new LineamentModel();
    
	public MainWindow()
	{
		QMenuBar menuBar = new QMenuBar();
		setMenuBar(menuBar);

        setWindowTitle(tr("Near Lineaments Viewer"));
        //setWindowIcon(new QIcon("classpath:com/trolltech/images/qt-logo.png"));
        
        imageViewer = new ImageViewer(lineamentModel, this);
        setCentralWidget(imageViewer);

        try {
            createActions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        createMenus();
        createToolBars();
        createStatusBar();
        createDockWindows();

        resize(800, 600);
	}
	
    private void createActions()
    {
        exitAct = new QAction(tr("E&xit"), this);
        exitAct.setShortcut(tr("Ctrl+Q"));
        exitAct.setStatusTip(tr("Exit the application"));
        exitAct.triggered.connect(this, "close()");

        aboutAct = new QAction(tr("&About"), this);
        aboutAct.setStatusTip(tr("Show the application's About box"));
        aboutAct.triggered.connect(this, "about()");
    }

    private void createMenus()
    {
        fileMenu = menuBar().addMenu(tr("&File"));
        //fileMenu.addSeparator();
        fileMenu.addAction(exitAct);

        //editMenu = menuBar().addMenu(tr("&Edit"));

        menuBar().addSeparator();

        helpMenu = menuBar().addMenu(tr("&Help"));
        helpMenu.addAction(aboutAct);
        //helpMenu.addSeparator();
    }

    private void createToolBars()
    {
        //fileToolBar = addToolBar(tr("File"));

        //editToolBar = addToolBar(tr("Edit"));
    }

    private void createStatusBar()
    {
        statusBar().showMessage(tr("Ready"));
    }

    private void createDockWindows()
    {
    	QDockWidget dock = new QDockWidget(tr("Image Browser"), this);
    	dock.setFeatures(DockWidgetFeature.DockWidgetMovable, DockWidgetFeature.DockWidgetFloatable);
    	imageBrowser = new ImageBrowser(this);
    	imageBrowser.fitFileDoubleClicked.connect(this.imageViewer, "addNewTab(String)");
    	dock.setWidget(imageBrowser);
    	addDockWidget(Qt.DockWidgetArea.LeftDockWidgetArea, dock);
    }
    
    public void about()
    {
        QMessageBox.about(this,
                         tr("About Near Lineaments Viewer"),
                         tr("(C) 2009 The Johns Hopkins University Applied Physics Laboratory"));
    }

}
