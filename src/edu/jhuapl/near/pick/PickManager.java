package edu.jhuapl.near.pick;

import java.util.HashMap;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.popupmenus.PopupManager;

public class PickManager extends Picker
{
    public enum PickMode
    {
        DEFAULT,
        CIRCLE_SELECTION,
        LINE_DRAW,
        CIRCLE_DRAW,
        ELLIPSE_DRAW,
        POINT_DRAW
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

        modelManager.addPropertyChangeListener(this);

        nondefaultPickers.put(PickMode.LINE_DRAW, new LinePicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.CIRCLE_DRAW, new CirclePicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.ELLIPSE_DRAW, new EllipsePicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.POINT_DRAW, new PointPicker(renderer, modelManager));
        nondefaultPickers.put(PickMode.CIRCLE_SELECTION, new CircleSelectionPicker(renderer, modelManager));

        defaultPicker = new DefaultPicker(renderer, statusBar, modelManager, popupManager);

        addPicker(defaultPicker);
    }

    public void setPickMode(PickMode mode)
    {
        if (this.pickMode == mode)
            return;

        this.pickMode = mode;

        if (this.pickMode == PickMode.DEFAULT)
        {
            renderer.setInteractorToDefault();
            for (PickMode pm : nondefaultPickers.keySet())
            {
                removePicker(nondefaultPickers.get(pm));
            }
            defaultPicker.setSuppressPopups(false);
        }
        else
        {
            renderer.setInteractorToNone();
            for (PickMode pm : nondefaultPickers.keySet())
            {
                if (pm != this.pickMode)
                {
                    removePicker(nondefaultPickers.get(pm));
                }
            }
            addPicker(nondefaultPickers.get(this.pickMode));
            defaultPicker.setSuppressPopups(true);
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
}
