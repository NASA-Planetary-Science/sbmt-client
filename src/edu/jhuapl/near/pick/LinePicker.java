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
import edu.jhuapl.near.model.Line;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

public class LinePicker extends Picker
{
    private ModelManager modelManager;
    private vtkRenderWindowPanel renWin;
    private SmallBodyModel smallBodyModel;
    private LineModel lineModel;

    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker linePicker;
    private vtkCellPicker lineSelectionPicker;

    private int vertexIdBeingEdited = -1;

    private boolean profileMode = false;

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

    public LinePicker(
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.lineModel = (LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);

        profileMode = lineModel.hasProfileMode();

        smallBodyPicker = new vtkCellPicker();
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

        linePicker = new vtkCellPicker();
        linePicker.PickFromListOn();
        linePicker.InitializePickList();
        vtkPropCollection linePickList = linePicker.GetPickList();
        linePickList.RemoveAllItems();
        linePicker.AddPickList(lineModel.getLineActor());

        lineSelectionPicker = new vtkCellPicker();
        lineSelectionPicker.PickFromListOn();
        lineSelectionPicker.InitializePickList();
        vtkPropCollection lineSelectionPickList = lineSelectionPicker.GetPickList();
        lineSelectionPickList.RemoveAllItems();
        lineSelectionPicker.AddPickList(lineModel.getLineSelectionActor());
    }

    public void mousePressed(MouseEvent e)
    {
        // If we pressed a vertex of an existing lineament, begin dragging that vertex.
        // If we pressed a point on the body, begin drawing a new line.


        vertexIdBeingEdited = -1;
        lastDragPosition = null;

        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
        {
            if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
                return;

            int pickSucceeded = doPick(e, lineSelectionPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = lineSelectionPicker.GetActor();

                if (pickedActor == lineModel.getLineSelectionActor())
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        vertexIdBeingEdited = lineSelectionPicker.GetCellId();

                        if (profileMode)
                        {
                            int lineId = lineModel.getLineIdFromSelectionCellId(vertexIdBeingEdited);
                            lineModel.selectStructure(lineId);
                        }

                        lineModel.selectCurrentLineVertex(vertexIdBeingEdited);
                    }
                    else
                    {
                        vertexIdBeingEdited = -1;
                        if (profileMode)
                            lineModel.selectStructure(-1);
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
                        lineModel.insertVertexIntoSelectedLine(pos);
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
                vertexIdBeingEdited >= 0 &&
                lastDragPosition != null)
        {
            int vertexId = vertexIdBeingEdited;

            if (profileMode)
                vertexId = lineModel.getVertexIdFromSelectionCellId(vertexIdBeingEdited);

            lineModel.updateSelectedLineVertex(vertexId, lastDragPosition);
        }

        vertexIdBeingEdited = -1;
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

                    lineModel.moveSelectionVertex(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, lineSelectionPicker, renWin);

        // If we're in profile mode, then do not allow dragging of a vertex if we're
        // in the middle of creating a new profile. We can determine if we're in the
        // middle of creating one if the last line in the LineModel has fewer than 2
        // vertices.
        boolean profileModeOkToDrag = true;
        if (profileMode)
        {
            int lineId = lineModel.getNumberOfStructures() - 1;
            if (lineId >= 0)
            {
                Line line = (Line)lineModel.getStructure(lineId);
                if (line.controlPointIds.size() < 2)
                    profileModeOkToDrag = false;
            }
        }

        if (pickSucceeded == 1 &&
            lineSelectionPicker.GetActor() == lineModel.getLineSelectionActor() &&
            profileModeOkToDrag)
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

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }

    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
        {
            lineModel.removeCurrentLineVertex();
        }
    }
}
