package edu.jhuapl.near.pick;

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
        POINT_DRAW
    }

    private PickMode pickMode = PickMode.DEFAULT;
    private Renderer renderer;
    private vtkRenderWindowPanel renWin;

    private Picker linePicker;
    private Picker circlePicker;
    private Picker pointPicker;
    private DefaultPicker defaultPicker;
    private Picker circleSelectionPicker;

    public PickManager(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        this.renderer = renderer;
        this.renWin = renderer.getRenderWindowPanel();

        modelManager.addPropertyChangeListener(this);

        renWin.addMouseListener(this);
        renWin.addMouseMotionListener(this);
        renWin.addMouseWheelListener(this);

        linePicker = new LinePicker(renderer, modelManager);
        circlePicker = new CirclePicker(renderer, modelManager);
        pointPicker = new PointPicker(renderer, modelManager);

        circleSelectionPicker = new CircleSelectionPicker(renderer, modelManager);

        defaultPicker = new DefaultPicker(renderer, statusBar, modelManager, popupManager);

        addPicker(defaultPicker);
    }

    public void setPickMode(PickMode mode)
    {
        if (this.pickMode == mode)
            return;

        this.pickMode = mode;
        switch(this.pickMode)
        {
        case DEFAULT:
            renderer.setInteractorToDefault();
            removePicker(linePicker);
            removePicker(circlePicker);
            removePicker(pointPicker);
            removePicker(circleSelectionPicker);
            defaultPicker.setSuppressPopups(false);
            break;
        case LINE_DRAW:
            renderer.setInteractorToNone();
            removePicker(circlePicker);
            removePicker(pointPicker);
            removePicker(circleSelectionPicker);
            addPicker(linePicker);
            defaultPicker.setSuppressPopups(true);
            break;
        case CIRCLE_DRAW:
            renderer.setInteractorToNone();
            removePicker(linePicker);
            removePicker(pointPicker);
            removePicker(circleSelectionPicker);
            addPicker(circlePicker);
            defaultPicker.setSuppressPopups(true);
            break;
        case POINT_DRAW:
            renderer.setInteractorToNone();
            removePicker(linePicker);
            removePicker(circlePicker);
            removePicker(circleSelectionPicker);
            addPicker(pointPicker);
            defaultPicker.setSuppressPopups(true);
            break;
        case CIRCLE_SELECTION:
            renderer.setInteractorToNone();
            removePicker(linePicker);
            removePicker(pointPicker);
            removePicker(circlePicker);
            addPicker(circleSelectionPicker);
            defaultPicker.setSuppressPopups(true);
            break;
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
    }

    private void removePicker(Picker picker)
    {
        renWin.removeMouseListener(picker);
        renWin.removeMouseMotionListener(picker);
        renWin.removeMouseWheelListener(picker);
    }
}
