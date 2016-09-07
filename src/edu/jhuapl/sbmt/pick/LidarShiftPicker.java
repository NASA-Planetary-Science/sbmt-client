package edu.jhuapl.sbmt.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.jogl.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;

/**
 * This class is used to allow the user to translate a lidar track to a new location.
 * This works as follows. All the tracks are translated such that the currently selected
 * point is translated to the point on the asteroid the user clicked on.
 */
public class LidarShiftPicker extends Picker
{
    private ModelManager modelManager;
    private vtksbmtJoglCanvas renWin;
    private PolyhedralModel smallBodyModel;
    private LidarSearchDataCollection lidarModel;
    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker pointPicker;

    public LidarShiftPicker(
            Renderer renderer,
            ModelManager modelManager,
            LidarSearchDataCollection lidarModel
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.lidarModel = lidarModel;

        smallBodyPicker = new vtkCellPicker();
        smallBodyPicker.PickFromListOn();
        smallBodyPicker.InitializePickList();
        smallBodyModel = (PolyhedralModel)modelManager.getPolyhedralModel();
        List<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyPicker.AddPickList(act);
        }
        smallBodyPicker.AddLocator(smallBodyModel.getCellLocator());

        pointPicker = new vtkCellPicker();
        pointPicker.PickFromListOn();
        pointPicker.InitializePickList();
        vtkPropCollection pointPickList = pointPicker.GetPickList();
        pointPickList.RemoveAllItems();
        pointPicker.AddPickList(lidarModel.getProps().get(0));
    }

    public void mousePressed(MouseEvent e)
    {
        mouseDragged(e);
    }

    public void mouseDragged(MouseEvent e)
    {
        double[] selectedPoint = lidarModel.getSelectedPoint();
        if (selectedPoint != null)
        {
            int pickSucceeded = doPick(e, smallBodyPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    double[] pickPosition = smallBodyPicker.GetPickPosition();
                    double[] shift = {
                            pickPosition[0]-selectedPoint[0],
                            pickPosition[1]-selectedPoint[1],
                            pickPosition[2]-selectedPoint[2]};

                    lidarModel.setTranslation(shift);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, pointPicker, renWin);
        if (pickSucceeded == 1 &&
                pointPicker.GetActor() == lidarModel.getProps().get(0))
        {
            if (renWin.getComponent().getCursor().getType() != Cursor.HAND_CURSOR)
                renWin.getComponent().setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        else
        {
            if (renWin.getComponent().getCursor().getType() != getDefaultCursor())
                renWin.getComponent().setCursor(new Cursor(getDefaultCursor()));
        }
    }

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }
}
