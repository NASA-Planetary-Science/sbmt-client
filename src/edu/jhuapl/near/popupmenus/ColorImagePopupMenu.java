package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.model.ColorImage;
import edu.jhuapl.near.model.ColorImage.ColorImageKey;
import edu.jhuapl.near.model.ColorImage.NoOverlapException;
import edu.jhuapl.near.model.ColorImageCollection;


public class ColorImagePopupMenu extends PopupMenu
{
    private ColorImageCollection imageCollection;
    private ColorImageKey imageKey;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
//    private ModelInfoWindowManager infoPanelManager;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on the small body, 2 for right clicks on images
     * mapped to the small body.
     */
    public ColorImagePopupMenu(
            ColorImageCollection imageCollection,
            ModelInfoWindowManager infoPanelManager)
    {
        this.imageCollection = imageCollection;
//        this.infoPanelManager = infoPanelManager;

        showRemoveImageIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveImageIn3DMenuItem.setText("Show Color Image");
        this.add(showRemoveImageIn3DMenuItem);

//        if (this.infoPanelManager != null)
//        {
//            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
//            showImageInfoMenuItem.setText("Properties...");
//            this.add(showImageInfoMenuItem);
//        }

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

//    private class ShowInfoAction extends AbstractAction
//    {
//        public void actionPerformed(ActionEvent e)
//        {
//            try
//            {
//                imageCollection.addImage(imageKey);
//                infoPanelManager.addData(imageCollection.getImage(imageKey));
//
//                updateMenuItems();
//            }
//            catch (FitsException e1) {
//                e1.printStackTrace();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//        }
//    }


    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (imageCollection.getImage((vtkActor)pickedProp) != null)
            {
                ColorImage image = imageCollection.getImage((vtkActor)pickedProp);
                setCurrentImage(image.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
