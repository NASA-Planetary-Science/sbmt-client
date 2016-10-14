package edu.jhuapl.sbmt.gui.eros;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.Renderer.LightingType;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.illum.IlluminationField;
import edu.jhuapl.saavtk.illum.PolyhedralModelIlluminator;
import edu.jhuapl.saavtk.illum.UniformIlluminationField;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.eros.NISSpectraCollection;
import edu.jhuapl.sbmt.model.eros.NISSpectrum;
import edu.jhuapl.sbmt.model.eros.NISStatistics;
import edu.jhuapl.sbmt.model.eros.NISStatistics.Sample;
import edu.jhuapl.sbmt.model.eros.NISStatisticsCollection;


public class NISPopupMenu extends PopupMenu
{
    private ModelManager modelManager;
    private String currentSpectrum;
    private JMenuItem showRemoveSpectrumIn3DMenuItem;
    private JMenuItem showSpectrumInfoMenuItem;
    private JMenuItem centerSpectrumMenuItem;
    private JMenuItem showFrustumMenuItem;
    private JMenuItem saveSpectrumMenuItem;
    private SbmtInfoWindowManager infoPanelManager;
    private JMenuItem showToSunVectorMenuItem;
    private JMenuItem setIlluminationMenuItem;
    private JMenuItem showOutlineMenuItem;
    //private SmallBodyModel erosModel;

    private JMenuItem showStatisticsMenuItem;
    private Renderer renderer;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public NISPopupMenu(
            ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, Renderer renderer)
    {
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;
        this.renderer=renderer;
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

        if (this.infoPanelManager != null)
        {
            showStatisticsMenuItem=new JMenuItem(new ShowStatisticsAction());
            showStatisticsMenuItem.setText("Statistics...");
            this.add(showStatisticsMenuItem);
        }
        centerSpectrumMenuItem = new JMenuItem(new CenterImageAction());
        centerSpectrumMenuItem.setText("Center in Window");
        if (renderer!=null)
            this.add(centerSpectrumMenuItem);

        showFrustumMenuItem = new JCheckBoxMenuItem(new ShowFrustumAction());
        showFrustumMenuItem.setText("Show Frustum");
        this.add(showFrustumMenuItem);

        showOutlineMenuItem = new JCheckBoxMenuItem(new ShowOutlineAction());
        showOutlineMenuItem.setText("Show Outline");
        this.add(showOutlineMenuItem);

        showToSunVectorMenuItem = new JCheckBoxMenuItem(new ShowToSunVectorAction());
        showToSunVectorMenuItem.setText("Show Sunward Vector");
        this.add(showToSunVectorMenuItem);

        setIlluminationMenuItem = new JMenuItem(new SetIlluminationAction());
        setIlluminationMenuItem.setText("Set Illumination");
        if (renderer!=null)
            this.add(setIlluminationMenuItem);

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
            showOutlineMenuItem.setSelected(image.isOutlineShowing());
            showOutlineMenuItem.setEnabled(true);
            centerSpectrumMenuItem.setEnabled(true);
            showToSunVectorMenuItem.setSelected(image.isToSunVectorShowing());
            showToSunVectorMenuItem.setEnabled(true);
            setIlluminationMenuItem.setEnabled(true);
        }
        else
        {
            showFrustumMenuItem.setSelected(false);
            showFrustumMenuItem.setEnabled(false);
            showOutlineMenuItem.setSelected(false);
            showOutlineMenuItem.setEnabled(false);
            centerSpectrumMenuItem.setEnabled(false);
            showToSunVectorMenuItem.setSelected(false);
            showToSunVectorMenuItem.setEnabled(false);
            setIlluminationMenuItem.setEnabled(false);
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
            showStatisticsWindow();
        }

    }

    public double[] simulateLighting(Vector3D toSunUnitVector)
    {
        IlluminationField illumField=new UniformIlluminationField(toSunUnitVector.negate());
        SmallBodyModel smallBodyModel=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        PolyhedralModelIlluminator illuminator=new PolyhedralModelIlluminator(smallBodyModel);
        illuminator.illuminate(illumField);
        return illuminator.getIlluminationFactorArray();
    }

    public void showStatisticsWindow()
    {
        NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
        List<NISSpectrum> spectra=model.getSelectedSpectra();
        if (spectra.size()==0)
            spectra.add(model.getSpectrum(currentSpectrum));    // this was the old default behavior, but now we just do this if there are no spectra explicitly selected
        //

        //
        // compute statistics
        List<Sample> emergenceAngle=Lists.newArrayList();
        for (NISSpectrum spectrum : spectra)
        {
            Path fullPath=Paths.get(spectrum.getFullPath());
            Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
            Vector3D toSunVector=NISSearchPanel.getToSunUnitVector(relativePath.toString());
//            double[] illumFacs=simulateLighting(toSunVector);

            Vector3D scpos=new Vector3D(spectrum.getSpacecraftPosition());
            vtkPolyData footprint=spectrum.getUnshiftedFootprint();

            vtkSelectPolyData selectionFilter=new vtkSelectPolyData();
            selectionFilter.SetInputData(modelManager.getPolyhedralModel().getSmallBodyPolyData());
            selectionFilter.SetLoop(footprint.GetPoints());
            selectionFilter.Update();
            vtkPolyData selectedFaces=selectionFilter.GetOutput();

            Frustum frustum=new Frustum(scpos.toArray(), spectrum.getFrustumCorner(0), spectrum.getFrustumCorner(1), spectrum.getFrustumCorner(2), spectrum.getFrustumCorner(3));
            emergenceAngle.addAll(NISStatistics.sampleEmergenceAngle(spectrum,selectedFaces, frustum));
        }

        NISStatistics stats=new NISStatistics(emergenceAngle, spectra);
        NISStatisticsCollection statsModel=(NISStatisticsCollection)modelManager.getModel(ModelNames.STATISTICS);
        statsModel.addStatistics(stats);

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

    private class CenterImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            NISSpectrum spectrum = model.getSpectrum(currentSpectrum);
            double[] up=new Vector3D(spectrum.getFrustumCorner(1)).subtract(new Vector3D(spectrum.getFrustumCorner(0))).toArray();
            renderer.setCameraOrientation(spectrum.getFrustumOrigin(), spectrum.getShiftedFootprint().GetCenter(), up, renderer.getCameraViewAngle());
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


    private class ShowOutlineAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                NISSpectrum spectrum = model.getSpectrum(currentSpectrum);

                spectrum.setShowOutline(showOutlineMenuItem.isSelected());

                updateMenuItems();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }

    private class ShowToSunVectorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                NISSpectrum spectrum = model.getSpectrum(currentSpectrum);

                spectrum.setShowToSunVector(showToSunVectorMenuItem.isSelected());

                updateMenuItems();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }

    private class SetIlluminationAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                NISSpectrum spectrum = model.getSpectrum(currentSpectrum);

                renderer.setLighting(LightingType.FIXEDLIGHT);
                Path fullPath=Paths.get(spectrum.getFullPath());
                Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
                Vector3D toSunVector=NISSearchPanel.getToSunUnitVector(relativePath.toString());
                renderer.setFixedLightDirection(toSunVector.toArray()); // the fixed light direction points to the light

                System.out.println("!");

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
