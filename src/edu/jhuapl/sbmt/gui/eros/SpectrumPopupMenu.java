package edu.jhuapl.sbmt.gui.eros;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;
import com.jidesoft.utils.SwingWorker;

import vtk.vtkActor;
import vtk.vtkIdTypeArray;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.Renderer.LightingType;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.illum.IlluminationField;
import edu.jhuapl.saavtk.illum.PolyhedralModelIlluminator;
import edu.jhuapl.saavtk.illum.UniformIlluminationField;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.eros.SpectrumStatistics;
import edu.jhuapl.sbmt.model.eros.SpectrumStatistics.Sample;
import edu.jhuapl.sbmt.model.spectrum.Spectrum;
import edu.jhuapl.sbmt.model.eros.NISStatisticsCollection;
import edu.jhuapl.sbmt.model.eros.SpectraCollection;


public class SpectrumPopupMenu extends PopupMenu implements PropertyChangeListener
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


    ComputeStatisticsTask task;
    JProgressBar statisticsProgressBar=new JProgressBar(0,100);

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public SpectrumPopupMenu(
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
        SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);

        boolean containsSpectrum = model.containsSpectrum(currentSpectrum);
        showRemoveSpectrumIn3DMenuItem.setSelected(containsSpectrum);

        if (showSpectrumInfoMenuItem != null)
            showSpectrumInfoMenuItem.setEnabled(containsSpectrum);

        saveSpectrumMenuItem.setEnabled(containsSpectrum);

        if (containsSpectrum)
        {
            Spectrum image = model.getSpectrum(currentSpectrum);
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
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
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
                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
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

    public double[] simulateLighting(Vector3D toSunUnitVector, List<Integer> faces)
    {
        IlluminationField illumField=new UniformIlluminationField(toSunUnitVector.negate());
        SmallBodyModel smallBodyModel=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        PolyhedralModelIlluminator illuminator=new PolyhedralModelIlluminator(smallBodyModel);
        return illuminator.illuminate(illumField, faces);
    }

    public void showStatisticsWindow()
    {
        SmallBodyModel smallBodyModel=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
        List<Spectrum> spectra=model.getSelectedSpectra();
        if (spectra.size()==0)
            spectra.add(model.getSpectrum(currentSpectrum));    // this was the old default behavior, but now we just do this if there are no spectra explicitly selected
        //

        //
        // compute statistics
        task=new ComputeStatisticsTask(spectra);
        task.addPropertyChangeListener(this);
        task.execute();

    }

    class ComputeStatisticsTask extends SwingWorker<Void, Void>
    {
        List<Sample> emergenceAngle=Lists.newArrayList();
        List<Sample> incidenceAngle=Lists.newArrayList();   // this has nan for faces that are occluded
        List<Sample> irradiation=Lists.newArrayList();
        List<Sample> phaseAngle=Lists.newArrayList();   // this can have a different number of items than the other lists due to occluded faces
        List<Spectrum> spectra;

        public ComputeStatisticsTask(List<Spectrum> spectra)
        {
            this.spectra=spectra;
        }

        @Override
        protected Void doInBackground() throws Exception
        {
            for (int i=0; i<spectra.size(); i++)
            {
                setProgress((int)(100*(double)i/(double)spectra.size()));

                Spectrum spectrum=spectra.get(i);
                Vector3D scpos=new Vector3D(spectrum.getSpacecraftPosition());

                vtkIdTypeArray ids=(vtkIdTypeArray)spectrum.getUnshiftedFootprint().GetCellData().GetArray(GenericPolyhedralModel.cellIdsArrayName);
                List<Integer> selectedIds=Lists.newArrayList();
                for (int m=0; m<ids.GetNumberOfTuples(); m++)
                    selectedIds.add(ids.GetValue(m));

                Path fullPath=Paths.get(spectrum.getFullPath());
                Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
                Vector3D toSunVector=SpectrumSearchPanel.getToSunUnitVector(relativePath.toString());
                double[] illumFacs=simulateLighting(toSunVector,selectedIds);

                emergenceAngle.addAll(SpectrumStatistics.sampleEmergenceAngle(spectrum, scpos));
                // XXX: incidence angle currently ignores occlusion
                incidenceAngle.addAll(SpectrumStatistics.sampleIncidenceAngle(spectrum, toSunVector));
                phaseAngle.addAll(SpectrumStatistics.samplePhaseAngle(incidenceAngle, emergenceAngle));
                irradiation.addAll(SpectrumStatistics.sampleIrradiance(spectrum, illumFacs));
            }

            return null;
        }

        @Override
        protected void done()
        {

            SpectrumStatistics stats=new SpectrumStatistics(emergenceAngle, incidenceAngle, phaseAngle, irradiation, spectra);
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

    }

    private class CenterImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            Spectrum spectrum = model.getSpectrum(currentSpectrum);
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
                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                Spectrum spectrum = model.getSpectrum(currentSpectrum);

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
                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                Spectrum spectrum = model.getSpectrum(currentSpectrum);

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
                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                Spectrum spectrum = model.getSpectrum(currentSpectrum);

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
                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                Spectrum spectrum = model.getSpectrum(currentSpectrum);

                renderer.setLighting(LightingType.FIXEDLIGHT);
                Path fullPath=Paths.get(spectrum.getFullPath());
                Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
                Vector3D toSunVector=SpectrumSearchPanel.getToSunUnitVector(relativePath.toString());
                renderer.setFixedLightDirection(toSunVector.toArray()); // the fixed light direction points to the light

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
                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.addSpectrum(currentSpectrum);
                Spectrum spectrum = model.getSpectrum(currentSpectrum);

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
            SpectraCollection msiImages = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            String name = msiImages.getSpectrumName((vtkActor)pickedProp);
            setCurrentSpectrum(name);
            show(e.getComponent(), e.getX(), e.getY());
        }
    }



    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource()==task)
        {
            if (task.isDone())
                statisticsProgressBar.setVisible(false);
            else
                statisticsProgressBar.setVisible(true);
            statisticsProgressBar.setValue(task.getProgress());
        }

    }

}
