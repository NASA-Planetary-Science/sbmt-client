package edu.jhuapl.sbmt.spectrum.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;
import com.jidesoft.utils.SwingWorker;

import vtk.vtkActor;
import vtk.vtkIdTypeArray;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.illum.IlluminationField;
import edu.jhuapl.saavtk.illum.PolyhedralModelIlluminator;
import edu.jhuapl.saavtk.illum.UniformIlluminationField;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumIOException;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics.Sample;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatisticsCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;


public class SpectrumPopupMenu<S extends BasicSpectrum> extends PopupMenu implements PropertyChangeListener
{
    private ModelManager modelManager;
    private String currentSpectrum;
    private JMenuItem showRemoveSpectrumIn3DMenuItem;
    private JMenuItem showSpectrumInfoMenuItem;
    private JMenuItem centerSpectrumMenuItem;
    private JMenuItem showFrustumMenuItem;
    private JMenu saveSpectrumMenuItem;
    private JMenuItem saveOriginalSpectrumMenuItem;
    private JMenuItem saveHumanReadableSpectrumMenuItem;
    private SbmtInfoWindowManager infoPanelManager;
    private JMenuItem showToSunVectorMenuItem;
    private JMenuItem setIlluminationMenuItem;
    private JMenuItem showOutlineMenuItem;
    private List<S> spectrum = new ArrayList<S>();
    private JMenuItem showStatisticsMenuItem;
    private Renderer renderer;
    private Vector3D currentIlluminationVector;

    private IBasicSpectrumRenderer<S> spectrumRenderer;
    ComputeStatisticsTask task;
    JProgressBar statisticsProgressBar=new JProgressBar(0,100);

