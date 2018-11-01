package edu.jhuapl.sbmt.gui.dem;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkProp;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.OpacityChanger;
import edu.jhuapl.saavtk.gui.dialog.ShapeModelImporterDialog;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.popup.PopupMenu;
//import edu.jhuapl.near.popupmenus.ImagePopupMenu.ShowInfoAction;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SmallBodyViewConfigMetadataIO;
import edu.jhuapl.sbmt.model.dem.DEM;
import edu.jhuapl.sbmt.model.dem.DEM.DEMKey;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection.DEMBoundary;
import edu.jhuapl.sbmt.model.dem.DEMCollection;

import nom.tam.fits.FitsException;


public class DEMPopupMenu extends PopupMenu
{
    private Component invoker;
    private PolyhedralModel smallBodyModel;
    private DEMCollection demCollection;
    private DEMBoundaryCollection demBoundaryCollection;
    private List<DEMKey> demKeys = new ArrayList<DEMKey>();
    private JMenuItem mapDEMMenuItem;
    private JMenuItem mapBoundaryMenuItem;
    private JMenuItem centerDemMenuItem;
    private JMenuItem showDEMInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideDEMMenuItem;
    private JMenuItem exportCustomModelItem;
    private JMenu colorMenu;
    private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem customColorMenuItem;
    private Renderer renderer;
    private String demFilename;
    private Vector<ViewConfig> config;
    private ShapeModelImporterDialog dialog;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public DEMPopupMenu(
            PolyhedralModel smallBodyModel,
            DEMCollection demCollection,
            DEMBoundaryCollection demBoundaryCollection,
            Renderer renderer,
            Component invoker)
    {
        this.smallBodyModel = smallBodyModel;
        this.demCollection = demCollection;
        this.demBoundaryCollection = demBoundaryCollection;
        this.renderer = renderer;
        this.invoker = invoker;

        mapDEMMenuItem = new JCheckBoxMenuItem(new MapDEMAction());
        mapDEMMenuItem.setText("Map DEM");
        this.add(mapDEMMenuItem);

        mapBoundaryMenuItem = new JCheckBoxMenuItem(new MapBoundaryAction());
        mapBoundaryMenuItem.setText("Map DEM Boundary");
        this.add(mapBoundaryMenuItem);

        centerDemMenuItem = new JMenuItem(new CenterDemAction());
        centerDemMenuItem.setText("Center DEM in Window");

        this.add(centerDemMenuItem);

        showDEMInfoMenuItem = new JMenuItem(new ShowInfoAction());
        showDEMInfoMenuItem.setText("Properties...");
        this.add(showDEMInfoMenuItem);

        saveToDiskMenuItem = new JMenuItem(new SaveDEMAction());
        saveToDiskMenuItem.setText("Save FITS File...");
        this.add(saveToDiskMenuItem);

        changeOpacityMenuItem = new JMenuItem(new ChangeOpacityAction());
        changeOpacityMenuItem.setText("Change Opacity...");
        this.add(changeOpacityMenuItem);

        hideDEMMenuItem = new JCheckBoxMenuItem(new HideDEMAction());
        hideDEMMenuItem.setText("Hide DEM");
        this.add(hideDEMMenuItem);

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

        exportCustomModelItem = new JMenuItem(new ExportCustomModelAction());
        exportCustomModelItem.setText("Export as Custom Model");
        this.add(exportCustomModelItem);

    }

    public void setCurrentDEM(DEMKey key)
    {
        demKeys.clear();
        demKeys.add(key);

        updateMenuItems();
    }

