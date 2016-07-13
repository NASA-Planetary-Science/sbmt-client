package edu.jhuapl.near.gui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;


/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 */
public class MainWindow extends JFrame
{
    private StatusBar statusBar;
    private FileMenu fileMenu;
    private ViewMenu viewMenu;
    private FavoritesMenu favoritesMenu;
    private HelpMenu helpMenu;
    private ViewManager rootPanel;

    /**
     * @param tempCustomShapeModelPath path to shape model. May be null.
     * If non-null, the main window will create a temporary custom view of the shape model
     * which will be shown first. This temporary view is not saved into the custom application
     * folder and will not be available unless explicitely imported.
     */
    public MainWindow(String tempCustomShapeModelPath)
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createStatusBar();

        rootPanel = new ViewManager(statusBar, this, tempCustomShapeModelPath);

        createMenus();

        this.add(rootPanel, BorderLayout.CENTER);

        ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
        setIconImage(erosIcon.getImage());
        pack();

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

        viewMenu = new ViewMenu(rootPanel);
        viewMenu.setMnemonic('V');
        menuBar.add(viewMenu);

        favoritesMenu = new FavoritesMenu(new FavoritesFile(), rootPanel);
        viewMenu.add(new JSeparator());
        viewMenu.add(favoritesMenu);

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
}
