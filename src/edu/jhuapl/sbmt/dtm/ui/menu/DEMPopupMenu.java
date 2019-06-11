package edu.jhuapl.sbmt.dtm.ui.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.ShapeModelImporterDialog;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.camera.CoordinateSystem;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.popup.PopupMenu;
//import edu.jhuapl.near.popupmenus.ImagePopupMenu.ShowInfoAction;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.dtm.controller.DEMPopupMenuActionListener;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection.DEMBoundary;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.DEMKey;


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
    private DEMPopupMenuActionListener listener;

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
            Component invoker,
            DEMPopupMenuActionListener listener)
    {
        this.smallBodyModel = smallBodyModel;
        this.demCollection = demCollection;
        this.demBoundaryCollection = demBoundaryCollection;
        this.renderer = renderer;
        this.invoker = invoker;
        this.listener = listener;

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

    public void removeCenterMenu()
    {
    	this.remove(centerDemMenuItem);
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
    	if (demKeys.size() == 0) return;
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
        	listener.mapDEM(demKeys, mapDEMMenuItem.isSelected());
            updateMenuItems();
        }
    }

    /**
     * Action which will cause the DEM to be centered in the view of the Renderer.
     */
    public class CenterDemAction extends AbstractAction
    {
        // Cache vars
        private Map<DEMKey, CoordinateSystem> cacheMap = new HashMap<>();

        @Override
        public void actionPerformed(ActionEvent aEvent)
        {
        	listener.centerDEM(demKeys, renderer);
            updateMenuItems();
        }
    }

    private class MapBoundaryAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        	listener.showDEMBoundary(demKeys, mapBoundaryMenuItem.isSelected());
            updateMenuItems();
        }
    }

    private class ShowInfoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        	listener.showDEMProperties(demKeys, smallBodyModel);
        }
    }

    private class SaveDEMAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        	listener.saveDEMToFITS(demKeys, invoker);
        }
    }

    private class ChangeOpacityAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        	listener.changeDEMOpacity(demKeys, renderer);
        }
    }

    private class HideDEMAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        	listener.showDEM(demKeys, !hideDEMMenuItem.isSelected());
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
            listener.setDEMBoundaryColor(demKeys, color);
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
            if (newColor == null) return;
            listener.setDEMBoundaryColor(demKeys, newColor);
            updateMenuItems();
        }
    }

    private class ExportCustomModelAction extends AbstractAction
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
        	String modelName = listener.exportDEMToCustomModel(demKeys, smallBodyModel);
            this.firePropertyChange(Properties.CUSTOM_MODEL_ADDED, "", modelName);
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
