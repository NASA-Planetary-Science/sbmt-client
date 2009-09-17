package edu.jhuapl.near;

import javax.swing.*;

import java.awt.*;

public class ErosLineamentViewer extends JFrame 
{
	private JSplitPane splitPane = null;
	private ImageGLWidget imageViewer;
	private ControlPanel controlPanel;

    private LineamentModel lineamentModel = new LineamentModel();
    
	public ErosLineamentViewer()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		imageViewer = new ImageGLWidget(lineamentModel);
		controlPanel = new ControlPanel(imageViewer);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, imageViewer);
		splitPane.setOneTouchExpandable(true);

        imageViewer.setMinimumSize(new Dimension(100, 100));
        imageViewer.setPreferredSize(new Dimension(700, 700));
        controlPanel.setMinimumSize(new Dimension(300, 100));
        controlPanel.setPreferredSize(new Dimension(300, 700));

		this.getContentPane().add(splitPane);
		
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
	
    private void createActions()
    {
    }

    private void createMenus()
    {
    }

    private void createToolBars()
    {
    }

    private void createStatusBar()
    {
    }

    private void createDockWindows()
    {
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

        String appName = "Eros Lineament Viewer";
        //Class<AppFrame> appFrameClass)

//        if (Configuration.isMacOS() && appName != null)
//        {
//            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
//        }

        try
        {
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