    SpectraCollection<S> collection;
    SpectrumBoundaryCollection<S> boundaries;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
    public SpectrumPopupMenu(SpectraCollection<S> collection, SpectrumBoundaryCollection<S> sbc,
            ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, Renderer renderer)
    {
        this.modelManager = modelManager;
        this.collection = collection;
        this.boundaries = sbc;
        this.infoPanelManager = infoPanelManager;
        this.renderer=renderer;
        showRemoveSpectrumIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveSpectrumIn3DMenuItem.setText("Show Footprint");
        this.add(showRemoveSpectrumIn3DMenuItem);

        if (this.infoPanelManager != null)
        {
            showSpectrumInfoMenuItem = new JMenuItem(new ShowSpectrumAction());
            showSpectrumInfoMenuItem.setText("Graph Spectrum...");
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

        setIlluminationMenuItem = new JCheckBoxMenuItem(new SetIlluminationAction());
        setIlluminationMenuItem.setText("Simulate Lighting");
        if (renderer!=null)
            this.add(setIlluminationMenuItem);

        saveSpectrumMenuItem = new JMenu("Save Spectrum");


        saveOriginalSpectrumMenuItem = new JMenuItem(new SaveOriginalSpectrumAction());
        saveOriginalSpectrumMenuItem.setText("Save Original Spectrum...");
        saveHumanReadableSpectrumMenuItem = new JMenuItem(new SaveSpectrumAction());
        saveHumanReadableSpectrumMenuItem.setText("Save Human Readable Spectrum...");

        saveSpectrumMenuItem.add(saveOriginalSpectrumMenuItem);
        saveSpectrumMenuItem.add(saveHumanReadableSpectrumMenuItem);

        this.add(saveSpectrumMenuItem);
    }



    public void setCurrentSpectrum(String name)
    {
        currentSpectrum = name;
        updateMenuItems();
    }

    private void updateMenuItems()
    {
        spectrumRenderer = collection.getSpectrumForName(currentSpectrum);
        boolean containsSpectrum = false;
        if (spectrumRenderer != null) containsSpectrum = true;
        showRemoveSpectrumIn3DMenuItem.setSelected(containsSpectrum);

        if (showSpectrumInfoMenuItem != null)
            showSpectrumInfoMenuItem.setEnabled(containsSpectrum);

        saveSpectrumMenuItem.setEnabled(containsSpectrum);

        if (containsSpectrum)
        {
            showFrustumMenuItem.setSelected(spectrumRenderer.isFrustumShowing());
            showFrustumMenuItem.setEnabled(true);
            showOutlineMenuItem.setSelected(spectrumRenderer.isOutlineShowing());
            showOutlineMenuItem.setSelected(boundaries.getVisibility(spectrumRenderer.getSpectrum()));
            showOutlineMenuItem.setEnabled(true);
            centerSpectrumMenuItem.setEnabled(true);
            showToSunVectorMenuItem.setSelected(spectrumRenderer.isToSunVectorShowing());
            showToSunVectorMenuItem.setEnabled(true);
            if ((renderer.getLighting() == LightingType.FIXEDLIGHT) && (currentIlluminationVector.equals(new Vector3D(spectrumRenderer.getSpectrum().getToSunUnitVector())))) setIlluminationMenuItem.setSelected(true);
            else setIlluminationMenuItem.setSelected(false);
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

        if (collection.getSelectedItems().size() > 1) setIlluminationMenuItem.setEnabled(false);


    }

    BasicSpectrumInstrument instrument;

    public void setCurrentSpectrum(S spec)
    {
        spectrum.clear();
        spectrum.add(spec);
        currentSpectrum = spec.getSpectrumName();
        updateMenuItems();
    }

    public void setCurrentSpectra(List<S> keys)
    {
        spectrum.clear();
        spectrum.addAll(keys);
        currentSpectrum = keys.get(0).getSpectrumName().substring(keys.get(0).getSpectrumName().lastIndexOf("/")+1);
        updateMenuItems();
    }

    public void setInstrument(BasicSpectrumInstrument instrument)
    {
        this.instrument=instrument;
    }

    private class ShowRemoveIn3DAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (showRemoveSpectrumIn3DMenuItem.isSelected())
            {
            	try
				{
					collection.addSpectrum(spectrum.get(0), spectrum.get(0).isCustomSpectra);
				}
            	catch (SpectrumIOException e1)
				{
            		JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
		                     e1.getMessage(),
		                     "Error",
		                     JOptionPane.ERROR_MESSAGE);
				}
            }
            else
                collection.removeSpectrum(spectrum.get(0));
        }
    }

    private class ShowSpectrumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                infoPanelManager.addData(spectrumRenderer);
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
        List<IBasicSpectrumRenderer<S>> spectra = collection.getSelectedSpectra();
        if (spectra.size()==0)
            spectra.add(spectrumRenderer);    // this was the old default behavior, but now we just do this if there are no spectra explicitly selected

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
        List<IBasicSpectrumRenderer<S>> spectra;

        public ComputeStatisticsTask(List<IBasicSpectrumRenderer<S>> spectra)
        {
            this.spectra=spectra;
        }

        @Override
        protected Void doInBackground() throws Exception
        {
            for (int i=0; i<spectra.size(); i++)
            {
                setProgress((int)(100*(double)i/(double)spectra.size()));

                IBasicSpectrumRenderer<S> spectrum=spectra.get(i);
                Vector3D scpos=new Vector3D(spectrum.getSpectrum().getSpacecraftPosition());

                vtkIdTypeArray ids=(vtkIdTypeArray)spectrum.getUnshiftedFootprint().GetCellData().GetArray(GenericPolyhedralModel.cellIdsArrayName);
                List<Integer> selectedIds=Lists.newArrayList();
                for (int m=0; m<ids.GetNumberOfTuples(); m++)
                    selectedIds.add(ids.GetValue(m));

                Path fullPath=Paths.get(spectrum.getSpectrum().getFullPath());
                Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
                Vector3D toSunVector=new Vector3D(spectrum.getSpectrum().getToSunUnitVector());
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

            SpectrumStatistics<S> stats=new SpectrumStatistics<S>(emergenceAngle, incidenceAngle, phaseAngle, irradiation, spectra);
            SpectrumStatisticsCollection<S> statsModel=(SpectrumStatisticsCollection<S>)modelManager.getModel(ModelNames.STATISTICS);
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
            double[] up=new Vector3D(spectrumRenderer.getSpectrum().getFrustumCorner(1)).subtract(new Vector3D(spectrumRenderer.getSpectrum().getFrustumCorner(0))).toArray();
            if (spectrumRenderer.getShiftedFootprint()!=null)
                renderer.setCameraOrientation(spectrumRenderer.getSpectrum().getFrustumOrigin(), spectrumRenderer.getShiftedFootprint().GetCenter(), up, renderer.getCameraViewAngle());
        }
    }

    private class ShowFrustumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            spectrumRenderer.setShowFrustum(showFrustumMenuItem.isSelected());
            updateMenuItems();
        }
    }


    private class ShowOutlineAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
          	boundaries.setVisibility(spectrumRenderer.getSpectrum(), showOutlineMenuItem.isSelected());
            updateMenuItems();
        }
    }

    private class ShowToSunVectorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                spectrumRenderer.setShowToSunVector(showToSunVectorMenuItem.isSelected());
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
        	currentIlluminationVector = null;
        	JMenuItem menuItem = (JMenuItem)e.getSource();
        	if (menuItem.isSelected()) currentIlluminationVector = new Vector3D(spectrumRenderer.getSpectrum().getToSunUnitVector());
            try
            {
                if (renderer.getLighting() == LightingType.FIXEDLIGHT && currentIlluminationVector == null)
                {
                    renderer.setLighting(LightingType.LIGHT_KIT);
                }
                else
                {
                    renderer.setLighting(LightingType.FIXEDLIGHT);
                    renderer.setFixedLightDirection(currentIlluminationVector.toArray()); // the fixed light direction points to the light
                }


                updateMenuItems();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }

    private class SaveOriginalSpectrumAction extends AbstractAction
    {
    	 public void actionPerformed(ActionEvent e)
         {
             try
             {
                 String name = new File(spectrumRenderer.getSpectrum().getFullPath()).getName();
                 File file = CustomFileChooser.showSaveDialog(saveSpectrumMenuItem, "Select File", name);
                 if (file != null)
                 {
                	 File cachedFile = new File(spectrumRenderer.getSpectrum().getFullPath());
                	 FileUtil.copyFile(cachedFile, file);
                	 File toInfoFilename = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getAbsolutePath()) + ".INFO");
                	 spectrumRenderer.getSpectrum().saveInfofile(toInfoFilename);
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

    private class SaveSpectrumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                String name = new File(spectrumRenderer.getSpectrum().getFullPath()).getName();
                name = FilenameUtils.getBaseName(name) + "-humanReadable.txt";
                File file = CustomFileChooser.showSaveDialog(saveSpectrumMenuItem, "Select File", name);
                if (file != null)
                {
                    spectrumRenderer.getSpectrum().saveSpectrum(file);
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
    	if (pickedProp == null)
    	{
	    	// Bail if we do not have selected items
			List<S> tmpS = collection.getSelectedItems().asList();
			if (tmpS.size() == 0)
				return;
			setCurrentSpectrum(tmpS.get(0));
			show(e.getComponent(), e.getX(), e.getY());
    	}
    	else
    	{
	        if (pickedProp instanceof vtkActor)
	        {
	            String name = collection.getSpectrumName((vtkActor)pickedProp);
	            setCurrentSpectrum(name);
	            show(e.getComponent(), e.getX(), e.getY());
	        }
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