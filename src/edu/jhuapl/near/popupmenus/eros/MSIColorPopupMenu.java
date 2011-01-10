package edu.jhuapl.near.popupmenus.eros;

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
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.MSIImageCollection;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection.Boundary;
import edu.jhuapl.near.model.eros.MSIImage.MSIKey;
import edu.jhuapl.near.popupmenus.PopupMenu;


public class MSIColorPopupMenu extends PopupMenu
{
    private ModelManager modelManager;
    private MSIKey msiKey;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private ModelInfoWindowManager infoPanelManager;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public MSIColorPopupMenu(
            ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager)
    {
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;

        showRemoveImageIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveImageIn3DMenuItem.setText("Show Image");
        this.add(showRemoveImageIn3DMenuItem);

        if (this.infoPanelManager != null)
        {
            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showImageInfoMenuItem.setText("Properties...");
            this.add(showImageInfoMenuItem);
        }

    }

    public void setCurrentImage(MSIKey key)
    {
        msiKey = key;

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);

        boolean containsImage = msiImages.containsImage(msiKey);

        showRemoveImageIn3DMenuItem.setSelected(containsImage);

        if (showImageInfoMenuItem != null)
            showImageInfoMenuItem.setEnabled(containsImage);
    }


    private class ShowRemoveIn3DAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            MSIImageCollection model = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
            try
            {
                if (showRemoveImageIn3DMenuItem.isSelected())
                    model.addImage(msiKey);
                else
                    model.removeImage(msiKey);

                updateMenuItems();
            }
            catch (FitsException e1) {
                e1.printStackTrace();
            }
            catch (IOException e1) {
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
                MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
                msiImages.addImage(msiKey);
                infoPanelManager.addData(msiImages.getImage(msiKey));

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


    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (modelManager.getModel(pickedProp) instanceof MSIBoundaryCollection)
            {
                MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
                Boundary boundary = msiBoundaries.getBoundary((vtkActor)pickedProp);
                setCurrentImage(boundary.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
            else if (modelManager.getModel(pickedProp) instanceof MSIImageCollection)
            {
                MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
                MSIImage image = msiImages.getImage((vtkActor)pickedProp);
                setCurrentImage(image.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