    public void setCurrentDEMs(List<DEMKey> keys)
    {
        demKeys.clear();
        demKeys.addAll(keys);

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        boolean selectMapImage = true;
        boolean enableMapImage = true;
        boolean selectMapBoundary = true;
        boolean enableMapBoundary = true;
        boolean enableShowDEMInfo = false;
        boolean enableSaveToDisk = false;
        boolean enableChangeOpacity = false;
        boolean selectHideImage = true;
        boolean enableHideImage = true;
        boolean enableBoundaryColor = true;
        boolean enableExport = false;

        for (DEMKey demKey : demKeys)
        {
            boolean containsDEM = demCollection.containsDEM(demKey);
            boolean containsBoundary = false;

            if (demBoundaryCollection != null)
                containsBoundary = demBoundaryCollection.containsBoundary(demKey);

            if (!containsBoundary)
            {
                selectMapBoundary = containsBoundary;
                enableBoundaryColor = false;
            }

            if (!containsDEM)
            {
                selectMapImage = containsDEM;
            }

            if (showDEMInfoMenuItem != null && demKeys.size() == 1)
            {
                enableShowDEMInfo = containsDEM;
                //enableShowDEMInfo = true;
            }

            if (demKeys.size() == 1)
            {
                enableSaveToDisk = containsDEM;
                enableChangeOpacity = containsDEM;
            }

            if (containsDEM)
            {
                DEM dem = demCollection.getDEM(demKey);
                if (dem.isVisible())
                    selectHideImage = false;
            }
            else
            {
                selectHideImage = false;
                enableHideImage = false;
            }
        }

        if (enableBoundaryColor)
        {
            HashSet<String> colors = new HashSet<String>();
            for (DEMKey demKey : demKeys)
            {
                int[] c = demBoundaryCollection.getBoundary(demKey).getBoundaryColor();
                colors.add(c[0] + " " + c[1] + " " + c[2]);
            }

            // If the boundary color equals one of the predefined colors, then check
            // the corresponding menu item.
            int[] currentColor = demBoundaryCollection.getBoundary(demKeys.get(0)).getBoundaryColor();
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

        centerDemMenuItem.setEnabled(enableHideImage);
        exportCustomModelItem.setEnabled(enableHideImage);

        mapDEMMenuItem.setSelected(selectMapImage);
        mapDEMMenuItem.setEnabled(enableMapImage);
        mapBoundaryMenuItem.setSelected(selectMapBoundary);
        mapBoundaryMenuItem.setEnabled(enableMapBoundary);
        if (showDEMInfoMenuItem != null)
            showDEMInfoMenuItem.setEnabled(enableShowDEMInfo);
        saveToDiskMenuItem.setEnabled(enableSaveToDisk);
        changeOpacityMenuItem.setEnabled(enableChangeOpacity);
        hideDEMMenuItem.setSelected(selectHideImage);
        hideDEMMenuItem.setEnabled(enableHideImage);
        colorMenu.setEnabled(enableBoundaryColor);
    }

    public class MapDEMAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (DEMKey demKey : demKeys)
            {
                try
                {
                    if (mapDEMMenuItem.isSelected())
                    {
                        demCollection.addDEM(demKey);
                    }
                    else
                    {
                        demCollection.removeDEM(demKey);
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

    public class CenterDemAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (demKeys.size() != 1)
                return;

            DEMKey demKey = demKeys.get(0);
            DEM dem=demCollection.getDEM(demKey);

            vtkCamera cam=renderer.getRenderWindowPanel().getActiveCamera();

            // Find center of DEM. Arbitrarily create an origin exactly opposite this center.
            final double[] centerArray = dem.getCenter();
            final double[] origin = new double[] { -centerArray[0], -centerArray[1], -centerArray[2] };

            // Figure out roughly the size of the DEM and from it compute a maximum radius
            // in angle space for a circle centered on the DEM, perpendicular to the
            // "center" array computed above.
            final Vector3D center = new Vector3D(centerArray);
            final double mPerKm = 1000.;
            final double maximumRadius = dem.getScale() * dem.getHalfSize() / mPerKm;
            final double maximumAngle = maximumRadius / (center.getNorm() * 2.);

            // Find vectors roughly along the DEM surface, roughly oriented by latitude/longitude.
            Vector3D latVec = findPointNearToCenter(dem, origin, centerArray, maximumAngle, true);
            Vector3D lonVec = findPointNearToCenter(dem, origin, centerArray, maximumAngle, false);

            // Compute a normal from these two vectors.
            Vector3D normal = lonVec.crossProduct(latVec).normalize();

            cam.SetFocalPoint(centerArray);
            cam.SetPosition(center.add(normal.scalarMultiply(2. * dem.getBoundingBoxDiagonalLength())).toArray());
            cam.SetViewUp(lonVec.crossProduct(normal).normalize().toArray());
            renderer.getRenderWindowPanel().resetCameraClippingRange();
            renderer.getRenderWindowPanel().Render();

            updateMenuItems();
        }
    }

    private Vector3D findPointNearToCenter(DEM dem, final double[] origin, final double [] center, final double maximumAngle, boolean alongLat)
    {
        final int maxIterations = 10;
        LatLon centerLatLon = MathUtil.reclat(center);

        double [] point0 = new double[] { 0., 0., 0. };
        double angle = maximumAngle;
        boolean foundMatch = false;
        for (int index = 0; index < maxIterations; ++index)
        {
            LatLon dir = getDirToTry(centerLatLon, angle, alongLat);
            if (dem.computeRayIntersection(origin, MathUtil.latrec(dir), point0) >= 0)
            {
                foundMatch = true;
                break;
            }
            angle *= .5;
        }

        if (!foundMatch)
        {
            point0 = center;
        }

        double [] point1 = new double[] { 0., 0., 0. };
        angle = -maximumAngle;
        foundMatch = false;
        for (int index = 0; index < maxIterations; ++index)
        {
            LatLon dir = getDirToTry(centerLatLon, angle, alongLat);
            if (dem.computeRayIntersection(origin, MathUtil.latrec(dir), point1) >= 0)
            {
                foundMatch = true;
                break;
            }
            angle *= .5;
        }

        if (!foundMatch)
        {
            point1 = center;
        }

        if (point0 == point1)
        {
            return null;
        }
        return new Vector3D(point1).subtract(new Vector3D(point0));
    }

    private LatLon getDirToTry(final LatLon centerLatLon, double angle, boolean alongLat)
    {
        return alongLat ? new LatLon(centerLatLon.lat + angle, centerLatLon.lon) : new LatLon(centerLatLon.lat, centerLatLon.lon + angle);
    }

    private class MapBoundaryAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (DEMKey demKey : demKeys)
            {
                try
                {
                    if (mapBoundaryMenuItem.isSelected())
                        demBoundaryCollection.addBoundary(demKey);
                    else
                        demBoundaryCollection.removeBoundary(demKey);
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
            if (demKeys.size() != 1)
                return;

            DEMKey demKey = demKeys.get(0);

            try
            {
                DEM macroDEM = demCollection.getDEM(demKey);
                if(macroDEM.hasView())
                {
                    // View already exists, just bring it to the front
                    macroDEM.getView().toFront();
                }
                else
                {
                    // No view currently exists, create one and associate it to the DEM
                    DEMView view=new DEMView(demKey, demCollection, smallBodyModel);
                    macroDEM.setView(view);

                    // ugh... this is a hack to get the renderer to wake up and look at the DEM, otherwise it is by default looking at the origin and the user might think that there is a bug since the DEM is usually not visible from that viewpoint
                    // ....  It would be nice if instead we could call getRenderer().resetCamera() but there is some synchronization issue between the 3d view and DEM actors that breaks this approach -- so just making the camera at least look in the direction of the DEM seems to get around the issue for now -- zimmemi1
                    view.getRenderer().getRenderWindowPanel().getActiveCamera().SetFocalPoint(macroDEM.getBoundingBox().getCenterPoint());
                }
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

    private class SaveDEMAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (demKeys.size() != 1)
                return;
            DEMKey demKey = demKeys.get(0);

            File file = null;
            try
            {
                demCollection.addDEM(demKey);
                String imageFileName = new File(demKey.fileName).getName();

                file = CustomFileChooser.showSaveDialog(invoker, "Save FITS file", imageFileName, "fit");
                if (file != null)
                {
                    File fitFile = FileCache.getFileFromServer("file://" + demKey.fileName);
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

    private class ChangeOpacityAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (demKeys.size() != 1)
                return;
            DEMKey demKey = demKeys.get(0);

            DEM dem = demCollection.getDEM(demKey);
            if (dem != null)
            {
                OpacityChanger opacityChanger = new OpacityChanger(dem);
                opacityChanger.setLocationRelativeTo(renderer);
                opacityChanger.setVisible(true);
            }
        }
    }

    private class HideDEMAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (DEMKey demKey : demKeys)
            {
                try
                {
                    demCollection.addDEM(demKey);
                    DEM dem = demCollection.getDEM(demKey);
                    dem.setVisible(!hideDEMMenuItem.isSelected());
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
            for (DEMKey demKey : demKeys)
            {
                DEMBoundary boundary = demBoundaryCollection.getBoundary(demKey);
                boundary.setBoundaryColor(color);
            }

            updateMenuItems();
        }
    }

    private class CustomBoundaryColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            DEMBoundary boundary = demBoundaryCollection.getBoundary(demKeys.get(0));
            int[] currentColor = boundary.getBoundaryColor();
            Color newColor = ColorChooser.showColorChooser(invoker, currentColor);
            if (newColor != null)
            {
                for (DEMKey demKey : demKeys)
                {
                    boundary = demBoundaryCollection.getBoundary(demKey);
                    boundary.setBoundaryColor(newColor);
                }
            }
        }
    }

    private class ExportCustomModelAction extends AbstractAction
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
//            if (demKeys.size() != 1)
//                return;
            DEMKey demKey = demKeys.get(0);

            DEM dem = demCollection.getDEM(demKey);
            if (dem != null)
            {
                vtkPolyData demPolydata = dem.getDem();
                demFilename = dem.getKey().fileName;

                vtkPolyDataWriter writer = new vtkPolyDataWriter();
                writer.SetFileName(demFilename.substring(0, demFilename.length()-3) + "vtk");
                writer.SetFileTypeToBinary();
                writer.SetInputData(demPolydata);
                writer.Write();

                //write out copy of current model's smallbody view config here
                config = new Vector<ViewConfig>();
                config.add(smallBodyModel.getConfig().clone());


                dialog = new ShapeModelImporterDialog(null);

                String extension = FilenameUtils.getExtension(demFilename);
                dialog.populateCustomDEMImport(demFilename.substring(0, demFilename.length()-3) + extension, extension);
                dialog.beforeOKRunner = new Runnable()
                {
                    final String filename = demFilename;
                    @Override
                    public void run()
                    {
                        try
                        {
                            SmallBodyViewConfig config2 = (SmallBodyViewConfig)(config.get(0));
                            config2.modelLabel = dialog.getNameOfImportedShapeModel();
                            config2.customTemporary = false;
                            config2.author = ShapeModelType.CUSTOM;
                            SmallBodyViewConfigMetadataIO metadataIO = new SmallBodyViewConfigMetadataIO(new Vector<ViewConfig>(config));
                            metadataIO.write(new File(demFilename.substring(0, demFilename.length()-3) + "json"), dialog.getNameOfImportedShapeModel());
                            SmallBodyViewConfig config = (SmallBodyViewConfig)metadataIO.getConfigs().get(0);
                            dialog.setDisplayName(dialog.getNameOfImportedShapeModel());
                        }
                        catch (IOException e1)
                        {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                };


                dialog.show();


                this.firePropertyChange(Properties.CUSTOM_MODEL_ADDED, "", dialog.getNameOfImportedShapeModel());
            }
        }

    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (demBoundaryCollection != null && demBoundaryCollection.getBoundary((vtkActor)pickedProp) != null)
            {
                DEMBoundary boundary = demBoundaryCollection.getBoundary((vtkActor)pickedProp);
                setCurrentDEM(boundary.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
            else if (demCollection.getDEM((vtkActor)pickedProp) != null)
            {
                DEM dem = demCollection.getDEM((vtkActor)pickedProp);
                setCurrentDEM(dem.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
