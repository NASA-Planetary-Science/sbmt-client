package edu.jhuapl.near.popupmenus.eros;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import vtk.vtkActor;
import vtk.vtkPolyData;
import vtk.vtkProp;
import vtk.vtkSelectPolyData;
import vtk.vtkTriangle;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.model.eros.NISStatistics;
import edu.jhuapl.near.popupmenus.PopupMenu;


public class NISPopupMenu extends PopupMenu
{
    private ModelManager modelManager;
    private String currentSpectrum;
    private JMenuItem showRemoveSpectrumIn3DMenuItem;
    private JMenuItem showSpectrumInfoMenuItem;
    private JMenuItem centerSpectrumMenuItem;
    private JMenuItem showFrustumMenuItem;
    private JMenuItem saveSpectrumMenuItem;
    private ModelInfoWindowManager infoPanelManager;
    //private SmallBodyModel erosModel;

    private JMenuItem showStatisticsMenuItem;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public NISPopupMenu(
            ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager)
    {
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;
        //this.erosModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);

        showRemoveSpectrumIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveSpectrumIn3DMenuItem.setText("Show Footprint");
        this.add(showRemoveSpectrumIn3DMenuItem);

        if (this.infoPanelManager != null)
        {
            showSpectrumInfoMenuItem = new JMenuItem(new ShowSpectrumAction());
            showSpectrumInfoMenuItem.setText("Spectrum...");
            this.add(showSpectrumInfoMenuItem);
        }

    /*    if (this.infoPanelManager != null)
        {
            showStatisticsMenuItem=new JMenuItem(new ShowStatisticsAction());
            showStatisticsMenuItem.setText("Statistics...");
            this.add(showStatisticsMenuItem);
        }*/
        // XXX: IN-PROGRESS NIS STATISTICS FUNCTIONALITY IS TEMPORARILY DISABLED FOR RELEASE OF OLA BUGFIXES - SEE nisStats BRANCH

        centerSpectrumMenuItem = new JMenuItem(new CenterImageAction());
        centerSpectrumMenuItem.setText("Center in Window");
        //this.add(centerImageMenuItem);

        showFrustumMenuItem = new JCheckBoxMenuItem(new ShowFrustumAction());
        showFrustumMenuItem.setText("Show Frustum");
        this.add(showFrustumMenuItem);

        saveSpectrumMenuItem = new JMenuItem(new SaveSpectrumAction());
        saveSpectrumMenuItem.setText("Save Spectrum...");
        this.add(saveSpectrumMenuItem);
    }


    public void setCurrentSpectrum(String name)
    {
        currentSpectrum = name;

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);

        boolean containsSpectrum = model.containsSpectrum(currentSpectrum);
        showRemoveSpectrumIn3DMenuItem.setSelected(containsSpectrum);

        if (showSpectrumInfoMenuItem != null)
            showSpectrumInfoMenuItem.setEnabled(containsSpectrum);

        saveSpectrumMenuItem.setEnabled(containsSpectrum);

        if (containsSpectrum)
        {
            NISSpectrum image = model.getSpectrum(currentSpectrum);
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
            NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            try
            {
                if (showRemoveSpectrumIn3DMenuItem.isSelected())
                    model.addSpectrum(currentSpectrum);
                else
                    model.removeSpectrum(currentSpectrum);

                updateMenuItems();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class ShowSpectrumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                infoPanelManager.addData(model.getSpectrum(currentSpectrum));

                updateMenuItems();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private class ShowStatisticsAction extends AbstractAction
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            List<NISSpectrum> spectra=model.getSelectedSpectra();
            if (spectra.size()==0)
                spectra.add(model.getSpectrum(currentSpectrum));    // this was the old default behavior, but now we just do this if there are no spectra explicitly selected
            //
            List<Double> th=Lists.newArrayList();   // TODO: should keep references to spectra, too
            for (NISSpectrum spectrum : spectra)
            {
                Vector3D scpos=new Vector3D(spectrum.getSpacecraftPosition());
                Vector3D frustCenter=new Vector3D(spectrum.getFrustumCenter());
                vtkPolyData footPrint=spectrum.getShiftedFootprint();

                vtkSelectPolyData selectionFilter=new vtkSelectPolyData();
                selectionFilter.SetInputData(modelManager.getSmallBodyModel().getSmallBodyPolyData());
                selectionFilter.SetLoop(footPrint.GetPoints());
                selectionFilter.Update();

                vtkPolyData selectedFaces=selectionFilter.GetOutput();
                for (int c=0; c<selectedFaces.GetNumberOfCells(); c++)
                {
                    vtkTriangle tri=(vtkTriangle)selectedFaces.GetCell(c);
                    double[] nml=new double[3];
                    new vtkTriangle().ComputeNormal(tri.GetPoints().GetPoint(0), tri.GetPoints().GetPoint(1), tri.GetPoints().GetPoint(2), nml);
                    th.add(Math.acos(new Vector3D(nml).normalize().dotProduct(frustCenter.subtract(scpos).normalize())));
                }
            }

            int cnt=0;
            for (NISSpectrum spectrum : spectra)
            {
                model.setOrdinal(spectrum, cnt);
                cnt++;
            }


            double[] thArray=new double[th.size()];
            for (int i=0; i<thArray.length; i++)
                thArray[i]=th.get(i);
            NISStatistics stats=new NISStatistics(thArray, spectra);
            //NISStatisticsCollection statsModel=(NISStatisticsCollection)modelManager.getModel(ModelNames.STATISTICS);
            //statsModel.addStatistics(stats);

            try
            {
                infoPanelManager.addData(stats);
            }
            catch (Exception e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }

    }

    private class CenterImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        }
    }

    private class ShowFrustumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                NISSpectrum spectrum = model.getSpectrum(currentSpectrum);

                spectrum.setShowFrustum(showFrustumMenuItem.isSelected());

                updateMenuItems();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }

    private class SaveSpectrumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                NISSpectrum spectrum = model.getSpectrum(currentSpectrum);

                String name = new File(spectrum.getFullPath()).getName();
                name = name.substring(0, name.length()-4) + ".txt";
                File file = CustomFileChooser.showSaveDialog(saveSpectrumMenuItem, "Select File", name);

                if (file != null)
                {
                    spectrum.saveSpectrum(file);
                }
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(saveSpectrumMenuItem),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }
        }

    }


    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            NISSpectraCollection msiImages = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            String name = msiImages.getSpectrumName((vtkActor)pickedProp);
            setCurrentSpectrum(name);
            show(e.getComponent(), e.getX(), e.getY());
        }
    }

}
