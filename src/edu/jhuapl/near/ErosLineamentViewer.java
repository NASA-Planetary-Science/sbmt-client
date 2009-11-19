package edu.jhuapl.near;

import javax.swing.*;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.ControlPanel;
import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.gui.FileMenu;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.pick.PickManager;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.NativeLibraryLoader;

import java.awt.*;

public class ErosLineamentViewer extends JFrame 
{
	private JSplitPane splitPane;
	private ErosRenderer imageViewer;
	private ControlPanel controlPanel;
	private StatusBar statusBar;
	private FileMenu fileMenu;
	private ModelManager modelManager;
	private PickManager pickManager;
	private ModelInfoWindowManager infoPanelManager;
	
	public ErosLineamentViewer()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createStatusBar();

		modelManager = new ModelManager();
	
		infoPanelManager = new ModelInfoWindowManager(modelManager);
		
		imageViewer = new ErosRenderer(modelManager);

		vtkRenderWindowPanel renWin = imageViewer.getRenderWindowPanel();
		pickManager = new PickManager(renWin, statusBar, modelManager, infoPanelManager);

        controlPanel = new ControlPanel(imageViewer, modelManager, infoPanelManager);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, imageViewer);
		splitPane.setOneTouchExpandable(true);

        imageViewer.setMinimumSize(new Dimension(100, 100));
        imageViewer.setPreferredSize(new Dimension(700, 700));
        controlPanel.setMinimumSize(new Dimension(300, 100));
        controlPanel.setPreferredSize(new Dimension(300, 700));

		createMenus(imageViewer);
        createToolBars();

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

    private void createMenus(ErosRenderer imageViewer)
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
    	statusBar = new StatusBar();
    	this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
    }

//    public void about()
//    {
//        QMessageBox.about(this,
//                         tr("About Near Lineaments Viewer"),
//                         tr("(C) 2009 The Johns Hopkins University Applied Physics Laboratory"));
//    }

    public static void main(String[] args)
    {
    	try
        {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
        }

    	NativeLibraryLoader.loadVtkLibraries();

        try
        {
        	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            final ErosLineamentViewer frame = new ErosLineamentViewer();
        	
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
            	public void run()
            	{
                    frame.setTitle("Eros Viewer");
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
