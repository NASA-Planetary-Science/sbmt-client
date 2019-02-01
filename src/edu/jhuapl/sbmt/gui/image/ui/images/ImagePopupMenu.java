package edu.jhuapl.sbmt.gui.image.ui.images;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.gui.dialog.OpacityChanger;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundary;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.leisa.LEISAJupiterImage;
import edu.jhuapl.sbmt.model.mvic.MVICQuadJupiterImage;

import nom.tam.fits.FitsException;


public class ImagePopupMenu<K extends ImageKeyInterface> extends PopupMenu
{
    private Component invoker;
    private ImageCollection imageCollection;
    private PerspectiveImageBoundaryCollection imageBoundaryCollection;
    private List<ImageKeyInterface> imageKeys = new ArrayList<ImageKeyInterface>();
    //private List<ImageKey> keySet = new ArrayList<Image.ImageKey>();
    private JMenuItem mapImageMenuItem;
    private JMenuItem mapBoundaryMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem showImageSpectrumMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem saveBackplanesMenuItem;
    private JMenuItem centerImageMenuItem;
    private JMenuItem showFrustumMenuItem;
    private JMenuItem exportENVIImageMenuItem;
    private JMenuItem exportInfofileMenuItem;
    private JMenuItem changeNormalOffsetMenuItem;
    private JCheckBoxMenuItem simulateLightingMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideImageMenuItem;
    private JMenu colorMenu;
    private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem customColorMenuItem;
    private SbmtInfoWindowManager infoPanelManager;
    private SbmtSpectrumWindowManager spectrumPanelManager;
    private Renderer renderer;
    private ModelManager modelManager;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public ImagePopupMenu(
            ModelManager modelManager,
            ImageCollection imageCollection,
            PerspectiveImageBoundaryCollection imageBoundaryCollection,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            Renderer renderer,
            Component invoker)
    {
        this.modelManager = modelManager;
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;
        this.infoPanelManager = infoPanelManager;
        this.spectrumPanelManager = spectrumPanelManager;
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

        if (this.spectrumPanelManager != null)
        {
            showImageSpectrumMenuItem = new JMenuItem(new ShowSpectrumAction());
            if (spectrumPanelManager.getNumberSpectrumModels() > 0)
            {
                showImageSpectrumMenuItem.setText("Spectrum...");
                this.add(showImageSpectrumMenuItem);
            }
        }

        saveToDiskMenuItem = new JMenuItem(new SaveImageAction());
        saveToDiskMenuItem.setText("Save FITS Image...");
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

        exportENVIImageMenuItem = new JMenuItem(new ExportENVIImageAction()); // twupy1
        exportENVIImageMenuItem.setText("Export ENVI Image...");
        this.add(exportENVIImageMenuItem);

        exportInfofileMenuItem = new JCheckBoxMenuItem(new ExportInfofileAction());
        exportInfofileMenuItem.setText("Export INFO File...");
        this.add(exportInfofileMenuItem);

        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction());
        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
        this.add(changeNormalOffsetMenuItem);

        simulateLightingMenuItem = new JCheckBoxMenuItem(new SimulateLightingAction());
        simulateLightingMenuItem.setText("Simulate Lighting");
        this.add(simulateLightingMenuItem);

        changeOpacityMenuItem = new JMenuItem(new ChangeOpacityAction());
        changeOpacityMenuItem.setText("Change Opacity...");
        this.add(changeOpacityMenuItem);

        hideImageMenuItem = new JCheckBoxMenuItem(new HideImageAction());
        hideImageMenuItem.setText("Hide Image");
        this.add(hideImageMenuItem);

