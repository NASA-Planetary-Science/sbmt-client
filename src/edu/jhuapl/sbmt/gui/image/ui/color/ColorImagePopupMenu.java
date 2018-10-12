package edu.jhuapl.sbmt.gui.image.ui.color;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.gui.dialog.OpacityChanger;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.model.image.ColorImage;
import edu.jhuapl.sbmt.model.image.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;

import nom.tam.fits.FitsException;


public class ColorImagePopupMenu extends PopupMenu
{
    private Component invoker;
    private ColorImageCollection imageCollection;
    private ColorImageKey imageKey;
    //private ModelManager modelManager;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem changeNormalOffsetMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideImageMenuItem;
    private SbmtInfoWindowManager infoPanelManager;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on the small body, 2 for right clicks on images
     * mapped to the small body.
     */
    public ColorImagePopupMenu(
            ColorImageCollection imageCollection,
            SbmtInfoWindowManager infoPanelManager,
            ModelManager modelManager,
            Component invoker)
    {
        this.imageCollection = imageCollection;
        this.infoPanelManager = infoPanelManager;
        //this.modelManager = modelManager;
        this.invoker = invoker;

        showRemoveImageIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveImageIn3DMenuItem.setText("Map Color Image");
        this.add(showRemoveImageIn3DMenuItem);

        if (this.infoPanelManager != null)
        {
            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showImageInfoMenuItem.setText("Properties...");
            this.add(showImageInfoMenuItem);
        }

        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction());
        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
        this.add(changeNormalOffsetMenuItem);

        changeOpacityMenuItem = new JMenuItem(new ChangeOpacityAction());
        changeOpacityMenuItem.setText("Change Opacity...");
        this.add(changeOpacityMenuItem);

        hideImageMenuItem = new JCheckBoxMenuItem(new HideImageAction());
        hideImageMenuItem.setText("Hide Image");
        this.add(hideImageMenuItem);
    }

    public void setCurrentImage(ColorImageKey key)
    {
        imageKey = key;

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        boolean containsImage = imageCollection.containsImage(imageKey);

        showRemoveImageIn3DMenuItem.setSelected(containsImage);

        if (showImageInfoMenuItem != null)
            showImageInfoMenuItem.setEnabled(containsImage);

        changeNormalOffsetMenuItem.setEnabled(containsImage);
        changeOpacityMenuItem.setEnabled(containsImage);
        hideImageMenuItem.setEnabled(containsImage);

        ColorImage image = imageCollection.getImage(imageKey);
        if (image != null)
        {
            boolean selectHideImage = !image.isVisible();
            hideImageMenuItem.setSelected(selectHideImage);
        }
    }


    private class ShowRemoveIn3DAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (showRemoveImageIn3DMenuItem.isSelected())
                    imageCollection.addImage(imageKey);
                else
                    imageCollection.removeImage(imageKey);

                updateMenuItems();
            }
            catch (FitsException e1) {
                e1.printStackTrace();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            catch (NoOverlapException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class ShowInfoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                imageCollection.addImage(imageKey);
                infoPanelManager.addData(imageCollection.getImage(imageKey));

                updateMenuItems();
            }
            catch (FitsException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private class ChangeNormalOffsetAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            ColorImage image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
                changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                changeOffsetDialog.setVisible(true);
            }
        }
    }

    private class ChangeOpacityAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            ColorImage image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                OpacityChanger opacityChanger = new OpacityChanger(image);
                opacityChanger.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                opacityChanger.setVisible(true);
            }
        }
    }

    private class HideImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                imageCollection.addImage(imageKey);
                ColorImage image = imageCollection.getImage(imageKey);
                image.setVisible(!hideImageMenuItem.isSelected());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            updateMenuItems();
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (imageCollection.getImage((vtkActor)pickedProp) != null)
            {
                ColorImage image = imageCollection.getImage((vtkActor)pickedProp);
                setCurrentImage(image.getColorKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
