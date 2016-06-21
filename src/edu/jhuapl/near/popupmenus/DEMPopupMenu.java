package edu.jhuapl.near.popupmenus;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.DEMView;
import edu.jhuapl.near.gui.NormalOffsetChangerDialog;
import edu.jhuapl.near.gui.OpacityChanger;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.DEM;
import edu.jhuapl.near.model.DEM.DEMKey;
import edu.jhuapl.near.model.DEMCollection;
import edu.jhuapl.near.model.MapletBoundaryCollection;
import edu.jhuapl.near.model.SmallBodyModel;
//import edu.jhuapl.near.popupmenus.ImagePopupMenu.ShowInfoAction;
import edu.jhuapl.near.util.ColorUtil;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;


public class DEMPopupMenu extends PopupMenu
{
    private Component invoker;
    private SmallBodyModel smallBodyModel;
    private DEMCollection demCollection;
    private MapletBoundaryCollection demBoundaryCollection;
    private ArrayList<DEMKey> demKeys = new ArrayList<DEMKey>();
    private JMenuItem mapDEMMenuItem;
    private JMenuItem mapBoundaryMenuItem;
    private JMenuItem showDEMInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem changeNormalOffsetMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideDEMMenuItem;
    private JMenu colorMenu;
    private ArrayList<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem customColorMenuItem;
    private Renderer renderer;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public DEMPopupMenu(
            SmallBodyModel smallBodyModel,
            DEMCollection demCollection,
            MapletBoundaryCollection imageBoundaryCollection,
            Renderer renderer,
            Component invoker)
    {
        this.smallBodyModel = smallBodyModel;
        this.demCollection = demCollection;
        this.demBoundaryCollection = imageBoundaryCollection;
        this.renderer = renderer;
        this.invoker = invoker;

        mapDEMMenuItem = new JCheckBoxMenuItem(new MapDEMAction());
        mapDEMMenuItem.setText("Map DEM");
        this.add(mapDEMMenuItem);

        mapBoundaryMenuItem = new JCheckBoxMenuItem(new MapBoundaryAction());
        mapBoundaryMenuItem.setText("Map DEM Boundary");
        this.add(mapBoundaryMenuItem);

        showDEMInfoMenuItem = new JMenuItem(new ShowInfoAction());
        showDEMInfoMenuItem.setText("Properties...");
        this.add(showDEMInfoMenuItem);

        saveToDiskMenuItem = new JMenuItem(new SaveDEMAction());
        saveToDiskMenuItem.setText("Save Original FITS File...");
        this.add(saveToDiskMenuItem);

        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction());
        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
        this.add(changeNormalOffsetMenuItem);

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

    }

    public void setCurrentDEM(DEMKey key)
    {
        demKeys.clear();
        demKeys.add(key);

        updateMenuItems();
    }

    public void setCurrentDEMs(ArrayList<DEMKey> keys)
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
        boolean enableChangeNormalOffset = false;
        boolean selectHideImage = true;
        boolean enableHideImage = true;
        boolean enableBoundaryColor = true;

        for (DEMKey demKey : demKeys)
        {
            boolean containsDEM = demCollection.containsDEM(demKey);
            boolean containsBoundary = false;

            // twupy1
            //if (demBoundaryCollection != null)
            //    containsBoundary = demBoundaryCollection.containsBoundary(demKey);

            if (!containsBoundary)
            {
                selectMapBoundary = containsBoundary;
                enableBoundaryColor = false;
            }

            if (!containsDEM)
                selectMapImage = containsDEM;

            if (showDEMInfoMenuItem != null && demKeys.size() == 1)
                enableShowDEMInfo = containsDEM;

            if (demKeys.size() == 1)
            {
                enableSaveToDisk = containsDEM;
                enableChangeNormalOffset = containsDEM;
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

        // twupy1
        /*if (enableBoundaryColor)
        {
            HashSet<String> colors = new HashSet<String>();
            for (ImageKey imageKey : demKeys)
            {
                int[] c = demBoundaryCollection.getBoundary(imageKey).getBoundaryColor();
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
        }*/

        mapDEMMenuItem.setSelected(selectMapImage);
        mapDEMMenuItem.setEnabled(enableMapImage);
        mapBoundaryMenuItem.setSelected(selectMapBoundary);
        mapBoundaryMenuItem.setEnabled(enableMapBoundary);
        if (showDEMInfoMenuItem != null)
            showDEMInfoMenuItem.setEnabled(enableShowDEMInfo);
        saveToDiskMenuItem.setEnabled(enableSaveToDisk);
        changeNormalOffsetMenuItem.setEnabled(enableChangeNormalOffset);
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

    private class MapBoundaryAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (DEMKey demKey : demKeys)
            {
                // twupy1
                /*
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
                }*/
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
                new DEMView(demKey, demCollection,
                        smallBodyModel, demBoundaryCollection);
                //imageCollection.addImage(demKey);
                //infoPanelManager.addData(imageCollection.getImage(demKey));

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
                String imageFileName = new File(demKey.name).getName();

                file = CustomFileChooser.showSaveDialog(invoker, "Save FITS file", imageFileName, "fit");
                if (file != null)
                {
                    File fitFile = FileCache.getFileFromServer(demKey.name);
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

    private class ChangeNormalOffsetAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (demKeys.size() != 1)
                return;
            DEMKey demKey = demKeys.get(0);

            DEM dem = demCollection.getDEM(demKey);
            if (dem != null)
            {
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(dem);
                changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                changeOffsetDialog.setVisible(true);
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
            // twupy1
            /*for (DEMKey demKey : demKeys)
            {
                PerspectiveImageBoundary boundary = demBoundaryCollection.getBoundary(demKey);
                boundary.setBoundaryColor(color);
            }*/

            updateMenuItems();
        }
    }

    private class CustomBoundaryColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            // twupy1
            /*
            PerspectiveImageBoundary boundary = demBoundaryCollection.getBoundary(demKeys.get(0));
            int[] currentColor = boundary.getBoundaryColor();
            Color newColor = ColorChooser.showColorChooser(invoker, currentColor);
            if (newColor != null)
            {
                for (ImageKey imageKey : demKeys)
                {
                    boundary = demBoundaryCollection.getBoundary(imageKey);
                    boundary.setBoundaryColor(newColor);
                }
            }
            */
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (demBoundaryCollection != null && demBoundaryCollection.getBoundary((vtkActor)pickedProp) != null)
            {
                // twupy1
                /*MapletBoundary boundary = demBoundaryCollection.getBoundary((vtkActor)pickedProp);
                setCurrentBoundary(boundary.getKey());
                show(e.getComponent(), e.getX(), e.getY());*/
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