        colorMenu = new JMenu("Boundary Color");
        this.add(colorMenu);
        for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
        {
            JCheckBoxMenuItem colorMenuItem = new JCheckBoxMenuItem(new BoundaryColorAction(color.color()));
            colorMenuItems.add(colorMenuItem);
            colorMenuItem.setText(color.toString().toLowerCase().replace('_', ' '));
            colorMenu.add(colorMenuItem);
        }
        colorMenu.addSeparator();
        customColorMenuItem = new JMenuItem(new CustomBoundaryColorAction());
        customColorMenuItem.setText("Custom...");
        colorMenu.add(customColorMenuItem);

    }

    public void setCurrentImage(ImageKeyInterface key)
    {
        imageKeys.clear();
        imageKeys.add(key);

        updateMenuItems();
    }

    public void setCurrentImages(List<ImageKeyInterface> keys)
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
        boolean simulateLightingOn = false;
        boolean enableChangeOpacity = false;
        boolean selectHideImage = true;
        boolean enableHideImage = true;
        boolean enableBoundaryColor = true;

        for (ImageKeyInterface imageKey : imageKeys)
        {
            boolean containsImage = imageCollection.containsImage(imageKey);
            boolean containsBoundary = false;
            if (imageBoundaryCollection != null)
                containsBoundary = imageBoundaryCollection.containsBoundary(imageKey);

            if (!containsBoundary)
            {
                selectMapBoundary = containsBoundary;
                enableBoundaryColor = false;
            }

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
                //imageCollection.addImage(imageKey);
                if ( image instanceof PerspectiveImage )
                {
                    PerspectiveImage pImage = (PerspectiveImage)imageCollection.getImage(imageKey);
                    //image.setShowFrustum(showFrustumMenuItem.isSelected());
                    if (!(image instanceof PerspectiveImage) || !((PerspectiveImage)image).isFrustumShowing())
                        selectShowFrustum = false;
                    if (imageKeys.size() == 1)
                    {
                        enableSimulateLighting = true;
                        simulateLightingOn = pImage.isSimulatingLighingOn();
                    }
                    if (image.isVisible())
                        selectHideImage = false;
                } else
                {
                    if (!(image instanceof PerspectiveImage) || !((PerspectiveImage)image).isFrustumShowing())
                        selectShowFrustum = false;
                    if (imageKeys.size() == 1)
                        enableSimulateLighting = true;
                    if (image.isVisible())
                        selectHideImage = false;
                }
            }
            else
            {
                selectShowFrustum = false;
                enableShowFrustum = false;
                selectHideImage = false;
                enableHideImage = false;
            }

