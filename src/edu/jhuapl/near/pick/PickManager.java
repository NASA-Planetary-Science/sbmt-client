package edu.jhuapl.near.pick;

import java.awt.Cursor;
import java.util.HashMap;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.util.Preferences;

public class PickManager extends Picker
{
    public enum PickMode
    {
        DEFAULT,
        CIRCLE_SELECTION,
        LINE_DRAW,
        CIRCLE_DRAW,
        ELLIPSE_DRAW,
        POINT_DRAW,
        LIDAR_SHIFT
    }

    private PickMode pickMode = PickMode.DEFAULT;
    private Renderer renderer;
    private vtkRenderWindowPanel renWin;

    private DefaultPicker defaultPicker;

    private HashMap<PickMode, Picker> nondefaultPickers = new HashMap<PickMode, Picker>();

    public PickManager(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        this.renderer = renderer;
        this.renWin = renderer.getRenderWindowPanel();

        nondefaultPickers.put(PickMode.LINE_DRAW, new LinePicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.CIRCLE_DRAW, new CirclePicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.ELLIPSE_DRAW, new EllipsePicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.POINT_DRAW, new PointPicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.CIRCLE_SELECTION, new CircleSelectionPicker(renderer, modelManager));

        LidarSearchDataCollection lidarModel = (LidarSearchDataCollection) modelManager.getModel(ModelNames.LIDAR_SEARCH);
        if (lidarModel != null)
            nondefaultPickers.put(PickMode.LIDAR_SHIFT, new LidarShiftPicker(renderer, modelManager));

        defaultPicker = new DefaultPicker(renderer, statusBar, modelManager, popupManager);

        setPickTolerance(Preferences.getInstance().getAsDouble(
                Preferences.PICK_TOLERANCE, Picker.DEFAULT_PICK_TOLERANCE));

        addPicker(defaultPicker);
    }

    public void setPickMode(PickMode mode)
    {
        if (this.pickMode == mode)
            return;

        this.pickMode = mode;

        if (this.pickMode == PickMode.DEFAULT)
        {
            renderer.setInteractorStyleToDefault();
            for (PickMode pm : nondefaultPickers.keySet())
            {
                removePicker(nondefaultPickers.get(pm));
            }
            defaultPicker.setSuppressPopups(false);
            renWin.setCursor(new Cursor(defaultPicker.getDefaultCursor()));
        }
        else
        {
            renderer.setInteractorStyleToNone();
            for (PickMode pm : nondefaultPickers.keySet())
            {
                if (pm != this.pickMode)
                {
                    removePicker(nondefaultPickers.get(pm));
                }
            }
            Picker picker = nondefaultPickers.get(this.pickMode);
            addPicker(picker);
            defaultPicker.setSuppressPopups(true);
            renWin.setCursor(new Cursor(picker.getDefaultCursor()));
        }
    }

    public DefaultPicker getDefaultPicker()
    {
        return defaultPicker;
    }

    private void addPicker(Picker picker)
    {
        renWin.addMouseListener(picker);
        renWin.addMouseMotionListener(picker);
        renWin.addMouseWheelListener(picker);
        renWin.addKeyListener(picker);
    }

    private void removePicker(Picker picker)
    {
        renWin.removeMouseListener(picker);
        renWin.removeMouseMotionListener(picker);
        renWin.removeMouseWheelListener(picker);
        renWin.removeKeyListener(picker);
    }

    public double getPickTolerance()
    {
        // All the pickers managed by this class should have the same
        // tolerance so just return tolerance of the default picker.
        return defaultPicker.getPickTolerance();
    }

    public void setPickTolerance(double pickTolerance)
    {
        defaultPicker.setPickTolerance(pickTolerance);
        for (PickMode pm : nondefaultPickers.keySet())
            nondefaultPickers.get(pm).setPickTolerance(pickTolerance);
    }
}
