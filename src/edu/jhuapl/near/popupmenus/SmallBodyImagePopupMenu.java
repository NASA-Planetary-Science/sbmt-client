package edu.jhuapl.near.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.gui.NormalOffsetChangerDialog;
import edu.jhuapl.near.model.SmallBodyImageMap;
import edu.jhuapl.near.model.SmallBodyImageMapCollection;


public class SmallBodyImagePopupMenu extends PopupMenu
{
    private Component invoker;
    private SmallBodyImageMapCollection imageCollection;
    private SmallBodyImageMap currentImage;
    private JMenuItem changeNormalOffsetMenuItem;

    public SmallBodyImagePopupMenu(
            SmallBodyImageMapCollection imageCollection)
    {
        this.imageCollection = imageCollection;

        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction());
        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
        this.add(changeNormalOffsetMenuItem);
    }

    private class ChangeNormalOffsetAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (currentImage != null)
            {
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(currentImage);
                changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                changeOffsetDialog.setVisible(true);
            }
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            currentImage = imageCollection.getImage((vtkActor)pickedProp);
            if (currentImage != null)
            {
                this.invoker = e.getComponent();
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
