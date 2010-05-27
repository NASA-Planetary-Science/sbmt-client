package edu.jhuapl.near;

import javax.swing.*;

import vtk.vtkJavaGarbageCollector;
import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.ControlPanel;
import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.gui.ErosViewer;
import edu.jhuapl.near.gui.FileMenu;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.util.NativeLibraryLoader;

import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program. 
 * @author kahneg1
 *
 */
public class Sbmt extends JFrame 
{
	//private JSplitPane splitPane;
	//private ErosRenderer renderer;
	//private ControlPanel controlPanel;
	private StatusBar statusBar;
	private FileMenu fileMenu;
	//private ModelManager modelManager;
	//private PickManager pickManager;
	//private PopupManager popupManager;
	//private ModelInfoWindowManager infoPanelManager;
	private static vtkJavaGarbageCollector garbageCollector;
	
	public Sbmt()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createStatusBar();

        JPanel rootPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        
        ErosViewer erosViewer = new ErosViewer(statusBar);
        
		//modelManager = new ModelManager();
	
		//infoPanelManager = new ModelInfoWindowManager(modelManager);
		
		//renderer = new ErosRenderer(modelManager);

		//popupManager = new PopupManager(renderer, modelManager, infoPanelManager);

		//pickManager = new PickManager(renderer, statusBar, modelManager, infoPanelManager, popupManager);

        //controlPanel = new ControlPanel(renderer, modelManager, infoPanelManager, pickManager);

		//splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        //        controlPanel, renderer);
		//splitPane.setOneTouchExpandable(true);

        //renderer.setMinimumSize(new Dimension(100, 100));
        //renderer.setPreferredSize(new Dimension(800, 800));
        //controlPanel.setMinimumSize(new Dimension(320, 100));
        //controlPanel.setPreferredSize(new Dimension(320, 800));

		createMenus(erosViewer.getRenderWindowPanel());

		rootPanel.setLayout(new CardLayout());
		rootPanel.add(erosViewer, "a");
		
		this.add(rootPanel, BorderLayout.CENTER);
		
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

    private void createMenus(vtkRenderWindowPanel renderer)
    {
    	JMenuBar menuBar = new JMenuBar();

    	fileMenu = new FileMenu(renderer, true);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    private void createStatusBar()
    {
    	statusBar = new StatusBar();
    	this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
    }

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

    	garbageCollector = new vtkJavaGarbageCollector();
    	//garbageCollector.SetDebug(true);
    	garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
    	garbageCollector.SetAutoGarbageCollection(true);
    	
        try
        {
        	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            final Sbmt frame = new Sbmt();
        	
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
            	public void run()
            	{
            		ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
                    frame.setIconImage(erosIcon.getImage());
                    frame.setTitle("Small Body Mapping Tool");
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