//            System.out.println("Image collection: " + imageCollection.getImages().iterator());
//            System.out.println("Image keys: " + imageKeys.size());
//            System.out.println("KetSet: " + keySet.size());
//            System.out.println("key info: " + imageKeys.get(0).source + imageKeys.get(0).name + imageKeys.get(0).band);


            if (imageKey.getSource() == ImageSource.LOCAL_CYLINDRICAL || imageKey.getSource() == ImageSource.IMAGE_MAP)
            {
                enableMapBoundary = false;
                enableShowFrustum = false;
                enableSimulateLighting = false;
                if (centerImageMenuItem != null)
                    enableCenterImage = false;
                enableSaveBackplanes = false;
                enableSaveToDisk = false;

                if (imageKey.getSource() == ImageSource.IMAGE_MAP)
                {
                    enableMapImage = false;
                    enableHideImage = false;
                }
            }
            else if (imageKey.getSource() == ImageSource.LOCAL_PERSPECTIVE)
            {
//                enableSaveToDisk = false;
                enableSaveToDisk = true;
                enableSaveBackplanes = false;
            }
        }

        if (enableBoundaryColor)
        {
            HashSet<String> colors = new HashSet<String>();
            for (ImageKeyInterface imageKey : imageKeys)
            {
                int[] c = imageBoundaryCollection.getBoundary(imageKey).getBoundaryColor();
                colors.add(c[0] + " " + c[1] + " " + c[2]);
            }

            // If the boundary color equals one of the predefined colors, then check
            // the corresponding menu item.
            int[] currentColor = imageBoundaryCollection.getBoundary(imageKeys.get(0)).getBoundaryColor();
            for (JCheckBoxMenuItem item : colorMenuItems)
            {
                BoundaryColorAction action = (BoundaryColorAction)item.getAction();
                Color color = action.color;
                if (colors.size() == 1 &&
                        currentColor[0] == color.getRed() &&
                        currentColor[1] == color.getGreen() &&
                        currentColor[2] == color.getBlue())
                {
                    item.setSelected(true);
                }
                else
                {
                    item.setSelected(false);
                }
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
        exportInfofileMenuItem.setEnabled(enableSaveToDisk);
        exportENVIImageMenuItem.setEnabled(enableSaveToDisk);
        simulateLightingMenuItem.setEnabled(enableSimulateLighting);
        simulateLightingMenuItem.setSelected(simulateLightingOn);
        changeOpacityMenuItem.setEnabled(enableChangeOpacity);
        hideImageMenuItem.setSelected(selectHideImage);
        hideImageMenuItem.setEnabled(enableHideImage);
        colorMenu.setEnabled(enableBoundaryColor);
    }

    public class MapImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
//            System.out.println("MapImageAction.actionPerformed()");
            for (ImageKeyInterface imageKey : imageKeys)
            {
                try
                {
                    if (mapImageMenuItem.isSelected())
                    {
                        imageCollection.addImage(imageKey);
                        //keySet.add(imageKey);
                    }

                    else
                    {
                        imageCollection.removeImage(imageKey);
                        //keySet.remove(imageKey);
                        renderer.setLighting(LightingType.LIGHT_KIT);
                    }
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
            for (ImageKeyInterface imageKey : imageKeys)
            {
                try
                {
                    if (mapBoundaryMenuItem.isSelected())
                    {
                        imageBoundaryCollection.addBoundary(imageKey);
                        Image image = imageCollection.getImage(imageKey);
                        if(image != null)
                        {
                            imageBoundaryCollection.getBoundary(imageKey).setOffset(image.getOffset());
                        }
                    }
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
            ImageKeyInterface imageKey = imageKeys.get(0);

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

    private class ShowSpectrumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKeyInterface imageKey = imageKeys.get(0);

            try
            {
                imageCollection.addImage(imageKey);
                Image image = imageCollection.getImage(imageKey);
                if (image instanceof LEISAJupiterImage || image instanceof MVICQuadJupiterImage)
                    spectrumPanelManager.addData(imageCollection.getImage(imageKey));

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
            ImageKeyInterface imageKey = imageKeys.get(0);

            File file = null;
            try
            {
                imageCollection.addImage(imageKey);
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                String path = image.getFitFileFullPath();
                String extension = path.substring(path.lastIndexOf("."));
                String imageFileName = new File(path).getName();

                file = CustomFileChooser.showSaveDialog(invoker, "Save FITS image", imageFileName, "fit");
                if (file != null)
                {
                    File fitFile = FileCache.getFileFromServer(imageKey.getName() + extension);

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
            ImageKeyInterface imageKey = imageKeys.get(0);

            double[] spacecraftPosition = new double[3];
            double[] focalPoint = new double[3];
            double[] upVector = new double[3];
            double viewAngle = 0.0;

            if (imageBoundaryCollection != null && imageBoundaryCollection.containsBoundary(imageKey))
            {
                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKey);
                boundary.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
                viewAngle = boundary.getImage().getMaxFovAngle();
            }
            else if (imageCollection.containsImage(imageKey))
            {
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
                viewAngle = image.getMaxFovAngle();
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
            ImageKeyInterface imageKey = imageKeys.get(0);

            // First generate the DDR

            String defaultFilename = new File(imageKey.getName() + "_DDR.IMG").getName();
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
//                    String imgName = file.getName();
                    File imgName = file;
                    String lblName = file.getAbsolutePath();
                    lblName = lblName.substring(0, lblName.length()-4);
//                    if (file.getAbsolutePath().endsWith("img"))
//                        lblName += ".lbl";
//                    else
//                        lblName += ".LBL";

                    File lblFile = new File(lblName);

                    imageCollection.addImage(imageKey);
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);

                    updateMenuItems();

                    image.generateBackplanesLabel(imgName, lblFile);
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
            for (ImageKeyInterface imageKey : imageKeys)
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

    private class ExportENVIImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            // Only works for a single image (for now)
            if (imageKeys.size() != 1)
                return;
            ImageKeyInterface imageKey = imageKeys.get(0);

            File file = null;
            try
            {
                // Get the PerspectiveImage
                imageCollection.addImage(imageKey);
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);

                // Default name
                String fullPathName = image.getFitFileFullPath();
                if (fullPathName == null)
                    fullPathName = image.getPngFileFullPath();
                String imageFileName = new File(fullPathName).getName();

                String defaultFileName = null;
                if (imageFileName != null)
                    defaultFileName = imageFileName.substring(0, imageFileName.length()-4);

                // Open save dialog
                file = CustomFileChooser.showSaveDialog(invoker, "Export ENVI image as", defaultFileName + ".hdr", "hdr");
                if (file != null)
                {
                    String filename = file.getAbsolutePath();
                    image.exportAsEnvi(filename.substring(0, filename.length()-4), "bsq", true);
                }
            }
            catch(Exception ex)
            {
                // Something went wrong during the file conversion/export process
                // (after save dialog has returned)
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to export ENVI image as " + file.getAbsolutePath(),
                        "Error Exporting ENVI Image",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private class ExportInfofileAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageKeyInterface imageKey : imageKeys)
            {
                if (imageCollection.getImage(imageKey) instanceof PerspectiveImage)
                {
                    try
                    {
                        imageCollection.addImage(imageKey);
                        PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(imageKey);
                        String fullPathName = image.getFitFileFullPath();
                        if (fullPathName == null)
                            fullPathName = image.getPngFileFullPath();
                        String imageFileName = new File(fullPathName).getName();

                        String defaultFileName = null;
                        if (imageFileName != null)
                            defaultFileName = imageFileName.substring(0, imageFileName.length() - 3) + "INFO";

                        File file = CustomFileChooser.showSaveDialog(invoker, "Save INFO file as...", defaultFileName);
                        if (file == null)
                        {
                            return;
                        }

                        String filename = file.getAbsolutePath();

                        System.out.println("Exporting INFO file for " + image.getImageName() + " to " + filename);

                        image.saveImageInfo(filename);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
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
            ImageKeyInterface imageKey = imageKeys.get(0);

            Image image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
                changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                changeOffsetDialog.setVisible(true);
                int[] temp = boundaries.getBoundary(imageKey).getBoundaryColor();
                boundaries.getBoundary(imageKey).setOffset(image.getOffset());
                Color color = new Color(temp[0],temp[1],temp[2]);
                boundaries.getBoundary(imageKey).setBoundaryColor(color);
            }
        }
    }

    private LightingType origLightingType;
    private Double origLightIntensity;
    private LatLon origLightPosition;

    private class SimulateLightingAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKeyInterface imageKey = imageKeys.get(0);

            PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
            if (image != null)
            {
                if (simulateLightingMenuItem.isSelected())
                {
                    System.out.println("Simulate Lighting On");
                    // store original lighting parameters
                    origLightingType = renderer.getLighting();
                    origLightIntensity = renderer.getLightIntensity();
                    origLightPosition = renderer.getFixedLightPosition();

                    double[] sunDir = image.getSunVector();
                    renderer.setFixedLightDirection(sunDir);
                    renderer.setLighting(LightingType.FIXEDLIGHT);

                    // uncheck simulate lighting for all mapped images
                    PerspectiveImage pImage;
                    for(Image tempImage : imageCollection.getImages())
                    {
                        if (tempImage instanceof PerspectiveImage)
                        {
                            pImage = (PerspectiveImage) tempImage;
                            pImage.setSimulateLighting(false);
                        }
                    }
                }
                else
                {
                    System.out.println("Simulate Lighting Off");
                    renderer.setLighting(LightingType.LIGHT_KIT);
//                    renderer.setLighting(origLightingType);
//                    renderer.setLightIntensity(origLightIntensity);
//                    renderer.setFixedLightPosition(origLightPosition);
                }
            }
            image.setSimulateLighting(simulateLightingMenuItem.isSelected());
            updateMenuItems();
        }
    }

    private class ChangeOpacityAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageKeyInterface imageKey = imageKeys.get(0);

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
            for (ImageKeyInterface imageKey : imageKeys)
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

    private class BoundaryColorAction extends AbstractAction
    {
        private Color color;

        public BoundaryColorAction(Color color)
        {
            this.color = color;
        }

        public void actionPerformed(ActionEvent e)
        {
            for (ImageKeyInterface imageKey : imageKeys)
            {
                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKey);
                boundary.setBoundaryColor(color);
            }

            updateMenuItems();
        }
    }

    private class CustomBoundaryColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKeys.get(0));
            int[] currentColor = boundary.getBoundaryColor();
            Color newColor = ColorChooser.showColorChooser(invoker, currentColor);
            if (newColor != null)
            {
                for (ImageKeyInterface imageKey : imageKeys)
                {
                    boundary = imageBoundaryCollection.getBoundary(imageKey);
                    boundary.setBoundaryColor(newColor);
                }
            }
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

            if (e.isShiftDown())
            {
                showImageInfoMenuItem.doClick();
                setVisible(false);
            }
        }
    }

}
