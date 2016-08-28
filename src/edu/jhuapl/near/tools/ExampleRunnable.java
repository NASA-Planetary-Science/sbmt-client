package edu.jhuapl.near.tools;

import java.util.concurrent.TimeUnit;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.near.gui.ExampleMainWindow;
import edu.jhuapl.near.gui.MainWindow;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class ExampleRunnable implements Runnable
{
    private String[] args;
    public ExampleRunnable(String[] args)
    {
        this.args = args;
    }

    public void run()
    {
        SmallBodyConfig.initialize();

        String tempShapeModelPath = null;
        // Parse options that come first
        int nargs = args.length;
        int i = 0;
        for (; i < nargs; ++i) {
            if (args[i].equals("--model") && i < nargs + 1)
            {
                tempShapeModelPath = args[i+1];
            }
            else
            {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

        if (tempShapeModelPath == null)
            tempShapeModelPath = "data/frank.obj";

        NativeLibraryLoader.loadVtkLibraries();

        vtkJavaGarbageCollector garbageCollector = new vtkJavaGarbageCollector();
        //garbageCollector.SetDebug(true);
        garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
        garbageCollector.SetAutoGarbageCollection(true);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setDismissDelay(600000); // 10 minutes

        MainWindow frame = new ExampleMainWindow(tempShapeModelPath);
        frame.setVisible(true);
    }

}
