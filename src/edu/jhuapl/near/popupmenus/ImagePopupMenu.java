package edu.jhuapl.near.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
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
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ImageBoundary;
import edu.jhuapl.near.model.ImageBoundaryCollection;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;


public class ImagePopupMenu extends PopupMenu
{
    private Component invoker;
    private ImageCollection imageCollection;
    private ImageBoundaryCollection imageBoundaryCollection;
    private ImageKey imageKey;
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
    public ImagePopupMenu(
            ImageCollection imageCollection,
            ImageBoundaryCollection imageBoundaryCollection,
            ModelInfoWindowManager infoPanelManager,
            Renderer renderer,
            Component invoker)
    {
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;
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

    public void setCurrentImage(ImageKey key)
    {
        imageKey = key;

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        boolean containsImage = imageCollection.containsImage(imageKey);
        boolean containsBoundary = imageBoundaryCollection.containsBoundary(imageKey);

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
            Image image = imageCollection.getImage(imageKey);
            showFrustumMenuItem.setSelected(image.isFrustumShowing());
            showFrustumMenuItem.setEnabled(true);
        }
        else
        {
            showFrustumMenuItem.setSelected(false);
            showFrustumMenuItem.setEnabled(false);
        }
    }


    public class ShowRemoveIn3DAction extends AbstractAction
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
        }
    }

    private class ShowRemoveOutlineIn3DAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (showRemoveBoundaryIn3DMenuItem.isSelected())
                    imageBoundaryCollection.addBoundary(imageKey);
                else
                    imageBoundaryCollection.removeBoundary(imageKey);

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

    private class SaveImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            File file = CustomFileChooser.showSaveDialog(invoker, "Save FIT file", imageKey.name + ".FIT", "fit");
            try
            {
                if (file != null)
                {
                    File fitFile = FileCache.getFileFromServer(imageKey.name + ".FIT");

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
            double[] focalPoint = new double[3];
            double[] upVector = new double[3];
            double viewAngle = 0.0;

            if (imageBoundaryCollection.containsBoundary(imageKey))
            {
                ImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKey);
                boundary.getCameraOrientation(spacecraftPosition, focalPoint, upVector);

                viewAngle = boundary.getImage().getFovAngle();
            }
            else if (imageCollection.containsImage(imageKey))
            {
                Image image = imageCollection.getImage(imageKey);
                image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);

                viewAngle = image.getFovAngle();
            }
            else
            {
                return;
            }

            // Increase the view angle by a little amount to show some of
            // the asteroid around the image.
            viewAngle = 1.25 * viewAngle;

            renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
        }
    }

    private class SaveBackplanesAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            // First generate the DDR

            String defaultFilename = new File(imageKey.name + "_DDR.IMG").getName();
            File file = CustomFileChooser.showSaveDialog(invoker, "Save Backplanes DDR", defaultFilename, "img");

            try
            {
                if (file != null)
                {
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                    imageCollection.addImage(imageKey);
                    Image image = imageCollection.getImage(imageKey);

                    updateMenuItems();

                    float[] backplanes = image.generateBackplanes();

                    byte[] buf = new byte[4];
                    for (int i=0; i<backplanes.length; ++i)
                    {
                        int v = Float.floatToIntBits(backplanes[i]);
                        buf[0] = (byte)(v >>> 24);
                        buf[1] = (byte)(v >>> 16);
                        buf[2] = (byte)(v >>>  8);
                        buf[3] = (byte)(v >>>  0);
                        out.write(buf, 0, buf.length);
                    }

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

            // Then generate the LBL file using the same filename but with a lbl extension.
            // The extension is chosen to have the same case as the img file.

            try
            {
                if (file != null)
                {
                    String lblName = file.getAbsolutePath();
                    lblName = lblName.substring(0, lblName.length()-4);
                    if (file.getAbsolutePath().endsWith("img"))
                        lblName += ".lbl";
                    else
                        lblName += ".LBL";

                    file = new File(lblName);

                    OutputStream out = new FileOutputStream(file);

                    imageCollection.addImage(imageKey);
                    Image image = imageCollection.getImage(imageKey);

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
                imageCollection.addImage(imageKey);
                Image image = imageCollection.getImage(imageKey);
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
            if (imageBoundaryCollection.getBoundary((vtkActor)pickedProp) != null)
            {
                ImageBoundary boundary = imageBoundaryCollection.getBoundary((vtkActor)pickedProp);
                setCurrentImage(boundary.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
            else if (imageCollection.getImage((vtkActor)pickedProp) != null)
            {
                Image image = imageCollection.getImage((vtkActor)pickedProp);
                setCurrentImage(image.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
