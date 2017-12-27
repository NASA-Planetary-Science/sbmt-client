package edu.jhuapl.sbmt.tools;

import java.util.concurrent.TimeUnit;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import vtk.vtkJavaGarbageCollector;
import vtk.vtkNativeLibrary;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.client.SbmtMainWindow;
import edu.jhuapl.sbmt.client.SmallBodyMappingTool;
import edu.jhuapl.sbmt.client.SmallBodyMappingTool.Mission;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;

public class SbmtRunnable implements Runnable
{
    private final String[] args;
    public SbmtRunnable(String[] args)
    {
        this.args = args;
    }

    public void run()
    {
        SmallBodyViewConfig.initialize();
        configureMissionBodies();

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

      //  NativeLibraryLoader.loadVtkLibraries();

        vtkNativeLibrary.LoadAllNativeLibraries();

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

    protected void configureMissionBodies()
    {
        disableAllBodies();
        enableMissionBodies(SmallBodyMappingTool.getMission());
    }

    protected void disableAllBodies()
    {
        for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
        {
            each.enable(false);
        }
    }

    protected void enableMissionBodies(Mission mission)
    {
        for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
        {
            if (each instanceof SmallBodyViewConfig)
            {
                SmallBodyViewConfig config = (SmallBodyViewConfig) each;
                setBodyEnableState(mission, config);
            }
        }

    }

    protected void setBodyEnableState(Mission mission, SmallBodyViewConfig config)
    {
        switch (mission)
        {
        case HAYABUSA2:
            if (
                    ShapeModelBody.EROS.equals(config.body) ||
                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelAuthor.HAYABUSA2.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case HAYABUSA2_STAGE:
            if (
//                    ShapeModelBody.EROS.equals(config.body) ||
//                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelAuthor.HAYABUSA2.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case HAYABUSA2_DEPLOY:
            if (
//                    ShapeModelBody.EROS.equals(config.body) ||
//                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelAuthor.HAYABUSA2.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case NEARTOOL:
            config.enable(true);
            break;
        case OSIRIS_REX:
            if (
                    ShapeModelBody.RQ36.equals(config.body) ||
                    ShapeModelBody.EROS.equals(config.body) ||
                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelAuthor.OREX.equals(config.author)
               )
            {
                config.enable(true);
            }
            break;
            default:
                throw new AssertionError();
        }
    }
}
