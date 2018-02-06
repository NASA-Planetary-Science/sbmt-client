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

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.OpacityChanger;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.popup.PopupMenu;
//import edu.jhuapl.near.popupmenus.ImagePopupMenu.ShowInfoAction;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
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
    private JMenuItem showDEMInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideDEMMenuItem;
    private JMenu colorMenu;
    private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
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

        showDEMInfoMenuItem = new JMenuItem(new ShowInfoAction());
        showDEMInfoMenuItem.setText("Properties...");
        this.add(showDEMInfoMenuItem);

        saveToDiskMenuItem = new JMenuItem(new SaveDEMAction());
        saveToDiskMenuItem.setText("Save Original FITS File...");
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
                    macroDEM.setView(new DEMView(demKey, demCollection, smallBodyModel));
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
