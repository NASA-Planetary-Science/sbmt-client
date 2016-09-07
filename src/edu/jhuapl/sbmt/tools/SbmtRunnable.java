package edu.jhuapl.sbmt.tools;

import java.util.concurrent.TimeUnit;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SbmtMainWindow;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;

public class SbmtRunnable implements Runnable
{
    private String[] args;
    public SbmtRunnable(String[] args)
    {
        this.args = args;
    }

    public void run()
    {
        SmallBodyViewConfig.initialize();

        // Parse options that come first
        int i = 0;
        for (; i < args.length; ++i) {
            if (args[i].equals("--beta")) {
                SmallBodyViewConfig.betaMode = true;
            }else {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

        // After options comes the args
        String tempShapeModelPath = null;
        if (Configuration.isAPLVersion() && args.length - i >= 1){
            tempShapeModelPath = args[i++];
        }

        NativeLibraryLoader.loadVtkLibraries();

        vtkJavaGarbageCollector garbageCollector = new vtkJavaGarbageCollector();
        //garbageCollector.SetDebug(true);
        garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
        garbageCollector.SetAutoGarbageCollection(true);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setDismissDelay(600000); // 10 minutes

        MainWindow frame = new SbmtMainWindow(tempShapeModelPath);
//        MainWindow frame = new MainWindow("data/Torso.stl");
        frame.setVisible(true);
    }

}
