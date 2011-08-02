package edu.jhuapl.near.pick;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;

/**
 * This is the picker normally in use by default.
 * @author eli
 *
 */
public class DefaultPicker extends Picker
{
    private vtkRenderWindowPanel renWin;
    private StatusBar statusBar;
    private ModelManager modelManager;
    private PopupManager popupManager;
    private vtkCellPicker mousePressNonSmallBodyCellPicker; // includes all props EXCEPT the small body
    private vtkCellPicker smallBodyCellPicker; // only includes small body prop
    private DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
    private DecimalFormat decimalFormatter2 = new DecimalFormat("#0.000");
    private boolean suppressPopups = false;

    public DefaultPicker(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.statusBar = statusBar;
        this.modelManager = modelManager;
        this.popupManager = popupManager;

        modelManager.addPropertyChangeListener(this);

        SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

        // See comment in the propertyChange function below as to why
        // we use a custom pick list for these pickers.
        mousePressNonSmallBodyCellPicker = new vtkCellPicker();
        mousePressNonSmallBodyCellPicker.SetTolerance(0.002);
        mousePressNonSmallBodyCellPicker.PickFromListOn();
        mousePressNonSmallBodyCellPicker.InitializePickList();

        smallBodyCellPicker = new vtkCellPicker();
        smallBodyCellPicker.SetTolerance(0.002);
        smallBodyCellPicker.PickFromListOn();
        smallBodyCellPicker.InitializePickList();
        ArrayList<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyCellPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyCellPicker.AddPickList(act);
        }
        smallBodyCellPicker.AddLocator(smallBodyModel.getCellLocator());
    }

    public void setSuppressPopups(boolean b)
    {
        this.suppressPopups = b;
    }

    public void mousePressed(MouseEvent e)
    {
        if (renWin.GetRenderWindow().GetNeverRendered() > 0)
            return;

        // First try picking on the non-small-body picker. If that fails try the small body picker.
        int pickSucceeded = doPick(e, mousePressNonSmallBodyCellPicker, renWin);

        if (pickSucceeded == 1)
        {
            vtkActor pickedActor = mousePressNonSmallBodyCellPicker.GetActor();
            Model model = modelManager.getModel(pickedActor);

            if (model != null)
            {
                int cellId = mousePressNonSmallBodyCellPicker.GetCellId();
                double[] pickPosition = mousePressNonSmallBodyCellPicker.GetPickPosition();
                String text = model.getClickStatusBarText(pickedActor, cellId, pickPosition);
                statusBar.setLeftText(text);
                pcs.firePropertyChange(
                        Properties.MODEL_PICKED,
                        null,
                        new PickEvent(e, pickedActor, cellId, pickPosition));
            }
        }
        else
        {
            // If the non-small-body picker failed, see if the user clicked on the small body itself.
            pickSucceeded = doPick(e, smallBodyCellPicker, renWin);

            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyCellPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model != null)
                {
                    int cellId = smallBodyCellPicker.GetCellId();
                    double[] pickPosition = smallBodyCellPicker.GetPickPosition();
                    String text = model.getClickStatusBarText(pickedActor, cellId, pickPosition);
                    statusBar.setLeftText(text);
                    pcs.firePropertyChange(
                            Properties.MODEL_PICKED,
                            null,
                            new PickEvent(e, pickedActor, cellId, pickPosition));
                }
            }
            else
            {
                statusBar.setLeftText(" ");
            }
        }
    }

    public void mouseClicked(MouseEvent e)
    {
        // Note that in general when a popup should appear is system dependent. On some systems
        // popups are triggered on mouse press and on others on mouse release.
        // However, in the renderer, we always want the popup to appear on mouse RELEASE
        // not mouse press regardless of platform, because otherwise the popup will interfere
        // with renderer's zoom in and out feature. Therefore, to avoid this whole
        // issue we only show the popup within the mouseClicked call when the right
        // mouse button was pressed, since the mouseClicked event is only thrown
        // when the mouse is released.
        if (e.getClickCount() == 1 &&
                (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
        {
            maybeShowPopup(e);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        showPositionInfoInStatusBar(e);
    }

    public void mouseDragged(MouseEvent e)
    {
        showPositionInfoInStatusBar(e);
    }

    public void mouseMoved(MouseEvent e)
    {
        if (renWin.GetRenderWindow().GetNeverRendered() > 0)
            return;

        showPositionInfoInStatusBar(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (suppressPopups)
            return;

        if (renWin.GetRenderWindow().GetNeverRendered() > 0)
            return;

        int pickSucceeded = doPick(e, mousePressNonSmallBodyCellPicker, renWin);
        if (pickSucceeded == 1)
        {
            vtkActor pickedActor = mousePressNonSmallBodyCellPicker.GetActor();
            popupManager.showPopup(
                    e,
                    pickedActor,
                    mousePressNonSmallBodyCellPicker.GetCellId(),
                    mousePressNonSmallBodyCellPicker.GetPickPosition());
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(Properties.MODEL_CHANGED))
        {
            // Whenever the model actors change, we need to update the pickers
            // internal list of all actors to pick from. The small body actor is excluded
            // from this list since many other actors occupy the same position
            // as parts of the small body and we want the picker to pick these other
            // actors rather than the small body. Note that this exclusion only applies
            // to the following picker.
            ArrayList<vtkProp> actors = modelManager.getPropsExceptSmallBody();
            vtkPropCollection mousePressNonSmallBodyCellPickList = mousePressNonSmallBodyCellPicker.GetPickList();
            mousePressNonSmallBodyCellPickList.RemoveAllItems();
            for (vtkProp act : actors)
            {
                mousePressNonSmallBodyCellPicker.AddPickList(act);
            }

            // Note that this picker includes only the small body prop so that if the
            // the previous picker fails, we then invoke this picker on small body itself.
            // Note: even though we initialized this picker in the constructor with
            // the small body prop, we need to reset the props here again to support situation
            // where user hides small body where we don't want to show anything in status bar.
            // Without the following, coordinates would still be shown in status bar even
            // if asteroid is hidden.
            actors = modelManager.getSmallBodyModel().getProps();
            vtkPropCollection mousePressSmallBodyCellPickList = smallBodyCellPicker.GetPickList();
            mousePressSmallBodyCellPickList.RemoveAllItems();
            for (vtkProp act : actors)
            {
                smallBodyCellPicker.AddPickList(act);
            }
        }
    }

    private void showPositionInfoInStatusBar(MouseEvent e)
    {
        if (renWin.GetRenderWindow().GetNeverRendered() > 0)
            return;

        vtkCamera activeCamera = renWin.GetRenderer().GetActiveCamera();
        double[] cameraPos = activeCamera.GetPosition();
        double distance = Math.sqrt(
                cameraPos[0]*cameraPos[0] +
                cameraPos[1]*cameraPos[1] +
                cameraPos[2]*cameraPos[2]);
        String distanceStr = decimalFormatter.format(distance);
        if (distanceStr.length() == 5)
            distanceStr = "  " + distanceStr;
        else if (distanceStr.length() == 6)
            distanceStr = " " + distanceStr;
        distanceStr += " km";

        int pickSucceeded = doPick(e, smallBodyCellPicker, renWin);

        if (pickSucceeded == 1)
        {
            double[] pos = smallBodyCellPicker.GetPickPosition();
            LatLon llr = MathUtil.reclat(pos);

            // Note \u00B0 is the unicode degree symbol

            //double sign = 1.0;
            double lat = llr.lat*180/Math.PI;
            //if (lat < 0.0)
            //    sign = -1.0;
            String latStr = decimalFormatter.format(lat);
            if (latStr.length() == 5)
                latStr = "  " + latStr;
            else if (latStr.length() == 6)
                latStr = " " + latStr;
            //if (lat >= 0.0)
            //    latStr += "\u00B0N";
            //else
            //    latStr += "\u00B0S";
            latStr += "\u00B0";

            // Note that the convention seems to be that longitude
            // is never negative and is shown as E. longitude.
            double lon = llr.lon*180/Math.PI;
            if (lon < 0.0)
                lon += 360.0;
            String lonStr = decimalFormatter.format(lon);
            if (lonStr.length() == 5)
                lonStr = "  " + lonStr;
            else if (lonStr.length() == 6)
                lonStr = " " + lonStr;
            //if (lon >= 0.0)
            //    lonStr += "\u00B0E";
            //else
            //    lonStr += "\u00B0W";
            lonStr += "\u00B0";

            double rad = llr.rad;
            String radStr = decimalFormatter2.format(rad);
            if (radStr.length() == 5)
                radStr = " " + radStr;
            radStr += " km";

            statusBar.setRightText("Lat: " + latStr + "  Lon: " + lonStr + "  Radius: " + radStr + "  Distance: " + distanceStr + " ");
        }
        else
        {
            statusBar.setRightText("Distance: " + distanceStr + " ");
        }
    }

}
