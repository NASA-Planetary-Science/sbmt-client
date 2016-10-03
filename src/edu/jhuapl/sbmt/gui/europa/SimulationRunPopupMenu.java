package edu.jhuapl.sbmt.gui.europa;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.model.europa.SimulationRun;
import edu.jhuapl.sbmt.model.europa.SimulationRun.SimulationRunKey;
import edu.jhuapl.sbmt.model.europa.SimulationRunCollection;


public class SimulationRunPopupMenu extends PopupMenu
{
    private SimulationRunCollection simulationRunCollection;
    private ArrayList<SimulationRunKey> runKeys = new ArrayList<SimulationRunKey>();
    private JMenuItem mapRunMenuItem;
    private JMenuItem showRunInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private SbmtInfoWindowManager infoPanelManager;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public SimulationRunPopupMenu(
            SimulationRunCollection runCollection,
            SbmtInfoWindowManager infoPanelManager,
            Renderer renderer,
            Component invoker)
    {
        this.simulationRunCollection = runCollection;
        this.infoPanelManager = infoPanelManager;

        mapRunMenuItem = new JCheckBoxMenuItem(new MapRunAction());
        mapRunMenuItem.setText("Map Run");
        this.add(mapRunMenuItem);

//        mapBoundaryMenuItem = new JCheckBoxMenuItem(new MapBoundaryAction());
//        mapBoundaryMenuItem.setText("Map Run Boundary");
//        this.add(mapBoundaryMenuItem);
//
        if (this.infoPanelManager != null)
        {
            showRunInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showRunInfoMenuItem.setText("Properties...");
            this.add(showRunInfoMenuItem);
        }

        saveToDiskMenuItem = new JMenuItem(new SaveRunAction());
        saveToDiskMenuItem.setText("Save Run...");
        this.add(saveToDiskMenuItem);
    }

    public void setCurrentRun(SimulationRunKey key)
    {
        runKeys.clear();
        runKeys.add(key);

        updateMenuItems();
    }

    public void setCurrentRuns(List<SimulationRunKey> keys)
    {
        runKeys.clear();
        runKeys.addAll(keys);

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        boolean selectMapRun = true;
        boolean enableMapRun = true;
        boolean enableShowRunInfo = false;
        boolean enableSaveToDisk = false;
        boolean selectHideRun = true;
        boolean enableHideRun = true;

        for (SimulationRunKey imageKey : runKeys)
        {
            boolean containsRun = simulationRunCollection.containsRun(imageKey);
            boolean containsBoundary = false;

            if (!containsRun)
                selectMapRun = containsRun;

            if (showRunInfoMenuItem != null && runKeys.size() == 1)
                enableShowRunInfo = containsRun;

            if (runKeys.size() == 1)
            {
                enableSaveToDisk = containsRun;
            }

            if (containsRun)
            {
                SimulationRun image = simulationRunCollection.getRun(imageKey);
                if (image.isVisible())
                    selectHideRun = false;
            }
            else
            {
                selectHideRun = false;
                enableHideRun = false;
            }
                enableSaveToDisk = true;
        }

        mapRunMenuItem.setSelected(selectMapRun);
        mapRunMenuItem.setEnabled(enableMapRun);
        if (showRunInfoMenuItem != null)
            showRunInfoMenuItem.setEnabled(enableShowRunInfo);
        saveToDiskMenuItem.setEnabled(enableSaveToDisk);
    }


    public class MapRunAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (SimulationRunKey runKey : runKeys)
            {
                if (mapRunMenuItem.isSelected())
                    simulationRunCollection.addRun(runKey);
                else
                    simulationRunCollection.removeRun(runKey);
            }

            updateMenuItems();
        }
    }

    private class ShowInfoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            SimulationRunKey selectedRunKey = null;
            for (SimulationRunKey runKey : runKeys)
            {
                if (mapRunMenuItem.isSelected())
                {
                    selectedRunKey = runKey;
                    break;
                }
            }

            if (selectedRunKey != null)
            {
                SimulationRun selectedRun = simulationRunCollection.getRun(selectedRunKey);
                try
                {
                    System.out.println("Property panel not implemented yet.");
//                    simulationRunCollection.addRun(imageKey);
//                    infoPanelManager.addData(simulationRunCollection.getRun(imageKey));

                    updateMenuItems();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class SaveRunAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            SimulationRunKey selectedRunKey = null;
            for (SimulationRunKey runKey : runKeys)
            {
                if (mapRunMenuItem.isSelected())
                {
                    selectedRunKey = runKey;
                    break;
                }
            }

            if (selectedRunKey != null)
            {
                SimulationRun selectedRun = simulationRunCollection.getRun(selectedRunKey);
                System.out.println("Save to file not implemented yet.");

//                File file = null;
//                try
//                {
//                    file = CustomFileChooser.showSaveDialog(invoker, "Save Run to File", selectedRunKey.name, "cvs");
//                    if (file != null)
//                    {
//                        System.out.println("Save to file not implemented yet.");
//                    }
//                }
//                catch(Exception ex)
//                {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
//                            "Unable to save file to " + file.getAbsolutePath(),
//                            "Error Saving File",
//                            JOptionPane.ERROR_MESSAGE);
//                    ex.printStackTrace();
//                }
            }
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (simulationRunCollection.getRun((vtkActor)pickedProp) != null)
            {
                SimulationRun run = simulationRunCollection.getRun((vtkActor)pickedProp);
                setCurrentRun(run.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


}
