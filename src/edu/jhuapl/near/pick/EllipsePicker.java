package edu.jhuapl.near.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

public class EllipsePicker extends Picker
{
    private ModelManager modelManager;
    private vtkRenderWindowPanel renWin;
    private SmallBodyModel smallBodyModel;
    private EllipseModel ellipseModel;

    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker ellipsePicker;

    private int vertexIdBeingEdited = -1;

    // There are 2 types of line editing possible:
    //   1. Dragging an existing vertex to a new locations
    //   2. Extending a line by adding new vertices
    public enum EditMode
    {
        VERTEX_DRAG_OR_DELETE,
        VERTEX_ADD
    }

    private EditMode currentEditMode = EditMode.VERTEX_ADD;

    private double[] lastDragPosition;
    private boolean changeFlatteningKeyPressed = false;
    private boolean changeAngleKeyPressed = false;

    public EllipsePicker(
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.ellipseModel = (EllipseModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);

        smallBodyPicker = new vtkCellPicker();
        smallBodyPicker.SetTolerance(0.002);
        smallBodyPicker.PickFromListOn();
        smallBodyPicker.InitializePickList();
        smallBodyModel = modelManager.getSmallBodyModel();
        ArrayList<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyPicker.AddPickList(act);
        }
        smallBodyPicker.AddLocator(smallBodyModel.getCellLocator());

        ellipsePicker = new vtkCellPicker();
        ellipsePicker.SetTolerance(0.002);
        ellipsePicker.PickFromListOn();
        ellipsePicker.InitializePickList();
        vtkPropCollection ellipsePickList = ellipsePicker.GetPickList();
        ellipsePickList.RemoveAllItems();
        ellipsePicker.AddPickList(ellipseModel.getBoundaryActor());
    }

    public void mousePressed(MouseEvent e)
    {
        // If we pressed a vertex of an existing ellipse, begin dragging that vertex.
        // If we pressed a point on the asteroid, begin drawing a new ellipse.


        vertexIdBeingEdited = -1;
        lastDragPosition = null;

        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
        {
            if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
                return;

            int pickSucceeded = doPick(e, ellipsePicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = ellipsePicker.GetActor();

                if (pickedActor == ellipseModel.getBoundaryActor())
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        int cellId = ellipsePicker.GetCellId();
                        int pointId = ellipseModel.getPolygonIdFromBoundaryCellId(cellId);
                        this.vertexIdBeingEdited = pointId;
                    }
                    else
                    {
                        vertexIdBeingEdited = -1;
                    }
                }
            }
        }
        else if (this.currentEditMode == EditMode.VERTEX_ADD)
        {
            if (e.getButton() != MouseEvent.BUTTON1)
                return;

            int pickSucceeded = doPick(e, smallBodyPicker, renWin);

            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    double[] pos = smallBodyPicker.GetPickPosition();
                    if (e.getClickCount() == 1)
                    {
                        ellipseModel.addNewStructure(pos);
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
//        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
//                vertexIdBeingEdited >= 0 &&
//                lastDragPosition != null)
//        {
//            pointModel.updateSelectedLineVertex(vertexIdBeingEdited, lastDragPosition);
//        }
//
//        vertexIdBeingEdited = -1;
    }

    public void mouseDragged(MouseEvent e)
    {
        //if (e.getButton() != MouseEvent.BUTTON1)
        //    return;


        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
            vertexIdBeingEdited >= 0)
        {
            int pickSucceeded = doPick(e, smallBodyPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    lastDragPosition = smallBodyPicker.GetPickPosition();

                    if (e.isControlDown() || e.isShiftDown())
                        ellipseModel.changeRadiusOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else if (changeFlatteningKeyPressed)
                        ellipseModel.changeFlatteningOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else if (changeAngleKeyPressed)
                        ellipseModel.changeAngleOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else
                        ellipseModel.movePolygon(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, ellipsePicker, renWin);
        if (pickSucceeded == 1 &&
                ellipsePicker.GetActor() == ellipseModel.getBoundaryActor())
        {
            if (renWin.getCursor().getType() != Cursor.HAND_CURSOR)
                renWin.setCursor(new Cursor(Cursor.HAND_CURSOR));

            currentEditMode = EditMode.VERTEX_DRAG_OR_DELETE;
        }
        else
        {
            if (renWin.getCursor().getType() != getDefaultCursor())
                renWin.setCursor(new Cursor(getDefaultCursor()));

            currentEditMode = EditMode.VERTEX_ADD;
        }
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_SLASH)
            changeFlatteningKeyPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_PERIOD)
            changeAngleKeyPressed = true;
    }

    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_SLASH)
            changeFlatteningKeyPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_PERIOD)
            changeAngleKeyPressed = false;
    }

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }
}
