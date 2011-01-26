package edu.jhuapl.near.popupmenus.eros;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.MSIImageCollection;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection.Boundary;
import edu.jhuapl.near.model.eros.MSIImage.MSIKey;
import edu.jhuapl.near.popupmenus.PopupMenu;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.MathUtil;


public class MSIPopupMenu extends PopupMenu
{
    private Component invoker;
    private ModelManager modelManager;
    private MSIKey msiKey;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem showRemoveBoundaryIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem saveBackplanesMenuItem;
    private JMenuItem centerImageMenuItem;
    private JMenuItem showFrustumMenuItem;
    private ModelInfoWindowManager infoPanelManager;
    private Renderer renderer;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public MSIPopupMenu(
            ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            Renderer renderer,
            Component invoker)
    {
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;
        this.renderer = renderer;
        this.invoker = invoker;

        showRemoveImageIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveImageIn3DMenuItem.setText("Show Image");
        this.add(showRemoveImageIn3DMenuItem);

        showRemoveBoundaryIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveOutlineIn3DAction());
        showRemoveBoundaryIn3DMenuItem.setText("Show Image Boundary");
        this.add(showRemoveBoundaryIn3DMenuItem);

        if (this.infoPanelManager != null)
        {
            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showImageInfoMenuItem.setText("Properties...");
            this.add(showImageInfoMenuItem);
        }

        saveToDiskMenuItem = new JMenuItem(new SaveImageAction());
        saveToDiskMenuItem.setText("Save Raw FIT Image to Disk...");
        this.add(saveToDiskMenuItem);

        saveBackplanesMenuItem = new JMenuItem(new SaveBackplanesAction());
        saveBackplanesMenuItem.setText("Generate Backplanes...");
        this.add(saveBackplanesMenuItem);

        if (renderer != null)
        {
            centerImageMenuItem = new JMenuItem(new CenterImageAction());
            centerImageMenuItem.setText("Center in Window");
            this.add(centerImageMenuItem);
        }

        showFrustumMenuItem = new JCheckBoxMenuItem(new ShowFrustumAction());
        showFrustumMenuItem.setText("Show Frustum");
        this.add(showFrustumMenuItem);

    }

    public void setCurrentImage(MSIKey key)
    {
        msiKey = key;

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
        MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);

        boolean containsImage = msiImages.containsImage(msiKey);
        boolean containsBoundary = msiBoundaries.containsBoundary(msiKey);

        showRemoveBoundaryIn3DMenuItem.setSelected(containsBoundary);

        showRemoveImageIn3DMenuItem.setSelected(containsImage);

        if (centerImageMenuItem != null)
        {
            if (containsBoundary || containsImage)
                centerImageMenuItem.setEnabled(true);
            else
                centerImageMenuItem.setEnabled(false);
        }

        if (showImageInfoMenuItem != null)
            showImageInfoMenuItem.setEnabled(containsImage);

        saveBackplanesMenuItem.setEnabled(containsImage);
        saveToDiskMenuItem.setEnabled(containsImage);

        if (containsImage)
        {
            MSIImage image = msiImages.getImage(msiKey);
            showFrustumMenuItem.setSelected(image.isFrustumShowing());
            showFrustumMenuItem.setEnabled(true);
        }
        else
        {
            showFrustumMenuItem.setSelected(false);
            showFrustumMenuItem.setEnabled(false);
        }
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

    private class ShowRemoveOutlineIn3DAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
            try
            {
                if (showRemoveBoundaryIn3DMenuItem.isSelected())
                    model.addBoundary(msiKey);
                else
                    model.removeBoundary(msiKey);

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

    private class SaveImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            File file = CustomFileChooser.showSaveDialog(invoker, "Save FIT file", msiKey.name + ".FIT", "fit");
            try
            {
                if (file != null)
                {
                    File fitFile = FileCache.getFileFromServer(msiKey.name + ".FIT");

                    FileUtil.copyFile(fitFile, file);
                }
            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private class CenterImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            double[] spacecraftPosition = new double[3];
            double[] boresightDirection = new double[3];
            double[] upVector = new double[3];

            MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
            MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
            if (msiBoundaries.containsBoundary(msiKey))
            {
                Boundary boundary = msiBoundaries.getBoundary(msiKey);
                boundary.getCameraOrientation(spacecraftPosition, boresightDirection, upVector);
            }
            else if (msiImages.containsImage(msiKey))
            {
                MSIImage image = msiImages.getImage(msiKey);
                image.getCameraOrientation(spacecraftPosition, boresightDirection, upVector);
            }
            else
            {
                return;
            }

            final double norm = MathUtil.vnorm(spacecraftPosition);
            double[] position = {
                    spacecraftPosition[0] + 0.6*norm*boresightDirection[0],
                    spacecraftPosition[1] + 0.6*norm*boresightDirection[1],
                    spacecraftPosition[2] + 0.6*norm*boresightDirection[2]
            };
            double[] focalPoint = {
                    position[0] + 0.25*norm*boresightDirection[0],
                    position[1] + 0.25*norm*boresightDirection[1],
                    position[2] + 0.25*norm*boresightDirection[2]
            };

            renderer.setCameraOrientation(position, focalPoint, upVector);
        }
    }

    private class SaveBackplanesAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            // First generate the DDR

            File file = CustomFileChooser.showSaveDialog(invoker, "Save Backplanes DDR", msiKey.name + "_DDR.IMG");

            try
            {
                if (file != null)
                {
                    OutputStream out = new FileOutputStream(file);

                    MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
                    msiImages.addImage(msiKey);
                    MSIImage image = msiImages.getImage(msiKey);

                    updateMenuItems();

                    float[] backplanes = image.generateBackplanes();

                    byte[] buf = new byte[4 * backplanes.length];
                    for (int i=0; i<backplanes.length; ++i)
                    {
                        int v = Float.floatToIntBits(backplanes[i]);
                        buf[4*i + 0] = (byte)(v >>> 24);
                        buf[4*i + 1] = (byte)(v >>> 16);
                        buf[4*i + 2] = (byte)(v >>>  8);
                        buf[4*i + 3] = (byte)(v >>>  0);
                    }
                    out.write(buf, 0, buf.length);
                    out.close();
                }
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

            // Then generate the LBL file

            file = CustomFileChooser.showSaveDialog(invoker, "Save Backplanes Label", msiKey.name + "_DDR.LBL");

            try
            {
                if (file != null)
                {
                    OutputStream out = new FileOutputStream(file);

                    MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
                    msiImages.addImage(msiKey);
                    MSIImage image = msiImages.getImage(msiKey);

                    updateMenuItems();

                    String lblstr = image.generateBackplanesLabel();

                    byte[] bytes = lblstr.getBytes();
                    out.write(bytes, 0, bytes.length);
                    out.close();
                }
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

        }
    }

    private class ShowFrustumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
                msiImages.addImage(msiKey);
                MSIImage image = msiImages.getImage(msiKey);
                image.setShowFrustum(showFrustumMenuItem.isSelected());

                updateMenuItems();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
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
