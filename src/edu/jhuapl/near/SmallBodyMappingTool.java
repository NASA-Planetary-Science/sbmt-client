package edu.jhuapl.near;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.near.gui.deimos.DeimosViewer;
import edu.jhuapl.near.gui.FileMenu;
import edu.jhuapl.near.gui.HelpMenu;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.View;
import edu.jhuapl.near.gui.ViewMenu;
import edu.jhuapl.near.gui.eros.ErosViewer;
import edu.jhuapl.near.gui.itokawa.ItokawaViewer;
import edu.jhuapl.near.gui.vesta.VestaViewer;
import edu.jhuapl.near.util.NativeLibraryLoader;


/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program. 
 * @author kahneg1
 *
 */
public class SmallBodyMappingTool extends JFrame 
{
	private StatusBar statusBar;
	private FileMenu fileMenu;
	private ViewMenu viewMenu;
	private HelpMenu helpMenu;
	private JPanel rootPanel;
	private ArrayList<View> views;
	private static vtkJavaGarbageCollector garbageCollector;
	
	public SmallBodyMappingTool()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createStatusBar();

        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(BorderFactory.createEmptyBorder());

        ErosViewer erosViewer = new ErosViewer(statusBar);
        DeimosViewer deimosViewer = new DeimosViewer(statusBar);
        ItokawaViewer itokawaViewer = new ItokawaViewer(statusBar);
        VestaViewer vestaViewer = new VestaViewer(statusBar);
        
        views = new ArrayList<View>();
        views.add(erosViewer);
        views.add(deimosViewer);
        views.add(itokawaViewer);
        views.add(vestaViewer);
        
		createMenus();

		rootPanel.setLayout(new CardLayout());
		rootPanel.add(erosViewer, ErosViewer.NAME);
		rootPanel.add(deimosViewer, DeimosViewer.NAME);
		rootPanel.add(itokawaViewer, ItokawaViewer.NAME);
		rootPanel.add(vestaViewer, VestaViewer.NAME);
		
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

    private void createMenus()
    {
    	JMenuBar menuBar = new JMenuBar();

    	fileMenu = new FileMenu(rootPanel);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        viewMenu = new ViewMenu(rootPanel, views);
        viewMenu.setMnemonic('V');
        menuBar.add(viewMenu);
        
    	helpMenu = new HelpMenu(rootPanel);
        helpMenu.setMnemonic('H');
        menuBar.add(helpMenu);

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
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
            	public void run()
            	{
                    NativeLibraryLoader.loadVtkLibraries();

                    garbageCollector = new vtkJavaGarbageCollector();
                    //garbageCollector.SetDebug(true);
                    garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
                    garbageCollector.SetAutoGarbageCollection(true);
                    
                	try
                    {
                		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        System.out.println(UIManager.getSystemLookAndFeelClassName());
                    } 
                    catch (Exception e) 
                    {
                    	e.printStackTrace();
                    }

                	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
                	
                	SmallBodyMappingTool frame = new SmallBodyMappingTool();
                	
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
