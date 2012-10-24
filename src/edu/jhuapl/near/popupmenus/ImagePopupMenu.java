package edu.jhuapl.near.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.NormalOffsetChangerDialog;
import edu.jhuapl.near.gui.OpacityChanger;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.Renderer.LightingType;
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.PerspectiveImageBoundary;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;


public class ImagePopupMenu extends PopupMenu
{
    private Component invoker;
    private ImageCollection imageCollection;
    private PerspectiveImageBoundaryCollection imageBoundaryCollection;
    private ArrayList<ImageKey> imageKeys = new ArrayList<Image.ImageKey>();
    private JMenuItem mapImageMenuItem;
    private JMenuItem mapBoundaryMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem saveBackplanesMenuItem;
    private JMenuItem centerImageMenuItem;
    private JMenuItem showFrustumMenuItem;
    private JMenuItem changeNormalOffsetMenuItem;
    private JMenuItem simulateLightingMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideImageMenuItem;
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
            PerspectiveImageBoundaryCollection imageBoundaryCollection,
            ModelInfoWindowManager infoPanelManager,
            Renderer renderer,
            Component invoker)
    {
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;
        this.infoPanelManager = infoPanelManager;
        this.renderer = renderer;
        this.invoker = invoker;

        mapImageMenuItem = new JCheckBoxMenuItem(new MapImageAction());
        mapImageMenuItem.setText("Map Image");
        this.add(mapImageMenuItem);

        mapBoundaryMenuItem = new JCheckBoxMenuItem(new MapBoundaryAction());
        mapBoundaryMenuItem.setText("Map Image Boundary");
        this.add(mapBoundaryMenuItem);

        if (this.infoPanelManager != null)
        {
            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showImageInfoMenuItem.setText("Properties...");
            this.add(showImageInfoMenuItem);
        }

        saveToDiskMenuItem = new JMenuItem(new SaveImageAction());
        saveToDiskMenuItem.setText("Save Original FITS Image...");
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

        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction());
        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
        this.add(changeNormalOffsetMenuItem);

        simulateLightingMenuItem = new JMenuItem(new SimulateLightingAction());
        simulateLightingMenuItem.setText("Simulate Lighting");
        this.add(simulateLightingMenuItem);

        changeOpacityMenuItem = new JMenuItem(new ChangeOpacityAction());
        changeOpacityMenuItem.setText("Change Opacity...");
        this.add(changeOpacityMenuItem);

        hideImageMenuItem = new JCheckBoxMenuItem(new HideImageAction());
        hideImageMenuItem.setText("Hide Image");
        this.add(hideImageMenuItem);

    }

    public void setCurrentImage(ImageKey key)
    {
        imageKeys.clear();
        imageKeys.add(key);

        updateMenuItems();
    }

    public void setCurrentImages(ArrayList<ImageKey> keys)
    {
        imageKeys.clear();
        imageKeys.addAll(keys);

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        boolean selectMapImage = true;
        boolean enableMapImage = true;
        boolean selectMapBoundary = true;
        boolean enableMapBoundary = true;
        boolean enableCenterImage = false;
        boolean enableShowImageInfo = false;
        boolean enableSaveBackplanes = false;
        boolean enableSaveToDisk = false;
        boolean enableChangeNormalOffset = false;
        boolean selectShowFrustum = true;
        boolean enableShowFrustum = true;
        boolean enableSimulateLighting = false;
        boolean enableChangeOpacity = false;
        boolean selectHideImage = true;
        boolean enableHideImage = true;

        for (ImageKey imageKey : imageKeys)
        {
            boolean containsImage = imageCollection.containsImage(imageKey);
            boolean containsBoundary = false;
            if (imageBoundaryCollection != null)
                containsBoundary = imageBoundaryCollection.containsBoundary(imageKey);

            if (!containsBoundary)
                selectMapBoundary = containsBoundary;

            if (!containsImage)
                selectMapImage = containsImage;

            if (centerImageMenuItem != null && imageKeys.size() == 1)
            {
                enableCenterImage = containsBoundary || containsImage;
            }

            if (showImageInfoMenuItem != null && imageKeys.size() == 1)
                enableShowImageInfo = containsImage;

            if (imageKeys.size() == 1)
            {
                enableSaveBackplanes = containsImage;
                enableSaveToDisk = containsImage;
                enableChangeNormalOffset = containsImage;
                enableChangeOpacity = containsImage;
            }

            if (containsImage)
            {
                Image image = imageCollection.getImage(imageKey);
                if (!(image instanceof PerspectiveImage) || !((PerspectiveImage)image).isFrustumShowing())
                    selectShowFrustum = false;
                if (imageKeys.size() == 1)
                    enableSimulateLighting = true;
                if (image.isVisible())
                    selectHideImage = false;
            }
            else
            {
                selectShowFrustum = false;
                enableShowFrustum = false;
                selectHideImage = false;
                enableHideImage = false;
            }

            if (imageKey.source == ImageSource.LOCAL_CYLINDRICAL || imageKey.source == ImageSource.IMAGE_MAP)
            {
                enableMapBoundary = false;
                enableShowFrustum = false;
                enableSimulateLighting = false;
                if (centerImageMenuItem != null)
                    enableCenterImage = false;
                enableSaveBackplanes = false;
                enableSaveToDisk = false;

                if (imageKey.source == ImageSource.IMAGE_MAP)
                {
                    enableMapImage = false;
                    enableHideImage = false;
                }
            }
            else if (imageKey.source == ImageSource.LOCAL_PERSPECTIVE)
            {
                enableSaveToDisk = false;
                enableSaveBackplanes = false;
            }
        }

        mapImageMenuItem.setSelected(selectMapImage);
        mapImageMenuItem.setEnabled(enableMapImage);
        mapBoundaryMenuItem.setSelected(selectMapBoundary);
        mapBoundaryMenuItem.setEnabled(enableMapBoundary);
        if (centerImageMenuItem != null)
            centerImageMenuItem.setEnabled(enableCenterImage);
        if (showImageInfoMenuItem != null)
            showImageInfoMenuItem.setEnabled(enableShowImageInfo);
        saveBackplanesMenuItem.setEnabled(enableSaveBackplanes);
        saveToDiskMenuItem.setEnabled(enableSaveToDisk);
        changeNormalOffsetMenuItem.setEnabled(enableChangeNormalOffset);
        showFrustumMenuItem.setSelected(selectShowFrustum);
        showFrustumMenuItem.setEnabled(enableShowFrustum);
        simulateLightingMenuItem.setEnabled(enableSimulateLighting);
        changeOpacityMenuItem.setEnabled(enableChangeOpacity);
        hideImageMenuItem.setSelected(selectHideImage);
        hideImageMenuItem.setEnabled(enableHideImage);
    }


    public class MapImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageKey imageKey : imageKeys)
            {
                try
                {
                    if (mapImageMenuItem.isSelected())
                        imageCollection.addImage(imageKey);
                    else
                        imageCollection.removeImage(imageKey);
                }
                catch (FitsException e1) {
                    e1.printStackTrace();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class MapBoundaryAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageKey imageKey : imageKeys)
            {
                try
                {
                    if (mapBoundaryMenuItem.isSelected())
                        imageBoundaryCollection.addBoundary(imageKey);
                    else
                        imageBoundaryCollection.removeBoundary(imageKey);
                }
                catch (FitsException e1) {
                    e1.printStackTrace();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class ShowInfoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKey imageKey = imageKeys.get(0);

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
            if (imageKeys.size() != 1)
                return;
            ImageKey imageKey = imageKeys.get(0);

            File file = null;
            try
            {
                imageCollection.addImage(imageKey);
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                String path = image.getFitFileFullPath();
                String extension = path.substring(path.lastIndexOf("."));

                file = CustomFileChooser.showSaveDialog(invoker, "Save FITS image", imageKey.name + extension, "fit");
                if (file != null)
                {
                    File fitFile = FileCache.getFileFromServer(imageKey.name + extension);

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
            if (imageKeys.size() != 1)
                return;
            ImageKey imageKey = imageKeys.get(0);

            double[] spacecraftPosition = new double[3];
            double[] focalPoint = new double[3];
            double[] upVector = new double[3];
            double viewAngle = 0.0;

            if (imageBoundaryCollection != null && imageBoundaryCollection.containsBoundary(imageKey))
            {
                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKey);
                boundary.getCameraOrientation(spacecraftPosition, focalPoint, upVector);

                viewAngle = boundary.getImage().getFovAngle();
            }
            else if (imageCollection.containsImage(imageKey))
            {
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);

                viewAngle = image.getFovAngle();
            }
            else
            {
                return;
            }

            renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
        }
    }

    private class SaveBackplanesAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKey imageKey = imageKeys.get(0);

            // First generate the DDR

            String defaultFilename = new File(imageKey.name + "_DDR.IMG").getName();
            File file = CustomFileChooser.showSaveDialog(invoker, "Save Backplanes DDR", defaultFilename, "img");

            try
            {
                if (file != null)
                {
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                    imageCollection.addImage(imageKey);
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);

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
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);

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
            for (ImageKey imageKey : imageKeys)
            {
                try
                {
                    imageCollection.addImage(imageKey);
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                    image.setShowFrustum(showFrustumMenuItem.isSelected());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class ChangeNormalOffsetAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKey imageKey = imageKeys.get(0);

            Image image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
                changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                changeOffsetDialog.setVisible(true);
            }
        }
    }

    private class SimulateLightingAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKey imageKey = imageKeys.get(0);

            PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
            if (image != null)
            {
                double[] sunDir = image.getSunVector();
                renderer.setFixedLightDirection(sunDir);
                renderer.setLighting(LightingType.FIXEDLIGHT);
            }
        }
    }

    private class ChangeOpacityAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKey imageKey = imageKeys.get(0);

            Image image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                OpacityChanger opacityChanger = new OpacityChanger(image);
                opacityChanger.setLocationRelativeTo(renderer);
                opacityChanger.setVisible(true);
            }
        }
    }

    private class HideImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageKey imageKey : imageKeys)
            {
                try
                {
                    imageCollection.addImage(imageKey);
                    Image image = imageCollection.getImage(imageKey);
                    image.setVisible(!hideImageMenuItem.isSelected());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (imageBoundaryCollection != null && imageBoundaryCollection.getBoundary((vtkActor)pickedProp) != null)
            {
                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary((vtkActor)pickedProp);
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
