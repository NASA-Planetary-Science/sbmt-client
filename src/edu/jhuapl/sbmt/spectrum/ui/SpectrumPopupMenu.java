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
import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.illum.IlluminationField;
import edu.jhuapl.saavtk.illum.PolyhedralModelIlluminator;
import edu.jhuapl.saavtk.illum.UniformIlluminationField;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.saavtk.view.light.LightingType;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.EnabledState;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
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
    private List<String> currentSpectrum;
    private JMenuItem showRemoveSpectrumIn3DMenuItem;
    private JMenuItem showSpectrumMenuItem;
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
    	this.currentSpectrum = new ArrayList<String>();
        this.modelManager = modelManager;
        this.collection = collection;
        this.boundaries = sbc;
        this.infoPanelManager = infoPanelManager;
        this.renderer=renderer;
        showRemoveSpectrumIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveSpectrumIn3DMenuItem.setText("Map Footprint");
        this.add(showRemoveSpectrumIn3DMenuItem);

        showSpectrumMenuItem = new JCheckBoxMenuItem(new ShowSpectrumAction());
        showSpectrumMenuItem.setText("Show Footprint");
        this.add(showSpectrumMenuItem);

        if (this.infoPanelManager != null)
        {
            showSpectrumInfoMenuItem = new JMenuItem(new ShowSpectrumGraphAction());
            showSpectrumInfoMenuItem.setText("Graph Spectrum...");
            this.add(showSpectrumInfoMenuItem);

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
        currentSpectrum.add(name);
        updateMenuItems();
    }

    private void updateMenuItems()
    {
    	if (collection.getSelectedItems().size() == 1)
    	{
    		S selectedSpectrum = collection.getSelectedItems().asList().get(0);
    		IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(selectedSpectrum);
    		boolean isMapped = collection.isSpectrumMapped(selectedSpectrum);
    		showRemoveSpectrumIn3DMenuItem.setSelected(isMapped);
    		if (showSpectrumInfoMenuItem != null)
    			showSpectrumInfoMenuItem.setEnabled(isMapped);
    		if (showStatisticsMenuItem != null)
    			showStatisticsMenuItem.setEnabled(isMapped);
    		showSpectrumMenuItem.setEnabled(isMapped);
    		showSpectrumMenuItem.setSelected(collection.getVisibility(selectedSpectrum));
    		showFrustumMenuItem.setSelected(collection.getFrustumVisibility(selectedSpectrum));
            showFrustumMenuItem.setEnabled(isMapped);
            showOutlineMenuItem.setSelected(boundaries.getVisibility(selectedSpectrum));
            showOutlineMenuItem.setEnabled(isMapped);
            centerSpectrumMenuItem.setEnabled(isMapped);
            showToSunVectorMenuItem.setEnabled(isMapped);
            setIlluminationMenuItem.setEnabled(isMapped);
            saveSpectrumMenuItem.setEnabled(isMapped);
            if (spectrumRenderer != null && (renderer != null))
            {
            	showToSunVectorMenuItem.setSelected(spectrumRenderer.isToSunVectorShowing());
            	showToSunVectorMenuItem.setEnabled(isMapped);
            	if ((renderer.getLightCfg().getType() == LightingType.FIXEDLIGHT) && (currentIlluminationVector.equals(new Vector3D(selectedSpectrum.getToSunUnitVector())))) setIlluminationMenuItem.setSelected(true);
                else setIlluminationMenuItem.setSelected(false);
                setIlluminationMenuItem.setEnabled(isMapped);
            }

    	}
    	else if (collection.getSelectedItems().size() > 1)
    	{
    		boolean allMapped = true;
    		boolean allOutlined = true;
    		boolean allFrustra = true;
    		boolean allShown = true;
    		for (S spec : collection.getSelectedItems())
    		{
    			if (!collection.isSpectrumMapped(spec)) { allMapped = false; break; }
    		}

    		EnabledState boundaryVisbility = boundaries.getBoundaryVisbility(collection.getSelectedItems());
    		EnabledState frustumVisbility = collection.getFrustumVisbility(collection.getSelectedItems());
    		EnabledState spectrumVisibility = collection.getVisibility(collection.getSelectedItems());
    		if (frustumVisbility == EnabledState.PARTIAL) allFrustra = false;
    		if (boundaryVisbility == EnabledState.PARTIAL) allOutlined = false;
    		if (spectrumVisibility == EnabledState.PARTIAL) allShown = false;
    		showRemoveSpectrumIn3DMenuItem.setSelected(allMapped);
    		if (showStatisticsMenuItem != null)
    			showStatisticsMenuItem.setEnabled(false);
    		if (showSpectrumInfoMenuItem != null)
    			showSpectrumInfoMenuItem.setEnabled(false);

    		showSpectrumMenuItem.setSelected(spectrumVisibility == EnabledState.ALL);
    		showSpectrumMenuItem.setEnabled(allMapped && allShown);

			showFrustumMenuItem.setSelected(frustumVisbility == EnabledState.ALL);
			showFrustumMenuItem.setEnabled(allMapped && allFrustra);

            showOutlineMenuItem.setSelected(boundaryVisbility == EnabledState.ALL);
            showOutlineMenuItem.setEnabled(allMapped && allOutlined);
            centerSpectrumMenuItem.setEnabled(false);
            showToSunVectorMenuItem.setEnabled(false);
            showToSunVectorMenuItem.setSelected(false);
            setIlluminationMenuItem.setSelected(false);
            setIlluminationMenuItem.setEnabled(false);
            saveSpectrumMenuItem.setEnabled(allMapped);
    	}
    }

    public void setCurrentSpectrum(S spec)
    {
        spectrum.clear();
        spectrum.add(spec);
        currentSpectrum.add(spec.getSpectrumName());
        updateMenuItems();
    }

    public void setCurrentSpectra(List<S> keys)
    {
        spectrum.clear();
        spectrum.addAll(keys);
        for (S key : keys)
        	currentSpectrum.add(key.getSpectrumName().substring(keys.get(0).getSpectrumName().lastIndexOf("/")+1));
        updateMenuItems();
    }

    private class ShowRemoveIn3DAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (showRemoveSpectrumIn3DMenuItem.isSelected())
            {
            	try
				{
            		for (S spec : collection.getSelectedItems())
            		{
            			collection.addSpectrum(spec, spec.isCustomSpectra);
            			boundaries.addBoundary(spec);
            		}
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
            	for (S spec : collection.getSelectedItems())
            	{
            		collection.removeSpectrum(spec);
            		boundaries.removeBoundary(spec);
            	}
        }
    }

    private class ShowSpectrumAction extends AbstractAction
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		if (showSpectrumMenuItem.isSelected())
    		{
    			for (S spec : collection.getSelectedItems())
            		collection.setVisibility(spec, true);
    		}
    		else
    		{
    			for (S spec : collection.getSelectedItems())
            		collection.setVisibility(spec, false);
    		}
    	}
    }

    private class ShowSpectrumGraphAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
            	S spec = collection.getSelectedItems().asList().get(0);
                infoPanelManager.addData(collection.getRendererForSpectrum(spec));
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
            spectra.add(collection.getRendererForSpectrum(collection.getSelectedItems().asList().get(0)));    // this was the old default behavior, but now we just do this if there are no spectra explicitly selected

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
        	S spec = collection.getSelectedItems().asList().get(0);
        	IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(spec);

    		double[] up=new Vector3D(spectrumRenderer.getSpectrum().getFrustumCorner(1)).subtract(new Vector3D(spectrumRenderer.getSpectrum().getFrustumCorner(0))).toArray();
    		if (spectrumRenderer.getShiftedFootprint()!=null)
    			renderer.setCameraOrientation(spectrumRenderer.getSpectrum().getFrustumOrigin(), spectrumRenderer.getShiftedFootprint().GetCenter(), up, renderer.getCameraViewAngle());

        }
    }

    private class ShowFrustumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        	for (S spec : collection.getSelectedItems())
        	{
        		collection.getRendererForSpectrum(spec).setShowFrustum(showFrustumMenuItem.isSelected());
        	}

            updateMenuItems();
        }
    }


    private class ShowOutlineAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
    		boolean allOutlined = true;
    		for (S spec : collection.getSelectedItems())
    		{
    			if (!boundaries.getVisibility(spec)) { allOutlined = false; break; }
    		}

    		for (S spec : collection.getSelectedItems())
    		{
    			boundaries.setVisibility(spec, !allOutlined);
    		}

            updateMenuItems();
        }
    }

    private class ShowToSunVectorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
            	S spec = collection.getSelectedItems().asList().get(0);
            	IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(spec);
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
        	for (S spec : collection.getSelectedItems())
        	{
        		IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(spec);
	        	if (menuItem.isSelected()) currentIlluminationVector = new Vector3D(spectrumRenderer.getSpectrum().getToSunUnitVector());
	            try
	            {
	                if (renderer.getLightCfg().getType() == LightingType.FIXEDLIGHT && currentIlluminationVector == null)
	                {
	                    LightUtil.switchToLightKit(renderer);
	                }
	                else
	                {
	                    renderer.setLightCfgToFixedLightAtDirection(currentIlluminationVector); // the fixed light direction points to the light
	                }


	                updateMenuItems();
	            }
	            catch (Exception ex)
	            {
	                ex.printStackTrace();
	            }
        	}
        }
    }

	private class SaveOriginalSpectrumAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				if (collection.getSelectedItems().size() == 1)
				{
					IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(collection.getSelectedItems().asList().get(0));
					String name = new File(spectrumRenderer.getSpectrum().getFullPath()).getName();
					File file = CustomFileChooser.showSaveDialog(saveSpectrumMenuItem, "Select File", name);
					if (file == null) return;
					S spec = spectrumRenderer.getSpectrum();
					if (spectrumRenderer.getSpectrum().isCustomSpectra == false)
					{
						File cachedFile = new File(spectrumRenderer.getSpectrum().getFullPath());
						FileUtil.copyFile(cachedFile, file);
						File toInfoFilename = new File(file.getParentFile(),
								FilenameUtils.getBaseName(file.getAbsolutePath()) + ".INFO");
						System.out.println("SpectrumPopupMenu.SaveOriginalSpectrumAction: actionPerformed: to info file name " + toInfoFilename);
						spectrumRenderer.getSpectrum().saveInfofile(toInfoFilename);
					}
					else
					{
						File cachedFile = new File(spec.getFullPath());
						FileUtil.copyFile(cachedFile, file);
						File cachedInfoFile = new File(cachedFile.getParentFile(),
								FilenameUtils.getBaseName(cachedFile.getAbsolutePath()) + ".INFO");
						File toInfoFilename = new File(file.getParentFile(),
								FilenameUtils.getBaseName(file.getAbsolutePath()) + ".INFO");
						FileUtil.copyFile(cachedInfoFile, toInfoFilename);

					}
				}
				else if (collection.getSelectedItems().size() > 1)
				{
					File directory = DirectoryChooser.showOpenDialog(null, "Save Spectra to Directory...");
					if (directory == null) return;
					for (S spec : collection.getSelectedItems())
					{
						IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(spec);
						String name = new File(spectrumRenderer.getSpectrum().getFullPath()).getName();
//						File file = CustomFileChooser.showSaveDialog(saveSpectrumMenuItem, "Select File", name);

						if (spectrumRenderer.getSpectrum().isCustomSpectra == false)
						{
							File cachedFile = new File(spectrumRenderer.getSpectrum().getFullPath());
							FileUtil.copyFile(cachedFile, new File(directory, name));
							File toInfoFilename = new File(directory,
									FilenameUtils.getBaseName(new File(spectrumRenderer.getSpectrum().getFullPath()).getAbsolutePath()) + ".INFO");
							spectrumRenderer.getSpectrum().saveInfofile(toInfoFilename);
						}
						else
						{
							File cachedFile = new File(spec.getFullPath());
							FileUtil.copyFile(cachedFile, new File(directory, name));
							File cachedInfoFile = new File(cachedFile.getParentFile(),
									FilenameUtils.getBaseName(cachedFile.getAbsolutePath()) + ".INFO");
							File toInfoFilename = new File(directory,
									FilenameUtils.getBaseName(new File(directory, name).getAbsolutePath()) + ".INFO");
							FileUtil.copyFile(cachedInfoFile, toInfoFilename);
						}

					}
				}


			} catch (IOException e1)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(saveSpectrumMenuItem),
						"There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

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
            	if (collection.getSelectedItems().size() == 1)
				{
            		IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(collection.getSelectedItems().asList().get(0));
	                String name = new File(spectrumRenderer.getSpectrum().getFullPath()).getName();
	                name = FilenameUtils.getBaseName(name) + "-humanReadable.txt";
	                File file = CustomFileChooser.showSaveDialog(saveSpectrumMenuItem, "Select File", name);
	                if (file == null) return;
	                S spec = spectrumRenderer.getSpectrum();
					spectrumRenderer.getSpectrum().saveSpectrum(file);
				}
            	else if (collection.getSelectedItems().size() > 1)
				{
					File directory = DirectoryChooser.showOpenDialog(null, "Save Spectra to Directory...");
					if (directory == null) return;
	            	for (S spec : collection.getSelectedItems())
	            	{
	            		IBasicSpectrumRenderer<S> spectrumRenderer = collection.getRendererForSpectrum(spec);
		                String name = new File(spectrumRenderer.getSpectrum().getFullPath()).getName();
		                name = FilenameUtils.getBaseName(name) + "-humanReadable.txt";
//		                File file = CustomFileChooser.showSaveDialog(saveSpectrumMenuItem, "Select File", name);
	                	spectrumRenderer.getSpectrum().saveSpectrum(new File(directory, name));
	            	}
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
