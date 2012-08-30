package edu.jhuapl.near.popupmenus.eros;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.popupmenus.PopupMenu;


public class NISPopupMenu extends PopupMenu
{
    private ModelManager modelManager;
    private String currentSpectrum;
    private JMenuItem showRemoveSpectrumIn3DMenuItem;
    private JMenuItem showSpectrumInfoMenuItem;
    private JMenuItem centerSpectrumMenuItem;
    private JMenuItem showFrustumMenuItem;
    private ModelInfoWindowManager infoPanelManager;
    //private SmallBodyModel erosModel;

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
            showSpectrumInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showSpectrumInfoMenuItem.setText("Spectrum...");
            this.add(showSpectrumInfoMenuItem);
        }

        centerSpectrumMenuItem = new JMenuItem(new CenterImageAction());
        centerSpectrumMenuItem.setText("Center in Window");
        //this.add(centerImageMenuItem);

        showFrustumMenuItem = new JCheckBoxMenuItem(new ShowFrustumAction());
        showFrustumMenuItem.setText("Show Frustum");
        this.add(showFrustumMenuItem);
    }


    public void setCurrentSpectrum(String name)
    {
        currentSpectrum = name;

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);

        boolean containsSpectrum = model.containsSpectrum(currentSpectrum);
        showRemoveSpectrumIn3DMenuItem.setSelected(containsSpectrum);

        if (showSpectrumInfoMenuItem != null)
            showSpectrumInfoMenuItem.setEnabled(containsSpectrum);

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
            NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);
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

    private class ShowInfoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);
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
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);
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

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            NISSpectraCollection msiImages = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);
            String name = msiImages.getSpectrumName((vtkActor)pickedProp);
            setCurrentSpectrum(name);
            show(e.getComponent(), e.getX(), e.getY());
        }
    }

}
